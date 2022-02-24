package ft_project;

import java.util.*;

public class App {
    final Boolean lDiversityEnabled = false; 

    private int k, delta, beta, aveInfoLoss, thresholdInfoLoss;

    private Set<Cluster> nonAnonymisedClusters, anonymisedClusters;

    public static void main(String[] args) {
        // predefine thresholds/constants
        int k = 2;
        int delta = 2;
        int beta = 2;

        // initialize app
        App app = new App();

        // create data stream
        Stream dataStream = new Stream("src/main/resources/adult.csv");

        // run CASTLE
        app.castle(dataStream, k, delta, beta);

        // close file
        dataStream.close();
    }

    public void castle(Stream s, int k, int delta, int beta) {
        // set algorithm parameters
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
                nonAnonymisedClusters.add(new Cluster(t));
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
        Map<Integer, Set<Cluster>> enlargementMap = new HashMap<>(); 

        for (Cluster C_j : this.nonAnonymisedClusters) {
            // get the cluster enlargement value if given t
            int e = enlargement(C_j, t);

            // if enlargement value is is not already in enlargement map then initialize and empty cluster list
            Set<Cluster> clusterList = enlargementMap.get(e);
            if (clusterList == null) {
                clusterList = new LinkedHashSet<Cluster>();
                enlargementMap.put(e, clusterList);
            }

            // add cluster to list inside of map
            clusterList.add(C_j);
        }

        // todo:// has been added due to next line will cause an issue if nonAnonymisedClusters was empty (first tuple and no clusters made)
        if (enlargementMap.size() == 0) {
            return null;
        }
 
        // Let min be the minimum element in E;
        int minEnlargement = Collections.min(enlargementMap.keySet());

        // Let SetC_min be the set of clusters C~ in nonAnonymisedClusters with Enlargement(C~, t) = min;
        Set<Cluster> SetC_min = enlargementMap.get(minEnlargement);

        // initialize SetC_ok
        Set<Cluster> SetC_ok = new LinkedHashSet<Cluster>();

        for (Cluster C_j : SetC_min) {
            // Create a copy of C_j that we push t into
            try {
                Cluster copyOfC_j = (Cluster)C_j.clone();

                copyOfC_j.add(t);

                // Calculate information loss, if less than aveInfoLoss threshold insert into SetC_ok
                if (informationLoss(copyOfC_j) < this.aveInfoLoss) {
                    // todo:// does it mean add the copy with t or the original? 
                    SetC_ok.add(copyOfC_j);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (SetC_ok.size() == 0) {
            if (this.nonAnonymisedClusters.size() >= this.beta) { // |nonAnonymisedClusters| >= beta
                // return 'any cluster in SetC_min with minimum size';
                return SetC_min.stream().min(Comparator.comparing(Cluster::size)).orElseThrow(NoSuchElementException::new);
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
        for(Cluster cluster : this.nonAnonymisedClusters) {
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
            for (Cluster cluster: this.anonymisedClusters) {
                if (cluster.contains(t)) {
                    KC_set.add(cluster);
                }
            }

            if (KC_set.size() > 0) {
                // let KC be a cluster randomly selected from KC_set;
                Cluster KC = getRandomCluster(KC_set);

                // todo:// Output t with the generalisation of KC; ??
                return;
            }

            int m = 0;
            for (Cluster C_j : nonAnonymisedClusters) {
                if (c.size() < C_j.size()) {
                    m++;
                }
            }

            if (2 * m > nonAnonymisedClusters.size() || nonAnonymisedClusters.stream().mapToInt(Cluster::size).sum() < this.k) {
                // todo:// Suppress tuple t; "CASTLE suppresses t, that is, it outputs t with the most generalized QI value"
                return;
            }
            
            Cluster MC = this.merge_clusters(c, nonAnonymisedClusters);
            outputCluster(MC);
        }
    }

    public Cluster merge_clusters(Cluster c, Set<Cluster> clusterList) {
        // This process continues until C’s size is at least k.
        while (c.size() >= this.k) {
            int smallestEnlargement = 0;
            Cluster clusterWithSmallest = null;
            for (Cluster toMergeCluster : clusterList) {
                if (c == toMergeCluster) {
                    // do not merge c with itself
                    continue;
                }
    
                // calculate the enlargement of C due to the possible merge with Ci.
                // and track which would bring the minimum enlargement
                int possibleEnlargement = enlargement(c, toMergeCluster);
                if (clusterWithSmallest == null || possibleEnlargement < smallestEnlargement) {
                    clusterWithSmallest = toMergeCluster;
                    smallestEnlargement = possibleEnlargement;
                }
            }

            // Select the cluster, which brings the minimum enlargement to C, and merges C with it. 
            c.merge(clusterWithSmallest);
        }

        // Then, the resulting cluster is given in output
        return c;
    }

    private Cluster getRandomCluster(Set<Cluster> set) {
        int random = new Random().nextInt(set.size());
        for(Cluster item : set) {
            if (random-- == 0) {
                return item;
            }
        }
        return set.iterator().next();
    }

    public void outputCluster(Cluster c) {
        Set<Cluster> SC;
        if (c.size() >= 2 * this.k) {
            if (lDiversityEnabled) {
                SC = splitL(c, 0); // todo:// second argument is temporary
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

            // todo:// Update aveInfoLoss according to informationLoss(C_i);
            // aveInfoLoss is updated to be the average information loss of
            // the most recent k-anonymized clusters including the new ones
            // todo:// this is not right what does it mean?
            this.aveInfoLoss = informationLoss(C_i);

            if (informationLoss(C_i) < this.aveInfoLoss) {
                this.anonymisedClusters.add(C_i);
            } else {
                // delete C_i from non-k anonymised clusters;
                this.nonAnonymisedClusters.remove(C_i);
            }
            this.nonAnonymisedClusters.remove(C_i);
        }
    }

    public int informationLoss(Cluster C_j) {
        // If tuple generalisation g = (v_1, ..., v_n) then
        // infoLoss(g) = 1/n * sum_{i=1}^n vInfoLoss(v_i)
        // with vInfoloss(I) being either:
        //
        // (u - l) / (U - L) for numerical range 
        // with interval I = [l, u] and domain [L, U]
        //
        // (|S_v| - 1) / (|S| - 1) for qualitative feature
        // with S_v set of leaf nodes in subtree rooted at v in DGH_i
        // and S set of all leaf nodes in DGH_i
        // Note: DGH_i is the domain generalisation hierarchy for a quasi-identifier q_i
        return 0;
    }

    public Set<Cluster> split(Cluster c) {
        Set<Cluster> SC = new LinkedHashSet<Cluster>();

        // BS = set of buckets created by grouping tuples in C by pid attribute
        Map<String, List<Tuple>> BS = new HashMap<>();
        for (Tuple t: c.getTuples()) {
            if (!BS.containsKey(t.getPid())) {
                BS.put(t.getPid(), new ArrayList<Tuple>());
            }

            BS.get(t.getPid()).add(t);
        } 
        
        
        while (BS.size() >= this.k) {
            // randomly select a bucket B from BS, and pick one of its tuples t;
            Random random = new Random();
            List<String> keys = new ArrayList<String>(BS.keySet());
            String randomKey = keys.get( random.nextInt(keys.size()) );
           
            // randomly selected bucket and tuple
            List<Tuple> B = BS.get(randomKey);
            Tuple t = B.get(random.nextInt(B.size()));
        

            // Create a new sub-cluster C_new over t;
            Cluster C_new = new Cluster(t);

            if (B.isEmpty()) {
                // delete B
                BS.remove(randomKey);
            }


            // H = heap with k-1 nodes, each with infinite distance to t;
        
            // for (Bucket b : BS \ B)
            Iterator<String> BSKey = BS.keySet().iterator();
            while(BSKey.hasNext()) {
                String pid = BSKey.next();
                if (randomKey == pid) { // handles BS \ B
                    continue;
                }
                List<Tuple> b = BS.get(pid);

                // pick one of its tuples t2, and calculate t2 distance to t;
                // if (|t - t2| < |t - root of H|) {
                    // root of H = t;
                    // adjust H accordingly;
                // }
            }        
        
            // for (Node n : H) {
            //   let t be the tuple in the node;
            //   insert t into C_new
            //   let B_j be the bucket containing t;
            //   Delete t from B_j;
            //   if (B_j.size() == 0) {
            //       delete B_j;
            //    }
            // }
            // Add C_new to SC;
        }

        // for (Bucket B_i : BS)
        Iterator<String> BSKey = BS.keySet().iterator();
        while(BSKey.hasNext()) {
            String pid = BSKey.next();
            List<Tuple> b = BS.get(pid);

            // pick a tuple t_i in B_i;
            // todo://

            // find nearest cluster of t_i in SC, and add all the tuples in B_i to it;
            // todo://

            // delete B_i;
            BS.remove(pid);
        }       

        return SC;
    }

    public Set<Cluster> splitL(Cluster c, Integer a_s) { // TODO a_s type of integer is temporary
        // BS = generate_buckets(c, a_s)
        // if (BS.size() < l) {
        //     return { c };
        // }

        Set<Cluster> SC = new LinkedHashSet<Cluster>(); // set of sub-clusters;
        
        // while (BS.size() >= l && sum of bucket sizes >= k) {
        //     randomly select a B from BS;
        //     randomly select a tuple t from B;
        //     generate a sub-cluster C_sub over t;
        //     delete t from B;
        //     for (Bucket B_j : BS) {
        //         for (Tuple t_i : B_j) {
        //             let e_i be enlargement(C_sub, t_i);
        //         }
        //         Sort tuples of B_j by ascending order of their enlargement e_i;
        //         Let T_j be the set of the first k * (B_j.size() / sum of bucket sizes) tuples in B_j;
        //         Insert T_j into C_sub;
        //         delete T_j from B_j;
        //         if (B_j.size() == 0) {
        //             delete B_j from BS;
        //         }
        //     }
        //     Add C_sub to SC;
        // }

        // for (Bucket B : BS) {
        //     for (Tuple t_i : B) {
        //         C_near = nearest subcluster of t_i in SC;
        //         insert t_i into c_near;
        //     }
        //     delete B;
        // }

        for (Cluster sc_i : SC) {
            for (Tuple t: sc_i.getTuples()) {
                // let G_t be the set of tuples in C such that G_t = {t2 in C | t.pid = t2.pid}
                // insert G_t into SC_i;
                // delete G_t from C_i;
            }
        }

        return SC;
    }

    public void generate_buckets(Cluster c, Integer a_s) { // TODO a_s type of integer is temporary
        // Doesn't seem to fully specify what it wants, other than the buckets being disjoint
    }

    public int enlargement(Cluster c, Tuple t) {
        // Depends on quantitive and qualative data types
        // Would probably need to pass a bunch of parameters not specified by the paper in here
        return 0;
    }

    public int enlargement(Cluster c1, Cluster c2) {
        // to finish merge clusters, need to be able to know the potential enlargement
        // if two clusters were to be merged
        return 0;
    }
}