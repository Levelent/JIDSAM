package ft_project;

import java.util.*;

public class Cluster implements Cloneable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private List<Tuple> tuples;

    public Cluster(Tuple t) {
        tuples = new LinkedList<Tuple>();
        this.add(t);
    }

    public void merge(Cluster c) {
        // TODO update generalisations on insert?

        tuples.addAll(c.getTuples());
    }

    public Set<String> distinctValues(int a_s) {
        // get all distinct values in cluster for a_s index
        Set<String> distinctValues = new HashSet<>();
        for (Tuple t : tuples) {
            distinctValues.add(t.getValue(a_s));
        }
        return distinctValues;
    }

    public int diversity(int a_s) {
        // C.diversity the number of distinct values of a_s for tuples in C
        return this.distinctValues(a_s).size();
    }

    public boolean contains(Tuple t) {
        return tuples.contains(t);
    }

    public void add(Tuple t) {
        tuples.add(t);
    }

    public void add(Collection<Tuple> s) {
        for (Tuple t : s) {
            this.add(t);
        }
    }

    public void removeSet(Set<Tuple> s) {
        for (Tuple t : s) {
            tuples.remove(t);
        }
    }

    public int size() {
        return tuples.size();
    }

    public List<Tuple> getTuples() {
        return tuples;
    }

    public String toString() {
        String out = ANSI_YELLOW + "Cluster" + ANSI_RESET + System.lineSeparator();

        // output generalisations
        out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();

        // TODO with generalisations

        // output all tuples;
        out += ANSI_CYAN + "Tuples" + ANSI_RESET + System.lineSeparator();
        for (Tuple t : tuples) {
            t.setAsBeenOutput();
            out += t.toString() + System.lineSeparator();
        }

        return out;
    }

    public Object clone() throws CloneNotSupportedException {
        Cluster c = (Cluster) super.clone();

        // deep copy tuples
        c.tuples = new LinkedList<Tuple>();
        for (Tuple t : this.tuples) {
            c.tuples.add((Tuple) t.clone());
        }

        return c;
    }
}
