package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.robustness;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.exceptions.ErrorLog;

public class MFARobustnessResult extends SteadyStateSimulationResult {
	
	private static final long serialVersionUID = 2590966879851025057L;


	/** Map<Flux id, [Objective function values]> 
	 * Each position of the values array, corresponds to the objective function value to the 
	 * simulation with the ith percentage of the wild type flux (key) 
	 * [0] 100%
	 * [1] (100 - percentageInterval)% 
	 * ...
	 * [numberOfIntervals] 0% */
	protected Map<String, double[]> fluxObjectiveValues;
	
	protected int percentageInterval;
	
	protected int numberOfIntervals;
	
	/** Used if the first simulation (the objective one) do not run properly */
	protected ErrorLog errorLog;
	
	public MFARobustnessResult(ISteadyStateModel model, String method, int percentageInterval){
		super(model, method, null);
		this.fluxObjectiveValues = new HashMap<String, double[]>();
		this.percentageInterval = percentageInterval;
		calculateNumberOfIntervals();
	}
	
	public MFARobustnessResult(ISteadyStateModel model, String method, int percentageInterval, int numberOfIntervals){
		super(model, method, null);
		this.fluxObjectiveValues = new HashMap<String, double[]>();
		this.percentageInterval = percentageInterval;
		this.numberOfIntervals = numberOfIntervals;
	}

	
	public void addSolution(String fluxId, int fluxPercentage, double objectiveValue){
		if(!fluxObjectiveValues.containsKey(fluxId))
			fluxObjectiveValues.put(fluxId, new double[numberOfIntervals]);
		int percentageIndex = getSolutionIndex(fluxPercentage);
		fluxObjectiveValues.get(fluxId)[percentageIndex] = objectiveValue;
	}
	
	public void setFluxPercentageSolutions(String fluxId, double[] objectiveValues){
		fluxObjectiveValues.put(fluxId, objectiveValues);
	}
	
	public int getSolutionIndex(int fluxPercentage){
		int n = fluxPercentage / percentageInterval;
		return (fluxPercentage % percentageInterval == 0) ? n+1 : n+2; 
	}

	public int calculateNumberOfIntervals(){
		int n = 100 / percentageInterval;
		this.numberOfIntervals = (100 % percentageInterval == 0) ? n+1 : n+2; 
		return this.numberOfIntervals;
	}
	
	
	public Map<String, double[]> getFluxObjectiveValues() {return fluxObjectiveValues;}
	public void setFluxObjectiveValues(Map<String, double[]> fluxObjectiveValues) {this.fluxObjectiveValues = fluxObjectiveValues;}
	public int getPercentageInterval() {return percentageInterval;}
	public void setPercentageInterval(int percentageInterval) {this.percentageInterval = percentageInterval;}
	public int getNumberOfIntervals(){return numberOfIntervals;}
	public void setNumberOfIntervals(int numberOfIntervals) {this.numberOfIntervals = numberOfIntervals;}
	public ErrorLog getErrorLog() {return errorLog;}
	public void setErrorLog(ErrorLog errorLog) {this.errorLog = errorLog;}
}
