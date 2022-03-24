package ft_project;

public class CategoryGeneralisation extends Generalisation {

    private DGH dgh;
    private String localRoot; // The name of the node the generalisation is locally rooted

    /**
     * Constructor using general dgh
     * 
     * @param dgh tree
     */
    CategoryGeneralisation(DGH dgh) {
        this.dgh = dgh;
    }

    /**
     * Constructor using dgh with a given start point
     * 
     * @param dgh       tree
     * @param localRoot the start point in the dgh
     */
    CategoryGeneralisation(DGH dgh, String localRoot) {
        this.dgh = dgh;
        this.localRoot = localRoot;
    }

    /**
     * Calculate the information loss between the local root and the dgh
     * 
     * @return the calculated information loss
     */
    public float infoLoss() {
        return (float) (dgh.countNodes(localRoot) - 1) / (dgh.countNodes() - 1);
    }

    /**
     * Updates the generalisation based on additional information
     * 
     * @param gen new value to be included into the generalisation
     * @return if the generalisation could be added
     */
    public Boolean updateGeneralisation(String gen) {
        String newLocalRoot = dgh.findCommonAncestor(localRoot, gen);
        if (newLocalRoot == null) {
            return false;
        }

        localRoot = newLocalRoot;
        return true;
    }

    public Boolean setGeneralisation(String gen) {
        // TODO check gen exists
        this.localRoot = gen;
        return true;
    }

    public Boolean setGeneralisation(CategoryGeneralisation g) {
        this.localRoot = g.localRoot;
        return true;
    }

    public Boolean setGeneralisation(ContinuousGeneralisation g) {
        return false;
    }

    /**
     * Return the string representation of the generalisation
     * 
     * @return the the local root of the dgh
     */
    public String toString() {
        return localRoot;
    }

    /**
     * Deep clone the generalisation
     * 
     * @return cloned version of generalisation
     */
    public Object clone() throws CloneNotSupportedException {
        CategoryGeneralisation cg = (CategoryGeneralisation) super.clone();

        cg.dgh = dgh;
        cg.localRoot = new String(localRoot);

        return cg;
    }
}