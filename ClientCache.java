import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ClientCache class stores the file locally for the client in order
 * to improve performance if the same file is used over and over.
 */
class ClientCache {

    private final String tempDir = System.getProperty("java.io.tmpdir");
    private final Path _cacheFilePath = Paths.get(tempDir + "/abshirelandron.txt");
    private String _fileName = "";
    private ClientState _state = ClientState.INVALID;

    /**
     * Constructor - deletes an existing cache on creation.
     */
    ClientCache() {
        if (Files.exists(_cacheFilePath)) {
            try {
                Files.delete(_cacheFilePath);
            } catch (IOException ex) {
                System.err.println("Error clearing cache: " + ex.getMessage());
            }
        }
    }

    /**
     * Clears out the cache
     */
    void clearCache() {
        try {
            if (Files.exists(_cacheFilePath)) {
                UnixTools.changeFileMode(_cacheFilePath.toString(), true);
                Files.delete(_cacheFilePath);
            }
        } catch (Exception ex) {
            System.err.println("Error clearing cache: " + ex.getMessage());
        }
    }

    /**
     * Get the name of the file cache
     *
     * @return Location of the file cache as a string
     */
    String getCacheName() {
        return _cacheFilePath.toString();
    }

    /**
     * Gets file name of the file stored in cache
     *
     * @return String containing the file name stored in the cache
     */
    String get_fileName() {
        return _fileName;
    }

    /**
     * Sets the file name of the file stored in the cache
     *
     * @param fileName String containing the name of the file being stored in the
     *                 cache
     */
    void set_fileName(String fileName) {
        _fileName = fileName;
    }

    /**
     * Gets the state of the client cache
     *
     * @return ClientState enum indicating the state of the cache
     */
    ClientState get_state() {
        return _state;
    }

    /**
     * Sets the state of the client cache, also sets the cache file in read or write
     * mode.
     *
     * @param state ClientState enum indicating the state to set the cache
     */
    void set_state(ClientState state) {
        _state = state;

        UnixTools.changeFileMode(_cacheFilePath.toString(), (_state != ClientState.READ_SHARED));
    }

    /**
     * Creates a cache from a new FileContent object in the temp folder for the user
     *
     * @param contents FileContent object containing the requested file
     */
    void createCache(FileContents contents) {
        try {
            Files.write(_cacheFilePath, contents.get());
        } catch (IOException ex) {
            System.err.println("Error creating cache: " + ex.getMessage());
        }

    }

    /**
     * Returns the content of the cache in a FileContents object
     *
     * @return FileContent object containing the file, null on error
     */
    FileContents getCache() {
        FileContents contents;
        try {
            contents = new FileContents(Files.readAllBytes(_cacheFilePath));
        } catch (IOException ex) {
            System.err.println("Error retreiving cache:" + ex.getMessage());
            return null;
        }

        return contents;
    }

    /**
     * Checks to see if the file name passed in is contained in the cache
     *
     * @param fileName String containing the name of the file to check to
     *                 see if it is in the cache
     * @return True if the cache contains the file, false otherwise
     */
    boolean cacheContainsFile(String fileName) {
        return _fileName.equals(fileName);
    }
}
