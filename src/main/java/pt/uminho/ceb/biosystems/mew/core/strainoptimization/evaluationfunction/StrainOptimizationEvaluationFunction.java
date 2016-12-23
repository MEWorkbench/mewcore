package pt.uminho.ceb.biosystems.mew.core.strainoptimization.evaluationfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.EvaluationFunctionEvent;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

/**
 * <p>
 * The <code>StrainOptimizationEvaluationFunctionPersistent</code>. This class
 * extends the original <code>AbstractMultiobjectiveEvaluationFunction</code>
 * but embraces the persistent model in the low level solvers.
 * <p>
 * This approach guarantees maximum performance when using solvers that support
 * such functionality.
 * 
 * @since Jan 10, 2014
 * @author pmaia updated in Oct 15, 2015
 */
public class StrainOptimizationEvaluationFunction extends AbstractMultiobjectiveEvaluationFunction<IRepresentation> implements IOptimizationEvaluationFunction {
	
	private static final long									serialVersionUID			= 1L;
	protected final boolean										_debug						= false;
																							
	protected ISteadyStateDecoder								_decoder					= null;
	protected Map<String, SimulationSteadyStateControlCenter>	_controlCenters				= null;
	protected Map<String, Map<String, Object>>					_simulationConfiguration	= null;
	protected Map<IObjectiveFunction, String>					_mapOF2Sim					= null;
	protected int												_numberOfObjectives			= 1;
																							
	public StrainOptimizationEvaluationFunction(
			ISteadyStateDecoder decoder,
			Map<String, Map<String, Object>> simulationConfiguration,
			Map<IObjectiveFunction, String> mapOF2Sim) throws Exception {
			
		super();
		this._decoder = decoder;
		this._simulationConfiguration = simulationConfiguration;
		this._mapOF2Sim = mapOF2Sim;
		this._numberOfObjectives = mapOF2Sim.size();
		initializeControlCenters();
	}
	
	protected void initializeControlCenters() {
		_controlCenters = new HashMap<String, SimulationSteadyStateControlCenter>();
		
		for (String method : _simulationConfiguration.keySet()) {
			Map<String, Object> methodConf = _simulationConfiguration.get(method);
//			String simMethod = (String) methodConf.get(SimulationProperties.METHOD_ID);
//			ISteadyStateModel model = (ISteadyStateModel) methodConf.get(SimulationProperties.MODEL);
//			EnvironmentalConditions envConditions = (EnvironmentalConditions) methodConf.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
//			GeneticConditions genCond = (GeneticConditions) methodConf.get(SimulationProperties.GENETIC_CONDITIONS);
//			SolverType solver = (SolverType) methodConf.get(SimulationProperties.SOLVER);
//			Boolean isMaximization = (Boolean) methodConf.get(SimulationProperties.IS_MAXIMIZATION);
//			Boolean overUnder2StepApproach = (Boolean) methodConf.get(SimulationProperties.OVERUNDER_2STEP_APPROACH);
//			FluxValueMap wtReference = (FluxValueMap) methodConf.get(SimulationProperties.WT_REFERENCE);
//			FluxValueMap ouReference = (FluxValueMap) methodConf.get(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
			
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(methodConf);
//			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envConditions, genCond, model, simMethod);
//			cc.setSolver(solver);
//			cc.setMaximization(isMaximization);
//			cc.setWTReference(wtReference);
//			cc.setOverUnder2StepApproach(overUnder2StepApproach);
//			cc.setUnderOverRef(ouReference);
			
			_controlCenters.put(method, cc);
		}
	}
	
