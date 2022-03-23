package ft_project;

import java.util.*;

public class XBAND extends Castle {
    protected Set<Pair<Tuple, Integer>> set_t;
    protected Set<Cluster> set_k;
    protected Set<Pair<Tuple, Integer>> pocket_t;

    PriorityQueue<Cluster> c_gen;

    private int omega;
    private int expirationBand;

    public XBAND(InStream s, int k, int delta, int omega, int t_kc, int expirationBand) {
        // set default algorithm parameters
        // expirationBand may be reffered to as Gamma
        super(s, k, delta, t_kc);
        this.omega = omega;
        this.expirationBand = expirationBand;
    }

    public void run() {
        set_t = new LinkedHashSet<>();
        set_k = new LinkedHashSet<>();
        pocket_t = new LinkedHashSet<>();
        c_gen = new PriorityQueue<Cluster>();
        Tuple t_n;
        int time = 0;
        while ((t_n = s.next()) != null) {
            Pair<Tuple, Integer> timeStamped = new Pair<Tuple, Integer>(t_n, time);
            set_t.add(timeStamped);
            if (set_k.size() >= omega) {
                set_k = new LinkedHashSet<>(); // remove re-usable clusters that exist >= omega
            }

            if (set_t.size() >= delta) {
                triggerPublish();
            }
            time++;
        }
        while (set_t.size() > 0) {
            // Stream has ended but we have tuples to output
            triggerPublish();
        }
        while (pocket_t.size() > 0) {
            // We need to empty the pocket too
            for (Pair<Tuple, Integer> p : pocket_t) {
                suppressAnonymization(p.x);
            }
        }

    }

    private void triggerPublish() {

        if (set_t.size() > k + expirationBand) {
            // For every tuple in the expiration band (the oldest Gamma tuples)
            generateExpirationBandClusters();

        } else if (set_t.size() >= k && set_t.size() < k + expirationBand) {
            generateExpirationBandClusters();
            Cluster c_best = c_gen.poll(); // priority queues should be min first be default
            c_best.output(outputStream);
            set_k.add(c_best);
            for (Tuple t_best : c_best.getTuples()) {
                // Tuples are now in Tuple,Timestamp pairs so we need to find the tuple in set_t
                // first as we dont know what its timestamp is
                Optional<Pair<Tuple, Integer>> found = set_t.stream().filter(p -> p.x == t_best).findFirst();
                Pair<Tuple, Integer> toRemove = found.isPresent() ? found.get() : null;
                set_t.remove(toRemove);
            }

            // Find all tuples in the expiration band *before* those we just published and
            // add them to the pocket
            int earliestTime = set_t.stream().reduce(new Pair<Tuple, Integer>(null, 999999),
                    (a, b) -> a.y < b.y ? a : b).y; // Set the identity to the newest time possible
            Pair<Tuple, Integer>[] set_t_array = (Pair<Tuple, Integer>[]) set_t.toArray();

            for (int i = 0; i < expirationBand; i++) {
                if (set_t_array[i].y < earliestTime) {
                    pocket_t.add(set_t_array[i]);
                    set_t.remove(set_t_array[i]);
                }
            }
            c_gen = new PriorityQueue<Cluster>(); // TODO: Determine if this is actually what is calld for
        } else {
            // Suppress the tuple to be expired
            Optional<Pair<Tuple, Integer>> found = set_t.stream().findFirst();
            Pair<Tuple, Integer> toSuppress = found.isPresent() ? found.get() : null;
            suppressAnonymization(toSuppress.x);
            c_gen = new PriorityQueue<Cluster>(); // TODO: Determine if this is actually what is calld for

        }
    }

    private void generateExpirationBandClusters() {
        Pair<Tuple, Integer>[] set_t_array = (Pair<Tuple, Integer>[]) set_t.toArray();
        for (int i = 0; i < expirationBand; i++) {
            Pair<Tuple, Integer> t = set_t_array[i];
            Cluster potCluster = new Cluster(t.x, DGHs);

            // create a cluster with each tuple's k-nearest neighbours
            // nearest neighbours of t with unique pid in Set_tp

            // By default should be a min priority queue
            PriorityQueue<Pair<Tuple, Integer>> neighbours = new PriorityQueue<>(
                    (t1, t2) -> Float.compare(enlargement(t1.x, t.x), enlargement(t2.x, t.x)));

            for (Pair<Tuple, Integer> t_i : set_t) {
                if (t_i != t) {
                    neighbours.add(t_i);
                }
            }
            for (int j = 0; j < k; j++) {
                potCluster.add(neighbours.remove().x);
            }
            c_gen.add(potCluster);

        }
    }

    private void suppressAnonymization(Tuple t) {
        PriorityQueue<Cluster> potentialCluseters = new PriorityQueue<>(
                (c1, c2) -> Float.compare(enlargement(c1, t), enlargement(c2, t)));
        for (Cluster c : set_k) {
            potentialCluseters.add(c);
        }
        if (set_k.size() == 0) {
            t.suppress(outputStream, DGHs);
        } else {
            Cluster best = potentialCluseters.poll();
            t.outputWith(outputStream, DGHs, best);
        }

    }

    public static class Pair<X, Y> {
        public final X x;
        public final Y y;

        public Pair(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
