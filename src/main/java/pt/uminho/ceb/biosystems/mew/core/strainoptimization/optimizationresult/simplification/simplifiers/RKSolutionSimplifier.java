package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKSolutionSet;

public class RKSolutionSimplifier<C extends ISteadyStateConfiguration> extends StrainOptimizationResultsSimplifier<C, RKSolution> {
	
	public RKSolutionSimplifier(C configuration) {
		super(configuration);
	}
	
	@Override
	public RKSolution createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res, List<Double> fitnesses) {
		return new RKSolution(gc, res, fitnesses);
	}
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions() {
		return new ReactionsSimplifier(configuration.getSimulationConfiguration());
	}
	
	@Override
	public IStrainOptimizationResultSet<C, RKSolution> createResultSetInstance(List<RKSolution> resultList) {
		return new RKSolutionSet<C>(configuration, resultList);
	}
	
}
