public class ClientState {

    public enum state {
        READ, WRITE, INVALID
    }

    private String m_ClientName;
    private state m_State;

    ClientState() {
        m_ClientName = null;
        m_State = ClientState.state.READ;
    }

    ClientState(String clientName, String s) {
        m_ClientName = clientName;
        setState(s);
    }

    public ClientState.state getState() {
        return m_State;

    }

    public String getName(){
        return m_ClientName;
    }

    public void setState(String s) {
        if (s.equalsIgnoreCase("R")) {
            m_State = ClientState.state.READ;
        } else if (s.equalsIgnoreCase("W")) {
            m_State = ClientState.state.WRITE;
        } else if (s.equalsIgnoreCase("I")) {
            m_State = ClientState.state.INVALID;
        }
    }
}