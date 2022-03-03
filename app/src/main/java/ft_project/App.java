package ft_project;

import java.util.*;

public class App {
    private Boolean lDiversityEnabled = false;

    private int k, delta, beta, l, a_s;
    private float aveInfoLoss, thresholdInfoLoss;

    private Set<Cluster> nonAnonymisedClusters, anonymisedClusters;
    private Map<String, DGH> DGHs;

    public static void main(String[] args) {
        // predefine thresholds/constants
        int k = 5;
        int delta = 10;
        int beta = 2;

        // l diversity thresholds/constants
        int l = 2;
        int a_s = 2;

        // initialize app
        App app = new App();

        // create data stream
        Stream dataStream = new Stream("../../resources/adult-100.csv");
        app.setDGHs(new DGHReader("../../resources/dgh").DGHs);

        // Stream dataStream = new Stream("./app/src/main/resources/adult-100.csv");
        // app.setDGHs(new DGHReader("./app/src/main/resources/dgh").DGHs);

        // run CASTLE
        app.castle(dataStream, k, delta, beta);

        // run CASTLE with l diversity
        // app.castle(dataStream, k, delta, beta, l, a_s);

        // close file
        dataStream.close();
    }

    public void setDGHs(Map<String, DGH> dghs) {
        DGHs = dghs;
    }

    public void castle(Stream s, int k, int delta, int beta) {
        // set default algorithm parameters
        this.k = k;
        this.delta = delta;
        this.beta = beta;

        // initializations for algorithm
        this.nonAnonymisedClusters = new LinkedHashSet<Cluster>(); // set of non-k_s anonymised clusters
        this.anonymisedClusters = new LinkedHashSet<Cluster>(); // set of k_s anonymised clusters
        this.thresholdInfoLoss = 0; // Let be initialised to 0, usually is the average information loss

        // define array to track tuple positions (most recent -> less recent [head])
        Queue<Tuple> tupleHistory = new LinkedList<>();

        Tuple t;
        while ((t = s.next()) != null) {
            Cluster c = bestSelection(t);
            if (c == null) {
                // create new cluster on t and insert it into nonAnonymisedClusters
                nonAnonymisedClusters.add(new Cluster(t, DGHs));
            } else {
                c.add(t); // add tuple t to cluster c
            }

            // add tuple to history
            tupleHistory.add(t);

            // let t2 be the tuple with position equal to t.p - delta
            if (tupleHistory.size() > this.delta) { // keeps list range to t.p to t.(p - delta)
                Tuple t2 = tupleHistory.remove(); // remove oldest tuple
                if (!t2.hasBeenOutput()) {
                    delayConstraint(t2);
                }
            }
        }
    }

    public void castle(Stream s, int k, int delta, int beta, int l, int a_s) {
        // set l diversity based parameters
        this.l = l;
        this.a_s = a_s; // index of the l diversity sensitive attribute
        this.lDiversityEnabled = true;

        // call castle
        this.castle(s, k, delta, beta);
    }

