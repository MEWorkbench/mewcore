package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.GenesSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GKSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public class GKSolutionSimplifier<C extends JecoliGenericConfiguration> extends StrainOptimizationResultsSimplifier<C,GKSolution> {

	public GKSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public GKSolution createSolution(GeneticConditions gc, Map<String,SteadyStateSimulationResult> res) {
		return new GKSolution(gc,res);
	}
	
	
	public ISimplifierGeneticConditions getSimplifierGeneticConditions(){
		return new GenesSimplifier(configuration.getGeneReactionSteadyStateModel(), 
								   configuration.getReferenceFluxDistribution(), 
								   configuration.getEnvironmentalConditions(),
								   configuration.getSolver());
	}

	@Override
	public IStrainOptimizationResultSet<C, GKSolution> createResultSetInstance(List<GKSolution> resultList) {
		return new GKSolutionSet<C>(configuration, resultList);
	}

}
