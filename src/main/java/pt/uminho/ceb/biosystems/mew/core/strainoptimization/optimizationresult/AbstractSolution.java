package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 04-03-2015.
 */
public abstract class AbstractSolution implements IStrainOptimizationResult {
	
	private static final long serialVersionUID = 1L;
	
	public static final String	OUTTER_DELIMITER	= ",,";
	public static final String	INNER_DELIMITER		= ",";
	
	protected Map<String, SteadyStateSimulationResult>	simulationResultMap;
	protected GeneticConditions							solutionGeneticConditions;
	
//	FIXME: LIST???? change to Map
	protected List<Double>								attributes;
	
	public AbstractSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
		this(solutionGeneticConditions, simulationResultMap, null);
	}
	
	public AbstractSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> attributes) {
		this.simulationResultMap = simulationResultMap;
		this.solutionGeneticConditions = solutionGeneticConditions;
		this.attributes = attributes;
	}
	
	@Override
	public SteadyStateSimulationResult getSimulationResultForMethod(String method) throws Exception {
		SteadyStateSimulationResult result = simulationResultMap.get(method);
		return result;
	}
	
	@Override
	public void addSimulationResultForMethod(String method, SteadyStateSimulationResult result) {
		simulationResultMap.put(method, result);
	}
	
	//        if(result == null){
	//            Map<String,Object> propertyMap = configuration.getPropertyMapCopy();
	//            EnvironmentalConditions environmentalConditions = (EnvironmentalConditions) configuration.getProperty(JecoliOptimizationProperties.ENVIRONMENTAL_CONDITIONS);
	//            ISteadyStateModel model = (ISteadyStateModel) configuration.getProperty(JecoliOptimizationProperties.STEADY_STATE_MODEL);
	//            SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions,solutionGeneticConditions,model,method);
	//            cc.setSolver(configuration.getSolver());
	//            cc.setMaximization(configuration.getIsMaximization());
	//            result = cc.simulate();
	//            simulationResultMap.put(method,result);
	//        }
	
	@Override
	public GeneticConditions getGeneticConditions() {
		return solutionGeneticConditions;
	}
	
	protected void writeMapOf2SimMap(OutputStreamWriter outputStream, IndexedHashMap<IObjectiveFunction, String> mapOf2SimMap) throws Exception {
		int counter = 0;
		for (IObjectiveFunction objectiveFunction : mapOf2SimMap.keySet()) {
			String simulationMethod = mapOf2SimMap.get(objectiveFunction);
			SteadyStateSimulationResult simulationResult = getSimulationResultForMethod(simulationMethod);
			if (counter > 0) outputStream.write(",");
			outputStream.write(String.valueOf(objectiveFunction.evaluate(simulationResult)));
			counter++;
		}
		outputStream.write(",");
		outputStream.flush();
	}
	
	public Map<String, SteadyStateSimulationResult> getSimulationResultMap() {
		return simulationResultMap;
	}

	
	@Override
	public List<Double> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Double> attributes) {
		this.attributes = attributes;
	}
	
}
