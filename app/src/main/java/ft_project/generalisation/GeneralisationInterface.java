package ft_project.generalisation;

public interface GeneralisationInterface {
    public float infoLoss();
    
    public Boolean updateGeneralisation(String a);
    public Boolean updateGeneralisation(float a, float b); 
}