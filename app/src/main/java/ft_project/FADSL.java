package ft_project;

import java.util.*;

public class FADSL extends FADS {
    protected float aveInfoLoss;
    protected int l, a_s;

    /**
     * Initialise the FADS algorithm
     * 
     * @param s     data stream
     * @param k     threshold
     * @param delta threshold
     * @param t_kc  reuse constraint (will be referred to as beta!)
     * @param l     diversity threshold
     * @param a_s   sensitive attribute index
     */
    public FADSL(InStream s, int k, int delta, int t_kc, int l, int a_s) {
        // set default algorithm parameters
        super(s, k, delta, t_kc);

        // set l diversity based parameters
        this.l = l;
        this.a_s = a_s;
    }

    /**
     * Publish a given tuple
     * 
     * @param t tuple to publish
     */
    public void publishTuple(Tuple t) {
        // Build a queue Q_tp from Set_tp by sorting tuples with unique pid in ascending
        // order of distance to t
        PriorityQueue<Tuple> Q_tp = new PriorityQueue<>(
                (t1, t2) -> Float.compare(enlargement(t1, t), enlargement(t2, t)));
        for (Tuple t_i : set_tp) {
            Q_tp.add(t_i);
        }

        // Let H_tp be a hash table that stores tuples by their sensitive attribute
        // values
        Hashtable<String, Set<Tuple>> H_tp = new Hashtable<>();

        // insert t into H_tp
        Set<Tuple> insertInto;
        if ((insertInto = H_tp.get(t.getValue(a_s))) == null) {
            insertInto = new LinkedHashSet<>();
            H_tp.put(t.getValue(a_s), insertInto);
        }
        insertInto.add(t);

        while (Q_tp.size() > 0) {
            Tuple t_prime = Q_tp.remove();
            if (H_tp.get(t_prime.getValue(a_s)) == null // if not defined then size 0 so will be less than
                    || H_tp.get(t_prime.getValue(a_s)).size() < Math.floor(Math.max(k, H_tp.values().size()) / l)) {
                // insert t_prime into H_tp
                if ((insertInto = H_tp.get(t_prime.getValue(a_s))) == null) {
                    insertInto = new LinkedHashSet<>();
                    H_tp.put(t_prime.getValue(a_s), insertInto);
                }
                insertInto.add(t_prime);
            }

            if (H_tp.keySet().size() >= l && H_tp.values().size() >= k) {
                break;
            }
        }

        if (H_tp.keySet().size() < l || H_tp.values().size() < k) {
            outputWithKCorSuppress(t);
        } else {
            // TODO not sure correct
            Cluster c_nc = new Cluster(t, DGHs);
            for (Set<Tuple> bucket : H_tp.values()) {
                c_nc.add(bucket);
            }

            outputWithKCorNC(t, c_nc);
        }
    }

    /**
     * Output a given tuple with existing k-anonymised generalisation that cover
     * tuple or suppress
     * 
     * @param t tuple to be output
     */
    public void outputWithKCorSuppress(Tuple t) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and has the smallest
        // generalisation information loss
        Cluster c_kc = getClusterWhichCoversWithSmallestLoss(t, set_kc);

        if (c_kc != null) {
            Set<Tuple> Set_t = new LinkedHashSet<>();

            // Find the tuples in C_kc whose sensitive attribute value is equal to t's and
            // insert them into Set_t;
            for (Tuple t_i : c_kc.getTuples()) {
                if (t.getValue(a_s) == t_i.getValue(a_s)) {
                    Set_t.add(t_i);
                }
            }

            if (Set_t.size() > Math.floor(c_kc.size() / l)) {
                t.suppress(outputStream, DGHs);
            } else {
                t.outputWith(outputStream, DGHs, c_kc);
            }
        } else {
            // suppress and publish t
            t.suppress(outputStream, DGHs);
        }
        set_tp.remove(t);
    }

    /**
     * Output a given tuple with either pre-existing generalisations that cover the
     * tuple or using generalisations from closest neighbours
     * 
     * @param t    tuple to be output
     * @param c_nc cluster with nearest neighbours to t
     */
    public void outputWithKCorNC(Tuple t, Cluster c_nc) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and has the smallest
        // generalisation information loss
        Cluster c_kc = getClusterWhichCoversWithSmallestLoss(t, set_kc);

        // if C_kc exists and its generalisation info loss is smaller than C_nc's then
        // initialise Set_t
        if (c_kc != null && c_kc.informationLoss() < c_nc.informationLoss()) {
            Set<Tuple> set_t = new LinkedHashSet<>();

            // Find the tuples in C_kc whose sensitive attribute value is equal to t's and
            // insert them into Set_t;
            for (Tuple t_i : c_kc.getTuples()) {
                if (t.getValue(a_s) == t_i.getValue(a_s)) {
                    set_t.add(t_i);
                }
            }

            if (set_t.size() > Math.floor(c_kc.size() / l)) {
                t.outputWith(outputStream, DGHs, c_nc);
                set_kc.add(c_nc);
            } else {
                t.outputWith(outputStream, DGHs, c_kc);
            }
        } else {
            t.outputWith(outputStream, DGHs, c_nc);
            set_kc.add(c_nc);
        }

        set_tp.remove(t);
    }
}
