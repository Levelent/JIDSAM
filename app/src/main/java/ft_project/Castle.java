package ft_project;

import java.util.*;

public class Castle {
    protected int k, delta, beta;
    protected float aveInfoLoss;

    protected Set<Cluster> nonAnonymisedClusters, anonymisedClusters;
    protected Map<String, DGH> DGHs;
    protected InStream s;
    protected OutStream outputStream;

    public Castle(InStream s, int k, int delta, int beta) {
        // set default algorithm parameters
        this.k = k;
        this.delta = delta;
        this.beta = beta;
        this.s = s;

        // initializations for algorithm
        this.nonAnonymisedClusters = new LinkedHashSet<Cluster>(); // set of non-k_s anonymised clusters
        this.anonymisedClusters = new LinkedHashSet<Cluster>(); // set of k_s anonymised clusters
        this.aveInfoLoss = 0; // Let be initialised to 0, usually is the average information loss
    }

    public void setDGHs(Map<String, DGH> dghs) {
        DGHs = dghs;
    }

    public void setOutputStream(OutStream stream) {
        this.outputStream = stream;
    }

    public void run() {

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

        if (c.size() >= this.k) {
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
                // default part of algorithm
                t.suppress(this.outputStream, this.DGHs);
                return;
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

    protected Cluster getRandomCluster(Set<Cluster> set) {
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
        if (c.size() >= 2 * this.k) {
            SC = split(c);
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
            // and ensure that the closest bucket is at the head of our heap
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
        // potential enlargement if two clusters were to be merged
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

        Cluster c1 = new Cluster(t1, DGHs);
        Cluster c2 = new Cluster(t2, DGHs);
        return enlargement(c1, c2);
    }
}