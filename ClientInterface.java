
/*
 *#############################################################################
 *#------------------------------ ClientInterface -----------------------------------
 *#  
 *#  @author 	Joshua Landron and Ed Abshire (originally Dr.Munehiro Fukuda)
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 *
 * Interface for FileClients, used by FileServers
 */
import java.rmi.*;

/**
 * RMI Required interface for the FileClient
 */
public interface ClientInterface extends Remote {

    public boolean invalidate() throws RemoteException;

    public boolean writeback() throws RemoteException;
}
