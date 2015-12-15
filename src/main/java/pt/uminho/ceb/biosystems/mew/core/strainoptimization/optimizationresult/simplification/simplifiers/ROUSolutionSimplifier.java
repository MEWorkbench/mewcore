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
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;

public class ROUSolutionSimplifier<C extends ISteadyStateConfiguration> extends StrainOptimizationResultsSimplifier<C, ROUSolution> {
	
	public ROUSolutionSimplifier(C configuration) {
		super(configuration);
	}
	
	@Override
	public ROUSolution createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res, List<Double> fitnesses) {
		return new ROUSolution(gc, res, fitnesses);
	}
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions() {
		return new ReactionsSimplifier(configuration.getSimulationConfiguration());
	}
	
	@Override
	public IStrainOptimizationResultSet<C, ROUSolution> createResultSetInstance(List<ROUSolution> resultList) {
		return new ROUSolutionSet<C>(configuration, resultList);
	}
	
}
