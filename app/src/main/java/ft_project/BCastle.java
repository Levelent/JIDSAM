package ft_project;

import java.util.*;

public class BCastle extends Castle {
    public BCastle(InStream s, int k, int delta, int beta) {
        super(s, k, delta, beta);
    }

    public Cluster bestSelection(Tuple t) {
        // TODO implement

        return new Cluster(t, DGHs);
    }

    public Cluster merge_clusters(Cluster c, Set<Cluster> clusterList) {
        // TODO implement

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
