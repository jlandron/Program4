import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerShutdown extends UnicastRemoteObject {

    public ServerShutdown() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, NotBoundException {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("java ServerShutdown [serverIp] [port]");
            System.exit(-1);
        }
        // Attempt to create a shutdown instance
        ServerInterface server;
        try {
            server = (ServerInterface) Naming.lookup("rmi://" + args[0] + ":" + args[1] + "/fileserver");
            server.shutDownServer(1234);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error shutting down server: " + e.getMessage());
            System.exit(-1);
        }

    }
}