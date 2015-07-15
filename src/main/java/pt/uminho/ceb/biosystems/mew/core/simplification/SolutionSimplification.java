package pt.uminho.ceb.biosystems.mew.core.simplification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

public class SolutionSimplification implements Serializable {
	
	private static final long						serialVersionUID	= 1L;
	
	private double									delta				= 0.000001;
	
	protected ISteadyStateModel						model;
	
	protected List<IObjectiveFunction>				objectiveFunctions;
	
	protected String								methodType			= SimulationProperties.FBA;
	
	protected SimulationSteadyStateControlCenter	center				= null;
	
	protected FluxValueMap							referenceFD			= null;
	
	public SolutionSimplification(ISteadyStateModel model, List<IObjectiveFunction> objFunctions, String methodType, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		this.model = model;
		this.objectiveFunctions = objFunctions;
		this.methodType = methodType;
		this.center = new SimulationSteadyStateControlCenter(envCond, null, model, methodType);
		this.center.setMaximization(true);
		this.center.setSolver(solver);
		this.center.setWTReference(referenceFD);
		//		CplexParamConfiguration.setDoubleParam("TiLim", 5.0);
	}
	
	public SolutionSimplification(ISteadyStateModel model, List<IObjectiveFunction> objFunctions, String methodType, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver, Boolean ou2stepApproach) {
		this(model, objFunctions, methodType, referenceFD, envCond, solver);
		center.setOverUnder2StepApproach(ou2stepApproach);
	}
	
	public SolutionSimplification(ISteadyStateModel model, List<IObjectiveFunction> objFunctions, String methodType, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver, String fbaObjective) {
		this(model, objFunctions, methodType, referenceFD, envCond, solver);
		center.setFBAObjSingleFlux(fbaObjective, 1.0);
		if (referenceFD == null && !fbaObjective.equals(SimulationProperties.PFBA)) try {
			center.addWTReference();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSolver(SolverType solver) {
		center.setSolver(solver);
	}
	
	public void setMargin(double delta) {
		this.delta = delta;
	}
	
	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution) throws Exception {
		return simplifyReactionsSolution(initialSolution, null);
	}
	
	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution, SteadyStateSimulationResult initialRes) throws Exception {
		
		SteadyStateSimulationResult origRes = null;
		if (initialRes == null) {
			center.setGeneticConditions(initialSolution);
			try {
				origRes = center.simulate();
			} catch (Exception e) {
				e.printStackTrace();
//				return new SolutionSimplificationResult(origRes, initialSolution, objectiveFunctions, null);
			}
		} else
			origRes = initialRes;		
			
			double[] initialFitnesses = evaluateSolution(origRes);
//			System.out.println("InitFit: "+Arrays.toString(initialFitnesses));
			
			Set<String> reactionIds = initialSolution.getReactionList().getReactionIds();
			
			List<String> rIDsIterator = new ArrayList<String>(reactionIds);
			
			GeneticConditions finalSolution = initialSolution;
			double[] finalFitnesses = initialFitnesses;
			SteadyStateSimulationResult finalRes = origRes;
			
			for (String reactionId : rIDsIterator) {
				
				double expressionLevel = initialSolution.getReactionList().getReactionFlux(reactionId);
				
				finalSolution.getReactionList().removeReaction(reactionId);
				
				center.setGeneticConditions(finalSolution);
				
				SteadyStateSimulationResult res = center.simulate();
				
				double[] simpfitnesses = evaluateSolution(res);
				
				if (compare(finalFitnesses, simpfitnesses)) {
					finalFitnesses = simpfitnesses;
					finalRes = res;
				} else {
					finalSolution.getReactionList().addReaction(reactionId, expressionLevel);
				}
			}
			
			SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(finalRes, finalSolution, objectiveFunctions, finalFitnesses);
			
			return optimizationSimplification;
		
	}
	
