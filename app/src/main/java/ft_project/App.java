package ft_project;

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
        InStream dataStream = new InStream("./app/src/main/resources/adult-100.csv");

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
            default:
                // run normal castle
                castle = new Castle(dataStream, k, delta, beta);
        }

        // set DGHs and output stream
        castle.setDGHs(new DGHReader("./app/src/main/resources/dgh").DGHs);

        castle.setOutputStream(outputStream);

        // run CASTLE
        castle.run();

        // close file
        dataStream.close();
        outputStream.close();
    }
}