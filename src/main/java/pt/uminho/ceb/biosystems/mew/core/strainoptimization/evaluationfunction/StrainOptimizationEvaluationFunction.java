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
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
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
 * @author pmaia Jan 10, 2014
 */
public class StrainOptimizationEvaluationFunction extends AbstractMultiobjectiveEvaluationFunction<IRepresentation> {
	
	private static final long						serialVersionUID			= 1L;
	
	protected final boolean							_debug						= false;
	
	protected ISteadyStateModel						_model						= null;
	
	protected ISteadyStateDecoder					_decoder					= null;
	
	protected SimulationSteadyStateControlCenter[]	_controlCenters				= null;
	
	protected List<String>							_simMethods					= null;
	
	protected Map<IObjectiveFunction, String>		_mapOF2Sim					= null;
	
	protected FluxValueMap							_wtReference				= null;
	
	protected int									_numberOfObjectives			= 1;
	
	protected SolverType							_solver						= SolverType.CPLEX3;
	
	protected EnvironmentalConditions				_environmentalConditions	= null;
	
	private boolean									_ou2stepApproach			= false;
	
	public StrainOptimizationEvaluationFunction(
			ISteadyStateModel model,
			ISteadyStateDecoder decoder,
			EnvironmentalConditions envConds,
			SolverType solver,
			List<String> simulationMethods,
			Map<IObjectiveFunction, String> mapOF2Sim,
			FluxValueMap wtReference,
			boolean isMaximization,
			int maxThreads,
			boolean ou2stepApproach) throws Exception {
		
		super(isMaximization);
		this._model = model;
		this._decoder = decoder;
		this._environmentalConditions = envConds;
		this._solver = solver;
		this._simMethods = simulationMethods;
		this._mapOF2Sim = mapOF2Sim;
		this._wtReference = wtReference;
		this._numberOfObjectives = mapOF2Sim.size();
		this._ou2stepApproach = ou2stepApproach;
		initializeControlCenters();
	}
	
	protected void initializeControlCenters() {
		_controlCenters = new SimulationSteadyStateControlCenter[_simMethods.size()];
		for (int i = 0; i < _simMethods.size(); i++) {
			String simMethod = _simMethods.get(i);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(_environmentalConditions, null, _model, simMethod);
			cc.setSolver(_solver);
			cc.setMaximization(isMaximization);
			cc.setWTReference(_wtReference);
			cc.setOverUnder2StepApproach(_ou2stepApproach);
			_controlCenters[i] = cc;
		}
	}
	
	@Override
	public void evaluate(ISolutionSet<IRepresentation> solutionSet) {
		
		for (int i = 0; i < solutionSet.getNumberOfSolutions(); i++) {
			evaluateSingleSolution(solutionSet.getSolution(i));
		}
		
		if (listeners != null && !listeners.isEmpty()) notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SOLUTIONSET_EVALUATION_EVENT, String.valueOf(solutionSet.getNumberOfSolutions()), solutionSet);
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
	
	public Object[] getSimulationProperty(String propertyKey) {
		
		Object[] toret = new Object[_controlCenters.length];
		
		for (int i = 0; i < _controlCenters.length; i++)
			toret[i] = _controlCenters[i].getProperty(propertyKey);
		
		return toret;
	}
	
	public void setSimulationProperty(String key, Object value) {
		
		for (int i = 0; i < _controlCenters.length; i++)
			_controlCenters[i].setSimulationProperty(key, value);
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
	public SimulationSteadyStateControlCenter[] getControlCenters() {
		return _controlCenters;
	}
	
	/**
	 * @return the simMethods
	 */
	public List<String> getSimMethods() {
		return _simMethods;
	}
	
	/**
	 * @param simMethods
	 *            the simMethods to set
	 */
	public void setSimMethods(List<String> simMethods) {
		this._simMethods = simMethods;
	}
	
	@Override
	public void evaluateSingleSolution(ISolution<IRepresentation> solution) {
		Double[] fitnessValues = null;
		
		try {
			fitnessValues = evaluateMO(solution.getRepresentation());
			
			boolean fitsOK = true;
			
			for (int i = 0; i < fitnessValues.length; i++) {
				if (fitnessValues[i] == null) {
					if (isMaximization)
						fitnessValues[i] = Double.MIN_VALUE;
					else
						fitnessValues[i] = Double.MAX_VALUE;
					
					fitsOK = false;
				}
				
			}
			
			solution.setFitnessValues(fitnessValues);
			if(fitnessValues.length==1)
				solution.setScalarFitnessValue(fitnessValues[0]);

			if (performFitnessAggregation) {
				double rawfitness;
				if (fitsOK)
					rawfitness = fitnessAggregation.aggregate(fitnessValues);
				else
					rawfitness = (isMaximization) ? Double.MIN_VALUE : Double.MAX_VALUE;
				
				solution.setScalarFitnessValue(rawfitness);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if (isMaximization)
				solution.setScalarFitnessValue(-Double.MAX_VALUE);
			else
				solution.setScalarFitnessValue(Double.MAX_VALUE);
		}
		
		if (listeners != null && !listeners.isEmpty()) notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SINGLE_SOLUTION_EVALUATION_EVENT, "", solution);
	}
	
	public Double[] evaluateMO(IRepresentation solution) throws Exception {
		Double[] resultList = new Double[_mapOF2Sim.size()];
		
		GeneticConditions gc = null;
		try {
			gc = _decoder.decode(solution);
			//			System.out.println("Eval testing sol "+gc.toStringOptions(", ", true));
		} catch (Exception e1) {
		}
		
		Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
		
		for (int i = 0; i < _simMethods.size(); i++) {
			String simMethod = _simMethods.get(i);
			_controlCenters[i].setGeneticConditions(gc);
			SteadyStateSimulationResult res = _controlCenters[i].simulate();
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
				if (result != null) System.out.println(result.getSolutionType());
				resultList[k] = of.getWorstFitness();
			}
			
			k++;
		}
		
		return resultList;
	}
	
}
