package ft_project;

import java.util.*;

public class App {
    private Boolean lDiversityEnabled = false;

    private int k, delta, beta, l, a_s;
    private float aveInfoLoss, thresholdInfoLoss;

    private Set<Cluster> nonAnonymisedClusters, anonymisedClusters;
    private Map<String, DGH> DGHs;
    private OutStream outputStream;

    public static void main(String[] args) {
        // predefine thresholds/constants
        int k = 5;
        int delta = 10;
        int beta = 2;

        // l diversity thresholds/constants
        int l = 2;
        int a_s = 2;

        // create data stream
        InStream dataStream = new InStream("./src/main/resources/adult-100.csv");
        // InStream dataStream = new InStream("../../resources/adult-100.csv");
        // InStream dataStream = new InStream("./app/src/main/resources/adult-100.csv");

        // create data out stream
        OutStream outputStream = new OutStream("output.txt");

        // initialise CASTLE
        // Castle castle = new Castle(dataStream, k, delta, beta);

        // run CASTLE with l diversity
        Castle castle = new CastleL(dataStream, k, delta, beta, l, a_s);

        // set DGHs and output stream
        castle.setDGHs(new DGHReader("./src/main/resources/dgh").DGHs);
        // app.setDGHs(new DGHReader("../../resources/dgh").DGHs);
        // app.setDGHs(new DGHReader("./app/src/main/resources/dgh").DGHs);

        castle.setOutputStream(outputStream);

        // run CASTLE
        castle.run();

        // close file
        dataStream.close();
        outputStream.close();
    }
}