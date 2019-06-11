import java.rmi.*;

/**
 * RMI Required interface for the FileClient
 */
public interface ClientInterface extends Remote {
    public boolean invalidate( ) throws RemoteException;
    public boolean writeback( ) throws 	RemoteException;
}
