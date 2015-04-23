package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public interface IMultipleSolutionsMethod {
	
	public List<SteadyStateSimulationResult> getAllSolutions() throws Exception;

}
