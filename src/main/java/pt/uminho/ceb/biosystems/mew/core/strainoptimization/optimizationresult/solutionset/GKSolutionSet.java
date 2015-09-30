package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gk.GKStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

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
	public GKSolution createSolution(GeneticConditions gc) {
		return new GKSolution(gc);
	}

	@Override
	public IStrainOptimizationReader getSolutionReaderInstance() {
		return new GKStrategyReader(baseConfiguration.getGeneReactionSteadyStateModel());
	}
}