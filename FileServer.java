import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String m_port;
    private Vector<File> m_files;
    private HashMap<String, Vector<ClientState>> m_ClientList;

    FileServer() throws RemoteException {
        m_port = null;
        m_files = null;
        m_ClientList = null;
    }

    public FileServer(String port) throws RemoteException {
        m_port = port;
        m_files = new Vector<>();
        m_ClientList = new HashMap<>();
    }

    public synchronized FileContents download(String client, String filename, String mode) throws RemoteException {
        System.out.println("Downloading " + filename + " started.");
        FileContents fileContents;
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getName() == filename) {
                Vector<ClientState> cStates;
                // check if file is checked out in Write mode
                if (m_ClientList.containsKey(filename)) {
                    cStates = m_ClientList.get(filename);
                    for (int j = 0; j < cStates.size(); j++) {
                        if (cStates.elementAt(j).getState() == FileClientState.WRITE_OWNED) {
                            System.out.println("File is being written to by another user. Download not completed");
                            return null;
                        }
                    }
                } else {
                    // file is not in map
                    cStates = new Vector<>();
                }
                cStates.add(new ClientState(client, FileClientState.fromId(mode)));
                m_ClientList.put(filename, cStates);

                try {
                    fileContents = new FileContents(Files.readAllBytes(m_files.elementAt(i).toPath()));
                    System.out.println("Downloading " + filename + " complete.");
                    return fileContents;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {
        // check if file exists, if it does, write over file assuming only one client
        // can change a file at a time. change later to check client state??
        System.out.println("Uploading " + filename + " started.");
        FileOutputStream foStream;
        for (int i = 0; i < this.m_files.size(); i++) {
            if (m_files.elementAt(i).getName() == filename) {

                // check if file is checked out in read mode anywhere, send invalidation if so.
                if (m_ClientList.containsKey(filename)) {
                    Vector<ClientState> cStates = m_ClientList.get(filename);
                    for (int j = 0; j < cStates.size(); j++) {
                        if (cStates.elementAt(j).getState() == FileClientState.READ_SHARED) {
                            try {
                                ClientInterface client = (ClientInterface) Naming
                                        .lookup(cStates.elementAt(j).getName());
                                client.invalidate();
                                // locally mark that is should be invalid.
                                cStates.elementAt(j).setState(FileClientState.INVALID);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    foStream = new FileOutputStream("files/" + filename);
                    foStream.write(contents.get());
                    foStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }
        File file = new File("files/" + filename);
        try {
            foStream = new FileOutputStream(file.getPath());
            foStream.write(contents.get());
            foStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        m_files.add(file);
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

}