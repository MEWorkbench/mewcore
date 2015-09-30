 package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;

public interface IGeneticConditionsSimplifiedResult {
	
	List<SteadyStateMultiSimulationResult> getSimplifiedSimulationResults();
	
	List<GeneticConditions> getSimplifiedGeneticConditions();
	
	int size();

}
