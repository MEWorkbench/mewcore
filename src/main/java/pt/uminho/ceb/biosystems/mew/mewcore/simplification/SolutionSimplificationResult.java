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
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class SolutionSimplificationResult implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected SteadyStateSimulationResult simulationResult;
	protected GeneticConditions simplifiedSolution;
	protected double[] fitnesses;
	protected List<IObjectiveFunction> objectiveFunctions;
	
	public SolutionSimplificationResult(SteadyStateSimulationResult simInitial, GeneticConditions newGC, 
			List<IObjectiveFunction> objectiveFunctions, double[] fitnesses) {
		this.simulationResult = simInitial;
		this.simplifiedSolution = newGC;
		this.objectiveFunctions = objectiveFunctions;
		this.fitnesses = fitnesses;
	}
	
	public SolutionSimplificationResult(SteadyStateSimulationResult simResult, double[] fitnesses){
		this(simResult, null, null, fitnesses);		
	}
	

	public SteadyStateSimulationResult getSimulationResult() {
		return simulationResult;
	}
	
	public void setSimulationResult(SteadyStateSimulationResult simulationResult) {
		this.simulationResult = simulationResult;
	}
	
	public GeneticConditions getSimplifiedSolution() {
		return simplifiedSolution;
	}
	
	public void setSimplifiedSolution(GeneticConditions simplifiedSolution) {
		this.simplifiedSolution = simplifiedSolution;
	}

	/**
	 * @return the fitnesses
	 */
	public double[] getFitnesses() {
		return fitnesses;
	}
	
	
	
}
