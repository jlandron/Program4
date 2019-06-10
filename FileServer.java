import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private boolean m_IsActive = true;
    private String m_port;
    private int m_ShutdownCode = 1234;
    private Vector<FileEntry> m_files;
    private Vector<String> m_ClientQueue;

    FileServer() throws RemoteException {
        m_port = null;
        m_files = null;
        m_ClientQueue = null;
    }

    public FileServer(String port) throws RemoteException {
        m_port = port;
        m_files = new Vector<>();
        m_ClientQueue = new Vector<>();
    }

    public FileContents download(String client, String filename, String mode) throws RemoteException {
        // check if server is being shutdown. No new requests.
        if (!m_IsActive) {
            return null;
        }
        System.out.println("Downloading " + filename + " to " + client + " started.");
        FileContents fileContents = null;
        int index = -1;
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                index = i;
            }
            m_files.elementAt(i).removeReader(client);
        }
        if (index == -1) {
            try {
                FileEntry file;
                FileInputStream fin = new FileInputStream(filename);
                file = new FileEntry(filename, ServerState.NOT_SHARED);
                file.setContents(Files.readAllBytes(Paths.get(filename)));
                m_files.insertElementAt(file, 0);
                index = 0;
            } catch (Exception e) {
                System.err.println("Download failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        if (ServerState.fromId(mode) == ServerState.READ_SHARED) {
            m_files.elementAt(index).addReader(client);
            if (m_files.elementAt(index).getState() == ServerState.NOT_SHARED) {
                m_files.elementAt(index).setState(ServerState.READ_SHARED);
            }
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
            System.out.println("requesting return of " + filename + " for " + client);
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
            m_files.elementAt(index).setOwner(m_ClientQueue.elementAt(0));
            m_ClientQueue.removeElementAt(0);
        }
        fileContents = new FileContents(m_files.elementAt(index).getContents().clone());
        System.out.println("Downloading " + filename + " to " + client + " finished.");

        return fileContents;
    }

    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {

        for (int i = 0; i < this.m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                if (m_files.elementAt(i).getState() == ServerState.WRITE_SHARED
                        || m_files.elementAt(i).getState() == ServerState.OWNERSHIP_CHANGE) {
                    System.out.println(m_files.elementAt(i).getReaders().toString());
                    System.out.println(
                            "Uploading " + filename + " from " + m_files.elementAt(i).getOwner() + " started.");
                    m_files.elementAt(i).setContents(contents.get());
                    m_files.elementAt(i).sendInvalidates();
                    if (m_files.elementAt(i).getState() == ServerState.WRITE_SHARED) {
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

    // give credit
    public void shutDownServer(int code) throws RemoteException {
        if (code != m_ShutdownCode) {
            System.err.println("Unauthorized call to shutdown server");
            return;
        }
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

            Naming.unbind("rmi://localhost:" + m_port + "/fileserver");

            UnicastRemoteObject.unexportObject(this, true);

        } catch (Exception e) {
            System.err.println("Failed to shutdown due to error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server shut down gracefully");
        return;
    }

    private boolean anyFilesBeingWritten() {
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getState() == ServerState.OWNERSHIP_CHANGE) {
                return true;
            }
        }
        return false;
    }
}