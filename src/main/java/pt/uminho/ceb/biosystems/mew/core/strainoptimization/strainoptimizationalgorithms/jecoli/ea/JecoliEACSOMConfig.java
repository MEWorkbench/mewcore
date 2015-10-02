package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;

/**
 * Created by ptiago on 03-03-2015.
 */
public class JecoliEACSOMConfig extends JecoliGenericConfiguration{


    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;


	public JecoliEACSOMConfig() {
        super();
        this.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,"EA");
    }

    public int getPopulationSize() {
        return getDefaultValue(JecoliOptimizationProperties.POPULATION_SIZE, 100);
    }



    public int getNumberOfElitistIndividuals() {
        return getDefaultValue(JecoliOptimizationProperties.ELITISM,1);
    }

    public int getNumberOfSurvivors() {
        return getDefaultValue(JecoliOptimizationProperties.NUMBER_OF_SURVIVORS, 49);
    }


    public int getOffSpringSize() {
        return getDefaultValue(JecoliOptimizationProperties.OFFSPRING_SIZE,50);
    }


}
