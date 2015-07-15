package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public interface IMultipleSolutionsMethod {
	
	public List<SteadyStateSimulationResult> getAllSolutions() throws Exception;

}
