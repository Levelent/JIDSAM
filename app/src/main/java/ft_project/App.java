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

        // test working by getting the first tuple
        System.out.println(dataStream.next().toString()); // todo:// remove this

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

        Tuple t;
        while ((t = s.next()) != null) {
            Cluster c = bestSelection(t);
            if (c == null) {
                // create new cluster on t and insert it into nonAnonymisedClusters
                nonAnonymisedClusters.add(new Cluster(t));
            } else {
                c.add(t); // add tuple t to cluster c
            }

            // let t2 be the tuple with position equal to t.p - delta
            // if (t2 has not been output) {
            //     delay_constraint(t2);
            // }
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
        Cluster c = new Cluster(t); // todo:// this is wrong but allows rest to be written

        if (c.size() >= this.k) {
            outputCluster(c);
            return;
        }

        // KC_set = All k_s anonymised clusters in anonymisedClusters containing t;
        // if KC_set is not empty {
        //     let KC be a cluster randomly selected from KC_set;
        //     Output t with the generalisation of KC;
        //     return;   
        // }
        //

        int m = 0;
        for (Cluster C_j : nonAnonymisedClusters) {
            if (c.size() < C_j.size()) {
                m++;
            }
        }

        // if (2 * m > |nonAnonymisedClusters| || sum of all cluster sizes in nonAnonymisedClusters < k) {
        //     Suppress tuple t;
        //     return;
        // }
        // MC = mergeClusters(C, nonAnonymisedClusters \ C);
        
        outputCluster(c);
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
            SC = new LinkedHashSet<Cluster>();
            SC.add(c); // SC = { c }; 
        }

        for (Cluster C_i : SC) {
            System.out.print(C_i.toString()); // output all tuples in C_i with its generalisation;

            // todo:// Update aveInfoLoss according to informationLoss(C_i);

            if (informationLoss(C_i) < this.aveInfoLoss) {
                this.anonymisedClusters.add(C_i);
            } else {
                // todo:// delete C_i;
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

    public void mergeClusters() {
        // Might not be worth an entire function? Unless long, as it's only done once
        // its probably a method of the class.. cluster1.merge(cluster2)
    }

    public Set<Cluster> split(Cluster c) {
        Set<Cluster> SC = new LinkedHashSet<Cluster>();
        // BS = set of buckets created by grouping tuples in C by pid attribute
        // while (BS.size() >= k) {
        //     randomly select a bucket B from BS, and pick one of its tuples t;
        //     Create a new sub-cluster C_new over t;
        //     if (B is empty) {
        //         delete B;
        //     }
        //     H = heap with k-1 nodes, each with infinite distance to t;
        //     for (Bucket b : BS \ B) {
        //         pick one of its tuples t2, and calculate t2 distance to t;
        //         if (|t - t2| < |t - root of H|) {
        //             root of H = t;
        //             adjust H accordingly;
        //         }
        //     }
        //     for (Node n : H) {
        //         let t be the tuple in the node;
        //         insert t into C_new
        //         let B_j be the bucket containing t;
        //         Delete t from B_j;
        //         if (B_j.size() == 0) {
        //             delete B_j;
        //         }
        //     }
        //     Add C_new to SC;
        // }
        // for (Bucket B_i : BS) {
        //     pick a tuple t_i in B_i;
        //     find nearest cluster of t_i in SC, and add all the tuples in B_i to it;
        //     delete B_i;
        // }
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
}