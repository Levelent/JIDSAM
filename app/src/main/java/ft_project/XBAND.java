package ft_project;

import java.lang.reflect.Array;
import java.util.*;

//TODO add proper comments
public class XBAND extends Castle {
    protected Set<Pair<Tuple, Integer>> set_t;
    protected Set<Cluster> set_k;
    protected Set<Pair<Tuple, Integer>> pocket_t;

    PriorityQueue<Cluster> c_gen;

    private int omega;
    private int expirationBand;

    public XBAND(InStream s, int k, int delta, int beta, int omega, int expirationBand) {
        // set default algorithm parameters
        // expirationBand may be reffered to as Gamma
        super(s, k, delta, beta);
        this.omega = omega;
        this.expirationBand = expirationBand;
    }

    public void run() {
        System.out.println(Constants.variant);
        set_t = new LinkedHashSet<>();
        set_k = new LinkedHashSet<>();
        pocket_t = new LinkedHashSet<>();
        c_gen = new PriorityQueue<Cluster>((c1, c2) -> Float.compare(c1.informationLoss(), c2.informationLoss()));
        Tuple t_n;
        int time = 0;
        while ((t_n = s.next()) != null) {
            Constants.outputProgress();
            Pair<Tuple, Integer> timeStamped = new Pair<Tuple, Integer>(t_n, time);
            set_t.add(timeStamped);
            if (set_k.size() >= omega) {
                set_k = new LinkedHashSet<>(); // remove re-usable clusters that exist >= omega
            }

            Optional<Pair<Tuple, Integer>> found = set_t.stream().findFirst();
            Pair<Tuple, Integer> first = found.isPresent() ? found.get() : null;

            if (first.y <= time - delta) {
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
                pocket_t.remove(p);
            }
        }

    }

    private void triggerPublish() {
        c_gen = new PriorityQueue<Cluster>((c1, c2) -> Float.compare(c1.informationLoss(), c2.informationLoss()));
        if (set_t.size() > k + expirationBand) {
            // For every tuple in the expiration band (the oldest Gamma tuples)
            generateExpirationBandClusters();
            // The following is as per algorithm description rather than the psuedocode
            // displayed in the paper
            Cluster c_best = c_gen.poll(); // priority queues should be min first by default
            c_best.output(outputStream);
            set_k.add(c_best);
            for (Tuple t_best : c_best.getTuples()) {
                // Tuples are now in Tuple,Timestamp pairs so we need to find the tuple in set_t
                // first as we dont know what its timestamp is
                Optional<Pair<Tuple, Integer>> found = set_t.stream().filter(p -> p.x == t_best).findFirst();
                Pair<Tuple, Integer> toRemove = found.isPresent() ? found.get() : null;
                set_t.remove(toRemove);
            }

        } else if (set_t.size() >= k && set_t.size() <= k + expirationBand) {
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
            int earliestTime = set_t.stream().reduce(new Pair<Tuple, Integer>(null, 99999),
                    (a, b) -> a.y < b.y ? a : b).y; // Set the identity to the newest time possible

            Iterator<Pair<Tuple, Integer>> iterator = set_t.iterator();
            int i = 0;
            Pair<Tuple, Integer> element = null;
            while (iterator.hasNext()) {
                element = iterator.next();
                if (i > expirationBand) {
                    break;
                }
                if (element.y < earliestTime) {
                    pocket_t.add(element);
                    set_t.remove(element);
                }
                i++;
            }

        } else {
            // Suppress the tuple to be expired
            Optional<Pair<Tuple, Integer>> found = set_t.stream().findFirst();
            Pair<Tuple, Integer> toSuppress = found.isPresent() ? found.get() : null;

            suppressAnonymization(toSuppress.x);
            set_t.remove(toSuppress);

        }
    }

    private void generateExpirationBandClusters() {

        Iterator<Pair<Tuple, Integer>> iterator = set_t.iterator();
        int i = 0;
        Pair<Tuple, Integer> element = null;
        while (iterator.hasNext()) {
            element = iterator.next();
            if (i > expirationBand) {
                break;
            }
            Tuple t = element.x;
            Cluster potCluster = new Cluster(t, DGHs);

            // create a cluster with each tuple's k-nearest neighbours
            // nearest neighbours of t with unique pid in Set_tp

            // By default should be a min priority queue
            PriorityQueue<Pair<Tuple, Integer>> neighbours = new PriorityQueue<>(
                    (p1, p2) -> Float.compare(enlargement(p1.x, t), enlargement(p2.x, t)));

            for (Pair<Tuple, Integer> p_i : set_t) {
                if (p_i != element) {
                    neighbours.add(p_i);
                }
            }
            for (int j = 0; j < k - 1; j++) {
                potCluster.add(neighbours.remove().x);
            }
            c_gen.add(potCluster);
            i++;
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
