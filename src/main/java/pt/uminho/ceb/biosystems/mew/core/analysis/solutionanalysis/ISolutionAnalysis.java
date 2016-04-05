package pt.uminho.ceb.biosystems.mew.core.analysis.solutionanalysis;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public interface ISolutionAnalysis {
	
	String[] getDescriptions();
	double[] analyse(SteadyStateSimulationResult result);

}
