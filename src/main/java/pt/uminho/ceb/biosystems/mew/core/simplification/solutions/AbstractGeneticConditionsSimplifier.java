package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public abstract class AbstractGeneticConditionsSimplifier implements ISimplifierGeneticConditions {
	
	private double delta = 0.000001;
	
	protected Map<String, SimulationSteadyStateControlCenter>	ccs						= null;
	protected Map<String, Map<String, Object>>					simulationConfiguration	= null;
	protected ISteadyStateGeneReactionModel						model					= null;
	
	public AbstractGeneticConditionsSimplifier(Map<String, Map<String, Object>> simulationConfiguration) {
		this.simulationConfiguration = simulationConfiguration;
		this.model = (ISteadyStateGeneReactionModel) simulationConfiguration.values().iterator().next().get(SimulationProperties.MODEL);
	}
	
	protected abstract SimulationSteadyStateControlCenter getControlCenterForMethod(String method) throws Exception;
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions geneticConditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) throws Exception {
		Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(geneticConditions, objectiveFunctions);
		double[] initialFitnesses = evaluateSolution(results, objectiveFunctions);
		return simplifyGeneticConditions(geneticConditions, objectiveFunctions, initialFitnesses);
	}
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions, double[] initialFitnesses)
			throws Exception {
		Set<String> ids = getGeneticConditionsIDs(conditions);
		
		List<String> iDsIterator = new ArrayList<String>(ids);
		
		GeneticConditions finalSolution = conditions;
		Map<String, SteadyStateSimulationResult> finalResults = null;
		double[] finalFitnesses = initialFitnesses;
		
		for (String id : iDsIterator) {
			
			double expLvl = getExpressionLevel(conditions, id);
			
			removeGeneticCondition(finalSolution, id);
			
//			long init = System.currentTimeMillis();
			Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(finalSolution, objectiveFunctions);
//			System.out.println((System.currentTimeMillis() - init));
			
			double[] simpfitnesses = evaluateSolution(results, objectiveFunctions);
			
			if (compare(finalFitnesses, simpfitnesses, objectiveFunctions)) {
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
			resultList[i] = of.evaluate(results.get(method));
		}
		return resultList;
	}
	
	protected boolean compare(double[] fitnesses, double[] simplifiedFitness, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) {
		boolean res = true;
		int i = 0;
		
		while (res && i < objectiveFunctions.size()) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			if (of.isMaximization()) {
				if (fitnesses[i] - simplifiedFitness[i] > delta) {
					res = false;
				}
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
