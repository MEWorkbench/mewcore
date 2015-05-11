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
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * @author pmaia
 *
 */
public class SteadyStateMTOptimizationResult extends SteadyStateMultiSimulationSet{
	
	ISteadyStateModel model;
	AlgorithmTypeEnum algorithmType;
	protected Map<String,ArrayList<Double>> fitnessesMap;
	protected Map<IObjectiveFunction,String> objectiveFunctions;
	
	public SteadyStateMTOptimizationResult(){
		super();
		getFitnessesMap();
	}
		
	public SteadyStateMTOptimizationResult(
			ISteadyStateModel model,
			Map<IObjectiveFunction,String> objectiveFunctions			
			){
		
		super();		
		this.model = model;
		getFitnessesMap();
		this.objectiveFunctions = objectiveFunctions;
	}
	
	
	public String addOptimizationResult(SteadyStateMultiSimulationResult result, ArrayList<Double> fitnesses){
		String id = addSimulationResult(result);
		fitnessesMap.put(id, fitnesses);
		
		return id;
	}
	
	public String addOptimizationResultNoRepeated(SteadyStateMultiSimulationResult result, ArrayList<Double> fitnesses){
		String id = "null";
				
		boolean containsAll = true;
		boolean sameGC = true;
		for(SteadyStateMultiSimulationResult res : this.getSimulationsMap().values())
			if(res.getGeneticConditions().equals(result.getGeneticConditions())){
				for(String method : result.getSimulations().keySet())
					if(!res.simulations.keySet().contains(method))
						containsAll = false;										
			}else
				sameGC = false;
		
		if(sameGC && containsAll)			
			return id;
		else
			return addOptimizationResult(result, fitnesses);
	}
	
	public Pair<SteadyStateMultiSimulationResult,ArrayList<Double>> getOptimizationResultPair(String id) throws Exception{
		
		SteadyStateMultiSimulationResult value = getSimulationResult(id);
		ArrayList<Double> pairValue = getFitnessForOptimizationResult(id);
		
		Pair<SteadyStateMultiSimulationResult,ArrayList<Double>> toret = new Pair<SteadyStateMultiSimulationResult, ArrayList<Double>>(value, pairValue);
		
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
	public Map<IObjectiveFunction,String> getObjectiveFunctionsList() {			
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
		
		for(String simID : this.simulationsMap.keySet()){
			SteadyStateMultiSimulationResult res = simulationsMap.get(simID);
			List<Pair<String,Double>> pairs = null;
			
			if(res.getGeneticConditions().getGeneList()!=null)
				pairs = res.getGeneticConditions().getGeneList().getPairsList();
			else pairs = res.getGeneticConditions().getReactionList().getPairsList();

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
