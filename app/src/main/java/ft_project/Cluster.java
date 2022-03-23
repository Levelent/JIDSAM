package ft_project;

import java.util.*;

public class Cluster implements Cloneable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private List<Tuple> tuples;
    private Map<String, Generalisation> generalisations;
    private Map<String, DGH> DGH;

    /**
     * Constructor to create a cluster with given tuple and DGHs
     * 
     * @param t tuple to start the cluster with
     * @param d associated DGH trees
     */
    public Cluster(Tuple t, Map<String, DGH> d) {
        DGH = d;
        tuples = new LinkedList<Tuple>();
        // Create our list of generalisations. Everything that doesn't exist as a DGH is
        // continuous, except pid and tid
        generalisations = new HashMap<String, Generalisation>();
        for (String heading : t.headings) {

            if (heading.equals("pid") || heading.equals("tid")) {
                continue;
            }
            DGH dgh = d.get(heading);

            if (dgh == null) {
                // Must be a continuous generalisation
                generalisations.put(heading, new ContinuousGeneralisation(Float.parseFloat(t.getValue(heading))));
            } else {
                generalisations.put(heading, new CategoryGeneralisation(dgh, t.getValue(heading)));
            }
        }

        this.add(t);
    }

    /**
     * Merger a given cluster into this cluster by adding all tuples
     * 
     * @param c cluster to merge
     */
    public void merge(Cluster c) {
        // add all tuples in c to t
        for (Tuple t : c.getTuples()) {
            this.add(t);
        }
    }

    /**
     * Get a set of distinctValues for a given index
     * 
     * @param a_s index to get the distinct values for
     * @return set of distinct values
     */
    public Set<String> distinctValues(int a_s) {
        // get all distinct values in cluster for a_s index
        Set<String> distinctValues = new HashSet<>();
        for (Tuple t : tuples) {
            distinctValues.add(t.getValue(a_s));
        }
        return distinctValues;
    }

    /**
     * Get the number of distinct values for a given index
     * 
     * @param a_s index to get the number of distinct values for
     * @return number of distinct values
     */
    public int diversity(int a_s) {
        // C.diversity the number of distinct values of a_s for tuples in C
        return this.distinctValues(a_s).size();
    }

    /**
     * Check if the cluster contains a given tuple
     * 
     * @param t the tuple to see if the cluster contains
     * @return if the cluster contains the given tuple
     */
    public boolean contains(Tuple t) {
        return tuples.contains(t);
    }

    /**
     * Add a given tuple to the cluster
     * 
     * @param t tuple to add
     */
    public void add(Tuple t) {
        tuples.add(t);
        for (String h : t.headings) {
            Generalisation generalisation = generalisations.get(h);
            if (generalisation == null) {
                continue;
            }

            // Test if the value is a number
            try {
                float num = Float.parseFloat(t.getValue(h));
                generalisation.updateGeneralisation(num);
            } catch (NumberFormatException e) {
                generalisation.updateGeneralisation(t.getValue(h));
            }
        }
    }

    /**
     * Add a collection of tuples to the cluster
     * 
     * @param s collection of tuples to add
     */
    public void add(Collection<Tuple> s) {
        for (Tuple t : s) {
            this.add(t);
        }
    }

    /**
     * Calculate the information loss due to the cluster generalisations
     * 
     * @return the information loss
     */
    public float informationLoss() {
        float average = 0;
        for (Generalisation g : generalisations.values()) {
            average += g.infoLoss();
        }
        return average / generalisations.size();
    }

    /**
     * Remove a set of tuple from the cluster
     * 
     * @param s the set of clusters to remove from the cluster
     */
    public void removeSet(Set<Tuple> s) {
        for (Tuple t : s) {
            tuples.remove(t);
        }
    }

    /**
     * Get the size of the cluster where the size is the number of distinct values
     * of pid
     * 
     * @return size of cluster
     */
    public int size() {
        Set<String> pids = new LinkedHashSet<>();
        for (Tuple t : tuples) {
            pids.add(t.getPid());
        }
        return pids.size();
    }

    /**
     * Getter for list of tuples contained in cluster
     * 
     * @return list of tuples
     */
    public List<Tuple> getTuples() {
        return tuples;
    }

    /**
     * Getter for generalisations
     * 
     * @return generalisation associated with cluster
     */
    public Map<String, Generalisation> getGeneralisations() {
        return generalisations;
    }

    /**
     * Get the DGH associated with cluster
     * 
     * @return the set DGH
     */
    public Map<String, DGH> getDGH() {
        return DGH;
    }

    /**
     * Output the cluster to the console and given output stream
     * 
     * @param outputStream the stream to output to
     */
    public void output(OutStream outputStream) {
        if (!App.silent) {
            // "internal" output
            String out = System.lineSeparator() + ANSI_YELLOW + "Cluster" + ANSI_RESET + System.lineSeparator();

            // output generalisations
            out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();

            String generalisations = "";
            for (String h : tuples.get(0).headings) {
                Generalisation g = this.generalisations.get(h);
                generalisations += (g == null) ? "" : g.toString() + " ";
            }
            out += generalisations + System.lineSeparator();

            // output all tuples;
            out += ANSI_CYAN + "Tuples" + ANSI_RESET + System.lineSeparator();
            for (Tuple t : tuples) {
                out += t.toString() + System.lineSeparator();
            }

            System.out.print(out);

            // external output
            for (Tuple t : tuples) {
                if (!t.hasBeenOutput()) {
                    outputStream.out.println(generalisations);
                    t.setAsBeenOutput();
                }
            }
        } else {
            for (Tuple t : tuples) {
                if (!t.hasBeenOutput()) {
                    t.setAsBeenOutput();
                }
            }
        }
    }

    /**
     * Deep clone the cluster
     * 
     * @return cloned cluster
     */
    public Object clone() throws CloneNotSupportedException {
        Cluster c = (Cluster) super.clone();

        // deep copy tuples
        c.tuples = new LinkedList<Tuple>();
        for (Tuple t : this.tuples) {
            c.tuples.add((Tuple) t.clone());
        }

        c.generalisations = new HashMap<String, Generalisation>();
        for (String key : generalisations.keySet()) {
            Generalisation cloned = (Generalisation) generalisations.get(key).clone();
            c.generalisations.put(key, cloned);
        }

        return c;
    }
}
