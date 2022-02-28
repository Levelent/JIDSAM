package ft_project;

import ft_project.DGH;

public class CategoryGeneralisation extends Generalisation implements Cloneable{

    private DGH dgh;
    private String localRoot; // The name of the node the generalisation is locally rooted

    CategoryGeneralisation(DGH dgh) {
        this.dgh = dgh;
    }

    CategoryGeneralisation(DGH dgh, String localRoot) {
        this.dgh = dgh;
        this.localRoot = localRoot; //TODO: Throw an error if localRoot doesnt exist in the dgh
    }

    public float infoLoss() {
        return (dgh.countNodes(localRoot) - 1) / (dgh.countNodes() - 1);
    }

    public Boolean updateGeneralisation(String gen) {
        String newLocalRoot = dgh.findCommonAncestor(localRoot, gen);

        if(newLocalRoot == null){
            return false; 
        }
        localRoot = newLocalRoot;
        return true;
    }

    public String toString(){
        return localRoot;

    }

    public Object clone() throws CloneNotSupportedException{
        CategoryGeneralisation cg = (CategoryGeneralisation) super.clone();

        cg.dgh = dgh;
        cg.localRoot = new String(localRoot);

        return cg;
    }

}