package ft_project;

import java.util.Map;
import java.util.Arrays;

public class Tuple implements Cloneable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private String pid;
    public final String[] headings;
    private String[] data;
    private boolean beenOutputted = false;

    /**
     * Constructor create tuple with given headings and data
     * 
     * @param headings of the data
     * @param data     associated with headings
     */
    public Tuple(String[] headings, String[] data) {
        this.headings = headings;
        this.data = data;

        // set pid
        this.pid = this.data[Arrays.asList(headings).indexOf("pid")];
    }

    /**
     * Method to suppress the tuple and output it
     * 
     * @param outputStream stream to output the tuple to
     * @param d            DGHs used to calculate category generalisations
     */
    public void suppress(OutStream outputStream, Map<String, DGH> d) {
        // "internal" output
        // output t with the most generalized QI value
        String out = System.lineSeparator() + ANSI_YELLOW + "Suppress" + ANSI_RESET + System.lineSeparator();

        // output generalisations
        out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();

        String generalisations = "";
        for (int i = 0; i < headings.length; i++) {
            String heading = headings[i];
            String value = data[i];

            if (heading.equals("pid") || heading.equals("tid")) {
                continue;
            }

            // suppress -> output most generalised possible
            DGH dgh = d.get(heading);
            if (dgh == null) {
                // Must be a continuous generalisation
                generalisations += (new ContinuousGeneralisation(Float.parseFloat(value))).getMaxGeneralisation();
            } else {
                generalisations += dgh.getRootValue();
            }
            generalisations += " ";
        }
        out += generalisations;

        // output tuple;
        out += ANSI_CYAN + "Tuple" + ANSI_RESET + System.lineSeparator();
        this.setAsBeenOutput();
        out += this.toString();

        if (!this.beenOutputted) {
            Constants.incrementOut(1);
            Constants.addInfoLoss(1);
            if (Constants.verbose) {
                System.out.println(out);
            }
            // external output
            outputStream.out.println(generalisations);
        }
    }

    /**
     * Method to output a tuple with the generalisation of a cluster
     * 
     * @param outputStream stream to output the tuple to
     * @param d            DGHs used to calculate category generalisations
     * @param c            cluster to use the generalisation of
     */
    public void outputWith(OutStream outputStream, Map<String, DGH> d, Cluster c) {
        // "internal" output
        // output t with the c generalisation
        String out = System.lineSeparator() + ANSI_YELLOW + "Publish" + ANSI_RESET + System.lineSeparator();

        // output generalisations
        out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();

        String generalisations = "";
        for (String h : headings) {
            Generalisation g = c.getGeneralisations().get(h);
            generalisations += (g == null) ? "" : g.toString() + " ";
        }
        out += generalisations + System.lineSeparator();

        // output tuple;
        out += ANSI_CYAN + "Tuple" + ANSI_RESET + System.lineSeparator();
        this.setAsBeenOutput();
        out += this.toString();

        if (!this.beenOutputted) {
            Constants.incrementOut(1, c);
            Constants.addInfoLoss(1);
            if (Constants.verbose) {
                System.out.println(out);
            }
            // external output
            outputStream.out.println(generalisations);
        }
    }

    /**
     * Get the id of the tuple
     * 
     * @return id of tuple
     */
    public String getPid() {
        return this.pid;
    }

    /**
     * Get the value of a given attribute in the tuple by its index
     * 
     * @param index of value
     * @return data value at given index
     */
    public String getValue(int index) {
        return data[index];
    }

    /**
     * Get the value of a given attribute in the tuple by the name of the tuple
     * 
     * @param fieldName name of field to get the value of
     * @return value associated with given heading
     */
    public String getValue(String fieldName) {
        for (int i = 0; i < headings.length; i++) {
            if (headings[i].equals(fieldName)) {
                return data[i];
            }
        }
        return null;
    }

    /**
     * @return if the tuple has been outputted
     */
    public Boolean hasBeenOutput() {
        return beenOutputted;
    }

    /**
     * Set the tuple as outputted
     */
    public void setAsBeenOutput() {
        this.beenOutputted = true;
    }

    /**
     * @return string representation of the tuple
     */
    public String toString() {
        String out = "";
        for (String item : data) {
            out += item + " ";
        }
        return out;
    }

    /**
     * @return deep clone of tuple
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
