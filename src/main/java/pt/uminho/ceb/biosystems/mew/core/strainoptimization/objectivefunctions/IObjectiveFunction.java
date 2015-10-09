package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.io.Serializable;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public interface IObjectiveFunction extends Serializable {
	
	double evaluate(SteadyStateSimulationResult simResult);
	
	double getWorstFitness();
	
	boolean isMaximization();
	
	double getUnnormalizedFitness(double fit);
	
	String getID();
	
	String getShortString();
	
	String getLatexString();
	
	String getLatexFormula();
	
	String getBuilderString();
	
	void validate(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration;
	
	Map<String, ObjectiveFunctionParameterType> mandatoryParameters();
	
	Map<String, Object> copyConfiguration();
	
	Map<String, ObjectiveFunctionParameterType> loadParameters();
	
}
