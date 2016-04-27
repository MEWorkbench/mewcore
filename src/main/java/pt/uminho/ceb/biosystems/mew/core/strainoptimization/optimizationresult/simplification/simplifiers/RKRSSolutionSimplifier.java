package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSwapsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISwapsSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKRSSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKRSSolutionSet;

public class RKRSSolutionSimplifier<C extends  ISteadyStateConfiguration & ISwapsSteadyStateConfiguration> extends StrainOptimizationResultsSimplifier<C, RKRSSolution>{

	public RKRSSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public RKRSSolution createSolution(GeneticConditions gc, Map<String,SteadyStateSimulationResult> res, List<Double> fitnesses) {
		return new RKRSSolution(gc, configuration.getReactionSwapMap(), res, fitnesses);
	}
	
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions(){
		return new ReactionsSwapsSimplifier(configuration.getSimulationConfiguration(),configuration.getReactionSwapMap());
	}

	@Override
	public IStrainOptimizationResultSet<C, RKRSSolution> createResultSetInstance(List<RKRSSolution> resultList) {
		return new RKRSSolutionSet<C>(configuration, resultList);
	}

}
