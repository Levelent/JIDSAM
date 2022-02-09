package ft_project;

public class App {

    public static void main(String[] args) {
    }

    public void castle(Stream s, int k, int delta, int beta) {
        // Let gamma be the set of non-k_s anonymised clusters, initialised to be empty
        // Let omega be the set of k_s anonymised clusters, initialised to be empty
        // Let rho be initialised to 0
        while (s.length() > 0) {
            Tuple t = s.next();
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
        // for (Tuple C_j : gamma)
        //     Let e be Enlargement(C_j, t);
        //     Insert e into E
        // Let min be the minimum element in E;
        // Let SetC_min be the set of clusters C~ in gamma with Enlargement(C~, t) = min;
        // for (Tuple C_j : SetC_min) {
        //     Create a copy of C_j that we push t into
        //     Calculate information loss, if less than rho threshold insert into SetC_ok
        // }
        // if 
        return Cluster();
    }

    public Object delayConstraint(Tuple t2) {

    }

    public int informationLoss(Cluster C_j, Tuple t) {

    }

    public void outputCluster(Cluster c) {

    }

    public void mergeClusters() {

    }

    public void generate_buckets() {

    }

    public void split() {

    }

    public void splitL() {

    }

    public void enlargement() {

    }
}

public class Stream {
    public int length() {
        return 0;
    }

    public Tuple next() {
        return new Tuple();
    }
}

public class Tuple {}

public class Cluster {}