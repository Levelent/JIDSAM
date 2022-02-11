package ft_project;

import java.util.*;

public class Cluster {
    List<Tuple> tuples;

    public Cluster(Tuple t) {
        tuples = new LinkedList<Tuple>();
    }

    public void add(Tuple t) {
        tuples.add(t);
    }

    public int size() {
        return tuples.size();
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
}
