import java.io.File;

public enum FileClientState
{
    INVALID("I"),
    READ_SHARED("R"),
    WRITE_OWNED("W"),
    RELEASE_OWNERSHIP("X");

    private String id;

    FileClientState(String id)
    {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static FileClientState fromId(String id)
    {
        for (FileClientState state : FileClientState.values())
        {
            if (state.id.equalsIgnoreCase(id))
            {
                return state;
            }
        }
        return null;
    }
}
