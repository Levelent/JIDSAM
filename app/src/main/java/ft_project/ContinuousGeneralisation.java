package ft_project;

public class ContinuousGeneralisation extends Generalisation implements Cloneable{
    private float UB;
    private float LB;
    private float ub;
    private float lb;

    ContinuousGeneralisation(float LB, float UB, float lb, float ub) {
        this.UB = UB;
        this.LB = LB;

        this.ub = ub;
        this.lb = lb;
    }

    ContinuousGeneralisation(float LB, float UB) {
        this.UB = UB;
        this.LB = LB;

        this.ub = UB;
        this.lb = LB;
    }

    ContinuousGeneralisation(float value) {
        this.UB = 9999999;
        this.LB = 0;

        this.ub = value;
        this.lb = value;
    }

    public float infoLoss() {
        return (this.ub - this.lb) / (this.UB - this.LB);
    }

    public Boolean updateGeneralisation(float lb, float ub) {
        if (lb >= this.LB && ub <= this.UB) {
            this.lb = lb;
            this.ub = ub;
            return true;
        }
        return false;
    }

    public Boolean updateGeneralisation(float data) {
        if(data<=lb && data>=LB){
            lb = data;
            return true;
        }else if(data>=ub && data <=UB){
            ub = data;
            return true;
        }
        return false;
    }

    public String toString(){
        return "[ " +Float.toString(lb) + " " +Float.toString(ub) +" ]";
    }

    public Object clone() throws CloneNotSupportedException{
        ContinuousGeneralisation cg = (ContinuousGeneralisation) super.clone();
        cg.UB = UB;
        cg.LB = LB;
        cg.ub = ub;
        cg.lb = lb;
        return cg;
    }
}