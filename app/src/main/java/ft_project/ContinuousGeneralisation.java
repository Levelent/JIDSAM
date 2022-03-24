package ft_project;

public class ContinuousGeneralisation extends Generalisation {
    public float UB;
    public float LB;
    public float ub;
    public float lb;

    /**
     * Constructor for continuous generalisation with custom bounds
     * 
     * @param LB min bound
     * @param UB max bound
     * @param lb current min bound
     * @param ub current max bound
     */
    ContinuousGeneralisation(float LB, float UB, float lb, float ub) {
        this.UB = UB;
        this.LB = LB;

        this.ub = ub;
        this.lb = lb;
    }

    /**
     * Constructor for continuous generalisation with custom bounds
     * 
     * @param LB min bound
     * @param UB max bound
     */
    ContinuousGeneralisation(float LB, float UB) {
        this.UB = UB;
        this.LB = LB;

        this.ub = UB;
        this.lb = LB;
    }

    /**
     * Constructor for continuous generalisation with just value
     * 
     * @param value the starting value
     */
    ContinuousGeneralisation(float value) {
        this.UB = 9999999;
        this.LB = 0;

        this.ub = value;
        this.lb = value;
    }

    /**
     * Get the max generalisation possible
     * 
     * @return string representation of max possible generalisation
     */
    public String getMaxGeneralisation() {
        return "[ " + Float.toString(this.LB) + " " + Float.toString(this.UB) + " ]";
    }

    /**
     * Calculate the current information loss
     * 
     * @return the information loss
     */
    public float infoLoss() {
        return (this.ub - this.lb) / (this.UB - this.LB);
    }

    /**
     * Update the generalisation with new lower and upper bounds
     * 
     * @param lb new lower bound
     * @param ub new upper bound
     * @return if the generalisation could be updated
     */
    public Boolean updateGeneralisation(float lb, float ub) {
        if (lb >= this.LB && ub <= this.UB) {
            this.lb = lb;
            this.ub = ub;
            return true;
        }
        return false;
    }

    /**
     * Update a generalisation bounds based on a new value provided
     * 
     * @param data value to update the generalisation with
     * @return if generalisation bounds were widen
     */
    public Boolean updateGeneralisation(float data) {
        if (data <= lb && data >= LB) {
            lb = data;
            return true;
        } else if (data >= ub && data <= UB) {
            ub = data;
            return true;
        }
        return false;
    }

    public Boolean setGeneralisation(float value) {
        this.UB = 9999999;
        this.LB = 0;

        this.ub = value;
        this.lb = value;
        return true;
    }

    public Boolean setGeneralisation(CategoryGeneralisation g) {
        return false;
    }

    public Boolean setGeneralisation(ContinuousGeneralisation g) {
        this.LB = g.LB;
        this.UB = g.UB;
        this.lb = g.lb;
        this.ub = g.ub;
        return true;
    }

    /**
     * Get the string representation of the generalisation
     * 
     * @return string representation of generalisation
     */
    public String toString() {
        return "[ " + Float.toString(lb) + " " + Float.toString(ub) + " ]";
    }

    /**
     * Deep clone the generalisation
     * 
     * @return cloned generalisation
     */
    public Object clone() throws CloneNotSupportedException {
        ContinuousGeneralisation cg = (ContinuousGeneralisation) super.clone();
        cg.UB = UB;
        cg.LB = LB;
        cg.ub = ub;
        cg.lb = lb;
        return cg;
    }
}