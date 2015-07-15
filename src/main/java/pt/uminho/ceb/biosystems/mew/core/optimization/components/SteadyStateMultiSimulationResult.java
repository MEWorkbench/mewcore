package pt.uminho.ceb.biosystems.mew.core.optimization.components;

import java.io.Serializable;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public class SteadyStateMultiSimulationResult implements Serializable{

	private static final long serialVersionUID = 1213026953127054960L;
	
	protected String solutionID;
	
	protected GeneticConditions geneticConditions;
	
	protected EnvironmentalConditions environmentalConditions;
	
	protected Map<String,SteadyStateSimulationResult> simulations;
	
	public SteadyStateMultiSimulationResult(
			String solutionID,
			GeneticConditions geneticConditions,
			EnvironmentalConditions environmentalConditions,
			Map<String,SteadyStateSimulationResult> simulations){
		
		this.solutionID = solutionID;
		this.geneticConditions = geneticConditions;
		this.environmentalConditions = environmentalConditions;
		this.simulations = simulations;
		
	};
	
	
	
	public SteadyStateSimulationResult getSimulationResultForMethod(String simulationMethod){
		return simulations.get(simulationMethod);
	}



	/**
	 * @return the solutionID
	 */
	public String getSolutionID() {
		return solutionID;
	}



	/**
	 * @param solutionID the solutionID to set
	 */
	public void setSolutionID(String solutionID) {
		this.solutionID = solutionID;
	}



	/**
	 * @return the geneticConditions
	 */
	public GeneticConditions getGeneticConditions() {
		return geneticConditions;
	}



	/**
	 * @param geneticConditions the geneticConditions to set
	 */
	public void setGeneticConditions(GeneticConditions geneticConditions) {
		this.geneticConditions = geneticConditions;
	}



	/**
	 * @return the environmentalConditions
	 */
	public EnvironmentalConditions getEnvironmentalConditions() {
		return environmentalConditions;
	}



	/**
	 * @param environmentalConditions the environmentalConditions to set
	 */
	public void setEnvironmentalConditions(
			EnvironmentalConditions environmentalConditions) {
		this.environmentalConditions = environmentalConditions;
	}



	/**
	 * @return the simulations
	 */
	public Map<String, SteadyStateSimulationResult> getSimulations() {
		return simulations;
	}



	/**
	 * @param simulations the simulations to set
	 */
	public void setSimulations(Map<String, SteadyStateSimulationResult> simulations) {
		this.simulations = simulations;
	}

}