    public Cluster bestSelection(Tuple t) {
        // used to keep track of clusters for a given enlargement value
        Map<Float, Set<Cluster>> enlargementMap = new HashMap<>();

        for (Cluster C_j : this.nonAnonymisedClusters) {
            // get the cluster enlargement value if given t
            float e = enlargement(C_j, t);

            // if enlargement value is is not already in enlargement map then initialize and
            // empty cluster list
            Set<Cluster> clusterList = enlargementMap.get(e);
            if (clusterList == null) {
                clusterList = new LinkedHashSet<Cluster>();
                enlargementMap.put(e, clusterList);
            }

            // add cluster to list inside of map
            clusterList.add(C_j);
        }

        // if no possible enlargements
        if (enlargementMap.size() == 0) {
            return null;
        }

        // Let min be the minimum element in E;
        float minEnlargement = Collections.min(enlargementMap.keySet());

        // Let SetC_min be the set of clusters C~ in nonAnonymisedClusters with
        // Enlargement(C~, t) = min;
        Set<Cluster> SetC_min = enlargementMap.get(minEnlargement);

        // initialize SetC_ok
        Set<Cluster> SetC_ok = new LinkedHashSet<Cluster>();

        for (Cluster C_j : SetC_min) {
            // Create a copy of C_j that we push t into
            try {
                Cluster copyOfC_j = (Cluster) C_j.clone();
                copyOfC_j.add(t);

                // Calculate information loss, if less than aveInfoLoss threshold insert into
                // SetC_ok
                if (copyOfC_j.informationLoss() < this.aveInfoLoss) {
                    SetC_ok.add(C_j);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (SetC_ok.size() == 0) {
            if (this.nonAnonymisedClusters.size() >= this.beta) { // |nonAnonymisedClusters| >= beta
                // return 'any cluster in SetC_min with minimum size';
                return SetC_min.stream().min(Comparator.comparing(Cluster::size))
                        .orElseThrow(NoSuchElementException::new);
            } else {
                return null;
            }
        }

        // return 'any cluster in SetC_ok with minimum size';
        return SetC_ok.stream().min(Comparator.comparing(Cluster::size)).orElseThrow(NoSuchElementException::new);
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

        if (c.size() >= this.k
                && (!this.lDiversityEnabled || c.diversity(this.a_s) >= this.l)) {
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
                System.out.print(KC.toString());
                return;
            }

            int m = 0;
            for (Cluster C_j : nonAnonymisedClusters) {
                if (c.size() < C_j.size()) {
                    m++;
                }
            }

            if (2 * m > nonAnonymisedClusters.size()) {
                t.suppress();
                return;
            }

            // line 17 of delay_constraint
            if (nonAnonymisedClusters.stream().mapToInt(Cluster::size).sum() < this.k) {
                // check for l diversity enabled
                if (this.lDiversityEnabled) {
                    // as on page 9 top of right column
                    // it must also be checked that there is at least l distinct values for a_s
                    // among all non anonymised clusters

                    Set<String> distinctValues = new HashSet<>();
                    for (Cluster c_i : nonAnonymisedClusters) {
                        distinctValues.addAll(c_i.distinctValues(this.a_s));
                    }

                    if (distinctValues.size() >= this.l) {
                        t.suppress();
                        return;
                    }
                } else {
                    // default part of algorithm
                    t.suppress();
                    return;
                }
            }

            Cluster MC = this.merge_clusters(c, nonAnonymisedClusters);
            outputCluster(MC);
        }
    }

    public Cluster merge_clusters(Cluster c, Set<Cluster> clusterList) {
        // This process continues until C’s size is at least k.
        while (c.size() < this.k) {
            float smallestEnlargement = 0;
            Cluster clusterWithSmallest = null;
            for (Cluster toMergeCluster : clusterList) {
                if (c == toMergeCluster) {
                    // do not merge c with itself
                    continue;
                }

                // calculate the enlargement of C due to the possible merge with Ci.
                // and track which would bring the minimum enlargement
                float possibleEnlargement = enlargement(c, toMergeCluster);
                if (clusterWithSmallest == null || possibleEnlargement < smallestEnlargement) {
                    clusterWithSmallest = toMergeCluster;
                    smallestEnlargement = possibleEnlargement;
                }
            }

            // Select the cluster, which brings the minimum enlargement to C, and merges C
            // with it.
            c.merge(clusterWithSmallest);
        }

        // Then, the resulting cluster is given in output
        return c;
    }

    private Cluster getRandomCluster(Set<Cluster> set) {
        int random = new Random().nextInt(set.size());
        for (Cluster item : set) {
            if (random-- == 0) {
                return item;
            }
        }
        return set.iterator().next();
    }

    public void outputCluster(Cluster c) {
        Set<Cluster> SC;
        if (c.size() >= 2 * this.k && (!this.lDiversityEnabled || c.diversity(this.a_s) >= this.l)) {
            if (lDiversityEnabled) {
                SC = splitL(c, this.a_s);
            } else {
                SC = split(c);
            }
        } else {
            // SC = { c };
            SC = new LinkedHashSet<Cluster>();
            SC.add(c);
        }

        for (Cluster C_i : SC) {
            System.out.print(C_i.toString()); // output all tuples in C_i with its generalisation;

            // TODO Update aveInfoLoss according to informationLoss(C_i);
            // aveInfoLoss is updated to be the average information loss of
            // the most recent k-anonymized clusters including the new ones
            // note below is not right what does it mean?

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
            // It seesm line 11 of output_cluster in the paper is redundant... technically
            // it will be handled by garbage collection
            this.nonAnonymisedClusters.remove(C_i);
        }
    }

    public Set<Cluster> split(Cluster c) {
        Set<Cluster> SC = new LinkedHashSet<Cluster>();

        // BS = set of buckets created by grouping tuples in C by pid attribute
        Map<String, List<Tuple>> BS = new HashMap<>();
        for (Tuple t : c.getTuples()) {
            if (!BS.containsKey(t.getPid())) {
                BS.put(t.getPid(), new ArrayList<Tuple>());
            }

            BS.get(t.getPid()).add(t);
        }

        while (BS.size() >= this.k) {
            // randomly select a bucket B from BS, and pick one of its tuples t;
            Random random = new Random();
            List<String> uniquePIDs = new ArrayList<String>(BS.keySet());
            String randomPID = uniquePIDs.get(random.nextInt(uniquePIDs.size()));

            // randomly selected bucket and tuple
            List<Tuple> B = BS.get(randomPID);
            Tuple t = B.get(random.nextInt(B.size()));

            // Create a new sub-cluster C_new over t;
            Cluster C_new = new Cluster(t, DGHs);

            if (B.isEmpty()) {
                // delete B
                BS.remove(randomPID);
            }

            // H = heap with k-1 nodes, each with infinite distance to t;
            Tuple[] H = new Tuple[this.k - 1];

            // for (Bucket b : BS \ B) calculate how close it is to our randomly selected
            // tuple
            // and ensure that the clostest bucket is at the head of our heap
            Iterator<String> BSKey = BS.keySet().iterator();
            List<Tuple> sortedTuples = new ArrayList<Tuple>();
            while (BSKey.hasNext()) {
                String pid = BSKey.next();
                if (randomPID == pid) { // handles BS \ B... helps ensures k-anonymity
                    continue;
                }

                List<Tuple> b = BS.get(pid);

                // pick one of its tuples t2 and calculate t2 distance to t;
                Tuple t2 = b.get(random.nextInt(b.size()));
                sortedTuples.add(t2);
            }
            // the order of the comparison matters here
            sortedTuples.sort((Tuple t1, Tuple t2) -> Float.compare(enlargement(t, t1), enlargement(t, t2)));
            System.arraycopy(sortedTuples.toArray(), 0, H, 0, H.length);

            // for each node in the heap
            for (Tuple n : H) {
                if (n == null) {
                    continue;
                }
                // insert n into C_new
                C_new.add(n);

                // let B_j be the bucket containing n;
                List<Tuple> B_j = BS.get(n.getPid());

                // Delete n from B_j;
                B_j.remove(n);

                if (B_j.size() == 0) {
                    // delete B_j;
                    BS.remove(n.getPid());
                }
            }

            // Add C_new to SC;
            SC.add(C_new);
        }

        // for (Bucket B_i : BS)
        Iterator<Map.Entry<String, List<Tuple>>> iterator = BS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Tuple>> entry = iterator.next();
            List<Tuple> b_i = entry.getValue();

            // pick a tuple t_i in B_i;
            Random random = new Random();
            Tuple t_i = b_i.get(random.nextInt(b_i.size()));

            // find nearest cluster of t_i in SC, and add all the tuples in B_i to it;
            Cluster nearestCluster = null;
            float smallestEnlargement = 0;
            for (Cluster c_possible : SC) {
                // nearest is cluster that requires min enlargement to enclose
                float enlargementRequired = enlargement(c_possible, t_i);
                if (nearestCluster == null || enlargementRequired < smallestEnlargement) {
                    nearestCluster = c_possible;
                    smallestEnlargement = enlargementRequired;
                }
            }

            // add all the tuples in B_i to nearest cluster
            nearestCluster.add(b_i);

            // delete B_i;
            iterator.remove();
        }

        return SC;
    }

    private int heapParent(Tuple[] H, int index) {
        return index / 2;
    }

    private Tuple[] heapSwap(Tuple[] H, int t1, int t2) {
        Tuple tmp = H[t1];

        H[t1] = H[t2];
        H[t2] = tmp;
        return H;
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

                // TODO this line is causing the issues as B_j size is 1 and i < 2
                for (int i = 0; i < this.k * (B_j.size() / sum); i++) {
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

        for (Cluster sc_i : SC) {
            try {
                Cluster clone = (Cluster) sc_i.clone();
                for (Tuple t_bar : clone.getTuples()) {
                    // let G_t be the set of tuples in C such that G_t = {t2 in C | t.pid = t2.pid}
                    Set<Tuple> G_t = new LinkedHashSet<>();
                    for (Tuple t : c.getTuples()) {
                        if (t.getPid() == t_bar.getPid()) {
                            G_t.add(t);
                        }
                    }

                    // insert G_t into SC_i;
                    sc_i.add(G_t);

                    // delete G_t from C;
                    c.removeSet(G_t);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

        }

        return SC;
    }

    public Map<String, List<Tuple>> generate_buckets(Cluster c, Integer a_s) {
        // group C's tuples into distinct buckets BS on the basis of
        // the values for the sensitive attribute
        Map<String, List<Tuple>> BS = new HashMap<>();
        for (Tuple t : c.getTuples()) {
            if (!BS.containsKey(t.getValue(a_s))) {
                BS.put(t.getValue(a_s), new ArrayList<Tuple>());
            }

            BS.get(t.getValue(a_s)).add(t);
        }

        return BS;
    }

    public float enlargement(Cluster c, Tuple t) {

        try {
            Cluster clone = (Cluster) c.clone();
            clone.add(t);

            return clone.informationLoss() - c.informationLoss();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public float enlargement(Cluster c1, Cluster c2) {
        // to finish merge clusters, need to be able to know the potential enlargement
        // if two clusters were to be merged

        // I Assume this is what they want... as if all tuples from one were added to
        // the other
        try {
            Cluster clone = (Cluster) c1.clone();
            clone.add(c2.getTuples());

            return clone.informationLoss() - c1.informationLoss();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public float enlargement(Tuple t1, Tuple t2) {
        if (t1 == null || t2 == null) {

            return 0;
        }

        // TODO
        // This feels like a dirty solution
        Cluster c1 = new Cluster(t1, DGHs);
        Cluster c2 = new Cluster(t2, DGHs);

        return enlargement(c1, c2);

    }
}