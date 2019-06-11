
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
import java.io.File;
import java.io.IOException;

/**
 * Class to hold Unix tools
 */
public class UnixTools {
    /**
     * Using ProcessBuilder because terminal windows suck Taken off discussion
     * boards because I was going to do the same thing.
     *
     * @param fileName Name of file to open in emacs
     * @return int containing exit code
     * @throws IOException          Exception due to IO
     * @throws InterruptedException Process Interrupted
     */
    public static int runEmacs(String fileName) throws IOException, InterruptedException {
        // Create the command array.
        String editor;

        if (System.getProperty("os.name").startsWith("Windows")) {
            editor = "notepad";
        } else {
            editor = "emacs";
        }

        String[] cmd = { editor, fileName };

        // Process builder with inherited IO should allow a console
        // based editor to take over the terminal.
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        return pb.start().waitFor();
    }

    /**
     * Using ProcessBuilder because terminal windows suck Taken off discussion
     * boards because I was going to do the same thing.
     *
     * @param fileName  : Name of file to open in emacs
     * @param writeable : if the file should be writable of not
     * @return : int containing exit code
     */
    public static boolean changeFileMode(String fileName, boolean writeAble) {
        boolean result = true;

        File cacheFile = new File(fileName);
        result = result & cacheFile.setReadable(true);
        result = result & cacheFile.setWritable(writeAble);

        return result;
    }
}
