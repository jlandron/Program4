import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileClient extends UnicastRemoteObject implements ClientInterface {

    private BufferedReader m_Reader = null;

    protected FileClient() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean invalidate() throws RemoteException {
        return false;
    }

    @Override
    public boolean writeback() throws RemoteException {
        return false;
    }

}