package ft_project;

import java.io.*;

public class OutStream {
    private File file;
    private FileOutputStream fos;
    public PrintWriter out; // use to output to file

    /**
     * Constructor to generate out stream to a file
     * 
     * @param filepath of file to for output to be written to
     */
    OutStream(String filepath) {
        try {
            this.file = new File(filepath);
            this.fos = new FileOutputStream(file);
            this.out = new PrintWriter(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the file writer
     */
    public void close() {
        try {
            out.flush();
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
