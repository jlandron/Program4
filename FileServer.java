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
        // check if server is being shutdown. No new requests.
        if (!m_IsActive) {
            return null;
        }
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
                m_files.add(file);
            } catch (Exception e) {
                System.err.println("Download failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
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
        fileContents = new FileContents(file.getContents());
        return fileContents;
    }

    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {
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
            System.err.println("Upload failed due to stated file not existing");
            return false;
        }

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