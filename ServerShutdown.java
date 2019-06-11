
/**
 *#############################################################################
 *#------------------------------ ServerShutdown ------------------------------
 *#  
 *#  @author 	Joshua Landron and Ed Abshire
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 * 
 * This class is instantiated after clients have shut down and server is ready to
 * be turned off 
 * ------------------------------------------------------------------------------
 **/
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerShutdown extends UnicastRemoteObject {
    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor
     */
    public ServerShutdown() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, NotBoundException {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("java ServerShutdown [serverIp] [port]");
            System.exit(-1);
        }
        // wait for enter to be pressed
        System.out.println("Press enter to shutdown the server");
        try {
            System.in.read();
        } catch (Exception e) {
            // TODO: handle exception
        }
        // Attempt to create a shutdown instance
        ServerInterface server;
        try {
            server = (ServerInterface) Naming.lookup("rmi://" + args[0] + ":" + args[1] + "/fileserver");
            server.shutDownServer();
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error shutting down server: " + e.getMessage());
            System.exit(-1);
        }
    }
}