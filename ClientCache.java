import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientCache {
    public enum FileMode {
        READ, WRITE
    }

    final Path _cacheFilePath = Paths.get("/tmp/abshirelandron.txt");
    private String _fileName = "";
    private FileClient.ClientState _state = FileClient.ClientState.INVALID;
    private FileMode _fileMode = FileMode.READ;

    ClientCache() {

    }

    FileMode get_fileMode() {
        return _fileMode;
    }

    void set_fileMode(FileMode mode) {
        _fileMode = mode;
    }

    String get_fileName() {
        return _fileName;
    }

    void set_fileName(String fileName) {
        _fileName = fileName;
    }

    FileClient.ClientState get_state() {
        return _state;
    }

    void set_state(FileClient.ClientState state) {
        _state = state;
        // TODO: Add chmod call here based on state
    }

    boolean cacheExists() {
        return Files.exists(_cacheFilePath);
    }

    boolean createCache(FileContents contents) {
        try {
            Files.write(_cacheFilePath, contents.get());
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    FileContents getCache() {
        FileContents contents = null;
        try {
            contents = new FileContents(Files.readAllBytes(_cacheFilePath));
        } catch (IOException ex) {
            return null;
        }

        return contents;
    }

    boolean cacheContainsFile(String fileName) {
        return _fileName.equals(fileName);
    }
}