	@Override
	public void evaluate(ISolutionSet<IRepresentation> solutionSet) {
		
		for (int i = 0; i < solutionSet.getNumberOfSolutions(); i++) {
			evaluateSingleSolution(solutionSet.getSolution(i));
		}
		
		if (listeners != null && !listeners.isEmpty())
			notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SOLUTIONSET_EVALUATION_EVENT, String.valueOf(solutionSet.getNumberOfSolutions()), solutionSet);
	}
	
	@Override
	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
	
	}
	
	@Override
	public IEvaluationFunction<IRepresentation> deepCopy() throws Exception {
		return null;
	}
	
	public ISteadyStateDecoder getDecoder() {
		return _decoder;
	}
	
	public int getNumberOfObjectives() {
		return _numberOfObjectives;
	}
	
	public void setNumberOfObjectives(int numberOfObjectives) {
		this._numberOfObjectives = numberOfObjectives;
	}
	
	public Object getSimulationPropertyForMethod(String propertyKey, String simMethodKey) {
		return _controlCenters.get(simMethodKey).getProperty(propertyKey);
	}
	
	public Object[] getSimulationProperty(String propertyKey) {
		
		Object[] toret = new Object[_controlCenters.size()];
		
		int i = 0;
		for (String m : _controlCenters.keySet()) {
			toret[i] = _controlCenters.get(m).getProperty(propertyKey);
			i++;
		}
		
		return toret;
	}
	
	public void setSimulationProperty(String key, Object value) {
		
		for (String m : _controlCenters.keySet())
			_controlCenters.get(m).setSimulationProperty(key, value);
	}
	
	public void setSimulationPropertyForMethod(String key, Object value, String simMethodKey) {
		_controlCenters.get(simMethodKey).setSimulationProperty(key, value);
	}
	
	public void setOverUnderReferenceDistribution(Map<String, Double> reference) {
		this.setSimulationProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, reference);
	}
	
	public List<IObjectiveFunction> getObjectiveFunctions() {
		if (!_mapOF2Sim.isEmpty())
			return new ArrayList<IObjectiveFunction>(_mapOF2Sim.keySet());
		else
			return null;
	}
	
	public Map<IObjectiveFunction, String> getMapOF2Sim() {
		return _mapOF2Sim;
	}
	
	/**
	 * @return the controlCenters
	 */
	public Map<String, SimulationSteadyStateControlCenter> getControlCenters() {
		return _controlCenters;
	}
	
	/**
	 * @return the simMethods
	 */
	public Map<String, Map<String, Object>> getSimMethods() {
		return _simulationConfiguration;
	}
	
	/**
	 * @param simMethods
	 *            the simMethods to set
	 */
	public void setSimMethods(Map<String, Map<String, Object>> simMethods) {
		this._simulationConfiguration = simMethods;
	}
	
	@Override
	public void evaluateSingleSolution(ISolution<IRepresentation> solution) {
		Double[] fitnessValues = null;
		
		try {
			fitnessValues = evaluateMO(solution.getRepresentation());
			
			boolean fitsOK = true;
			
			for (int i = 0; i < fitnessValues.length; i++) {
				if (fitnessValues[i] == null) {
					fitnessValues[i] = Double.MIN_VALUE;
					fitsOK = false;
				}
				
			}
			
			solution.setFitnessValues(fitnessValues);
			if (fitnessValues.length == 1)
				solution.setScalarFitnessValue(fitnessValues[0]);
				
			if (performFitnessAggregation) {
				double rawfitness;
				if (fitsOK) {
					rawfitness = fitnessAggregation.aggregate(fitnessValues);
				} else {
					rawfitness = Double.MIN_VALUE;
				}
				
				solution.setScalarFitnessValue(rawfitness);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			solution.setScalarFitnessValue(-Double.MAX_VALUE);
		}
		
		if (listeners != null && !listeners.isEmpty())
			notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SINGLE_SOLUTION_EVALUATION_EVENT, "", solution);
	}
	
	public Double[] evaluateMO(IRepresentation solution) throws Exception {
		Double[] resultList = new Double[_mapOF2Sim.size()];
		
		GeneticConditions gc = null;
		try {
			gc = _decoder.decode(solution);
			
//		System.out.println("Eval testing sol "+gc.toStringOptions(", ", true));
		
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
		
		for (String simMethod : _controlCenters.keySet()) {
			_controlCenters.get(simMethod).setGeneticConditions(gc);
			SteadyStateSimulationResult res = _controlCenters.get(simMethod).simulate();
			results.put(simMethod, res);
		}
		
		int k = 0;
		for (IObjectiveFunction of : _mapOF2Sim.keySet()) {
			String method = _mapOF2Sim.get(of);
			SteadyStateSimulationResult result = results.get(method);
			
			if (result != null && (result.getSolutionType().equals(LPSolutionType.OPTIMAL) || result.getSolutionType().equals(LPSolutionType.FEASIBLE))) {
				try {
					resultList[k] = of.evaluate(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				if (result != null)
					System.out.println(result.getSolutionType());
				resultList[k] = of.getWorstFitness();
			}
			
			k++;
		}
		
		return resultList;
	}
	
}
