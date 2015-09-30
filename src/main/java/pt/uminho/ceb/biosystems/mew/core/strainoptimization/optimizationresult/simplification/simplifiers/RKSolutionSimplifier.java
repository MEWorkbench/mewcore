package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ReactionsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public class RKSolutionSimplifier<C extends JecoliGenericConfiguration> extends StrainOptimizationResultsSimplifier<C, RKSolution>{

	public RKSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public RKSolution createSolution(GeneticConditions gc, Map<String,SteadyStateSimulationResult> res) {
		return new RKSolution(gc,res);
	}
	
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions(){
		return new ReactionsSimplifier(configuration.getSteadyStateModel(),
									   configuration.getReferenceFluxDistribution(),
									   configuration.getEnvironmentalConditions(),
									   configuration.getSolver());
	}

	@Override
	public IStrainOptimizationResultSet<C, RKSolution> createResultSetInstance(List<RKSolution> resultList) {
		return new RKSolutionSet<C>(configuration, resultList);
	}

}
