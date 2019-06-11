/**
 * ClientState enumeration to determine the state of the client
 */
public enum ClientState {
    INVALID("I"), READ_SHARED("R"), WRITE_OWNED("W"), RELEASE_OWNERSHIP("X");

    // Local variable to hold the id of the enum
    private String id;

    /**
     * "Constructor" - allows the enum to have a value
     *
     * @param id The single character value representing the enum
     */
    ClientState(String id) {
        this.id = id;
    }

    /**
     * Returns the string value of the enum
     *
     * @return the enum id ("R", "W", "I", "X")
     */
    public String getId() {
        return this.id;
    }

    /***
     * Gets the enumeration value based on the string id passed in
     *
     * @param id string value of the enum to get.
     * @return ClientState based on the enum, null otherwise
     */
    public static ClientState fromId(String id) {
        for (ClientState state : ClientState.values()) {
            if (state.id.equalsIgnoreCase(id)) {
                return state;
            }
        }
        return null;
    }
}