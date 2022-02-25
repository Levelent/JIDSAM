package ft_project;

import java.util.Arrays;

public class Tuple implements Cloneable {
    private String pid;
    private String[] headings;
    private String[] data;
    private boolean beenOutputted = false;

    public Tuple(String[] headings, String[] data) {
        this.headings = headings;
        this.data = data;

        // set pid
        this.pid = this.data[Arrays.asList(headings).indexOf("pid")];
    }

    public String getPid() {
        return this.pid;
    }

    public String getValue(int index) {
        return data[index];
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
