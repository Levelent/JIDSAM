package ft_project;

public class Generalisation implements Cloneable {

    /**
     * Empty constructor
     */
    Generalisation() {
    };

    /**
     * Get the information loss associated with generalisation
     * - should be overridden in extended class
     * 
     * @return the information loss
     */
    public float infoLoss() {
        return 0;
    }

    /**
     * Update the generalisation
     * 
     * @return true if generalisation widen
     */
    public Boolean updateGeneralisation() {
        return false;
    }

    /**
     * Update the generalisation
     * 
     * @return true if generalisation widen
     */
    public Boolean updateGeneralisation(String s) {
        return false;
    }

    /**
     * Update the generalisation
     * 
     * @return true if generalisation widen
     */
    public Boolean updateGeneralisation(float f) {
        return false;
    }

    /**
     * Deep clone the generalisation
     * 
     * @return cloned generalisation
     */
    public Object clone() throws CloneNotSupportedException {
        return (Generalisation) super.clone();
    }
}
