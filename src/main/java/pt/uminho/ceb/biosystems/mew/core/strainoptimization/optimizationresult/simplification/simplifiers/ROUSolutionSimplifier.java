package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public class ROUSolutionSimplifier<C extends JecoliGenericConfiguration> extends StrainOptimizationResultsSimplifier<C, ROUSolution>{

	public ROUSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public ROUSolution createSolution(GeneticConditions gc, Map<String,SteadyStateSimulationResult> res) {
		return new ROUSolution(gc,res);
	}
	
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions(){
		return new ReactionsSimplifier(configuration.getSteadyStateModel(),
									   configuration.getReferenceFluxDistribution(),
									   configuration.getEnvironmentalConditions(),
									   configuration.getSolver());
	}

	@Override
	public IStrainOptimizationResultSet<C, ROUSolution> createResultSetInstance(List<ROUSolution> resultList) {
		return new ROUSolutionSet<C>(configuration, resultList);
	}

}
