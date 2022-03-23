package ft_project;

import java.util.*;
import static java.lang.Math.min;

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

            // Remove the k-anonymised clusters in Set_kc that exist longer than or equal to
            // T_kc;
            set_kc.removeIf(c -> (c.size() >= beta));

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

            Iterator<Tuple> iter = set_tp.iterator();
            while (iter.hasNext()) {
                outputWithKCorSuppress(iter.next());

                // remove taken from end of outputWithKCorSuppress to allow removal while
                // iterating
                iter.remove();
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
        // info loss increase after adding tuple to cluster
        Cluster C_kc = getClusterWhichCoversWithSmallestLoss(t, set_kc);

        if (C_kc != null) {
            // Publish t with C_kc's generalisation
            t.outputWith(outputStream, DGHs, C_kc);
        } else {
            // suppress t
            t.suppress(outputStream, DGHs);
        }
    }

    /**
     * Output a given tuple with generalisations from a k-anonymised cluster that
     * covers the tuple or using the tuple's nearest neighbours
     * 
     * @param t tuple to be outputted
     */
    public void outputWithKCorNC(Tuple t) {
        // nearest neighbours of t with unique pid in Set_tp
        PriorityQueue<Tuple> neighbours = new PriorityQueue<>(
                (t1, t2) -> Float.compare(enlargement(t1, t), enlargement(t2, t)));
        for (Tuple t_i : set_tp) {
            neighbours.add(t_i);
        }

        int numOfNearestNeighbours = neighbours.size();

        // create a new cluster C_nc on t and its k-1 nearest neighbours
        // min is used in case numOfNearestNeighbours < k-1
        Cluster c_nc = new Cluster(t, DGHs);
        for (int i = 1; i <= min(k - 1, numOfNearestNeighbours); i++) {
            c_nc.add(neighbours.remove());
        }

        // Find a k-anonymised cluster C_kc in Set_kc that covers t and incurs least
        // info loss increase after adding tuple to cluster
        Cluster c_kc = getClusterWhichCoversWithSmallestLoss(t, set_kc);

        // if the nearest neighbours are fewer than k-1 then
        if (numOfNearestNeighbours < k - 1) {
            if (c_kc != null) {
                // Publish t with C_kc's generalisation
                t.outputWith(outputStream, DGHs, c_kc);
            } else {
                t.suppress(outputStream, DGHs);
            }
        } else if (c_kc != null && c_kc.informationLoss() < c_nc.informationLoss()) {
            // publish t with C_kc's generalisation
            t.outputWith(outputStream, DGHs, c_kc);
        } else {
            // publish t with C_nc's generalisation
            t.outputWith(outputStream, DGHs, c_nc);
            c_nc.output(outputStream);
            set_kc.add(c_nc);

            // remove the k-1 nearest neighbours of t from Set_tp
            for (Tuple t_i : c_nc.getTuples()) {
                if (t_i == t) {
                    continue;
                }

                // TODO line means that these ones never get output...
                set_tp.remove(t_i);
            }
        }

        set_tp.remove(t);
    }

    /**
     * Get the cluster which covers tuple t with the smallest information loss
     * 
     * @param t          tuple to cover
     * @param collection of clusters which can be used to cover t
     * @return cluster which covers tuple t with smallest information loss
     */
    protected Cluster getClusterWhichCoversWithSmallestLoss(Tuple t, Collection<Cluster> collection) {
        Cluster minCluster = null;
        float minLoss = 0;

        for (Cluster c : collection) {
            float loss = enlargement(c, t);
            if (minCluster == null || loss < minLoss) {
                minCluster = c;
                minLoss = loss;
            }
        }

        return minCluster;
    }
}
