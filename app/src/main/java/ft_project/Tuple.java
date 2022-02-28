package ft_project;

import java.util.Arrays;

public class Tuple implements Cloneable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private String pid;
    public final String[] headings;
    private String[] data;
    private boolean beenOutputted = false;

    public Tuple(String[] headings, String[] data) {
        this.headings = headings;
        this.data = data;

        // set pid
        this.pid = this.data[Arrays.asList(headings).indexOf("pid")];
    }

    public void suppress() {
        // it outputs t with the most generalized QI value
        String out = ANSI_YELLOW + "Suppress" + ANSI_RESET + System.lineSeparator();

        // output generalisations
        out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();

        // TODO with generalisations ~ "most generalized QI value"

        // output tuple;
        out += ANSI_CYAN + "Tuple" + ANSI_RESET + System.lineSeparator();
        this.setAsBeenOutput();
        out += this.toString() + System.lineSeparator();

        System.out.println(out);
    }

    public String getPid() {
        return this.pid;
    }

    public String getValue(int index) {
        return data[index];
    }

    public String getValue(String fieldName){
        for(int i = 0; i<headings.length;i++){
            if(headings[i].equals(fieldName)){
                return data[i];
            }
        }
        return null;
    }

    public Boolean hasBeenOutput() {
        return beenOutputted;
    }

    public void setAsBeenOutput() {
        this.beenOutputted = true;
    }

    public String toString() {
        String out = "";
        for (String item : data) {
            out += item + " ";
        }
        return out;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
