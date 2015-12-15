package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.GenesSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGeneSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GOUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GOUSolutionSet;

public class GOUSolutionSimplifier<C extends  ISteadyStateConfiguration & IGeneSteadyStateConfiguration> extends StrainOptimizationResultsSimplifier<C,GOUSolution>{

	public GOUSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public GOUSolution createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res, List<Double> fitnesses) {
		return new GOUSolution(gc,res,fitnesses);
	}

	@Override
	public ISimplifierGeneticConditions getSimplifierGeneticConditions() {
		return new GenesSimplifier(configuration.getSimulationConfiguration());
	}
	
	@Override
	public IStrainOptimizationResultSet<C, GOUSolution> createResultSetInstance(List<GOUSolution> resultList) {
		return new GOUSolutionSet<C>(configuration, resultList);
	}

}
