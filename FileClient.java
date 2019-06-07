import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileClient extends UnicastRemoteObject implements ClientInterface {

    public enum ClientState
    {
        INVALID,
        READ_SHARED,
        WRITE_OWNED,
        RELEASE_OWNERSHIP
    }

    private BufferedReader m_Reader = null;

    private String _fileName = "";   // File to read or write
    private boolean _writeMode = false; // Access mode for the file
    private ServerInterface _server = null;
    private ClientCache _clientCache = null;
    private String _clientName = "";


    protected FileClient() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    public FileClient(String ipAddress, String port)
            throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException
    {
       // _server = (ServerInterface) Naming.lookup("rmi://" + ipAddress + ":" + port + "/fileserver");
        _clientCache = new ClientCache();
        _clientName = InetAddress.getLocalHost().getHostName();
    }

    /**
     * Will invalidate the current file in the cache if there is one
     *
     * @return True if file is invalidated, false otherwise
     * @throws RemoteException
     */
    public boolean invalidate() throws RemoteException {

        if ((_clientCache == null) || (_clientCache.get_state() == ClientState.WRITE_OWNED))
        {
            return false;
        }

        _clientCache.set_state(ClientState.INVALID);
        return true;

    }

    public boolean writeback() throws RemoteException {
        if ((_clientCache == null) || (_clientCache.get_state() != ClientState.RELEASE_OWNERSHIP))
        {
            return false;
        }

        FileContents contents = _clientCache.getCache();
        if (contents == null)
        {
            return false;
        }

        _server.upload(_clientName, _fileName, contents);
        return true;
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

            if (!_clientCache.cacheContainsFile(_fileName))
            {
                try
                {
                    FileContents contents = _server.download(_clientName, _fileName, (_writeMode ? "W" : "R"));
                    _clientCache.createCache(contents);
                    _clientCache.set_state((_writeMode ? ClientState.WRITE_OWNED : ClientState.READ_SHARED));
                }
                catch (RemoteException ex)
                {
                    System.err.println("Error downloading file: " + _fileName);
                    System.err.println("Error: " + ex.getMessage());
                    continue;
                }
            }
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
        catch (RemoteException | NotBoundException | MalformedURLException | UnknownHostException ex)
        {
            System.err.println("Error creating file client: " + ex.getMessage());
            System.exit(-1);
        }

        // Start client execution until user types "quit" or "exit" for filename
        client.start();
    }
}