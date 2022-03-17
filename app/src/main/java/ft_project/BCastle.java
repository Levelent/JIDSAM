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
        // TODO implement - below is old one

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
}
