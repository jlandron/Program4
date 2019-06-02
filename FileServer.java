import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String port = null;
    private Vector<FileContents> files = null;

    FileServer() throws RemoteException {

    }

    public FileServer(String port) throws RemoteException {
        this.port = port;
        this.files = new Vector<>();
    }

    public FileContents download(String client, String filename, String mode) throws RemoteException {
        FileContents file = null;
        for (int i = 0; i < this.files.size(); i++) {

        }
        return file;
    }

    public boolean upload(String client, String filename, FileContents contents) throws RemoteException {
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
            e.printStackTrace();
            System.exit(-2);
        }
    }

}