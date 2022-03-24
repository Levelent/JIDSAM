package ft_project;

import java.io.*;

public class InStream {
    private FileInputStream fStream;
    // Get the object of DataInputStream
    private DataInputStream in;
    private BufferedReader br;

    public String[] headings; // to store the first row (file headings)

    /**
     * Constructor to set up reading from file to data stream
     * 
     * @param filepath file path of the file to read from
     */
    public InStream(String filepath) {
        try {
            // open file input stream
            fStream = new FileInputStream(filepath);
            // Get the object of DataInputStream
            in = new DataInputStream(fStream);
            br = new BufferedReader(new InputStreamReader(in));

            String strLine; // to store the currently read line

            // Read first line to get headings
            if ((strLine = br.readLine()) != null) {
                headings = strLine.split(",");
            } else {
                System.out.println("File is empty");
            }
        } catch (Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Close the file
     */
    public void close() {
        // close the file stream
        try {
            in.close();
        } catch (Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Get the next tuple in the file
     * 
     * @return next tuple
     */
    public Tuple next() {
        // Read File Line By Line
        String strLine; // to store the currently read line
        try {
            if ((strLine = br.readLine()) != null) {
                String[] row = strLine.split(",");
                return new Tuple(headings, row);
            }
        } catch (Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

}
