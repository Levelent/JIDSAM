package ft_project;

import java.io.*;

public class Stream {
    private FileInputStream fStream;
    // Get the object of DataInputStream
    private DataInputStream in;
    private BufferedReader br;

    private String[] headings; // to store the first row (file headings)

    public Stream(String filepath) {
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

    public void close() {
        // close the file stream
        try {
            in.close();
        } catch(Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public Tuple next() {
        // Read File Line By Line
        String strLine; // to store the currently read line
        try {
            if ((strLine = br.readLine()) != null) {
                String[] row = strLine.split(",");
                return new Tuple(headings, row);
            }
        } catch(Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }
}
