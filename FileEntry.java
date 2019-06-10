
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
 *  Uses Java RMI
 *  Assumes that input upon execution will be in order as follows:
 *  
 * java FileServer port#
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
     * no args constructor that simply prints an error message and exits
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
     * no args constructor that simply prints an error message and exits
     */
    public FileEntry(String filename, ServerState state) {
        m_Data = null;
        m_FileName = filename;
        m_Readers = new Vector<>();
        m_Owner = null;
        m_State = state;
    }

    /**
     * ------------------------------------Constructor----------------------------------
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
     * ------------------------------------Constructor----------------------------------
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
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void setOwner(String owner) {
        m_Owner = owner;
        m_State = ServerState.WRITE_SHARED;
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void setContents(byte[] contents) {
        m_Data = contents.clone();
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized void setState(ServerState state) {
        m_State = state;
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized byte[] getContents() {
        return m_Data.clone();
    }

    /**
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized String getOwner() {
        return m_Owner;
    }

    /**
     * ------------------------------------Constructor----------------------------------
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
     * ------------------------------------Constructor----------------------------------
     * no args constructor that simply prints an error message and exits
     */
    public synchronized ServerState getState() {
        return m_State;
    }

    /**
     * ------------------------------------Constructor----------------------------------
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
     * ------------------------------------Constructor----------------------------------
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