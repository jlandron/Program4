import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientCache {

    final Path _cacheFilePath = Paths.get("/tmp/abshirelandron.txt");
    private String _fileName = "";
    private FileClientState _state = FileClientState.INVALID;

    ClientCache() {}

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
     * @param fileName String containing the name of the file being stored in the cache
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
     * Sets the state of the client cache, also sets the cache file in
     * read or write mode.
     *
     * @param state FileClientState enum indicating the state to set the cache
     */
    void set_state(FileClientState state) {
        _state = state;
        // TODO: Add chmod call here based on state
    }

    boolean createCache(FileContents contents) {
        try
        {
            Files.write(_cacheFilePath, contents.get());
        }
        catch (IOException ex)
        {
            return false;
        }

        return true;
    }

    FileContents getCache() {
        FileContents contents = null;
        try
        {
            contents = new FileContents(Files.readAllBytes(_cacheFilePath));
        }
        catch (IOException ex)
        {
            return null;
        }

        return contents;
    }

    boolean cacheContainsFile(String fileName) {
        return _fileName.equals(fileName);
    }
}
