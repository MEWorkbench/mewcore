package pt.uminho.ceb.biosystems.mew.core.optimization.components;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;

public class SteadyStateMultiSimulationSet {

	
	protected Map<String, SteadyStateMultiSimulationResult> simulationsMap;
		
	public SteadyStateMultiSimulationSet(){		
		getSimulationsMap();
	}

	public String addSimulationResult(SteadyStateMultiSimulationResult simulation){
		int numSols = getNumberOfResults();
		
		String simID = "Simulation_"+numSols;
		simulationsMap.put(simID, simulation);
		
		return simID;
	}


	/**
	 * @return the simulationMap
	 */
	public Map<String, SteadyStateMultiSimulationResult> getSimulationsMap() {
		if(simulationsMap==null)
			simulationsMap = new HashMap<String, SteadyStateMultiSimulationResult>();
		return simulationsMap;
	}
	
	public SteadyStateMultiSimulationResult getSimulationResult(String id) throws Exception{
		SteadyStateMultiSimulationResult res = simulationsMap.get(id);
		if(res==null)
			throw new Exception("Non existent simulation solution with identifier ["+id+"]");
		
		return res;
	}
	
	public int getNumberOfResults(){
		return this.getSimulationsMap().size();
	}
}
