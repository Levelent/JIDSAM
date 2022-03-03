package ft_project;

import java.util.*;

public class Cluster implements Cloneable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private List<Tuple> tuples;
    private Map<String, Generalisation> generalisations;

    public Cluster(Tuple t, Map<String, DGH> d) {
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

    public void merge(Cluster c) {
        // add all tuples in c to t
        for (Tuple t : c.getTuples()) {
            this.add(t);
        }
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

    public void add(Collection<Tuple> s) {
        for (Tuple t : s) {
            this.add(t);
        }
    }

    public float informationLoss() {
        float average = 0;
        for (Generalisation g : generalisations.values()) {
            average += g.infoLoss();
        }
        return average / generalisations.size();
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
        String out = System.lineSeparator() + ANSI_YELLOW + "Cluster" + ANSI_RESET + System.lineSeparator();

        // output generalisations
        out += ANSI_CYAN + "Generalisations" + ANSI_RESET + System.lineSeparator();
        for (String h : tuples.get(0).headings) {
            // TODO: Add the heading the generalisation belongs to
            Generalisation g = generalisations.get(h);
            out += (g == null) ? "" : g.toString() + " ";
        }
        out += System.lineSeparator();

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

        c.generalisations = new HashMap<String, Generalisation>();
        for (String key : generalisations.keySet()) {
            Generalisation cloned = (Generalisation) generalisations.get(key).clone();
            c.generalisations.put(key, cloned);
        }

        return c;
    }
}