	public SteadyStateOptimizationResult simplifySteadyStateOptimizationResult(SteadyStateOptimizationResult optResultIN, boolean isGeneOpt) throws Exception {
		SteadyStateOptimizationResult optResultOut = new SteadyStateOptimizationResult(model, objectiveFunctions);
		
		for (String id : optResultIN.getSimulationMap().keySet()) {
			
			SteadyStateSimulationResult resOrig = optResultIN.getSimulationResult(id);
			LPSolutionType solutionType = resOrig.getSolutionType();

			if(solutionType.equals(LPSolutionType.FEASIBLE) || solutionType.equals(LPSolutionType.OPTIMAL)){
				SolutionSimplificationResult simp = null;
				if (isGeneOpt)
					simp = simplifyGenesSolution(resOrig.getGeneticConditions(), resOrig);
				else
					simp = simplifyReactionsSolution(resOrig.getGeneticConditions(), resOrig);
				
				if (simp != null) {
					SteadyStateSimulationResult resOut = simp.getSimulationResult();
					double[] fits = simp.getFitnesses();
					
					ArrayList<Double> fitnesses = new ArrayList<Double>();
					for (double f : fits)
						fitnesses.add(f);
					
					optResultOut.addOptimizationResultNoRepeated(resOut, fitnesses);
				}
			}
		}
		
		return optResultOut;
	}
	
	public SolutionSimplificationResult simplifyGenesSolution(GeneticConditions initialSolution) throws Exception {
		return simplifyGenesSolution(initialSolution, null);
	}
	
	public SolutionSimplificationResult simplifyGenesSolution(GeneticConditions initialSolution, SteadyStateSimulationResult initialRes) throws Exception {
		
		SteadyStateSimulationResult origRes;
		if (initialRes == null) {
			center.setGeneticConditions(initialSolution);
			origRes = center.simulate();
		} else
			origRes = initialRes;
		
		double[] initialFitnesses = evaluateSolution(origRes);
		// System.out.println("Init sol ["+initialSolution.toString()+"] fits="+Arrays.toString(initialFitnesses));
		
		Set<String> geneIds = initialSolution.getGeneList().getGeneIds();
		
		List<String> gIDsIterator = new ArrayList<String>(geneIds);
		
		GeneticConditions finalSolution = initialSolution;
		double[] finalFitnesses = initialFitnesses;
		SteadyStateSimulationResult finalRes = origRes;
		
		for (String geneId : gIDsIterator) {
			double expressionLevel = initialSolution.getGeneList().getGeneExpression(geneId);
			
			finalSolution.getGeneList().removeGene(geneId);
			finalSolution.updateReactionsList((ISteadyStateGeneReactionModel) model);
			
			center.setGeneticConditions(finalSolution);
			SteadyStateSimulationResult res = center.simulate();
			
			double[] simpfitnesses = evaluateSolution(res);
			
			// System.out.println("\tTest removal of ["+geneId+"] fits="+Arrays.toString(simpfitnesses));
			
			if (compare(finalFitnesses, simpfitnesses)) {
				
				finalFitnesses = simpfitnesses;
				finalRes = res;
			} else {
				finalSolution.getGeneList().addGene(geneId, expressionLevel);
				finalSolution.updateReactionsList((ISteadyStateGeneReactionModel) model);
				
			}
		}
		
		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(finalRes, finalSolution, objectiveFunctions, finalFitnesses);
		
		return optimizationSimplification;
	}
	
	private double[] evaluateSolution(SteadyStateSimulationResult result) throws Exception {
		int size = objectiveFunctions.size();
		double resultList[] = new double[size];
		for (int i = 0; i < size; i++) {
			IObjectiveFunction of = objectiveFunctions.get(i);
			resultList[i] = of.evaluate(result);
		}
		return resultList;
	}
	
	private boolean compare(double[] fitnesses, double[] simplifiedFitness) {
		boolean res = true;
		int i = 0;
		
		while (res && i < objectiveFunctions.size()) {
			IObjectiveFunction of = objectiveFunctions.get(i);
			if (of.isMaximization()) {
				if (fitnesses[i] - simplifiedFitness[i] > delta) res = false;
			} else if (simplifiedFitness[i] - fitnesses[i] > delta) res = false;
			i++;
		}
		
		return res;
	}
	
}
