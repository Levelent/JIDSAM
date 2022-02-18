package ft_project;

import java.util.*;

public class Cluster {
    private List<Tuple> tuples;

    public Cluster(Tuple t) {
        tuples = new LinkedList<Tuple>();
    }

    public void add(Tuple t) {
        tuples.add(t);
    }

    public int size() {
        return tuples.size();
    }

    public List<Tuple> getTuples() {
        return tuples;
    }

    public String toString()
    {
        String out = "Cluster" + System.lineSeparator();
        
        // output all tuples;
        for (Tuple t : tuples) {
            out += t.toString() + System.lineSeparator();
        }

        // TODO:// with generalisations
        return out;
    }

    public Cluster clone() throws CloneNotSupportedException
    {
        Cluster c = (Cluster)super.clone();

        // deep copy tuples
        c.tuples = new LinkedList<Tuple>();
        for (Tuple t : this.tuples){
            c.tuples.add(t.clone());
        }

        return c;
    }
}
