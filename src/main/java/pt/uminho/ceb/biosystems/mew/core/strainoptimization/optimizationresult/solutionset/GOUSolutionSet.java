package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GOUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUSolutionSet<T extends  JecoliGenericConfiguration> extends AbstractStrainOptimizationResultSet<T,GOUSolution> {
    
	private static final long	serialVersionUID	= 1L;

	public GOUSolutionSet(T baseConfiguration, List<GOUSolution> resultList) {
        super(baseConfiguration, resultList);
    }

    public GOUSolutionSet(T baseConfiguration) {
        super(baseConfiguration);
    }

    @Override
    public GOUSolution createSolution(JecoliGenericConfiguration baseConfiguration, double[] objectiveFunctionValueArray, GeneticConditions gc) throws Exception {
        GOUSolution newSolution = new GOUSolution(baseConfiguration,gc);
        constructSimulationResultMap(baseConfiguration, newSolution);
        return newSolution;
    }
}
