package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class RKSolutionSet<T extends  JecoliGenericConfiguration> extends AbstractStrainOptimizationResultSet<T,RKSolution> {
	
	private static final long	serialVersionUID	= 1L;

	public RKSolutionSet(T baseConfiguration, List<RKSolution> resultList) {
        super(baseConfiguration, resultList);
    }

    @Override
    public RKSolution createSolution(JecoliGenericConfiguration baseConfiguration, double[] objectiveFunctionValueArray, GeneticConditions gc) throws Exception {
        RKSolution newSolution = new RKSolution(baseConfiguration,gc);
        constructSimulationResultMap(baseConfiguration, newSolution);
        return newSolution;
    }



    public RKSolutionSet(T baseConfiguration) {
        super(baseConfiguration);
    }
}
