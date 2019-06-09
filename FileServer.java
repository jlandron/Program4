import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String m_port;
    private Vector<FileEntry> m_files;
    private Queue<String> m_ClientQueue;

    FileServer() throws RemoteException {
        m_port = null;
        m_files = null;
        m_ClientQueue = null;
    }

    public FileServer(String port) throws RemoteException {
        m_port = port;
        m_files = new Vector<>();
        m_ClientQueue = new LinkedList<>();
    }

    public FileContents download(String client, String filename, String mode) throws RemoteException {
        System.out.println("Downloading " + filename + " to " + client + " started.");
        FileContents fileContents = null;
        FileEntry file = null;
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                file = m_files.elementAt(i);
            }
            m_files.elementAt(i).removeReader(client);
        }
        if (file == null) {
            try {
                FileInputStream fin = new FileInputStream(filename);
                file = new FileEntry(filename, ServerState.NOT_SHARED);
                file.setContents(Files.readAllBytes(Paths.get(filename)));
            } catch (Exception e) {
                System.err.println("Download failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        fileContents = new FileContents(file.getContents());
        if (ServerState.fromId(mode) == ServerState.READ_SHARED) {
            file.addReader(client);
            if (file.getState() == ServerState.NOT_SHARED) {
                file.setState(ServerState.READ_SHARED);
            }
        } else if (ServerState.fromId(mode) == ServerState.WRITE_SHARED) {
            // add client to queue by default
            m_ClientQueue.add(client);
            // check state
            while (true) {
                // check if file is unshared or just being read
                if (file.getState() == ServerState.NOT_SHARED || file.getState() == ServerState.READ_SHARED) {
                    file.setState(ServerState.WRITE_SHARED);
                    break;
                } // check if file is in
                else if (file.getState() == ServerState.WRITE_SHARED) {
                    if (m_ClientQueue.peek().equals(client)) {
                        file.requestReturn();
                        break;
                    }
                }
                if (file.getState() == ServerState.OWNERSHIP_CHANGE) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.err.println("Error Downloading: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            file.setOwner(m_ClientQueue.remove());
        }
        return fileContents;
    }

    // {
    // fileContents = new FileContents(file.getContents());
    // System.out.println("Downloading " + filename +" to " + client + "
    // complete.");
    // return fileContents;
    // }catch(
    // Exception e)
    // {
    // e.printStackTrace();
    // return null;
    // }

    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {
        // check if file exists, if it does, write over file assuming only one client
        // can change a file at a time. change later to check client state??

        FileEntry file = null;
        for (int i = 0; i < this.m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                file = m_files.elementAt(i);
                ServerState state = file.getState();
                if (state == ServerState.WRITE_SHARED || state == ServerState.OWNERSHIP_CHANGE) {
                    System.out.println("Uploading " + filename + " started.");
                    file.setContents(contents.get());
                    file.sendInvalidates();
                    if (state == ServerState.WRITE_SHARED) {
                        file.setState(ServerState.NOT_SHARED);
                    } else if (state == ServerState.OWNERSHIP_CHANGE) {
                        file.setState(ServerState.WRITE_SHARED);
                    }
                } else {
                    return false;
                }
            }
        }
        if (file == null) {
            file = new FileEntry(filename, ServerState.NOT_SHARED);
        }

        file.setOwner(clientName);

        System.out.println("Uploading " + filename + " complete.");
        return true;
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
            e.printStackTrace();
            System.exit(-2);
        }
    }

    // give credit
    public void shutDownServer() throws RemoteException {
        try {
            Naming.unbind("rmi://localhost:" + m_port + "/fileserver");

            UnicastRemoteObject.unexportObject(this, false);
            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("server shut down gracefully");
        System.exit(0);
    }
}