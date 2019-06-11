
/*
 *#############################################################################
 *#------------------------------ FileServer -----------------------------------
 *#  
 *#  @author 	Joshua Landron 
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 */
import java.rmi.*;

public interface ServerInterface extends Remote {
    public FileContents download(String client, String filename, String mode) throws RemoteException;

    public boolean upload(String client, String filename, FileContents contents) throws RemoteException;

    public void shutDownServer() throws RemoteException;
}
