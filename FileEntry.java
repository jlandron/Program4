
/**
 *#############################################################################
 *#------------------------------ FileEntry -----------------------------------
 *#  
 *#  @author 	Joshua Landron
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 *
 * Implementation and assumptions:
 * 
 * ------------------------------------------------------------------------------
 **/
import java.rmi.Naming;
import java.util.Vector;

public class FileEntry {
    private String m_FileName;
    private Vector<String> m_Readers;
    private String m_Owner;
    private ServerState m_State;
    private byte[] m_Data;

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply sets all fields to null
     */
    public FileEntry() {
        m_Data = null;
        m_FileName = null;
        m_Readers = null;
        m_Owner = null;
        m_State = null;
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that initializes all fields to either the passed to it or
     * not.
     */
    public FileEntry(String filename, ServerState state) {
        m_Data = null;
        m_FileName = filename;
        m_Readers = new Vector<>();
        m_Owner = null;
        m_State = state;
    }

    /**
     * -----------------------------------addReader------------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized boolean addReader(String reader) {
        // first check if client is already a reader, if so they do not need to be
        // added.
        for (int i = 0; i < m_Readers.size(); i++) {
            if (m_Readers.elementAt(i).equals(reader)) {
                return false;
            }
        }
        m_Readers.add(reader);
        return true;
    }

    /**
     * ------------------------------------removeReader----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized boolean removeReader(String reader) {
        for (int i = 0; i < m_Readers.size(); i++) {
            if (m_Readers.elementAt(i).equals(reader)) {
                m_Readers.removeElementAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * ------------------------------------setOwner----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void setOwner(String owner) {
        m_Owner = owner;
        m_State = ServerState.WRITE_SHARED;
    }

    /**
     * -----------------------------------setContents----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void setContents(byte[] contents) {
        m_Data = contents.clone();
    }

    /**
     * ------------------------------------
     * setState---------------------------------- no args constructor that simply
     * prints an error message and exits
     */
    public synchronized void setState(ServerState state) {
        m_State = state;
    }

    /**
     * -----------------------------------
     * getContents---------------------------------- no args constructor that simply
     * prints an error message and exits
     */
    public synchronized byte[] getContents() {
        return m_Data.clone();
    }

    /**
     * ------------------------------------
     * getOwner--------------------------------- no args constructor that simply
     * prints an error message and exits
     */
    public synchronized String getOwner() {
        return m_Owner;
    }

    /**
     * ------------------------------------getName----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized String getName() {
        return m_FileName;
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized Vector<String> getReaders() {
        return m_Readers;
    }

    /**
     * ------------------------------------
     * getState---------------------------------- no args constructor that simply
     * prints an error message and exits
     */
    public synchronized ServerState getState() {
        return m_State;
    }

    /**
     * -----------------------------------sendInvalidates----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void sendInvalidates() {
        for (int i = 0; i < m_Readers.size(); i++) {
            try {
                System.out.println("Sending invalidation to " + m_Readers.elementAt(i));
                ClientInterface cInterface = (ClientInterface) Naming
                        .lookup("rmi://" + m_Readers.elementAt(i) + "/fileclient");
                cInterface.invalidate();
            } catch (Exception e) {
                System.out.println("Removing reader " + m_Readers.elementAt(i) + " Due to error: " + e.getMessage());
                e.printStackTrace();
                m_Readers.removeElementAt(i);
            }
        }
        m_Readers.clear();
    }

    /**
     * ------------------------------------requestReturn----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void requestReturn() {
        try {
            ClientInterface cInterface = (ClientInterface) Naming.lookup("rmi://" + m_Owner + "/fileclient");
            System.out.println("Requesting return from " + m_Owner);
            cInterface.writeback();
            m_State = ServerState.OWNERSHIP_CHANGE;
        } catch (Exception e) {
            System.out.println("Removing owner " + m_Owner + " Due to error: " + e.getMessage());
            e.printStackTrace();
            m_Owner = null;
        }
    }
}