
/*
 *#############################################################################
 *#------------------------------ FileContents -----------------------------------
 *#  
 *#  @author 	Joshua Landron and Ed Abshire (originally Dr.Munehiro Fukuda)
 *#  @date 	    01Jun2019
 *#  @version	9Jun2019
 *#
 *#  Built as part of CSS434 with Dr. Munehiro Fukuda, Spring 2019
 *#
 *#############################################################################
 *
 * Object to hold contents of a file for storage in clients cache
 */
import java.io.*;

public class FileContents implements Serializable {
    private byte[] contents;

    public FileContents() {
        this.contents = null;
    }

    public FileContents(byte[] contents) {
        this.contents = contents;
    }

    public void print() throws IOException {
        System.out.println("FileContents = " + contents);
    }

    public byte[] get() {
        return contents;
    }
}
