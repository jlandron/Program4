import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientCache {

    final String tempDir = System.getProperty("java.io.tmpdir");
    final Path _cacheFilePath = Paths.get(tempDir + "/abshirelandron.txt");
    private String _fileName = "";
    private FileClientState _state = FileClientState.INVALID;

    ClientCache() {
        if (Files.exists(_cacheFilePath)) {
            try {
                Files.delete(_cacheFilePath);
            } catch (IOException ex) {
                System.err.println("Error clearing cache: " + ex.getMessage());
            }
        }
    }

    public boolean clearCache() {
        try {
            if (Files.exists(_cacheFilePath)) {
                UnixTools.changeFileMode(_cacheFilePath.toString(), true);
                Files.delete(_cacheFilePath);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("Error clearing cache: " + ex.getMessage());
            return false;
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
     * @return FileClientState enum indicating the state of the cache
     */
    FileClientState get_state() {
        return _state;
    }

    /**
     * Sets the state of the client cache, also sets the cache file in read or write
     * mode.
     *
     * @param state FileClientState enum indicating the state to set the cache
     */
    void set_state(FileClientState state) {
        _state = state;

        UnixTools.changeFileMode(_cacheFilePath.toString(), (_state != FileClientState.READ_SHARED));
    }

    /**
     *
     * @param contents
     * @return
     */
    boolean createCache(FileContents contents) {
        try {
            Files.write(_cacheFilePath, contents.get());
        } catch (IOException ex) {
            System.err.println("Error creating cache: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    FileContents getCache() {
        FileContents contents = null;
        try {
            contents = new FileContents(Files.readAllBytes(_cacheFilePath));
        } catch (IOException ex) {
            System.err.println("Error retreiving cache:" + ex.getMessage());
            return null;
        }

        return contents;
    }

    /**
     *
     * @param fileName
     * @return
     */
    boolean cacheContainsFile(String fileName) {
        return _fileName.equals(fileName);
    }
}
