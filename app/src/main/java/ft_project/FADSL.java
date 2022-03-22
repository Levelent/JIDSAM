package ft_project;

import java.util.*;

public class FADSL extends FADS {
    protected float aveInfoLoss;

    /**
     * Initialise the FADS algorithm
     * 
     * @param s     data stream
     * @param k     threshold
     * @param delta threshold
     * @param t_kc  reuse constraint (will be referred to as beta!)
     */
    public FADSL(InStream s, int k, int delta, int t_kc) {
        // set default algorithm parameters
        super(s, k, delta, t_kc);
    }

    public void publishTuple(Tuple t) {
        // Build a queue Q_tp from Set_tp by sorting tuples with unique pid in ascending
        // order of distance to t
        // Let H_tp be a hash table that stores tuples by their sensitive attribute
        // values, initialised to phi
        // insert t into H_tp
        // while |Q_tp| > 0 do
        // dequeue tuple t' from Q_tp
        // if |H_tp(t'.a_s)| < flr(max(k, |H_tp.values|) / l) then
        // insert t' into H_tp
        // end if
        // if |H_tp.keys| >= l and |H_tp.values| >= k then
        // break;
        // end if
        // end while
        // if |H_tp.keys| < l or |H_tp.values| < k then
        // outputWithKCorSuppress(t);
        // else
        // outputWithKCorNC(t, H_tp.values);
        // end if
    }

    public void outputWithKCorSuppress(Tuple t) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and has the smallest
        // generalisation information loss
        // if C_kc exists then
        // Initialise Set_t to phi
        // Find the tuples in C_kc whose sensitive attribute value is equal to t's and
        // insert them into Set_t;
        // if |Set_t| > flr(|C| / l) then
        // Suppress and publish t
        // else
        // Publish t with C_kc's generalisation
        // end if
        // else
        // suppress and publish t
        // end if
        // Remove t from Set_tp;
    }

    public void outputWithKCorNC(Tuple t, Cluster c_nc) {
        // Find a k-anonymised cluster C_kc in Set_kc that covers t and has the smallest
        // generalisation information loss
        // if C_kc exists and its generalisation info loss is smaller than C_nc's then
        // initialise Set_t to phi
        // Find the tuples in C_kc whose sensitive attribute value is equal to t's and
        // insert them into Set_t;
        // if |Set_t| > flr(|C| / l) then
        // Publish t with C_nc's generalisation
        // Insert C_nc into Set_kc
        // else
        // publish t with C_kc's generalisation
        // end if
        // else
        // Publish t with C_nc's generalisation
        // Insert C_nc into Set_kc
        // end if
        // remove t from Set_tp
    }

}
