package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;

public class GeneticConditionSimplifiedResult implements IGeneticConditionsSimplifiedResult {
	
	protected List<GeneticConditions>					gc;
	protected List<SteadyStateMultiSimulationResult>	results;
	protected List<List<Double>>						fitnesses;
														
	public GeneticConditionSimplifiedResult(List<GeneticConditions> gc, List<SteadyStateMultiSimulationResult> results, List<List<Double>> fitnesses) {
		this.gc = gc;
		this.results = results;
		this.fitnesses = fitnesses;
	}
	
	public GeneticConditionSimplifiedResult(GeneticConditions gc, SteadyStateMultiSimulationResult result, List<Double> fitnesses) {
		List<GeneticConditions> gcs = new ArrayList<GeneticConditions>();
		List<SteadyStateMultiSimulationResult> results = new ArrayList<SteadyStateMultiSimulationResult>();
		List<List<Double>> fitnessesList = new ArrayList<List<Double>>();
		gcs.add(gc);
		results.add(result);
		fitnessesList.add(fitnesses);
		this.gc = gcs;
		this.results = results;
		this.fitnesses = fitnessesList;
	}
	
	public void addSolution(GeneticConditions gc, SteadyStateMultiSimulationResult result, List<Double> fitnesses) {
		this.gc.add(gc);
		this.results.add(result);
		this.fitnesses.add(fitnesses);
	}
	
	public void addAll(IGeneticConditionsSimplifiedResult result) {
		for (int i = 0; i < result.size(); i++) {
			addSolution(result.getSimplifiedGeneticConditions().get(i), result.getSimplifiedSimulationResults().get(i), result.getSimplifiedFitnesses().get(i));
		}
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
	public List<List<Double>> getSimplifiedFitnesses() {
		return fitnesses;
	}
	
	@Override
	public int size() {
		return gc.size();
	}
	
}
