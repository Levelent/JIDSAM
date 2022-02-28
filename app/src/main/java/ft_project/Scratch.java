package ft_project;
import java.io.*;
import java.util.*;

public class Scratch {
    public static void main(String[] args){

       
        // create data stream
        Stream dataStream = new Stream("./app/src/main/resources/adult.csv");
        Map<String, DGH> DGHs = new DGHReader("./app/src/main/resources/dgh").DGHs;
        //System.out.println(DGHs);
        Cluster c = new Cluster(dataStream.next(), DGHs);
        c.add(dataStream.next());
        c.add(dataStream.next());


        Cluster b = null;
        try{
            b = (Cluster) c.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }

        c.add(dataStream.next());
        c.add(dataStream.next());

        System.out.println( c );
        System.out.println( "\n\n");
        System.out.println( b );

    }   
}
