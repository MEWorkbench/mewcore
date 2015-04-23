/**
 * 
 */
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

/**
 * @author pmaia
 *
 */
public class SteadyStateSimulationSet implements Serializable {
	
	protected Map<String,SteadyStateSimulationResult> simulationMap;
	
	public SteadyStateSimulationSet(){
		getSimulationMap();
	}
	
	
	public String addSimulationResult(SteadyStateSimulationResult simulation){
		int numSols = getNumberOfResults();
		String simID = "Simulation_"+numSols;
		simulationMap.put(simID, simulation);
		
		return simID;
	}


	/**
	 * @return the simulationMap
	 */
	public Map<String, SteadyStateSimulationResult> getSimulationMap() {
		if(simulationMap==null)
			simulationMap = new HashMap<String, SteadyStateSimulationResult>();
		return simulationMap;
	}
	
	public SteadyStateSimulationResult getSimulationResult(String id) throws Exception{
		SteadyStateSimulationResult res = simulationMap.get(id);
		if(res==null)
			throw new Exception("Non existent simulation solution with identifier ["+id+"]");
		
		return res;
	}
	
	public int getNumberOfResults(){
		return this.getSimulationMap().size();
	}
	
	

}
