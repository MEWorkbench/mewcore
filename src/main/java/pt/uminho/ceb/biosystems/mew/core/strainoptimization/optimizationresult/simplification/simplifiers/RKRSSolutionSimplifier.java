package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKRSSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKRSSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public class RKRSSolutionSimplifier<C extends JecoliGenericConfiguration> extends StrainOptimizationResultsSimplifier<C, RKRSSolution>{

	public RKRSSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public RKRSSolution createSolution(GeneticConditions gc, Map<String,SteadyStateSimulationResult> res) {
		return new RKRSSolution(gc, configuration.getReactionSwapMap(), res);
	}
	
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions(){
		return new ReactionsSimplifier(configuration.getSteadyStateModel(),
									   configuration.getReferenceFluxDistribution(),
									   configuration.getEnvironmentalConditions(),
									   configuration.getSolver());
	}

	@Override
	public IStrainOptimizationResultSet<C, RKRSSolution> createResultSetInstance(List<RKRSSolution> resultList) {
		return new RKRSSolutionSet<C>(configuration, resultList);
	}

}
