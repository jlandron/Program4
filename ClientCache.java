import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientCache
{
    public enum FileMode {
        READ,
        WRITE
    }

    final Path _cacheFilePath = Path.of("/tmp/abshirelandron.txt");
    private String _fileName = "";
    private FileClient.ClientState _state = FileClient.ClientState.INVALID;
    private FileMode _fileMode = FileMode.READ;

    ClientCache()
    {

    }

    FileMode get_fileMode()
    {
        return _fileMode;
    }

    void set_fileMode(FileMode mode)
    {
        _fileMode = mode;
    }

    String get_fileName()
    {
        return _fileName;
    }

    void set_fileName(String fileName)
    {
        _fileName = fileName;
    }

    FileClient.ClientState get_state()
    {
        return _state;
    }

    void set_state(FileClient.ClientState state)
    {
        _state = state;
    }

    boolean cacheExists()
    {
        return Files.exists(_cacheFilePath);
    }

    boolean createCache(FileContents contents)
    {
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
}
