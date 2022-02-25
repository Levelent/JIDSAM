package ft_project.generalisation;

public class ContinuousGeneralisation implements GeneralisationInterface {
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
}