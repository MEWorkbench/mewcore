package pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Interface that gives access to: 
 * <br>ObjectiveFunction Map;
 * <br>Simulation Configuration;
 * <br>SteadyStateModel.
 * @author hgiesteira
 *
 */
public interface ISteadyStateConfiguration extends IGenericConfiguration {
	
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctionsMap();
	
	public void setObjectiveFunctionsMap(IndexedHashMap<IObjectiveFunction, String> objectiveFunctionMap);
	
	
	public Map<String,Map<String,Object>> getSimulationConfiguration();
	
	public void setSimulationConfiguration(Map<String,Map<String,Object>> simulationConfiguration);
	
	
	public ISteadyStateModel getSteadyStateModel();
	
	public void setModel(ISteadyStateModel model);
	
	
	public String getOptimizationStrategy();
	
	public void setOptimizationStrategy(String optimizationStrategy);
	
	
	public boolean getIsGeneOptimization();
	
	public boolean getIsOverUnderExpression();

}
