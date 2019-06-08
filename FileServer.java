import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.io.*;
import java.nio.file.Files;

class FileServer extends UnicastRemoteObject implements ServerInterface {
    private String port;
    private Vector<File> files;

    FileServer() throws RemoteException {
        port = null;
        files = null;
    }

    public FileServer(String port) throws RemoteException {
        this.port = port;
        this.files = new Vector<>();
    }

    public FileContents download(String client, String filename, String mode) throws RemoteException {
        FileContents fileContents;
        for (int i = 0; i < this.files.size(); i++) {
            if (files.elementAt(i).getName() == filename) {
                try {
                    fileContents = new FileContents(Files.readAllBytes(files.elementAt(i).toPath()));
                    return fileContents;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        File file = new File("files/" + filename);

        return null;
    }

    public boolean upload(String client, String filename, FileContents contents) throws RemoteException {
        // check if file exists, if it does, write over file assuming only one client
        // can change a file at a time. change later to check client state??
        for (int i = 0; i < this.files.size(); i++) {
            if (files.elementAt(i).getName() == filename) {
                try {
                    FileOutputStream foStream = new FileOutputStream("files/" + filename);
                    foStream.write(contents.get());
                    foStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
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
            e.printStackTrace();
            System.exit(-2);
        }
    }

}