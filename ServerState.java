/*
 *#############################################################################
 *#------------------------------ ServerState -----------------------------------
 *#  
 *#  @author 	Joshua Landron and Ed Abshire
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 *
 * ServerState enumeration to determine the state of the files in the server
 */
public enum ServerState {
    NOT_SHARED("N"), READ_SHARED("R"), WRITE_SHARED("W"), OWNERSHIP_CHANGE("C");

    // Local variable to hold the id of the enum
    private String id;

    /**
     * "Constructor" - allows the enum to have a value
     *
     * @param id The single character value representing the enum
     */
    ServerState(String id) {
        this.id = id;
    }

    /**
     * Returns the string value of the enum
     *
     * @return the enum id ("R", "W", "N", "C")
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
    public static ServerState fromId(String id) {
        for (ServerState state : ServerState.values()) {
            if (state.id.equalsIgnoreCase(id)) {
                return state;
            }
        }
        return null;
    }
}