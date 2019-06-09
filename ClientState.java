public class ClientState {

    public enum state {
        READ, WRITE, INVALID
    }

    private String m_ClientName;
    private FileClientState m_State;

    ClientState() {
        m_ClientName = null;
        m_State = FileClientState.READ_SHARED;
    }

    ClientState(String clientName, FileClientState state) {
        m_ClientName = clientName;
        m_State = state;
    }

    public FileClientState getState() {
        return m_State;

    }

    public String getName(){
        return m_ClientName;
    }

    public void setState(FileClientState state) {
        m_State = state;
    }
}