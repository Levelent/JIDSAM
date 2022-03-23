package ft_project;

import java.text.DecimalFormat;

public class Constants {
    public static String variant = "CASTLE";
    public static Boolean verbose = false;
    public static int tOutCount = 0;
    public static int streamSize = 100;
    public static float infoLossSum = 0;

    public static void setV(Boolean value) {
        verbose = value;
    }

    public static int incrementOut(int i) {
        tOutCount += i;
        return tOutCount;
    }

    public static int incrementOut(int i, Cluster c) {
        tOutCount += i;
        infoLossSum += c.informationLoss();
        return tOutCount;
    }

    public static void addInfoLoss(float loss) {
        infoLossSum += loss;
    }

    public static void setStreamSize(int size) {
        streamSize = size;
    }

    public static float aveInfoLoss() {
        return (float) infoLossSum / tOutCount;
    }

    public static void outputProgress() {
        if (!verbose) {
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.print("Tuples Processed: ");
            System.out.print(df.format(((float) tOutCount / streamSize) * 100f));
            System.out.print("%");
            System.out.print("    Information Loss: ");
            System.out.print(df.format(Constants.aveInfoLoss()));
            System.out.print('\r');
        }

    }
}
