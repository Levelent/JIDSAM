package ft_project;

public class Tuple {
    private String[] headings;
    private String[] data;

    public Tuple(String[] headings, String[] data) {
        this.headings = headings;
        this.data = data;
    }
    
    public String toString()
    {
        String out = "";
        for (String item : data) {
            out += item + " ";
        }
        return out;
    }

    public Tuple clone() throws CloneNotSupportedException
    {
        return (Tuple)super.clone();
    }
}
