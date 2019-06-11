
/*
 *#############################################################################
 *#------------------------------ FileServer -----------------------------------
 *#  
 *#  @author 	Joshua Landron
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 *
 * Implementation and assumptions:
 *  Uses Java RMI
 *  Assumes that input upon execution will be in order as follows:
 *  
 * java FileServer port#
 * 
 * This FileServer caches files and allows clients to write to them. 
 * * When a Client requests a file for Writing, the server checks the state of that
 * file. If the file is not currently being written to, the server allows the client
 * to download the file. If the file is being written to by another client, the 
 * server suspends the client thread and sends a request to the client that is 
 * writing the file to upload the file when they are finished writing to it.
 * The requesting client is resumed after the upload completes. 
 * * If a client requests a file for read, the server adds the clients name to the 
 * files reader list and allows the file to be downloaded. When that file is uploaded
 * to the server by any other client, the server sends invalidation messages to each
 * client that is reading the file.
 * ------------------------------------------------------------------------------
 */
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private boolean m_IsActive = true;
    private String m_port;
    private Vector<FileEntry> m_files;
    private Vector<String> m_ClientQueue;

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor
     * 
     * @throws RemoteException on RMI error
     */
    FileServer() throws RemoteException {
        super();
        m_port = null;
        m_files = null;
        m_ClientQueue = null;
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * 
     * @override constructor that takes a port string and instantiates the server
     *           fields.
     * 
     * @throws RemoteException on RMI Error
     * @param port String the server is assigned to
     */
    public FileServer(String port) throws RemoteException {
        super();
        m_port = port;
        m_files = new Vector<>();
        m_ClientQueue = new Vector<>();
    }

    /**
     * ------------------------------------download----------------------------------
     * Method used by FileClient to download the requested file if it exists. it
     * checks the state fo the file, sets the state of the file accordingly.
     * 
     * @throws RemoteException On RMI Error
     * @param client String containing client name, should be clientIP:port#
     * @param filename String containing the name of the file
     * @param mode String containing the operation mode (W or R)
     * @return FileContents : FileContents object that holds a byte[] with the file
     *         information
     */
    public FileContents download(String client, String filename, String mode) throws RemoteException {
        System.out.println("Downloading " + filename + " to " + client + " started.");
        FileContents fileContents = null;
        int index = -1;
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                index = i;
            }
            m_files.elementAt(i).removeReader(client);
        }
        // file is not in server cache, check if the file actually exists
        if (index == -1) {
            try {
                FileEntry file;
                FileInputStream fin = new FileInputStream(filename);
                file = new FileEntry(filename, ServerState.NOT_SHARED);
                file.setContents(Files.readAllBytes(Paths.get(filename)));
                m_files.add(file);
                index = m_files.size() - 1;
            } catch (Exception e) {
                System.err.println("Download failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        // if file is requested in READ mode, send the file and set the state if needed
        if (ServerState.fromId(mode) == ServerState.READ_SHARED) {
            m_files.elementAt(index).addReader(client);
            if (m_files.elementAt(index).getState() == ServerState.NOT_SHARED) {
                m_files.elementAt(index).setState(ServerState.READ_SHARED);
            }
        }
        // check if server is being shutdown. No new write requests.
        else if (!m_IsActive) {
            return null;
        }
        // requesting to write to file
        else if (ServerState.fromId(mode) == ServerState.WRITE_SHARED) {
            // add client to queue by default
            m_ClientQueue.add(client);
            // check if file is unshared or just being read
            if (m_files.elementAt(index).getState() == ServerState.NOT_SHARED
                    || m_files.elementAt(index).getState() == ServerState.READ_SHARED) {
                m_files.elementAt(index).setState(ServerState.WRITE_SHARED);
                fileContents = new FileContents(m_files.elementAt(index).getContents().clone());
                System.out.println("Downloading " + filename + " to " + client + " finished.");
                m_files.elementAt(index).setOwner(m_ClientQueue.elementAt(0));
                m_ClientQueue.removeElementAt(0);
                return fileContents;
            } // check if file is in
              // hold clients as long as they are not next in line.
            try {
                while (!m_ClientQueue.elementAt(0).equals(client)) {
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                System.err.println("Error Downloading: " + e.getMessage());
                e.printStackTrace();
            }
            // once client is next in line, request file return.
            System.out.println("Requesting return of " + filename);
            m_files.elementAt(index).requestReturn();
            // wait for the previous clint to return the file
            while (m_files.elementAt(index).getState() == ServerState.OWNERSHIP_CHANGE) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error Downloading: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // set the owner of the file requested and remove that client name from the
            // vector
            m_files.elementAt(index).setOwner(m_ClientQueue.elementAt(0));
            m_ClientQueue.removeElementAt(0);
        }
        // add the new contents to the fileContents object and return it to the client
        fileContents = new FileContents(m_files.elementAt(index).getContents().clone());
        System.out.println("Downloading " + filename + " to " + client + " finished.");
        return fileContents;
    }

    /**
     * ------------------------------------upload----------------------------------
     * method used by the FileClient to upload a file that is has written to. This
     * method only allows the current owner of a file to upload the file.
     * 
     * @throws RemoteException on RMI error
     * @param clientName String containing the client name
     * @param filename String containing the name of the file being uploaded
     * @param contents FileContents object that holds a byte[] with the file
     *                     information
     * @return boolean True if successful, false otherwise
     */
    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {

        for (int i = 0; i < this.m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                if (m_files.elementAt(i).getState() == ServerState.WRITE_SHARED
                        || m_files.elementAt(i).getState() == ServerState.OWNERSHIP_CHANGE) {
                    System.out.println(
                            "Upload of " + filename + " from " + m_files.elementAt(i).getOwner() + " started.");
                    m_files.elementAt(i).setContents(contents.get());
                    m_files.elementAt(i).sendInvalidates();
                    if (m_files.elementAt(i).getState() == ServerState.WRITE_SHARED) {
                        m_files.elementAt(i).setOwner("");
                        m_files.elementAt(i).setState(ServerState.NOT_SHARED);
                    } else if (m_files.elementAt(i).getState() == ServerState.OWNERSHIP_CHANGE) {
                        m_files.elementAt(i).setState(ServerState.WRITE_SHARED);
                    }
                    System.out.println("Uploading " + filename + " complete.");
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * ------------------------------------main-------------------------------------
     * main method that instantiates the server object with the RMI registry
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java FileServer port#");
            System.exit(-1);
        }
        try {
            FileServer fileServer = new FileServer(args[0]);
            Naming.rebind(("rmi://localhost:" + args[0] + "/fileserver"), fileServer);
            System.out.println("rmi://localhost:" + args[0] + "/fileserver started.");
        } catch (Exception e) {
            System.err.println("Failed to start server due to error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-2);
        }
    }

    /**
     * ---------------------------------shutDownServer----------------------------------
     * method that shuts down this server gracefully. sets the status of the server
     * to inactive and sends out return requests to any clients that are currently
     * writing to files. Once this method is called, no client can write to files.
     * Clients can read files from the server until unbind.
     * 
     * once the server receives all files being written to, it writes all cached
     * files back to the disk. and unbinds the server from the RMI registry and
     * exits.
     * 
     * @throws RemoteException on RMI error
     */
    public void shutDownServer() throws RemoteException {
        try {
            m_IsActive = false;
            for (int i = 0; i < m_files.size(); i++) {
                if (m_files.elementAt(i).getState() == ServerState.WRITE_SHARED) {
                    m_files.elementAt(i).requestReturn();
                }
            }
            while (anyFilesBeingWritten()) {
                Thread.sleep(1000);
            }
            // write all cached files to their permanent files.
            for (int i = 0; i < m_files.size(); i++) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(m_files.elementAt(i).getName());
                    fileOutputStream.write(m_files.elementAt(i).getContents());
                    fileOutputStream.close();
                } catch (Exception e) {
                    System.err.println("File unable to be written to. " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // remove server from RMI registry
            Naming.unbind("rmi://localhost:" + m_port + "/fileserver");
            // shut down this Server
            UnicastRemoteObject.unexportObject(this, true);

        } catch (Exception e) {
            System.err.println("Failed to shutdown due to error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server shut down gracefully");
    }

    /**
     * ---------------------------------anyFilesBeingWritten------------------------------
     * method that checks if there are any files being written to in the server
     * 
     * @return boolean : represents if any clients are still writing to a file.
     */
    private boolean anyFilesBeingWritten() {
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getState() == ServerState.OWNERSHIP_CHANGE) {
                return true;
            }
        }
        return false;
    }
}