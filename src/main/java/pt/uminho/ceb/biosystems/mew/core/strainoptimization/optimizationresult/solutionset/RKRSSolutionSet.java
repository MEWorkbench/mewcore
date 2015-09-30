package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rkrs.RKRSStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKRSSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 24-03-2015.
 */
public class RKRSSolutionSet<T extends JecoliGenericConfiguration> extends AbstractStrainOptimizationResultSet<T,RKRSSolution> {
    
	private static final long	serialVersionUID	= 1L;

	public RKRSSolutionSet(T baseConfiguration, List<RKRSSolution> resultList) {
        super(baseConfiguration, resultList);
    }

    @Override
    public RKRSSolution createSolution(GeneticConditions gc) {
        return new RKRSSolution(gc,baseConfiguration.getReactionSwapMap());
    }

    public RKRSSolutionSet(T baseConfiguration) {
        super(baseConfiguration);
    }

	@Override
	public IStrainOptimizationReader getSolutionReaderInstance() {
		return new RKRSStrategyReader();
	}
}