package ft_project;

public class App {

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
        // Let gamma be the set of non-k_s anonymised clusters, initialised to be empty
        // Let omega be the set of k_s anonymised clusters, initialised to be empty
        // Let rho be initialised to 0
        Tuple t;
        while ((t = s.next()) != null) {
            Cluster c = bestSelection(t);
            if (c == null) {
                // create new cluster on t and insert it into gamma
            } else {
                // push t to c
            }
            // let t2 be the tuple with position equal to t.p - delta
            // if (t2 has not been output) {
            //     delay_constraint(t2);
            // }
        }
    }

    public Cluster bestSelection(Tuple t) {
        // Let E be initialised empty
        // for (Tuple C_j : gamma) {
        //     e = Enlargement(C_j, t);
        //     E.insert(e);
        // }
        //
        // Let min be the minimum element in E;
        // Let SetC_min be the set of clusters C~ in gamma with Enlargement(C~, t) = min;
        //
        // for (Tuple C_j : SetC_min) {
        //     Create a copy of C_j that we push t into
        //     Calculate information loss, if less than rho threshold insert into SetC_ok
        // }
        //
        // if (SetC_ok is empty) {
        //     if |gamma| >= beta {
        //         return 'any cluster in SetC_min with minimum size';
        //     } else {
        //         return null;
        //     }
        // } else {
        //   return 'any cluster in SetC_ok with minimum size'; 
        // }
        return new Cluster();
    }

    public void delayConstraint(Tuple t) {
        // Let C be the non-k_s anonymised cluster to which t belongs
        // if c.size >= k {
        //     outputCluster(c);
        //     return;
        // }
        // KC_set = All k_s anonymised clusters in omega containing t;
        // if KC_set is not empty {
        //     let KC be a cluster randomly selected from KC_set;
        //     Output t with the generalisation of KC;
        //     return;   
        // }
        //
        // Let m be an integer set to 0;
        // for (Cluster C_j : gamma) {
        //     if (c.size < c_j.size) {
        //         m = m + 1;   
        //     }
        // }
        // if (2 * m > |gamma| || sum of all cluster sizes in gamma < k) {
        //     Suppress tuple t;
        //     return;
        // }
        // MC = mergeClusters(C, gamma \ C);
        // outputCluster(c);
    }

    public void outputCluster(Cluster c) {
        // if (c.size() >= 2 * k) {
        //     SC = split(c);
        // } else {
        //     SC = { c };  
        // }
        // for (Cluster c_i : SC) {
        //     output all tuples in C_i with its generalisation;
        //     Update rho according to informationLoss(C_i);
        //     if (informationLoss(C_i) < rho) {
        //         insert C_i into omega;
        //     } else {
        //         delete C_i;
        //     }
        //     delete C_i from gamma;
        // }
    }

    public void informationLoss(Cluster C_j, Tuple t) {
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
    }

    public void mergeClusters() {
        // Might not be worth an entire function? Unless long, as it's only done once
    }

    public void split(Cluster c) {
        // Initialise SC to be empty;
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
        // return SC;
    }

    public void splitL(Cluster c, Integer a_s) { // TODO a_s type of integer is temporary
        // BS = generate_buckets(c, a_s)
        // if (BS.size() < l) {
        //     return { c };
        // }
        // let SC be an empty set of sub-clusters;
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

        // for (Cluster sc_i : SC) {
        //     for (Tuple t : sc_i) {
        //         let G_t be the set of tuples in C such that G_t = {t2 in C | t.pid = t2.pid}
        //         insert G_t into SC_i;
        //         delete G_t from C_i;
        //     }
        // }
        // return SC;
    }

    public void generate_buckets(Cluster c, Integer a_s) { // TODO a_s type of integer is temporary
        // Doesn't seem to fully specify what it wants, other than the buckets being disjoint
    }

    public void enlargement() {
        // Depends on quantitive and qualative data types
        // Would probably need to pass a bunch of parameters not specified by the paper in here
    }
}