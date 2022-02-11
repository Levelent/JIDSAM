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
}
