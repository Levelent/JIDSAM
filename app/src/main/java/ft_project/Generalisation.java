package ft_project;
import java.util.*;

interface Generalisation{
    public float infoLoss();
    public Boolean updateGeneralisation();
}

public class categoryGen implements Generalisation{

    private DGH dgh;
    private String localRoot; //The name of the node the generalisation is locally rooted

    categoryGen(DGH dgh){
        this.dgh = dgh;
    }

    public float infoLoss(){
        return (dgh.countNodes(localRoot) -1)/(dgh.countNodes() - 1);
    }
    
    public Boolean updateGeneralisation(String localRoot){
        if(dgh.contains(localRoot)){
            this.localRoot = localRoot;
            return true;
        }
        return false;
    }

}

public class continuousGen implements Generalisation{

    private float UB;
    private float LB;
    private float ub;
    private float lb;

    continuousGen(float LB, float UB, float lb, float ub){
        this.UB = UB;
        this.LB = LB;

        this.ub =ub;
        this.lb = lb;
    }

    public float infoLoss(){
        return (this.ub-this.lb)/(this.UB-this.LB);
    }

    public Boolean updateGeneralisation(float lb, float ub){
        if(lb>=this.LB && ub <=this.UB){
            this.lb = lb;
            this.ub = ub;
            return true;
        }
        return false;
    }
}