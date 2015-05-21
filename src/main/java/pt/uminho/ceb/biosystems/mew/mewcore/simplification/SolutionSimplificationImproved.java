package pt.uminho.ceb.biosystems.mew.mewcore.simplification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import cern.colt.Arrays;

public class SolutionSimplificationImproved implements Serializable {
	
	private static final long						serialVersionUID	= 1L;
	
	private double									delta				= 0.000001;
	
	protected ISteadyStateModel						model;
	
	protected List<IObjectiveFunction>				objectiveFunctions;
	
	protected String								methodType			= SimulationProperties.FBA;
	
	protected SimulationSteadyStateControlCenter	center				= null;
	
	protected FluxValueMap							referenceFD			= null;
	
	public SolutionSimplificationImproved(ISteadyStateModel model, List<IObjectiveFunction> objFunctions, String methodType, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		this.model = model;
		this.objectiveFunctions = objFunctions;
		this.methodType = methodType;
		this.center = new SimulationSteadyStateControlCenter(envCond, null, model, methodType);
		this.center.setMaximization(true);
		this.center.setSolver(solver);
		this.center.setWTReference(referenceFD);
	}
	
	public SolutionSimplificationImproved(ISteadyStateModel model, List<IObjectiveFunction> objFunctions, String methodType, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver, String fbaObjective) {
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
		return simplifyReactionsSolution(initialSolution,null,false);
	}
	
	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution, boolean preSimplify) throws Exception {
		return simplifyReactionsSolution(initialSolution,null,preSimplify);
	} 
	
	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution, SteadyStateSimulationResult initialRes, boolean preSimplify) throws Exception{
		if(preSimplify)
			return preSimplifyReactionSolution(initialSolution, initialRes);
		else 
			return simplifyReactionsSolution(initialSolution, initialRes); 
				
	}
	
	public SolutionSimplificationResult preSimplifyReactionSolution(GeneticConditions initialSolution, SteadyStateSimulationResult initialRes) throws Exception {
		
		if (initialRes == null) {
			center.setGeneticConditions(initialSolution);
			try {
				initialRes = center.simulate();
				System.out.println("InitSol:"+initialSolution.toStringOptions(", ", false));
				System.out.println("InitFit: "+Arrays.toString(evaluateSolution(initialRes)));
			} catch (Exception e) {
				e.printStackTrace();
				return new SolutionSimplificationResult(initialRes, initialSolution, objectiveFunctions, null);
			}
		}

		
		Set<String> reactionIds = initialSolution.getReactionList().getReactionIds();
		List<String> rIDsIterator = new ArrayList<String>(reactionIds);		
		
		GeneticConditions finalSolution = initialSolution.clone();
		
		for (String reactionId : rIDsIterator) {
			if (initialRes.getFluxValues().getValue(reactionId) == 0.0)
				finalSolution.getReactionList().removeReaction(reactionId);			
		}
		
		center.setGeneticConditions(finalSolution);
		initialRes = center.simulate();
		
		System.out.println("MedSol:"+finalSolution.toStringOptions(", ", false));
//		return new SolutionSimplificationResult(initialRes, evaluateSolution(initialRes));
		return simplifyReactionsSolution(finalSolution,initialRes);
	}
	
	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution, SteadyStateSimulationResult initialRes) throws Exception {
		
		SteadyStateSimulationResult origRes = null;
		if (initialRes == null) {
			center.setGeneticConditions(initialSolution);
			try {
				origRes = center.simulate();
			} catch (Exception e) {
				e.printStackTrace();
				return new SolutionSimplificationResult(origRes, initialSolution, objectiveFunctions, null);
			}
		} else
			origRes = initialRes;
		
		double[] initialFitnesses = evaluateSolution(origRes);
		
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
		
		System.out.println("FinalSol:"+finalSolution.toStringOptions(",", false));
		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(finalRes, finalSolution, objectiveFunctions, finalFitnesses);
		
		return optimizationSimplification;
		
	}
	
	public SteadyStateOptimizationResult simplifySteadyStateOptimizationResult(SteadyStateOptimizationResult optResultIN, boolean isGeneOpt) throws Exception {
		SteadyStateOptimizationResult optResultOut = new SteadyStateOptimizationResult(model, objectiveFunctions);
		
		for (String id : optResultIN.getSimulationMap().keySet()) {
			
			SteadyStateSimulationResult resOrig = optResultIN.getSimulationResult(id);
			SolutionSimplificationResult simp = null;
			if (isGeneOpt) {
				simp = simplifyGenesSolution(resOrig.getGeneticConditions(), resOrig);
			} else {
				//				GeneticConditions original = resOrig.getGeneticConditions();
				//				ReactionChangesList rcl = new ReactionChangesList();
				//				for(String r : resOrig.getGeneticConditions().getReactionList().keySet())
				//					if(resOrig.getFluxValues().getValue(r)!=0.0){
				//						rcl.addReaction(r, original.getReactionList().getReactionFlux(r));
				//					}else
				//						System.out.println("pre simp: "+r);
				//				
				//				GeneticConditions gcNew = new GeneticConditions(rcl);
				//				
				simp = simplifyReactionsSolution(resOrig.getGeneticConditions(), resOrig);
			}
			
			if (simp != null) {
				SteadyStateSimulationResult resOut = simp.getSimulationResult();
				double[] fits = simp.getFitnesses();
				
				ArrayList<Double> fitnesses = new ArrayList<Double>();
				for (double f : fits)
					fitnesses.add(f);
				
				optResultOut.addOptimizationResultNoRepeated(resOut, fitnesses);
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