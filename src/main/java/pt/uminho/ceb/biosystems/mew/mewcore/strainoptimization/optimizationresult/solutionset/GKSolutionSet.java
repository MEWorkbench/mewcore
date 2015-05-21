package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solutionset;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solution.GKSolution;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GKSolutionSet<T extends  JecoliGenericConfiguration> extends AbstractStrainOptimizationResultSet<T,GKSolution> {
	
    
	private static final long	serialVersionUID	= 1L;

	public GKSolutionSet(T baseConfiguration, List<GKSolution> resultList) {
        super(baseConfiguration, resultList);
    }

    public GKSolutionSet(T baseConfiguration) {
        super(baseConfiguration);
    }

    @Override
    public GKSolution createSolution(JecoliGenericConfiguration baseConfiguration, double[] objectiveFunctionValueArray, GeneticConditions gc) throws Exception {
        GKSolution newSolution = new GKSolution(baseConfiguration,gc);
        constructSimulationResultMap(baseConfiguration,newSolution);
        return newSolution;
    }
}
