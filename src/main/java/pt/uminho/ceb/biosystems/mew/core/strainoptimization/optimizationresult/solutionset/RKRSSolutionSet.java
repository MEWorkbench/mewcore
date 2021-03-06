package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.util.HashMap;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISwapsSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rkrs.RKRSStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKRSSolution;

/**
 * Created by ptiago on 24-03-2015.
 */
public class RKRSSolutionSet<T extends IGenericConfiguration & ISwapsSteadyStateConfiguration> extends AbstractStrainOptimizationResultSet<T,RKRSSolution> {
    
	private static final long	serialVersionUID	= 1L;

	public RKRSSolutionSet(T baseConfiguration, List<RKRSSolution> resultList) {
        super(baseConfiguration, resultList);
    }

    @Override
    public RKRSSolution createSolution(GeneticConditions gc) {
        return new RKRSSolution(gc,baseConfiguration.getReactionSwapMap());
    }
    
    @Override
   	public RKRSSolution createSolution(GeneticConditions gc, List<Double> attributes) {
   		return new RKRSSolution(gc, baseConfiguration.getReactionSwapMap(), new HashMap<String,SteadyStateSimulationResult>(), attributes);
   	}

    public RKRSSolutionSet(T baseConfiguration) {
        super(baseConfiguration);
    }

	@Override
	public IStrainOptimizationReader getSolutionReaderInstance() {
		return new RKRSStrategyReader();
	}
}