package ft_project;

public class Tuple implements Cloneable {
    private String[] headings;
    private String[] data;
    private boolean beenOutputted = false;

    public Tuple(String[] headings, String[] data) {
        this.headings = headings;
        this.data = data;
    }

    public Boolean hasBeenOutput() {
        return beenOutputted;
    }

    public void setAsBeenOutput() {
        this.beenOutputted =true;
    }
    
    public String toString()
    {
        String out = "";
        for (String item : data) {
            out += item + " ";
        }
        return out;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
