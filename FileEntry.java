import java.rmi.Naming;
import java.util.Vector;

import javax.sql.rowset.spi.SyncResolver;

public class FileEntry {
    private String m_FileName;
    private Vector<String> m_Readers;
    private String m_Owner;
    private FileClientState m_State;
    private byte[] m_Data;

    public FileEntry() {
        m_Data = null;
        m_FileName = null;
        m_Readers = null;
        m_Owner = null;
        m_State = null;
    }

    public FileEntry(String filename, String id) {
        m_Data = null;
        m_FileName = filename;
        m_Readers = new Vector<>();
        m_Owner = null;
        m_State = FileClientState.fromId(id);
    }

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

    public synchronized void setOwner(String owner) {
        m_Owner = owner;
        m_State = FileClientState.WRITE_OWNED;
    }

    public synchronized void setContents(byte[] contents) {
        m_Data = contents.clone();
    }

    public synchronized void setState(String id) {
        m_State = FileClientState.fromId(id);
    }

    public byte[] getContents() {
        return m_Data.clone();
    }

    public String getOwner() {
        return m_Owner;
    }

    public String getName() {
        return m_FileName;
    }

    public Vector<String> getReaders() {
        return m_Readers;
    }

    public void sendInvalidates() {
        for (int i = 0; i < m_Readers.size(); i++) {
            try {
                ClientInterface cInterface = (ClientInterface) Naming.lookup(m_Readers.elementAt(i));
                cInterface.invalidate();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Removing reader " + m_Readers.elementAt(i) + " Due to error.");
                m_Readers.removeElementAt(i);
            }
        }
    }

    public void requestReturn() {
        try {
            ClientInterface cInterface = (ClientInterface) Naming.lookup(m_Owner);
            cInterface.writeback();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Removing owner " + m_Owner + " Due to error.");
            m_Owner = null;
        }
        m_State = FileClientState.fromId("X");
        m_Owner = null;
    }
}