package pt.uminho.ceb.biosystems.mew.core.simulation.fva;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public interface IFluxVariabilityAnalysis {
	
	public void defineRestrictions(EnvironmentalConditions restrictions);
	
	public SteadyStateSimulationResult run(ISteadyStateModel model, boolean isMaximization, String targetFlux) throws Exception ;

}
