package pt.uminho.ceb.biosystems.mew.mewcore.optimization.evalutionfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.EvaluationFunctionEvent;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunctionListener;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.aggregation.IAggregationFunction;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class MTSmartStrainOptimizationEvaluationFunction extends
		AbstractMultiobjectiveEvaluationFunction<IRepresentation> {
	
	private static final long						serialVersionUID			= -5115170819374649924L;
	
	private int										_MAX_THREADS				= 2;
	
	protected final boolean							_debug						= false;
	
	protected ISteadyStateModel						_model						= null;
	
	protected ISteadyStateDecoder					_decoder					= null;
	
	protected SimulationSteadyStateControlCenter[]	_controlCenters				= null;
	
	protected List<String>							_simMethods					= null;
	
	protected Map<IObjectiveFunction, String>		_mapOF2Sim					= null;
	
	protected int									_numberOfObjectives			= 1;
	
	protected SolverType							_solver						= SolverType.CPLEX;
	
	protected EnvironmentalConditions				_environmentalConditions	= null;
	
	public MTSmartStrainOptimizationEvaluationFunction(
			ISteadyStateModel model,
			ISteadyStateDecoder decoder,
			EnvironmentalConditions envConds,
			SolverType solver,
			List<String> simulationMethods,
			Map<IObjectiveFunction, String> mapOF2Sim,
			boolean isMaximization,
			int maxThreads)
			throws Exception {
		
		super(isMaximization);
		this._model = model;
		this._decoder = decoder;
		this._environmentalConditions = envConds;
		this._solver = solver;
		this._simMethods = simulationMethods;
		this._mapOF2Sim = mapOF2Sim;
		this._numberOfObjectives = mapOF2Sim.size();
		this._MAX_THREADS = maxThreads;
	}
	
	@Override
	public void evaluate(ISolutionSet<IRepresentation> solutionSet) {
		ExecutorService executor = Executors.newFixedThreadPool(_MAX_THREADS);
		
		for (int i = 0; i < solutionSet.getNumberOfSolutions(); i++) {
			ISolution<IRepresentation> solution = solutionSet.getSolution(i);
			SolutionWorker worker = new SolutionWorker(solution, isMaximization, performFitnessAggregation,
					fitnessAggregation, listeners, _simMethods, _mapOF2Sim, _decoder, _solver, _model, _environmentalConditions);
			executor.execute(worker);
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		
		if (listeners != null && !listeners.isEmpty())
			notifyEvaluationFunctionListeners(
					EvaluationFunctionEvent.SOLUTIONSET_EVALUATION_EVENT,
					String.valueOf(solutionSet.getNumberOfSolutions()),
					solutionSet);
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
				solution.setScalarFitnessValue(Double.MIN_VALUE);
			else
				solution.setScalarFitnessValue(Double.MAX_VALUE);
		}
		
		if (listeners != null && !listeners.isEmpty())
			notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SINGLE_SOLUTION_EVALUATION_EVENT, "", solution);
	}
	
	public Double[] evaluateMO(IRepresentation solution) throws Exception {
		Double[] resultList = new Double[_mapOF2Sim.size()];
		
		GeneticConditions gc = null;
		try {
			gc = _decoder.decode(solution);
		} catch (Exception e1) {
		}
		
		Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
		
		for (String simMethod : _simMethods) {
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(_environmentalConditions, gc, _model, simMethod);
			cc.setSolver(_solver);
			cc.setMaximization(isMaximization);
			cc.setGeneticConditions(gc);
			SteadyStateSimulationResult res = cc.simulate();
			results.put(simMethod, res);
		}
		
		int k = 0;
		for (IObjectiveFunction of : _mapOF2Sim.keySet()) {
			String method = _mapOF2Sim.get(of);
			SteadyStateSimulationResult result = results.get(method);
			
			if (result != null
					&& (result.getSolutionType().equals(LPSolutionType.OPTIMAL) || result.getSolutionType().equals(
							LPSolutionType.FEASIBLE))) {
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

class SolutionWorker extends AbstractMultiobjectiveEvaluationFunction<IRepresentation> implements Runnable {
	
	private static final long				serialVersionUID	= -7253959310316507550L;
	private ISolution<IRepresentation>		solution;
	private boolean							performFitnessAggregation;
	private IAggregationFunction			fitnessAggregation;
	private List<String>					simMethods;
	private Map<IObjectiveFunction, String>	mapOF2Sim;
	private ISteadyStateDecoder				decoder;
	private SolverType						solver;
	private ISteadyStateModel				model;
	private EnvironmentalConditions			envConds;
	
	public SolutionWorker(
			ISolution<IRepresentation> solution,
			boolean isMaximization,
			boolean performFitnessAggregation,
			IAggregationFunction fitnessAggregation,
			List<IEvaluationFunctionListener<IRepresentation>> listeners,
			List<String> simMethods,
			Map<IObjectiveFunction, String> mapOF2Sim,
			ISteadyStateDecoder decoder,
			SolverType solver,
			ISteadyStateModel model,
			EnvironmentalConditions envConds) {
		
		super(isMaximization);
		this.solution = solution;
		this.performFitnessAggregation = performFitnessAggregation;
		this.fitnessAggregation = fitnessAggregation;
		this.listeners = listeners;
		this.simMethods = simMethods;
		this.mapOF2Sim = mapOF2Sim;
		this.decoder = decoder;
		this.solver = solver;
		this.model = model;
		this.envConds = envConds;
		
	}
	
	@Override
	public void run() {
		// System.out.println("start thread");
		evaluateSingleSolution(solution);
		// System.out.println("end thread");
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
				solution.setScalarFitnessValue(Double.MIN_VALUE);
			else
				solution.setScalarFitnessValue(Double.MAX_VALUE);
		}
		
		if (listeners != null && !listeners.isEmpty())
			notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SINGLE_SOLUTION_EVALUATION_EVENT, "", solution);
	}
	
	public Double[] evaluateMO(IRepresentation solution) throws Exception {
		Double[] resultList = new Double[mapOF2Sim.size()];
		
		GeneticConditions gc = null;
		try {
			gc = decoder.decode(solution);
		} catch (Exception e1) {
		}
		
		Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
		
		for (String simMethod : simMethods) {
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envConds, null, model,
					simMethod);
			cc.setSolver(solver);
			cc.setMaximization(isMaximization);
			cc.setGeneticConditions(gc);
			SteadyStateSimulationResult res = cc.simulate();
			results.put(simMethod, res);
			// System.out.println(res.getOFvalue()+" for sol = "+gc.getGeneList().getStringRepresentation());
			
			// explicitly free memory
			cc = null;
			System.gc();
		}
		
		int k = 0;
		for (IObjectiveFunction of : mapOF2Sim.keySet()) {
			String method = mapOF2Sim.get(of);
			SteadyStateSimulationResult result = results.get(method);
			
			if (result != null
					&& (result.getSolutionType().equals(LPSolutionType.OPTIMAL) || result.getSolutionType().equals(
							LPSolutionType.FEASIBLE))) {
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
	
	@Override
	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
	}
	
	@Override
	public IEvaluationFunction<IRepresentation> deepCopy() throws Exception {
		return null;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return mapOF2Sim.size();
	}
}
