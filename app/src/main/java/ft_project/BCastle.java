package ft_project;

import java.util.*;

public class BCastle extends Castle {
    protected double alpha;

    /**
     * B-Castle Constructor
     * 
     * @param s     data stream
     * @param k     threshold
     * @param delta threshold
     * @param beta  threshold
     */
    public BCastle(InStream s, int k, int delta, int beta) {
        super(s, k, delta, beta);

        alpha = Math.ceil(delta / Math.sqrt(k + beta));
    }

    /**
     * Pick the best cluster to add a given tuple to
     * 
     * @param t tuple to pick selection for
     */
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

        if (SetC_ok.size() > 0) {
            Cluster c_j = getRandomCluster(SetC_ok);
            // let num(c) be the number of tuples in cluster c
            ArrayList<Cluster> ArrayC_ok = new ArrayList<>();
            ArrayC_ok.addAll(SetC_ok);

            int start = ArrayC_ok.indexOf(c_j);
            int quadStep, linearStep = 0;
            while ((quadStep = start + linearStep ^ 2) < ArrayC_ok.size()) {
                // quadratic search of Cj from Set C ok
                c_j = ArrayC_ok.get(quadStep);

                // check stopping condition
                if (c_j.size() < this.alpha) {
                    return c_j;
                } else {
                    c_j = ArrayC_ok.get(start + linearStep ^ 2);
                }

                linearStep++;
            }
        }

        if (this.nonAnonymisedClusters.size() >= this.beta) {
            // ascending order sort the enlargements
            List<Float> Eplus = new ArrayList<Float>(enlargementMap.keySet());
            Collections.sort(Eplus);

            for (Float enlargementValue : Eplus) {
                // linear probing hash search of cluster until size < alpha
                for (Cluster c_i : enlargementMap.get(enlargementValue)) {
                    if (c_i.size() < this.alpha) {
                        return c_i;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Merge a cluster with a given cluster set taking into account the correlated
     * distance
     * 
     * @param c           the cluster to merge into
     * @param clusterList list of clusters that can be used to merge
     */
    public Cluster merge_clusters(Cluster c, Set<Cluster> clusterList) {
        // implemented from bottom right of page 3
        while (c.size() < this.k) {
            Set<Cluster> SetC_low = new LinkedHashSet<>();

            float lowestCorrelationDistance = 0;
            for (Cluster c_i : clusterList) {
                if (c == c_i) {
                    // not including self
                    continue;
                }

                // find lowest correlation distance of c_i into setC_low
                float correlatedDistance = getCorrelatedDistance(c, c_i);
                if (SetC_low.size() == 0 || correlatedDistance < lowestCorrelationDistance) {
                    // new lowest found so clear SetC_low
                    SetC_low = new LinkedHashSet<>();
                    SetC_low.add(c_i);
                    lowestCorrelationDistance = correlatedDistance;
                } else if (SetC_low.size() > 0 || correlatedDistance == lowestCorrelationDistance) {
                    // same as lowest distance so add to SetC_low
                    SetC_low.add(c_i);
                }

            }

            Cluster c_j = getRandomCluster(SetC_low);
            c.merge(c_j);
            this.nonAnonymisedClusters.remove(c_j);
        }

        return c;
    }

    /**
     * Get the correlated distance between two clusters
     * 
     * @param C1 first cluster
     * @param C2 second cluster
     * @return correlated distance
     */
    protected float getCorrelatedDistance(Cluster C1, Cluster C2) {
        Map<String, Generalisation> c1Generalisation = C1.getGeneralisations();
        Map<String, Generalisation> c2Generalisation = C2.getGeneralisations();

        float correlatedDistance = 0;

        for (Map.Entry<String, Generalisation> entry : c1Generalisation.entrySet()) {
            // name of attribute associated with generalisation
            String key = entry.getKey();

            // generalisation from c1 an c2
            Generalisation gen1 = entry.getValue();
            Generalisation gen2 = c2Generalisation.get(key);

            // handle ranges and DGH
            DGH dgh1 = C1.getDGH().get(key);
            DGH dgh2 = C2.getDGH().get(key);
            if (dgh1 == null || dgh2 == null) {
                // Must be a continuous generalisation
                ContinuousGeneralisation cGen1 = (ContinuousGeneralisation) gen1;
                ContinuousGeneralisation cGen2 = (ContinuousGeneralisation) gen2;

                correlatedDistance += ((cGen1.lb - cGen2.lb) + (cGen1.ub - cGen2.ub)) / (cGen1.LB - cGen1.UB);
            } else {
                // Must be category based generalisation
                correlatedDistance += dgh1.countNodes(dgh1.findCommonAncestor(gen1.toString(), gen2.toString()))
                        / dgh1.countNodes();
            }

        }
        return correlatedDistance;
    }
}
