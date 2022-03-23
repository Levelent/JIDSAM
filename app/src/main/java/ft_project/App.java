package ft_project;

import java.util.*;

public class App {
    /**
     * App main function
     * 
     * @param args provided by command line interface
     */
    public static void main(String[] args) {
        // predefine thresholds/constants
        int k = 3;
        int delta = 15;
        int beta = 2;
        int omega = 100; // TODO refine this
        int expirationBand = 5;// Gamma

        // l diversity thresholds/constants
        int l = 2;
        int a_s = 2;

        // create data stream
        InStream dataStream = new InStream("./src/main/resources/adult-100.csv");

        // create data out stream
        OutStream outputStream = new OutStream("output.txt");

        // initialise CASTLE
        Castle castle;
        switch (args.length > 0 ? args[0] : "") {
            case "1":
                // run CASTLE with l diversity
                castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                break;
            case "2":
                // run B-CASTLE
                castle = new BCastle(dataStream, k, delta, beta);
                break;
            case "3":
                // run FADS (note beta is t_kc)
                castle = new FADS(dataStream, k, delta, beta);
                break;
            case "4":
                // run FADS with l diversity (note beta is t_kc)
                castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                break;
            case "5":
                castle = new XBAND(dataStream, k, delta, beta, omega, expirationBand);
            case "compare":
                compare();
                return;
            default:
                // run normal castle
                castle = new Castle(dataStream, k, delta, beta);
        }

        // set DGHs and output stream
        castle.setDGHs(new DGHReader("./src/main/resources/dgh").DGHs);

        castle.setOutputStream(outputStream);

        // run CASTLE
        castle.run();

        // close file
        dataStream.close();
        outputStream.close();
    }

    /**
     * Helper function to run all comparison tasks
     */
    public static void compare() {
        String dataSet = "./src/main/resources/adult-1000.csv";
        String[] versions = { "castle", "castlel", "bcastle", "FADS", "FADSl" };
        ArrayList<VaryDelta> instances_d = new ArrayList<>();
        ArrayList<VaryK> instances_k = new ArrayList<>();
        ArrayList<Thread> threads_d = new ArrayList<>();
        ArrayList<Thread> threads_k = new ArrayList<>();

        for (String version : versions) {
            // k
            VaryK k = new VaryK(dataSet, version);
            Thread t_k = new Thread(k);

            // delta
            VaryDelta d = new VaryDelta(dataSet, version);
            Thread t_d = new Thread(d);

            // add
            threads_k.add(t_k);
            instances_k.add(k);
            threads_d.add(t_d);
            instances_d.add(d);
            t_d.start();
            t_k.start();
        }

        // create comparison output for k
        OutStream compareOutStream = new OutStream("compare-k.csv");
        compareOutStream.out.println("version,k,avgInfoLoss");

        try {
            for (int i = 0; i < threads_k.size(); i++) {
                threads_k.get(i).join();
                for (String s : instances_k.get(i).value) {
                    compareOutStream.out.println(s);
                }
            }
        } catch (Exception e) {
            System.err.println("interrupted");
        }

        compareOutStream.close();

        // create comparison output for delta
        compareOutStream = new OutStream("compare-delta.csv");
        compareOutStream.out.println("version,delta,avgInfoLoss");

        try {
            for (int i = 0; i < threads_d.size(); i++) {
                threads_d.get(i).join();
                for (String s : instances_d.get(i).value) {
                    compareOutStream.out.println(s);
                }
            }
        } catch (Exception e) {
            System.err.println("interrupted");
        }

        compareOutStream.close();
    }

    /**
     * Run versions while varying k - output to compare-k.csv
     * 
     * @param dataSet to use for testing
     */
    public static class VaryK implements Runnable {
        private volatile ArrayList<String> value;

        private String dataSet, version;

        public VaryK(String dataSet, String version) {
            value = new ArrayList<>();
            this.dataSet = dataSet;
            this.version = version;
        }

        @Override
        public void run() {
            int[] ks = { 10, 50, 100, 200 };
            for (int k : ks) {
                // predefine thresholds/constants
                int delta = 10;
                int beta = 2;

                // l diversity thresholds/constants
                int l = 2;
                int a_s = 2;

                // create data stream
                InStream dataStream = new InStream(dataSet);

                // create data out stream
                OutStream outputStream = new OutStream("output.txt");

                // initialise CASTLE
                Castle castle;
                switch (version) {
                    case "castlel":
                        // run CASTLE with l diversity
                        castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "bcastle":
                        // run B-CASTLE
                        castle = new BCastle(dataStream, k, delta, beta);
                        break;
                    case "FADS":
                        // run FADS (note beta is t_kc)
                        castle = new FADS(dataStream, k, delta, beta);
                        break;
                    case "FADSl":
                        // run FADS with l diversity (note beta is t_kc)
                        castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                        break;
                    default:
                        // run normal castle
                        castle = new Castle(dataStream, k, delta, beta);
                }

                // set DGHs and output stream
                castle.setDGHs(new DGHReader("./src/main/resources/dgh").DGHs);

                castle.setOutputStream(outputStream);

                // run CASTLE
                castle.run();

                // close file
                dataStream.close();
                outputStream.close();

                // output comparison data
                value.add(version + "," + k + "," + castle.aveInfoLoss);
            }
        }

        public ArrayList<String> getValue() {
            return value;
        }
    }

    public static class VaryDelta implements Runnable {
        private volatile ArrayList<String> value;

        private String dataSet, version;

        public VaryDelta(String dataSet, String version) {
            value = new ArrayList<>();
            this.dataSet = dataSet;
            this.version = version;
        }

        @Override
        public void run() {
            int[] deltas = { 10, 50, 100, 200 };
            for (int delta : deltas) {
                // predefine thresholds/constants
                int k = 10;
                int beta = 2;

                // l diversity thresholds/constants
                int l = 2;
                int a_s = 2;

                // create data stream
                InStream dataStream = new InStream(dataSet);

                // create data out stream
                OutStream outputStream = new OutStream("output.txt");

                // initialise CASTLE
                Castle castle;
                switch (version) {
                    case "castlel":
                        // run CASTLE with l diversity
                        castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "bcastle":
                        // run B-CASTLE
                        castle = new BCastle(dataStream, k, delta, beta);
                        break;
                    case "FADS":
                        // run FADS (note beta is t_kc)
                        castle = new FADS(dataStream, k, delta, beta);
                        break;
                    case "FADSl":
                        // run FADS with l diversity (note beta is t_kc)
                        castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                        break;
                    default:
                        // run normal castle
                        castle = new Castle(dataStream, k, delta, beta);
                }

                // set DGHs and output stream
                castle.setDGHs(new DGHReader("./src/main/resources/dgh").DGHs);

                castle.setOutputStream(outputStream);

                // run CASTLE
                castle.run();

                // close file
                dataStream.close();
                outputStream.close();

                // output comparison data
                value.add(version + "," + delta + "," + castle.aveInfoLoss);
            }
        }

        public ArrayList<String> getValue() {
            return value;
        }
    }
}