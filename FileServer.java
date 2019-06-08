import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.sql.rowset.spi.SyncResolver;

import java.io.*;
import java.nio.file.Files;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String port;
    private Vector<File> files;
    private HashMap<String, FileClient> clientList;

    FileServer() throws RemoteException {
        this.port = null;
        this.files = null;
        this.clientList = null;
    }

    public FileServer(String port) throws RemoteException {
        this.port = port;
        this.files = new Vector<>();
        this.clientList = new HashMap<>();
    }

    public synchronized FileContents download(String client, String filename, String mode) throws RemoteException {
        System.out.println("Downloading " + filename + " started.");
        FileContents fileContents;

        for (int i = 0; i < this.files.size(); i++) {
            if (files.elementAt(i).getName() == filename) {
                for (Map.Entry<String, FileClient> entry : clientList.entrySet()) {
                    if(entry.getKey() == filename){ //check for write ownership

                    }
                }
                try {
                    fileContents = new FileContents(Files.readAllBytes(files.elementAt(i).toPath()));
                    System.out.println("Downloading " + filename + " complete.");
                    return fileContents;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public synchronized boolean upload(String client, String filename, FileContents contents) throws RemoteException {
        // check if file exists, if it does, write over file assuming only one client
        // can change a file at a time. change later to check client state??
        System.out.println("Uploading " + filename + " started.");
        FileOutputStream foStream;
        for (int i = 0; i < this.files.size(); i++) {
            if (files.elementAt(i).getName() == filename) {
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
        files.add(file);
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