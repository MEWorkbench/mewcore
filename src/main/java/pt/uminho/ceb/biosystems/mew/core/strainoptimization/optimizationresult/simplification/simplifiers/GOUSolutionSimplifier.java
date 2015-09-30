package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplificationnew.GenesSimplifier;
import pt.uminho.ceb.biosystems.mew.core.simplificationnew.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GOUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GOUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public class GOUSolutionSimplifier<C extends JecoliGenericConfiguration> extends StrainOptimizationResultsSimplifier<C,GOUSolution>{

	public GOUSolutionSimplifier(C configuration) {
		super(configuration);
	}

	@Override
	public GOUSolution createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res) {
		return new GOUSolution(gc,res);
	}

	@Override
	public ISimplifierGeneticConditions getSimplifierGeneticConditions() {
		return new GenesSimplifier(configuration.getGeneReactionSteadyStateModel(),
								   configuration.getReferenceFluxDistribution(),
								   configuration.getEnvironmentalConditions(),
								   configuration.getSolver());
	}
	
	@Override
	public IStrainOptimizationResultSet<C, GOUSolution> createResultSetInstance(List<GOUSolution> resultList) {
		return new GOUSolutionSet<C>(configuration, resultList);
	}

}
