package ft_project;

import java.util.*;

public class FADS extends Castle {
    protected float aveInfoLoss;
    protected Set<Tuple> set_tp;
    protected Set<Cluster> set_kc;

    /**
     * Initialise the FADS algorithm
     * 
     * @param s     data stream
     * @param k     threshold
     * @param delta threshold
     * @param t_kc  reuse constraint (will be referred to as beta!)
     */
    public FADS(InStream s, int k, int delta, int t_kc) {
        // set default algorithm parameters
        super(s, k, delta, t_kc);
    }

    /**
     * Run the FADS algorithm
     */
    public void run() {
        // Let Set_tp be the set of tuples waiting for release
        set_tp = new LinkedHashSet<>();

        // Let Set_kc be the set of published k-anonymised clusters who exist no longer
        // than t_kc
        set_kc = new LinkedHashSet<>();

        Tuple t_n;
        while ((t_n = s.next()) != null) {
            // Read a tuple t_n from S and insert into Set_tp
            set_tp.add(t_n);

            // Update the ranges of the numeric QIDs with respect to t_n;
            // Remove the k-anonymised clusters in Set_kc that exist longer than or equal to
            // T_kc;
            // TODO

            if (set_tp.size() > delta) {
                // Remove the earliest arrived tuple t from Set_tp;
                Tuple t = set_tp.iterator().next();
                publishTuple(t);
            }
        }

        while (set_tp.size() > 0) {
            // Remove the earliest arrived tuple t from Set_tp;
            Tuple t = set_tp.iterator().next();
            publishTuple(t);
        }
    }

    /**
     * Publish a given tuple
     * 
     * @param t tuple to be published
     */
    public void publishTuple(Tuple t) {
        if (set_tp.size() < k - 1) {
            set_tp.add(t);
            for (Tuple t_i : set_tp) {
                outputWithKCorSuppress(t_i);
            }
        } else {
            outputWithKCorNC(t);
        }
    }

    /**
     * Output a given tuple with k-anonymised generalisation or suppressed
     * 
     * @param t tuple to output
     */
    public void outputWithKCorSuppress(Tuple t) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and incurs least
        // info loss increase after adding tuple to cluster TODO
        Cluster C_kc = null;

        if (C_kc != null) {
            // Publish t with C_kc's generalisation
        } else {
            // suppress and publish t
        }

        set_tp.remove(t);
    }

    /**
     * Output a given tuple with generalisations from a k-anonymised cluster that
     * covers the tuple or using the tuple's nearest neighbours
     * 
     * @param t tuple to be outputted
     */
    public void outputWithKCorNC(Tuple t) {
        // Find the k-1 nearest neighbours of t with unique pid in Set_tp and create a
        // new cluster C_nc on t and its neighbours
        Cluster c_nc = null;

        // Find a k-anonymised cluster C_kc in Set_kc that covers t and incurs least
        // info loss increase after adding tuple to cluster
        Cluster c_kc = null;

        // if the nearest neighbours are fewer than k-1 then
        if (0 < k - 1) {
            if (c_kc != null) {
                // Publish t with C_kc's generalisation
            } else {
                // suppress and publish t
            }
        } else if (c_kc != null && c_kc.informationLoss() < c_nc.informationLoss()) {
            // publish t with C_kc's generalisation
        } else {
            // publish t with C_nc's generalisation
            set_kc.add(c_nc);
            // remove the k-1 nearest neighbours of t from Set_tp
        }

        set_tp.remove(t);
    }
}
