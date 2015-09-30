package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;

public class GeneticConditionSimplifiedResult implements IGeneticConditionsSimplifiedResult{
	
	protected List<GeneticConditions> gc;
	protected List<SteadyStateMultiSimulationResult> results;		
	
	public GeneticConditionSimplifiedResult(List<GeneticConditions> gc, List<SteadyStateMultiSimulationResult> results ) {
		this.gc = gc;
		this.results = results;
	}
	
	public GeneticConditionSimplifiedResult(GeneticConditions gc, SteadyStateMultiSimulationResult result) {
		List<GeneticConditions> gcs = new ArrayList<GeneticConditions>();
		List<SteadyStateMultiSimulationResult> results = new ArrayList<SteadyStateMultiSimulationResult>();
		gcs.add(gc);
		results.add(result);
		this.gc = gcs;
		this.results = results;
	}
	
	@Override
	public List<SteadyStateMultiSimulationResult> getSimplifiedSimulationResults() {
		return results;
	}

	@Override
	public List<GeneticConditions> getSimplifiedGeneticConditions() {
		return gc;
	}

	@Override
	public int size() {
		return gc.size();
	}
}
