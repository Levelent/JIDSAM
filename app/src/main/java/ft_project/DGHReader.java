package ft_project;

import java.io.*;
import java.util.*;

public class DGHReader {
    public final Map<String, DGH> DGHs;

    /**
     * Helper function to count the given indent of string
     * 
     * @param string to count indent of
     * @return the number of repeated 4 spaces
     */
    public static int indentCount(String string) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ' ') {
                count++;
            } else {
                return count / 4;
            }
        }
        return count / 4;
    }

    /**
     * Constructor used to read the DGH from a given file and generate the tree
     * 
     * @param filename name of file to read the information from
     */
    DGHReader(String filename) {
        HashMap<String, DGH> DGHs = new HashMap<String, DGH>();
        try {
            FileInputStream fStream;
            // Get the object of DataInputStream
            DataInputStream in;
            BufferedReader br;

            fStream = new FileInputStream(filename);
            in = new DataInputStream(fStream);
            br = new BufferedReader(new InputStreamReader(in));

            String strLine; // to store the currently read line
            Stack<String> stack = new Stack<String>();

            DGH latestDGH = null;
            int ic = 0; // index count
            while ((strLine = br.readLine()) != null) {
                if (strLine.charAt(0) == '$') {
                    // New tree needs created
                    DGH newDGH = new DGH(strLine.substring(1).strip());
                    DGHs.put(newDGH.name, newDGH);
                    latestDGH = newDGH;
                    stack = new Stack<String>();
                } else {
                    // The existing tree is in use
                    ic = indentCount(strLine);
                    strLine = strLine.trim();

                    if (stack.size() == 0) {
                        stack.add(strLine);
                        latestDGH.add(strLine); // A new DGH must've been created without the root name yet
                    } else {
                        if (ic > stack.size() - 1) {
                            String localRoot = stack.peek();
                            latestDGH.add(localRoot, strLine);
                            stack.add(strLine);
                        } else if (ic == stack.size() - 1) {
                            stack.pop();
                            String localRoot = stack.peek();
                            latestDGH.add(localRoot, strLine);
                            stack.add(strLine);
                        } else if (ic < stack.size() - 1) {
                            int c = stack.size() - ic;
                            for (int i = 0; i < c; i++) {
                                stack.pop();
                            }

                            String localRoot = stack.peek();
                            latestDGH.add(localRoot, strLine);
                            stack.add(strLine);
                        }
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.DGHs = DGHs;
    }
}
