import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import java.io.FileInputStream;
//import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String m_port;
    private Vector<FileEntry> m_files;

    FileServer() throws RemoteException {
        m_port = null;
        m_files = null;
    }

    public FileServer(String port) throws RemoteException {
        m_port = port;
        m_files = new Vector<>();
    }

    public synchronized FileContents download(String client, String filename, String mode) throws RemoteException {
        System.out.println("Downloading " + filename + " started.");
        FileContents fileContents;
        FileEntry file = null;
        for (int i = 0; i < m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                file = m_files.elementAt(i);
            }
        }
        if (file != null) {
            if (FileClientState.fromId(mode) == FileClientState.WRITE_OWNED) {
                file.requestReturn();
                file.setOwner(client);
                fileContents = new FileContents(file.getContents());
                return fileContents;
            } else if (FileClientState.fromId(mode) == FileClientState.READ_SHARED) {
            }
        } else {
            try {
                FileInputStream fin = new FileInputStream("files/" + filename);
                file = new FileEntry(filename, mode);
                fileContents = new FileContents(Files.readAllBytes(Paths.get("files/" + filename)));
                file.setContents(fileContents.get());
                if (FileClientState.fromId(mode) == FileClientState.WRITE_OWNED) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            fileContents = new FileContents(file.getContents());
            System.out.println("Downloading " + filename + " complete.");
            return fileContents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized boolean upload(String clientName, String filename, FileContents contents)
            throws RemoteException {
        // check if file exists, if it does, write over file assuming only one client
        // can change a file at a time. change later to check client state??
        System.out.println("Uploading " + filename + " started.");
        FileEntry file = null;
        for (int i = 0; i < this.m_files.size(); i++) {
            if (m_files.elementAt(i).getName().equals(filename)) {
                file = m_files.elementAt(i);
            }
        }
        if (file == null) {
            file = new FileEntry(filename, "R");
        }
        file.setOwner(clientName);
        file.sendInvalidates();
        file.setContents(contents.get());

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