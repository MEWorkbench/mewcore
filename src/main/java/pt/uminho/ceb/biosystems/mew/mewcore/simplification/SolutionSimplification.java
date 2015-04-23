/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.mewcore.simplification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.InfeasibleProblemException;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.SimulationMethodsEnum;
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

public class SolutionSimplification implements Serializable {
	
	private static final long						serialVersionUID	= 1L;
	
	private double									delta				= 0.000001;
	
	protected ISteadyStateModel						model;
	
	protected List<IObjectiveFunction>				objectiveFunctions;
	
	protected String								methodType			= SimulationProperties.PARSIMONIUS;
	
	protected SimulationSteadyStateControlCenter	center				= null;
	
	protected FluxValueMap							referenceFD			= null;
	
	public SolutionSimplification(
		ISteadyStateModel model,
		List<IObjectiveFunction> objFunctions,
		String methodType,
		FluxValueMap referenceFD,
		EnvironmentalConditions envCond,
		SolverType solver) {
		this.model = model;
		this.objectiveFunctions = objFunctions;
		this.methodType = methodType;
		this.center = new SimulationSteadyStateControlCenter(envCond, null, model, methodType);
		this.center.setMaximization(true);
		this.center.setSolver(solver);
		this.center.setWTReference(referenceFD);
		
	}
	
	public SolutionSimplification(
		ISteadyStateModel model,
		List<IObjectiveFunction> objFunctions,
		String methodType,
		FluxValueMap referenceFD,
		EnvironmentalConditions envCond,
		SolverType solver,
		String fbaObjective) {
		this(model, objFunctions, methodType, referenceFD, envCond, solver);
		center.setFBAObjSingleFlux(fbaObjective, 1.0);
		if (referenceFD == null && !fbaObjective.equals(SimulationMethodsEnum.PFBA.getSimulationProperty()))
			try {
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
		
		SteadyStateSimulationResult origRes;
		if (initialRes == null) {
			center.setGeneticConditions(initialSolution);
			origRes = center.simulate();
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
		
		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(finalRes, finalFitnesses);
		
		return optimizationSimplification;
	}
	
	public SteadyStateOptimizationResult simplifySteadyStateOptimizationResult(SteadyStateOptimizationResult optResultIN, boolean isGeneOpt) throws Exception {
		SteadyStateOptimizationResult optResultOut = new SteadyStateOptimizationResult(model, objectiveFunctions);
		
		for (String id : optResultIN.getSimulationMap().keySet()) {
			
			try {
				SteadyStateSimulationResult resOrig = optResultIN.getSimulationResult(id);
				SolutionSimplificationResult simp = null;
				if (isGeneOpt)
					simp = simplifyGenesSolution(resOrig.getGeneticConditions(), resOrig);
				else
					simp = simplifyReactionsSolution(resOrig.getGeneticConditions(), resOrig);
				
				SteadyStateSimulationResult resOut = simp.getSimulationResult();
				double[] fits = simp.getFitnesses();
				
				ArrayList<Double> fitnesses = new ArrayList<Double>();
				for (double f : fits)
					fitnesses.add(f);
				
				optResultOut.addOptimizationResultNoRepeated(resOut, fitnesses);
			} catch (InfeasibleProblemException inf) {
				inf.printStackTrace();
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
		
		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(finalRes, finalFitnesses);
		
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
				if (fitnesses[i] - simplifiedFitness[i] > delta)
					res = false;
			} else if (simplifiedFitness[i] - fitnesses[i] > delta)
				res = false;
			i++;
		}
		
		return res;
	}
	
}
