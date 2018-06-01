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
	
	protected Double											delta					= null;
	protected Boolean											keepOnlyMinSolution		= null;
	protected Map<String, SimulationSteadyStateControlCenter>	ccs						= null;
	protected Map<String, Map<String, Object>>					simulationConfiguration	= null;
	protected ISteadyStateModel									model					= null;
	protected Map<String, Object>								simplifierOptions		= null;
																						
	public AbstractGeneticConditionsSimplifier(Map<String, Map<String, Object>> simulationConfiguration) {
		this.simulationConfiguration = simulationConfiguration;
		this.model = (ISteadyStateModel) simulationConfiguration.values().iterator().next().get(SimulationProperties.MODEL);
		this.simplifierOptions = new HashMap<>();
		loadDefaultOptions();
	}
	
	private void loadDefaultOptions() {
		this.simplifierOptions.put(SimplifierOptions.DELTA, 1e-6);
		this.simplifierOptions.put(SimplifierOptions.KEEP_ONLY_MIN_SOLUTION, false);
	}
	
	public Object getOption(String optionKey) {
		return simplifierOptions.get(optionKey);
	}
	
	protected abstract SimulationSteadyStateControlCenter getControlCenterForMethod(String method) throws Exception;
	
	public void setSimplifierOptions(Map<String, Object> simplifierOptions) {
		if (simplifierOptions != null) {
			for (String opt : simplifierOptions.keySet()) {
				this.simplifierOptions.put(opt, simplifierOptions.get(opt));
			}
		}
	}
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions geneticConditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) throws Exception {
		Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(geneticConditions, objectiveFunctions);
		double[] initialFitnesses = evaluateSolution(results, objectiveFunctions);		
		
		IGeneticConditionsSimplifiedResult simpResult = simplifyGeneticConditions(geneticConditions, objectiveFunctions, initialFitnesses);
		Double[] minPercents = (Double[]) getOption(SimplifierOptions.MIN_PERCENT_PER_OBJFUNC);
		
		keepOnlyMinSolution = (Boolean) simplifierOptions.get(SimplifierOptions.KEEP_ONLY_MIN_SOLUTION);
		
		if (minPercents != null) {
			if (minPercents.length != objectiveFunctions.size()) {
				throw new Exception("Simplifier option [" + SimplifierOptions.MIN_PERCENT_PER_OBJFUNC + "] has length (" + minPercents.length
						+ ") and it must be the same length as the number of objective functions (" + objectiveFunctions.size() + ")");
			} else {
				double[] simpFitnesses = new double[objectiveFunctions.size()];
				GeneticConditions gc = simpResult.getSimplifiedGeneticConditions().get(0);
				for (int i = 0; i < simpFitnesses.length; i++) {
					simpFitnesses[i] = simpResult.getSimplifiedFitnesses().get(0).get(i);
				}
				IGeneticConditionsSimplifiedResult subSolutions = (keepOnlyMinSolution)
						? simplifyGeneticConditions(gc, objectiveFunctions, simpFitnesses, minPercents)
						: findSubSolutions(gc, objectiveFunctions, simpFitnesses, minPercents);
				subSolutions.addAll(simpResult);
				
				return subSolutions;
			}
		} else {
			return simpResult;
		}
	}
	
	protected void findNaNInResults(Map<String, SteadyStateSimulationResult> results) {
		for (String st : results.keySet()) {
			
			FluxValueMap resultsMap = results.get(st).getFluxValues();
			for (String s : resultsMap.keySet()) {
				if (Double.isNaN(resultsMap.get(s)))
					System.err.println(s + " Is NaN!");
			}
		}
	}
	
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions, double[] initialFitnesses,
			Double[] minPercents) throws Exception {
		TreeSet<String> ids = new TreeSet<String>(getGeneticConditionsIDs(conditions));
		
		List<String> iDsIterator = new ArrayList<String>(ids);
		
		delta = (Double) getOption(SimplifierOptions.DELTA);
		
		GeneticConditions finalSolution = conditions.clone();
		Map<String, SteadyStateSimulationResult> finalResults = simulateGeneticConditions(finalSolution, objectiveFunctions);
		double[] finalFitnesses = initialFitnesses;
		
		for (String id : iDsIterator) {
			
			double expLvl = getExpressionLevel(conditions, id);
			
			removeGeneticCondition(finalSolution, id);
			
			Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(finalSolution, objectiveFunctions);
			
			double[] simpfitnesses = evaluateSolution(results, objectiveFunctions);
			
			boolean valid = (minPercents == null) ? isBetter(finalFitnesses, simpfitnesses, objectiveFunctions) : isInRange(initialFitnesses, simpfitnesses, minPercents, objectiveFunctions);
			if (valid) {
				finalFitnesses = simpfitnesses;
				finalResults = results;
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
	
	@Override
	public IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions, double[] initialFitnesses)
			throws Exception {
		return simplifyGeneticConditions(conditions, objectiveFunctions, initialFitnesses, null);
	}
	
	public IGeneticConditionsSimplifiedResult findSubSolutions(GeneticConditions conditions, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions, double[] originalFitnesses,
			Double[] minPercents) throws Exception {
			
		List<GeneticConditions> gcsResults = new ArrayList<>();
		List<SteadyStateMultiSimulationResult> simResults = new ArrayList<>();
		List<List<Double>> fitResults = new ArrayList<>();
		
		TreeSet<String> ids = new TreeSet<String>(getGeneticConditionsIDs(conditions));
		
		List<String> iDsIterator = new ArrayList<String>(ids);
		
		delta = (Double) getOption(SimplifierOptions.DELTA);
		
		GeneticConditions finalSolution = conditions.clone();
		Map<String, SteadyStateSimulationResult> finalResults = simulateGeneticConditions(finalSolution, objectiveFunctions);
		double[] finalFitnesses = originalFitnesses;
		
		for (String id : iDsIterator) {
			
			double expLvl = getExpressionLevel(conditions, id);
			
			removeGeneticCondition(finalSolution, id);
			
			Map<String, SteadyStateSimulationResult> results = simulateGeneticConditions(finalSolution, objectiveFunctions);
			
			double[] simpfitnesses = evaluateSolution(results, objectiveFunctions);
			
			if (isInRange(originalFitnesses, simpfitnesses, minPercents, objectiveFunctions)) {
				finalFitnesses = simpfitnesses;
				finalResults = results;
				gcsResults.add(finalSolution.clone());
				simResults.add(new SteadyStateMultiSimulationResult(finalSolution.clone(), finalResults));
				List<Double> fitList = new ArrayList<>(finalFitnesses.length);
				for (double d : finalFitnesses) {
					fitList.add(d);
				}
				fitResults.add(fitList);
			} else {
				nextGeneticCondition(finalSolution, id, expLvl);
			}
		}
		
		return new GeneticConditionSimplifiedResult(gcsResults, simResults, fitResults);
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
	
	/*
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
			if (results != null && results.containsKey(method)) {
				double resValue = of.evaluate(results.get(method));
				resultList[i] = resValue;
			} else {
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
	
	public boolean isInRange(double[] originalFitnesses, double[] simpFitnesses, Double[] minPercents, IndexedHashMap<IObjectiveFunction, String> objectiveFunctions) {
		boolean res = true;
		int i = 0;
		
		while (res && i < objectiveFunctions.size()) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			if (of.isMaximization()) {
				if (simpFitnesses[i] <= (originalFitnesses[i] * minPercents[i])) {
					res = false;
				}
			} else if (simpFitnesses[i] >= (originalFitnesses[i] * minPercents[i])) {
				res = false;
			}
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
