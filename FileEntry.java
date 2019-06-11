
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
     * 
     * @param filename : name of this file object
     * @param state    : current state of this file object
     * 
     *                 constructor that initializes all fields to either the passed
     *                 to it or not.
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
     * 
     * method that adds a reader to m_Readers for this file
     * 
     * @param reader : string representing the clients name (ip:port#)
     * @return boolean : represents if the readers was added. returns false if the
     *         reader is already in the m_Readers list
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
     * 
     * method that removes a specific reader from any file they are currently
     * reading from.
     * 
     * @param reader : reader to be removed if they are reading from any file
     * @return boolean : represents if the reader was removed from any files reader
     *         list.
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
     * 
     * method that sets the owner field of this file object
     * 
     * @param owner : string representing the fileClient that is currently writing
     *              to this file
     */
    public synchronized void setOwner(String owner) {
        m_Owner = owner;
        m_State = ServerState.WRITE_SHARED;
    }

    /**
     * --------------------------------setContents----------------------------------
     * 
     * method that sets the contents of this file objects byte[]
     * 
     * @param contents : byte array holding the contents of this file.
     */
    public synchronized void setContents(byte[] contents) {
        m_Data = contents.clone();
    }

    /**
     * ----------------------------------setState----------------------------------
     * 
     * method that sets the state of this file object using ServerStates
     * 
     * @param state : a ServerState enum representing the current state of this file
     *              object.
     */
    public synchronized void setState(ServerState state) {
        m_State = state;
    }

    /**
     * -----------------------------------getContents-------------------------------
     * getter method to safely retrieve the contents of this file
     * 
     * @return m_Data : copy of this file's m_Data field.
     */
    public synchronized byte[] getContents() {
        return m_Data.clone();
    }

    /**
     * -------------------------------- getOwner-----------------------------------
     * getter method to return the current owner of this file.
     * 
     * @return m_Owner : string representing owner information
     */
    public synchronized String getOwner() {
        return m_Owner;
    }

    /**
     * ------------------------------------getName----------------------------------
     * 
     * getter method to return the name of this file
     * 
     * @return m_FileName : string representing the name of this file
     */
    public synchronized String getName() {
        return m_FileName;
    }

    /**
     * ------------------------------------getReaders----------------------------------
     * 
     * getter method that returns the current reader vector of this file
     * 
     * @return m_Readers : a vector holding strings of reader information.
     */
    public synchronized Vector<String> getReaders() {
        return m_Readers;
    }

    /**
     * ------------------------------------getState--------------------------------
     * 
     * getter method that returns the current state of this file.
     * 
     * @return m_State : a ServerState representing the current state of this file
     *         object
     */
    public synchronized ServerState getState() {
        return m_State;
    }

    /**
     * -----------------------------------sendInvalidates----------------------------------
     * 
     * method that send invalidation messages to all clients in this file's reader
     * list, this method also empties this files reader list so that multiple
     * invalidation calls are not made.
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
     * This method is called by the server to send a writeback request to the
     * current owner of a file. This method changes the state of this file in
     * preparation for the owning client to upload their version of the file to the
     * server.
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