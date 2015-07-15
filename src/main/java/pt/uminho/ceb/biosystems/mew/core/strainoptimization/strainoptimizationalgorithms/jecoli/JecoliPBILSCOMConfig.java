package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

/**
 * Created by ptiago on 10-03-2015.
 */
public class JecoliPBILSCOMConfig extends JecoliGenericConfiguration{

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliPBILSCOMConfig() {
        super();
        this.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,"PBIL");
    }

    public double getLearningRate() {
        return getDefaultValue(JecoliOptimizationProperties.LEARNING_RATE,0.01);
    }

    public int getNumberOfSolutionsToSelect() {
        return getDefaultValue(JecoliOptimizationProperties.NUMBER_OF_SOLUTIONS_TO_SELECT,3);
    }

    public int getPopulationSize() {
        return getDefaultValue(JecoliOptimizationProperties.POPULATION_SIZE, 5);
    }

    public int getNumberOfSolutionElements() {
        return getDefaultValue(JecoliOptimizationProperties.NUMBER_OF_SOLUTION_ELEMENTS, 5);
    }

    public int getIndividualSize() {
        return getDefaultValue(JecoliOptimizationProperties.INDIVIDUAL_SIZE, 5);
    }

    public double[] getInitialProbabilityVector() {
        double[] probabilityVector = (double[]) propertyMap.get(JecoliOptimizationProperties.INITIAL_PROPERTY_VECTOR);
        if(probabilityVector == null){
            probabilityVector = new double[getNumberOfSolutionElements()];
            for(int i = 0 ;i < probabilityVector.length;i++)
                probabilityVector[i] = 0.5;
            return probabilityVector;
        }
        return probabilityVector;
    }
}
