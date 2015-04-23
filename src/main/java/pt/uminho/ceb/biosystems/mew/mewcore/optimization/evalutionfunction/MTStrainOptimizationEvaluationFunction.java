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
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
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

public class MTStrainOptimizationEvaluationFunction extends AbstractMultiobjectiveEvaluationFunction<IRepresentation> {


	private static final long serialVersionUID = -5115170819374649924L;

	private int MAX_THREADS = 2;

	protected final boolean debug = false; 

	protected ISteadyStateModel model;

	protected ISteadyStateDecoder decoder;

	protected SimulationSteadyStateControlCenter[] controlCenters;
	
	protected List<String> simMethods;
	
	protected Map<IObjectiveFunction,String> mapOF2Sim;

	protected int numberOfObjectives = 1;

	protected SolverType solver;


	public MTStrainOptimizationEvaluationFunction(
			ISteadyStateModel model, 
			ISteadyStateDecoder decoder,
			EnvironmentalConditions envConds, 
			SolverType solver, 
			List<String> simulationMethods,
			Map<IObjectiveFunction,String> mapOF2Sim,
			boolean isMaximization,
			int maxThreads
			) throws Exception {

		super(isMaximization);		
		this.model = model;
		this.decoder = decoder;
		this.solver = solver;
		this.simMethods = simulationMethods;
		this.mapOF2Sim = mapOF2Sim;
		this.numberOfObjectives = mapOF2Sim.size();
		this.MAX_THREADS = maxThreads;

		this.controlCenters = new SimulationSteadyStateControlCenter[this.simMethods.size()];

		for(int i=0; i<this.simMethods.size(); i++){
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envConds,null,model,simulationMethods.get(i));
			cc.setSolver(solver);
			cc.setMaximization(isMaximization);
			controlCenters[i] = cc;
		}
	}
	
	@Override
    public void evaluate(ISolutionSet<IRepresentation> solutionSet) {
        for (int i = 0; i < solutionSet.getNumberOfSolutions(); i++) {
            ISolution<IRepresentation> solution = solutionSet.getSolution(i);
            evaluateSingleSolution(solution);                        	
        }
        if(listeners!=null && !listeners.isEmpty())
        	notifyEvaluationFunctionListeners(EvaluationFunctionEvent.SOLUTIONSET_EVALUATION_EVENT, String.valueOf(solutionSet.getNumberOfSolutions()), solutionSet);
    }

	@Override
	public Double[] evaluateMO(IRepresentation solution) throws Exception {
		Double[] resultList =  new Double[mapOF2Sim.size()];

		GeneticConditions gc = null;
		try {
			gc = decoder.decode(solution);
		} catch (Exception e1) {
		}


		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

		Map<String,SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();

		int i=0;
		for(String simMethod : simMethods){		
			SimulationSteadyStateControlCenter cc = controlCenters[i];
			cc.setGeneticConditions(gc);		
			Runnable worker = new SimulationWorker(cc, results, simMethod, i);
			executor.execute(worker);
			i++;
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {}
		// System.out.println("Result list: "+Arrays.toString(resultList));
		
		int k=0;
		for(IObjectiveFunction of: mapOF2Sim.keySet()){
			String method = mapOF2Sim.get(of);
			SteadyStateSimulationResult result = results.get(method);			
			
			if (result != null && (result.getSolutionType().equals(LPSolutionType.OPTIMAL) || result.getSolutionType().equals(LPSolutionType.FEASIBLE))){
				try {
					resultList[k] = of.evaluate(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				if(result!=null)
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


	public ISteadyStateDecoder getDecoder (){
		return decoder;
	}


	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

	public void setNumberOfObjectives(int numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
	}

	public Object[] getSimulationProperty(String propertyKey){

		Object[] toret = new Object[controlCenters.length];

		for(int i=0; i<controlCenters.length; i++)
			toret[i]= controlCenters[i].getProperty(propertyKey);

		return toret; 
	}

	public void setSimulationProperty(String key, Object value){

		for(int i=0; i<controlCenters.length; i++)
			controlCenters[i].setSimulationProperty(key, value);
	}
	

	public void setOverUnderReferenceDistribution(Map<String,Double> reference){
		this.setSimulationProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, reference);
	}
	

	public List<IObjectiveFunction> getObjectiveFunctions() {
		if(!mapOF2Sim.isEmpty())
			return new ArrayList<IObjectiveFunction>(mapOF2Sim.keySet());
		else return null;
	}
	
	public Map<IObjectiveFunction,String> getMapOF2Sim(){
		return mapOF2Sim;
	}

	/**
	 * @return the controlCenters
	 */
	public SimulationSteadyStateControlCenter[] getControlCenters() {
		return controlCenters;
	}

	/**
	 * @return the simMethods
	 */
	public List<String> getSimMethods() {
		return simMethods;
	}

	/**
	 * @param simMethods the simMethods to set
	 */
	public void setSimMethods(List<String> simMethods) {
		this.simMethods = simMethods;
	}

}

class SimulationWorker implements Runnable {

	private SimulationSteadyStateControlCenter cc;
	private Map<String, SteadyStateSimulationResult> res;
	private int i;
	private String method;


	public SimulationWorker(SimulationSteadyStateControlCenter cc, Map<String, SteadyStateSimulationResult> res, String method, int i){
		this.cc = cc;
		this.res = res;
		this.i = i;
		this.method = method;
	}

	@Override
	public void run() {
//		System.out.println(Thread.currentThread().getName()+" start.");
		SteadyStateSimulationResult result = null;
		try {
			result = processCommand();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		res.put(method,result);;

//		System.out.println(Thread.currentThread().getName()+" end.");
	}

	private SteadyStateSimulationResult processCommand() throws Exception {
		try {
			return cc.simulate();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
