package ft_project;

import java.util.*;

public class FADS extends Castle {
    protected float aveInfoLoss;

    /**
     * Initialise the FADS algorithm
     * 
     * @param s     data stream
     * @param k     threshold
     * @param delta threshold
     * @param t_kc  reuse constraint (will be referred to as beta!)
     */
    public FADS(InStream s, int k, int delta, int t_kc) {
        // set default algorithm parameters
        super(s, k, delta, t_kc);
    }

    /**
     * Run the FADS algorithm
     */
    public void run() {
        // Let Set_tp be the set of tuples waiting for release, initialised to phi.
        // Let Set_kc be the set of published k-anonymised clusters who exist no longer than t_kc, initialised to phi.
        // While S != phi do
        // Read a tuple t_n from S and insert into Set_tp
        // Update the ranges of the numeric QIDs with respect to t_n;
        // Remove the k-anonymised clusters in Set_kc that exist longer than or equal to T_kc;
        // if |Set_tp| >= delta 
        // Remove the earliest arrived tuple t from Set_tp;
        // publishTuple(t);
        // end if
        // end while
        // while |Set_tp| > 0 do
        // Remove the earliest arrived tuple t from Set_tp;
        // publishTuple(t);
        // end while
    }

    public void publishTuple(Tuple t) {
        // if |Set_tp| < k - 1 then
        // insert t into set_tp;
        // foreach t_i in set_tp do
        // outputWithKCorSuppress(t_i);
        // end foreach
        // else
        // outputWithKCorNC(t);
        // end if
    }

    public void outputWithKCorSuppress(Tuple t) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and incurs least info loss increase after adding tuple to cluster
        // if C_kc exists then
        // Publish t with C_kc's generalisation
        // else
        // suppress and publish t
        // end if
        // Remove t from Set_tp;
    }

    public void outputWithKCorNC(Tuple t) {
        // Find the k-1 nearest neighbours of t with unique pid in Set_tp and create a new cluster C_nc on t and its neighbours
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and incurs least info loss increase after adding tuple to cluster
        // if the nearest neighbours are fewer than k-1 then
        // if C_kc exists then
        // Publish t with C_kc's generalisation
        // else
        // suppress and publish t
        // end if
        // else if C_kc exists and its generalisation info loss is smaller than C_nc's then
        // publish t with C_kc's generalisation
        // else
        // publish t with C_nc's generalisation
        // insert C_nc into Set_kc
        // remove the k-1 nearest neighbours of t from Set_tp
        // end if
        // remove t from Set_tp
    }

}
