package ft_project.generalisation;

import ft_project.DGH;

public class CategoryGeneralisation implements GeneralisationInterface {

    private DGH dgh;
    private String localRoot; // The name of the node the generalisation is locally rooted

    CategoryGeneralisation(DGH dgh) {
        this.dgh = dgh;
    }

    public float infoLoss() {
        return (dgh.countNodes(localRoot) - 1) / (dgh.countNodes() - 1);
    }

    public Boolean updateGeneralisation(String localRoot) {
        if (dgh.contains(localRoot)) {
            this.localRoot = localRoot;
            return true;
        }
        return false;
    }

}