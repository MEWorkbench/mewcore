/**
 * 
 */
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

/**
 * @author pmaia
 *
 */
public class SteadyStateOptimizationResult extends SteadyStateSimulationSet {

	private static final long serialVersionUID = 1L;
	ISteadyStateModel model;
	AlgorithmTypeEnum algorithmType;
	protected Map<String,ArrayList<Double>> fitnessesMap;
	protected List<IObjectiveFunction> objectiveFunctions;
	
	public SteadyStateOptimizationResult(){
		super();
		getFitnessesMap();
	}
		
	public SteadyStateOptimizationResult(
			ISteadyStateModel model,
			List<IObjectiveFunction> objectiveFunctions			
			){
		
		super();		
		this.model = model;
		getFitnessesMap();
		this.objectiveFunctions = objectiveFunctions;
	}
	
	
	public String addOptimizationResult(SteadyStateSimulationResult result,ArrayList<Double> fitnesses){
		String id = addSimulationResult(result);
		fitnessesMap.put(id, fitnesses);
		
		return id;
	}
	
	public String addOptimizationResultNoRepeated(SteadyStateSimulationResult result, ArrayList<Double> fitnesses){
		String id = "null";
				
		for(SteadyStateSimulationResult res : this.getSimulationMap().values())
			if(res.getGeneticConditions().equals(result.getGeneticConditions()))
				return id;
		
		return addOptimizationResult(result, fitnesses);
	}
	
	public Pair<SteadyStateSimulationResult,ArrayList<Double>> getOptimizationResultPair(String id) throws Exception{
		
		SteadyStateSimulationResult value = getSimulationResult(id);
		ArrayList<Double> pairValue = getFitnessForOptimizationResult(id);
		
		Pair<SteadyStateSimulationResult,ArrayList<Double>> toret = new Pair<SteadyStateSimulationResult, ArrayList<Double>>(value, pairValue);
		
		return toret;
	}
	
	public ArrayList<Double> getFitnessForOptimizationResult(String id) throws Exception{
		ArrayList<Double> toret = fitnessesMap.get(id);
		
		if(toret==null)
			throw new Exception("Nonexistent fitness list for optimization result with id ["+id+"]");
		
		return toret;
	}
	
	/**
	 * The Objective Functions List
	 * 
	 * @return objectiveFunctions
	 */
	public List<IObjectiveFunction> getObjectiveFunctionsList() {			
		return objectiveFunctions;
	}

	/**
	 * Singleton instance of the Fitnesses Map
	 * 
	 * @return fitnessesMap
	 */
	public Map<String,ArrayList<Double>> getFitnessesMap(){
		if(fitnessesMap==null)
			fitnessesMap = new HashMap<String, ArrayList<Double>>();
		return fitnessesMap;
	}
	
	/**
	 * Returns the <code>ISteadyStateModel</code> associated with this optimization process
	 * 
	 * @return model
	 */
	public ISteadyStateModel getModel(){
		return model;
	}
	
	public void writeToFile(String file, String delimiter) throws IOException{
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(String simID : this.simulationMap.keySet()){
			SteadyStateSimulationResult res = simulationMap.get(simID);
			List<Pair<String,Double>> pairs = null;
			
			if(res.getGeneList()!=null)
				pairs = res.getGeneList().getPairsList();
			else pairs = res.getReactionList().getPairsList();

			ArrayList<Double> fits = fitnessesMap.get(simID);
			
			StringBuffer towrite = new StringBuffer();
			
			// fits
			for(Double d: fits)
				towrite.append(d+delimiter);
			
			// solutions
			for(Pair<String,Double> sol : pairs){
				towrite.append(delimiter+sol.getValue()+"="+sol.getPairValue());
			}
			
			bw.append(towrite.toString());
			bw.newLine();
		}

		bw.flush();
		fw.flush();
		bw.close();			
		fw.close();		
	}

}
