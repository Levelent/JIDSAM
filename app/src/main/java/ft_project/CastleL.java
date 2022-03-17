package ft_project;

import java.util.*;

public class CastleL extends Castle {
    protected int l, a_s;

    public CastleL(InStream s, int k, int delta, int beta, int l, int a_s) {
        super(s, k, delta, beta);

        // set l diversity algorithm parameters
        this.l = l;
        this.a_s = a_s; // index of the l diversity sensitive attribute
    }

    public void delayConstraint(Tuple t) {
        // Let C be the non-k_s anonymised cluster to which t belongs
        Cluster c = null;
        for (Cluster cluster : this.nonAnonymisedClusters) {
            if (cluster.contains(t)) {
                c = cluster;
                break;
            }
        }

        if (c.size() >= this.k && c.diversity(this.a_s) >= this.l) {
            outputCluster(c);
        } else {
            // KC_set = All k_s anonymised clusters in anonymisedClusters containing t;
            Set<Cluster> KC_set = new LinkedHashSet<Cluster>();
            for (Cluster cluster : this.anonymisedClusters) {
                if (cluster.contains(t)) {
                    KC_set.add(cluster);
                }
            }

            if (KC_set.size() > 0) {
                // let KC be a cluster randomly selected from KC_set;
                Cluster KC = getRandomCluster(KC_set);

                // Output t with the generalisation of KC;
                // KC_set is set of clusters that contain t so just output KC
                KC.output(outputStream);
                return;
            }

            int m = 0;
            for (Cluster C_j : nonAnonymisedClusters) {
                if (c.size() < C_j.size()) {
                    m++;
                }
            }

            if (2 * m > nonAnonymisedClusters.size()) {
                t.suppress(this.outputStream, this.DGHs);
                return;
            }

            // line 17 of delay_constraint
            if (nonAnonymisedClusters.stream().mapToInt(Cluster::size).sum() < this.k) {
                // as on page 9 top of right column
                // it must also be checked that there is at least l distinct values for a_s
                // among all non anonymised clusters

                Set<String> distinctValues = new HashSet<>();
                for (Cluster c_i : nonAnonymisedClusters) {
                    distinctValues.addAll(c_i.distinctValues(this.a_s));
                }

                if (distinctValues.size() >= this.l) {
                    t.suppress(this.outputStream, this.DGHs);
                    return;
                }
            }

            Cluster MC = this.merge_clusters(c, nonAnonymisedClusters);
            outputCluster(MC);
        }
    }

    public void outputCluster(Cluster c) {
        Set<Cluster> SC;
        if (c.size() >= 2 * this.k && c.diversity(this.a_s) >= this.l) {
            SC = splitL(c, this.a_s);
        } else {
            // SC = { c };
            SC = new LinkedHashSet<Cluster>();
            SC.add(c);
        }

        for (Cluster C_i : SC) {
            C_i.output(this.outputStream); // output all tuples in C_i with its generalisation;

            // Update aveInfoLoss according to informationLoss(C_i);
            // aveInfoLoss is updated to be the average information loss of
            // the most recent k-anonymized clusters including the new ones
            float aveSum = 0;
            HashSet<Cluster> cToAverage = new HashSet<Cluster>();
            cToAverage.addAll(anonymisedClusters);
            cToAverage.addAll(SC);
            for (Cluster cl : cToAverage) {
                aveSum += cl.informationLoss();
            }
            this.aveInfoLoss = aveSum / cToAverage.size();

            if (C_i.informationLoss() < this.aveInfoLoss) {
                this.anonymisedClusters.add(C_i);
            }
            // It seems line 11 of output_cluster in the paper is redundant... technically
            // it will be handled by garbage collection
            nonAnonymisedClusters.remove(C_i);
        }
    }

    public Set<Cluster> splitL(Cluster c, Integer a_s) {
        Map<String, List<Tuple>> BS = generate_buckets(c, a_s);

        // set of sub clusters initialized empty
        Set<Cluster> SC = new HashSet<>();

        if (BS.size() < this.l) {
            SC.add(c);
            return SC;
        }

        // size of bs > l and sum of bucket sizes > k
        int sum;
        while (BS.size() >= this.l && (sum = BS.values().stream().mapToInt(List::size).sum()) >= this.k) {
            // randomly select a B from BS;
            Random random = new Random();
            List<String> keys = new ArrayList<String>(BS.keySet());
            String randomKey = keys.get(random.nextInt(keys.size()));

            // randomly select a tuple t from B;
            List<Tuple> B = BS.get(randomKey);
            Tuple t = B.get(random.nextInt(B.size()));

            // generate a sub-cluster C_sub over t;
            Cluster C_sub = new Cluster(t, DGHs);

            // delete t from B;
            B.remove(t);

            // for (Bucket B_j : BS)
            Iterator<Map.Entry<String, List<Tuple>>> iterator = BS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<Tuple>> entry = iterator.next();
                List<Tuple> B_j = entry.getValue();

                // Sort tuples of B_j by ascending order of their enlargement e_i;
                Comparator<Tuple> sortByEnlargementComparator = (Tuple t1,
                        Tuple t2) -> Float.compare(enlargement(C_sub, t1), enlargement(C_sub, t2));
                Collections.sort(B_j, sortByEnlargementComparator);

                // Let T_j be the set of the first k * (B_j.size() / sum of bucket sizes) tuples
                // in B_j;
                Set<Tuple> T_j = new LinkedHashSet<>();

                for (int i = 0; i < this.k * ((double) B_j.size() / sum); i++) {
                    T_j.add(B_j.get(i));
                }

                // Insert T_j into C_sub;
                C_sub.add(T_j);

                // delete T_j from B_j;
                B_j.removeAll(T_j);

                if (B_j.size() == 0) {
                    // delete B_j from BS
                    // iterator last returned pid the key for B_j so removes B_j from BS
                    iterator.remove();
                }
            }
            SC.add(C_sub);
        }

        // for (Bucket B : BS)
        Iterator<Map.Entry<String, List<Tuple>>> iterator = BS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Tuple>> entry = iterator.next();
            List<Tuple> B = entry.getValue();

            for (Tuple t_i : B) {
                // C_near = nearest subCluster of t_i in SC;
                Cluster C_near = null;
                float smallestEnlargement = 0;
                for (Cluster possibleCluster : SC) {
                    float distanceFromT_iToPossible = enlargement(possibleCluster, t_i);
                    if (C_near == null || distanceFromT_iToPossible < smallestEnlargement) {
                        C_near = possibleCluster;
                        smallestEnlargement = distanceFromT_iToPossible;
                    }
                }

                // insert t_i into c_near;
                C_near.add(t_i);
            }

            // delete B;
            iterator.remove();
        }

        // line 25
        for (Cluster sc_i : SC) {
            for (int i = 0; i < sc_i.getTuples().size(); i++) {
                Tuple t_bar = sc_i.getTuples().get(i);
                // let G_t be the set of tuples in C such that G_t = {t2 in C | t.pid = t2.pid}
                Set<Tuple> G_t = new LinkedHashSet<>();
                for (Tuple t : c.getTuples()) {
                    if (t.getPid() == t_bar.getPid() && t != t_bar) {
                        G_t.add(t);
                    }
                }

                // insert G_t into SC_i;
                sc_i.add(G_t);

                // delete G_t from C;
                c.removeSet(G_t);
            }
        }

        return SC;
    }
}