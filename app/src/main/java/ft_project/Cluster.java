package ft_project;

import java.util.*;

public class Cluster implements Cloneable {
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

    public void merge(Cluster c) {
        /** todo://
         * function receives as input the
        cluster to be merged, i.e., C, and the set of non-ksanonymized clusters excluding C itself. The procedure,
for every non-ks-anonymized cluster Ci, calculates the
enlargement of C due to the possible merge with Ci. Then,
it selects the cluster, which brings the minimum enlargement to C, and merges C with it. This process continues
until C’s size is at least k. Then, the resulting cluster is given
in output
         */

        tuples.addAll(c.getTuples());
    }

    public String toString()
    {
        String out = "Cluster" + System.lineSeparator();
        
        // output all tuples;
        for (Tuple t : tuples) {
            t.setAsBeenOutput();
            out += t.toString() + System.lineSeparator();
        }

        // TODO:// with generalisations
        return out;
    }

    public Object clone() throws CloneNotSupportedException
    {
        Cluster c = (Cluster)super.clone();

        // deep copy tuples
        c.tuples = new LinkedList<Tuple>();
        for (Tuple t : this.tuples){
            c.tuples.add((Tuple)t.clone());
        }

        return c;
    }
}
