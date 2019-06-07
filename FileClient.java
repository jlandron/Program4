import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileClient extends UnicastRemoteObject implements ClientInterface {

    private BufferedReader m_Reader = null;

    private String _fileName = "";   // File to read or write
    private boolean _writeMode = false; // Access mode for the file
    private ServerInterface _server = null;


    protected FileClient() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    public FileClient(String ipAddress, String port) throws RemoteException, NotBoundException, MalformedURLException
    {
       // _server = (ServerInterface) Naming.lookup("rmi://" + ipAddress + ":" + port + "/fileserver");
    }

    public boolean invalidate() throws RemoteException {
        return false;
    }

    public boolean writeback() throws RemoteException {
        return false;
    }

    /***
     * getFileInfo
     *
     * Gets the info about the file that the user wants to read or write to
     * Exits the program if the user types "exit" or "quit"
     */
    private void getFileInfo()
    {
        // Initialize keyboard
        Scanner keyboard = new Scanner(System.in);

        // Get file name
        System.out.print("Enter file name: ");
        _fileName  = keyboard.next();

        // check for exit
        if (_fileName.equalsIgnoreCase("exit") || _fileName.equalsIgnoreCase("quit"))
        {
            System.exit(0);
        }

        // Get read/write mode
        System.out.print("(R)ead or (W)rite mode: ");
        _writeMode = keyboard.next().equalsIgnoreCase("W");
    }

    private void start()
    {
        System.out.println("Enter \"quit\" or \"exit\" to end the program.");
        while (true)
        {
            getFileInfo();

            // Check cache for file, download if needed
            //_server.download("hostname", _fileName, (_writeMode ? "W" : "R"));
        }
    }

    /**
     * Main entry point for FileClient class
     *
     * @param args command line parameters, ipaddress/host and port
     */
    public static void main(String[] args)
    {
        // Check command line arguments
        if (args.length != 2)
        {
            System.out.println("java FileClient [serverIp] [port]");
            System.exit(-1);
        }

        FileClient client = null;

        // Attempt to create file client, exit on error
        try
        {
            client = new FileClient(args[0], args[1]);
            Naming.rebind("rmi://localhost:" + args[1] + "/fileclient",client);
            System.out.println("rmi://localhost:" + args[1] + "/fileclient invoked, client ready.");
        }
        catch (RemoteException | NotBoundException | MalformedURLException ex)
        {
            System.err.println("Error creating file client: " + ex.getMessage());
            System.exit(-1);
        }

        // Start client execution until user types "quit" or "exit" for filename
        client.start();
    }
}