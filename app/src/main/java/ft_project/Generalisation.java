package ft_project;

public class Generalisation implements Cloneable {

    Generalisation() {
    };

    public float infoLoss() { // should be overridden in extended class
        return 0;
    }

    public Boolean updateGeneralisation() {
        return false;
    }

    public Boolean updateGeneralisation(String s) {
        return false;
    }

    public Boolean updateGeneralisation(float f) {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        Generalisation g = (Generalisation) super.clone();

        return g;
    }
}
