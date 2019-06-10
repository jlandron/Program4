import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

class FileClient extends UnicastRemoteObject implements ClientInterface {

    private String _fileName = ""; // File to read or write
    private boolean _writeMode = false; // Access mode for the file
    private ServerInterface _server = null;
    private ClientCache _clientCache = null;
    private String _clientName = "";

    protected FileClient() throws RemoteException {
        super();
    }

    public FileClient(String ipAddress, String port)
            throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
        _server = (ServerInterface) Naming.lookup("rmi://" + ipAddress + ":" + port + "/fileserver");
        _clientCache = new ClientCache();
        _clientName = (InetAddress.getLocalHost().getHostName() + ":" + port);
    }

    /**
     * Will invalidate the current file in the cache if there is one
     *
     * @return True if file is invalidated, false otherwise
     * @throws RemoteException if something happens on call
     */
    public boolean invalidate() throws RemoteException {

        if ((_clientCache == null) || (_clientCache.get_state() == FileClientState.WRITE_OWNED)) {
            return false;
        }
        _clientCache.set_state(FileClientState.INVALID);
        return true;
    }

    /**
     * writeback is called by the server, telling client to write back the file
     * after done with the current edit. Only valid in WRITE_SHARED
     *
     * @return true if valid state transition, false otherwise
     * @throws RemoteException on RMI error
     */
    public boolean writeback() throws RemoteException {
        if ((_clientCache == null) || (_clientCache.get_state() == FileClientState.INVALID)
                || (_clientCache.get_state() == FileClientState.READ_SHARED)) {
            return false;
        }

        _clientCache.set_state(FileClientState.RELEASE_OWNERSHIP);
        return true;
    }

    /***
     * getFileInfo
     *
     * Gets the info about the file that the user wants to read or write to Exits
     * the program if the user types "exit" or "quit"
     *
     * @return boolean true if user wants to exit, false otherwise
     */
    private boolean getFileInfo() {
        // Initialize keyboard
        Scanner keyboard = new Scanner(System.in);

        // Get file name
        System.out.print("Enter file name: ");
        _fileName = keyboard.next();

        // check for exit
        if (_fileName.equalsIgnoreCase("exit") || _fileName.equalsIgnoreCase("quit")) {
            // Want to exit
            return true;
        }

        // Get read/write mode
        System.out.print("(R)ead or (W)rite mode: ");
        _writeMode = keyboard.next().equalsIgnoreCase("W");
        return false;
    }

    /**
     * Starts the file client loops of reading or writing files until the user types
     * "quit" or "exit"
     */
    private void start() {
        boolean exit;

        System.out.println("Enter \"quit\" or \"exit\" to end the program.");
        while (true) {
            // Get information from user about file
            exit = getFileInfo();

            // If exiting, upload any file in write mode or release mode
            if (exit) {
                if ((_clientCache != null) && ((_clientCache.get_state() == FileClientState.WRITE_OWNED)
                        || (_clientCache.get_state() == FileClientState.RELEASE_OWNERSHIP))) {
                    if (!uploadFile(_clientCache.get_fileName(), _clientCache.getCache())) {
                        System.err.println("Error uploading file on exit: " + _clientCache.get_fileName());
                    }
                }
                System.exit(0);
            }

            // Check if cache doesn't contain the file user wants or that it is invalid
            if (!_clientCache.cacheContainsFile(_fileName) || _clientCache.get_state() == FileClientState.INVALID) {
                // If new file, check if cache is in write mode and upload the current file
                if (_clientCache.get_state() == FileClientState.WRITE_OWNED
                        || _clientCache.get_state() == FileClientState.RELEASE_OWNERSHIP) {
                    if (!uploadFile(_clientCache.get_fileName(), _clientCache.getCache())) {
                        // Error uploading the file to server, skip new download.
                        System.err.println("Upload failed: " + _clientCache.get_fileName());
                        continue;
                    }
                }

                // Attempt to get file and cache it
                FileContents contents = downloadFile(_fileName, _writeMode);
                if (contents == null) {
                    System.err.println("Unable to download file: " + _fileName);
                    continue;
                }
                _clientCache.clearCache();
                _clientCache.createCache(contents);
                _clientCache.set_fileName(_fileName);
                _clientCache.set_state((_writeMode ? FileClientState.WRITE_OWNED : FileClientState.READ_SHARED));
            }
            // Cache contains the file and it's not invalid
            else {
                // Check if switching from read to write
                if (_writeMode && (_clientCache.get_state() == FileClientState.READ_SHARED)) {
                    FileContents contents = downloadFile(_fileName, _writeMode);
                    if (contents == null) {
                        System.err.println("Unable to switch to write mode: " + _fileName);
                        continue;
                    }
                    _clientCache.set_state(FileClientState.WRITE_OWNED);
                }
            }

            // Launch editor
            try {
                UnixTools.runEmacs(_clientCache.getCacheName());
            } catch (Exception ex) {
                System.err.println("Error opening emacs: " + ex.getMessage());
            }

            // Check post-edit for upload request
            if (_clientCache.get_state() == FileClientState.RELEASE_OWNERSHIP) {
                if (!uploadFile(_clientCache.get_fileName(), _clientCache.getCache())) {
                    System.err.println("Upload failed:" + _clientCache.get_fileName());
                    continue;
                }
                _clientCache.set_state(FileClientState.READ_SHARED);
            }
        }
    }

    /**
     *
     * @param fileName
     * @param writeMode
     * @return
     */
    private FileContents downloadFile(String fileName, boolean writeMode) {
        FileContents contents = null;
        try {
            contents = _server.download(_clientName, fileName, (writeMode ? "W" : "R"));
        } catch (RemoteException ex) {
            System.err.println("Error downloading from server: " + ex.getMessage());
        }

        return contents;
    }

    /**
     *
     * @param fileName
     * @param contents
     * @return
     */
    private boolean uploadFile(String fileName, FileContents contents) {
        boolean result;
        try {
            result = _server.upload(_clientName, fileName, contents);
            _clientCache.set_state(FileClientState.INVALID);
        } catch (RemoteException ex) {
            System.err.println("Error uploading to server: " + ex.getMessage());
            result = false;
        }

        return result;
    }

    /**
     * Main entry point for FileClient class
     *
     * @param args command line parameters, ipaddress/host and port
     */
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("java FileClient [serverIp] [port]");
            System.exit(-1);
        }

        FileClient client = null;

        // Attempt to create file client, exit on error
        try {
            client = new FileClient(args[0], args[1]);
            Naming.rebind("rmi://localhost:" + args[1] + "/fileclient", client);
            System.out.println("rmi://localhost:" + args[1] + "/fileclient invoked, client ready.");
        } catch (RemoteException | NotBoundException | MalformedURLException | UnknownHostException ex) {
            System.err.println("Error creating file client: " + ex.getMessage());
            System.exit(-1);
        }

        // Start client execution until user types "quit" or "exit" for filename
        client.start();
    }
}