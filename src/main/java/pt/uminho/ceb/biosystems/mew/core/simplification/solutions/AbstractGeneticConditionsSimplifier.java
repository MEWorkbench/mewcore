package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public abstract class AbstractGeneticConditionsSimplifier implements ISimplifierGeneticConditions {
	
	private double delta = 1e-6;
	
	protected Map<String, SimulationSteadyStateControlCenter>	ccs						= null;
	protected Map<String, Map<String, Object>>					simulationConfiguration	= null;
	protected ISteadyStateModel									model					= null;
	
	public AbstractGeneticConditionsSimplifier(Map<String, Map<String, Object>> simulationConfiguration) {
		this.simulationConfiguration = simulationConfiguration;
		this.model = (ISteadyStateModel) simulationConfiguration.values().iterator().next().get(SimulationProperties.MODEL);
	}
	
	protected abstract SimulationSteadyStateControlCenter getControlCenterForMethod(String method) throws Exception;
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions geneticConditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) throws Exception {
		Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(geneticConditions, objectiveFunctions);
		double[] initialFitnesses = evaluateSolution(results, objectiveFunctions);
		return simplifyGeneticConditions(geneticConditions, objectiveFunctions, initialFitnesses);
	}
	
	
	protected void findNaNInResults(Map<String, SteadyStateSimulationResult> results) {
		for (String st : results.keySet()) {
			
			FluxValueMap resultsMap = results.get(st).getFluxValues();
			for (String s : resultsMap.keySet()) {
				if(Double.isNaN(resultsMap.get(s)))
					System.err.println(s + " Is NaN!");
			}
		}
	}
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions, double[] initialFitnesses)
			throws Exception {
		
		TreeSet<String> ids = new TreeSet<String>(getGeneticConditionsIDs(conditions));
		
		List<String> iDsIterator = new ArrayList<String>(ids);
		
		GeneticConditions finalSolution = conditions.clone();
		Map<String, SteadyStateSimulationResult> finalResults = simulateGeneticConditions(finalSolution, objectiveFunctions);
		double[] finalFitnesses = initialFitnesses;
		
		for (String id : iDsIterator) {
			
			double expLvl = getExpressionLevel(conditions, id);
			
			removeGeneticCondition(finalSolution, id);
			
			Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(finalSolution, objectiveFunctions);
			
			double[] simpfitnesses = evaluateSolution(results, objectiveFunctions);
			
			if (isBetter(finalFitnesses, simpfitnesses, objectiveFunctions)) {
					finalFitnesses = simpfitnesses;
					finalResults = results;
				//NOTE: SE ISTO NAO ESTIVER A FUNCIONAR EM CONDICOES, O PROBLEMA PODE SER DAQUI
			} else {
				nextGeneticCondition(finalSolution, id, expLvl);
			}
		}
		
		/** create fitnesses list */
		List<Double> fitList = new ArrayList<>(finalFitnesses.length);
		for (double d : finalFitnesses) {
			fitList.add(d);
		}
		
		return new GeneticConditionSimplifiedResult(finalSolution, new SteadyStateMultiSimulationResult(finalSolution, finalResults), fitList);
	}
	
	public Map<String, SteadyStateSimulationResult> simulateGeneticConditions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) throws Exception {
		buildControlCenters(objectiveFunctions);
		Map<String, SteadyStateSimulationResult> res = new HashMap<>();
		for (String method : objectiveFunctions.values()) {
			SimulationSteadyStateControlCenter center = getControlCenterForMethod(method);
			center.setGeneticConditions(conditions);

			try {
				SteadyStateSimulationResult mres = center.simulate();
				res.put(method, mres);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	/**
	 * Evaluates the solution based on a jecoli generic configuration and a map
	 * with method and matching simulation result
	 * 
	 * @param configuration jecoli generic configuration
	 * @param results map with method and simulation result
	 * @return double[] fitnesses
	 */
	protected double[] evaluateSolution(Map<String, SteadyStateSimulationResult> results, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) throws Exception {
		int size = objectiveFunctions.size();
		double resultList[] = new double[size];
		for (int i = 0; i < size; i++) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			String method = objectiveFunctions.get(of);
			// This must be reviewed but sometimes the results map is null
			// Maybe a validation should be performed before getting at this point
			if(results != null && results.containsKey(method)){
				double resValue = of.evaluate(results.get(method)); 
				resultList[i] = resValue;
			}else{
				resultList[i] = of.getWorstFitness();
			}
		}
		return resultList;
	}
	
	protected boolean isBetter(double[] fitnesses, double[] simplifiedFitness, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) {
		boolean res = true;
		int i = 0;
		
		while (res && i < objectiveFunctions.size()) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			if (of.isMaximization()) {
				if (fitnesses[i] - simplifiedFitness[i] > delta) 
					res = false;
			} else if (simplifiedFitness[i] - fitnesses[i] > delta)
				res = false;
			i++;
		}
		
		return res;
	}
	
	protected void buildControlCenters(IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) {
		if (ccs == null) {
			ccs = new HashMap<String, SimulationSteadyStateControlCenter>();
			for (String method : simulationConfiguration.keySet()) {
				Map<String, Object> methodConf = simulationConfiguration.get(method);
				SimulationSteadyStateControlCenter center = null;
				try {
					center = new SimulationSteadyStateControlCenter(methodConf);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ccs.put(method, center);
			}
		}
	}

}
