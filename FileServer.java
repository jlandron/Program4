import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileServer extends UnicastRemoteObject implements ServerInterface {

    FileServer() throws RemoteException{

    }

    
    public FileContents download(String client, String filename, String mode) throws RemoteException {
        return null;
    }

    
    public boolean upload(String client, String filename, FileContents contents) throws RemoteException {
        return false;
    }

}