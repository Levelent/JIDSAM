package ft_project;

import java.util.*;

public class BCastle extends Castle {
    protected double alpha;

    public BCastle(InStream s, int k, int delta, int beta) {
        super(s, k, delta, beta);

        alpha = Math.ceil(delta / Math.sqrt(k + beta));
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

        if (SetC_ok.size() > 0) {
            Cluster c_j = getRandomCluster(SetC_ok);
            // let num(c) be the number of tuples in cluster c
            while (c_j.size() > this.alpha) {
                // TODO quadratic probing hash search of Cj from Set C ok
            }
            return c_j;
        }

        if (this.nonAnonymisedClusters.size() >= this.beta) {
            // ascending order sort the enlargements
            List<Float> Eplus = new ArrayList<Float>(enlargementMap.keySet());
            Collections.sort(Eplus);

            for (Float enlargementValue : Eplus) {
                for (Cluster c_i : enlargementMap.get(enlargementValue)) {
                    if (c_i.size() > this.alpha) {
                        // TODO linear probing hash search of Ci from E+
                    } else {
                        return c_i;
                    }
                }
            }
        }

        return null;
    }

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

    protected float getCorrelatedDistance(Cluster C1, Cluster C2) {
        // TODO implement page 2 rhs
        return 0;
    }
}
