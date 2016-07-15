package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;

/**
 * Created by ptiago on 03-03-2015.
 */
public class JecoliEACSOMConfig extends JecoliGenericConfiguration {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JecoliEACSOMConfig() {
        super();
        this.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"EA");
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
    
    public double getCrossoverProbability() {
		return getDefaultValue(JecoliOptimizationProperties.CROSSOVER_PROBABILITY, getIsVariableSizeGenome() ? 0.25 : 0.5);
	}
	
	public double getMutationProbability() {
		return getDefaultValue(JecoliOptimizationProperties.MUTATION_PROBABILITY, 0.5);
	}
	
	public double getGrowProbability() {
		return getDefaultValue(JecoliOptimizationProperties.GROW_PROBABILITY, 0.125);
	}
	
	public double getShrinkProbability() {
		return getDefaultValue(JecoliOptimizationProperties.SHRINK_PROBABILITY, 0.125);
	}
	
	public double getMutationRadiusPercentage() {
		return getDefaultValue(JecoliOptimizationProperties.MUTATION_RADIUS_PERCENTAGE, 0.5);
	}

}
