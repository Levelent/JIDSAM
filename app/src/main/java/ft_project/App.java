package ft_project;

public class App {

    static String dataFolder = "./src/main/resources/1/";
    static String dataName = "adult-10000.csv";

    /**
     * App main function
     * 
     * @param args provided by command line interface
     */
    public static void main(String[] args) {
        Constants.streamSize = 10000;

        Constants.setV(false);
        // predefine thresholds/constants
        int k = 3;
        int delta = 15;
        int beta = 4;
        int omega = 100; // TODO refine this
        int expirationBand = 5;// Gamma

        // l diversity thresholds/constants
        int l = 2;
        int a_s = 2;

        // create data stream

        InStream dataStream = new InStream(String.format("%s%s", dataFolder, dataName));

        // create data out stream
        OutStream outputStream = new OutStream("output.txt");

        // initialise CASTLE
        Castle castle = new FADSL(dataStream, k, delta, beta, l, a_s);

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-k":
                    if (i + 1 < args.length) {
                        k = Integer.parseInt(arg);
                    }
                    break;

                case "-d":
                    if (i + 1 < args.length) {
                        delta = Integer.parseInt(arg);
                    }
                    break;

                case "-o":
                    if (i + 1 < args.length) {
                        omega = Integer.parseInt(arg);
                    }
                    break;

                case "-g":
                    if (i + 1 < args.length) {
                        expirationBand = Integer.parseInt(arg);
                    }
                    break;

                case "-b":
                    if (i + 1 < args.length) {
                        beta = Integer.parseInt(arg);
                    }
                    break;

                case "-c":

                    if (i + 1 >= args.length) {
                        break;
                    }

                    switch (args[i + 1]) {
                        case "1":
                            // run CASTLE with l diversity
                            Constants.variant = "CASTLE - L";
                            castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                            break;
                        case "2":
                            // run B-CASTLE
                            Constants.variant = "B-CASTLE";
                            castle = new BCastle(dataStream, k, delta, beta);
                            break;
                        case "3":
                            // run FADS (note beta is t_kc)
                            Constants.variant = "FADS";
                            castle = new FADS(dataStream, k, delta, beta);
                            break;
                        case "4":
                            // run FADS with l diversity (note beta is t_kc)
                            Constants.variant = "FADSL";
                            castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                            break;
                        case "5":
                            Constants.variant = "XBAND";

                            castle = new XBAND(dataStream, k, delta, beta, omega, expirationBand);
                            break;
                        default:
                            // run normal castle

                            Constants.variant = "CASTLE";
                            castle = new Castle(dataStream, k, delta, beta);
                    }

                    break;
                case "compare":

                    compare();
                    break;

                default:
                    continue;

            }
        }

        // set DGHs and output stream
        castle.setDGHs(new DGHReader(String.format("%sdgh.txt", dataFolder)).DGHs);

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
        String dataSet = String.format("%s%s", dataFolder, dataName);
        varyK(dataSet);
        varyDelta(dataSet);
    }

    /**
     * Run versions while varying k - output to compare-k.csv
     * 
     * @param dataSet to use for testing
     */
    public static void varyK(String dataSet) {
        String[] versions = { "castlel", "bcastle", "FADS", "XBAND", "FADSl", "castle" };

        // create comparison output
        OutStream compareOutStream = new OutStream(String.format("out_data/compare-k-%s", dataName));
        compareOutStream.out.println("version,k,avgInfoLoss");

        // run each version
        for (String version : versions) {
            int[] ks = { 10, 25, 50, 75, 100, 150, 200 };
            int[] deltas = { 20, 50, 100, 150, 200, 300, 400 };
            System.out.println("----------- New Version -----------");
            for (int i = 0; i < ks.length; i++) {
                System.out.println("");
                Constants.tOutCount = 0;
                Constants.infoLossSum = 0;
                // predefine thresholds/constants

                int k = ks[i];
                int delta = deltas[i];
                int beta = 2;

                // l diversity thresholds/constants
                int l = 2;
                int a_s = 2;

                // XBAND
                int omega = 100; //
                int expirationBand = 5;//

                // create data stream
                InStream dataStream = new InStream(dataSet);

                // create data out stream
                OutStream outputStream = new OutStream("output.txt");

                // initialise CASTLE
                Castle castle;
                switch (version) {
                    case "castlel":
                        // run CASTLE with l diversity
                        Constants.variant = "CASTLE - L";
                        castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "bcastle":
                        // run B-CASTLE
                        Constants.variant = "B-CASTLE";
                        castle = new BCastle(dataStream, k, delta, beta);
                        break;
                    case "FADS":
                        // run FADS (note beta is t_kc)
                        Constants.variant = "FADS";
                        castle = new FADS(dataStream, k, delta, beta);
                        break;
                    case "FADSl":
                        // run FADS with l diversity (note beta is t_kc)
                        Constants.variant = "FADSL";
                        castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "XBAND":
                        Constants.variant = "XBAND";
                        castle = new XBAND(dataStream, k, delta, beta, omega, expirationBand);
                        break;
                    default:
                        // run normal castle
                        Constants.variant = "CASTLE";
                        castle = new Castle(dataStream, k, delta, beta);
                }

                // set DGHs and output stream
                castle.setDGHs(new DGHReader(String.format("%sdgh.txt", dataFolder)).DGHs);

                castle.setOutputStream(outputStream);

                // run CASTLE
                castle.run();

                // close file
                dataStream.close();
                outputStream.close();

                // output comparison data
                compareOutStream.out.println(version + "," + k + "," + castle.aveInfoLoss);
            }
        }

        compareOutStream.close();
    }

    /**
     * Run versions while varying delta - output to compare-delta.csv
     * 
     * @param dataSet to use for running tests
     */
    public static void varyDelta(String dataSet) {
        String[] versions = { "castle", "castlel", "bcastle", "FADS", "FADSl", "XBAND" };

        // create comparison output
        OutStream compareOutStream = new OutStream(String.format("out_data/compare-delta-%s", dataName));
        compareOutStream.out.println("version,delta,avgInfoLoss");

        // run each version
        System.out.println("------ Vary Delta ----------");
        for (String version : versions) {
            int[] deltas = { 10, 25, 50, 75, 100, 150, 200 };
            for (int delta : deltas) {
                System.out.println("");

                Constants.tOutCount = 0;
                Constants.infoLossSum = 0;
                // predefine thresholds/constants
                int k = 10;
                int beta = 2;
                int omega = 100; // TODO refine this
                int expirationBand = 5;// Gamma

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
                        Constants.variant = "CASTLE - L";
                        castle = new CastleL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "bcastle":
                        // run B-CASTLE
                        Constants.variant = "B-CASTLE";
                        castle = new BCastle(dataStream, k, delta, beta);
                        break;
                    case "FADS":
                        // run FADS (note beta is t_kc)
                        Constants.variant = "FADS";
                        castle = new FADS(dataStream, k, delta, beta);
                        break;
                    case "FADSl":
                        // run FADS with l diversity (note beta is t_kc)
                        Constants.variant = "FADSL";
                        castle = new FADSL(dataStream, k, delta, beta, l, a_s);
                        break;
                    case "XBAND":
                        Constants.variant = "XBAND";
                        castle = new XBAND(dataStream, k, delta, beta, omega, expirationBand);
                        break;

                    default:
                        // run normal castle
                        castle = new Castle(dataStream, k, delta, beta);
                }

                // set DGHs and output stream
                castle.setDGHs(new DGHReader(String.format("%sdgh.txt", dataFolder)).DGHs);

                castle.setOutputStream(outputStream);

                // run CASTLE
                castle.run();

                // close file
                dataStream.close();
                outputStream.close();

                // output comparison data
                compareOutStream.out.println(version + "," + delta + "," + castle.aveInfoLoss);
            }
        }

        compareOutStream.close();
    }
}