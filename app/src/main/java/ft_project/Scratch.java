package ft_project;

import java.util.*;

public class Scratch {

    public Map<String, DGH> DGHs;

    public static void main(String[] args) {

        // create data stream
        Stream dataStream = new Stream("./app/src/main/resources/adult.csv");
        Map<String, DGH> DGHs = new DGHReader("./app/src/main/resources/dgh").DGHs;
        Scratch s = new Scratch();
        s.setDGH(DGHs);
        Tuple t = dataStream.next();
        // System.out.println(DGHs);
        ArrayList<Tuple> tuples = new ArrayList<Tuple>();
        for (int i = 0; i < 10; i++) {
            tuples.add(dataStream.next());
        }

        tuples.sort((Tuple t1, Tuple t2) -> Float.compare(s.enlargement(t2, t), s.enlargement(t1, t)));

        for (int i = 0; i < 10; i++) {
            System.out.println(s.enlargement(tuples.get(i), t));
        }

    }

    public void setDGH(Map<String, DGH> dgh) {
        this.DGHs = dgh;
    }

    public float enlargement(Tuple t1, Tuple t2) {
        if (t1 == null || t2 == null) {

            return 0;
        }

        // TODO
        // This feels like a dirty solution
        Cluster c1 = new Cluster(t1, DGHs);
        Cluster c2 = new Cluster(t2, DGHs);

        return enlargement(c1, c2);

    }

    public float enlargement(Cluster c1, Cluster c2) {
        // to finish merge clusters, need to be able to know the potential enlargement
        // if two clusters were to be merged

        // I Assume this is what they want... as if all tuples from one were added to
        // the other
        try {
            Cluster clone = (Cluster) c1.clone();
            clone.add(c2.getTuples());

            return clone.informationLoss() - c1.informationLoss();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
