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
package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.AddReactionsSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

//Flux value divided by the number of reactions of genome 
public class FluxValueNumberReactionsObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String fluxId = null;
	protected final double worstFitness = 0;
	
	public FluxValueNumberReactionsObjectiveFunction(String fluxId){
		this.fluxId = fluxId;
	}
	
	@Override
	public double evaluate (SteadyStateSimulationResult simResult){
		FluxValueMap fluxValues = simResult.getFluxValues();
		int numReactions = ((AddReactionsSimulationResult)simResult).getAddReactiontList().size();
		return fluxValues.getValue(fluxId)/numReactions;
	}

	@Override
	public double getWorstFitness() {
		return worstFitness;
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.FV_NR;
	}
	
	public String toString(){
		String nameExtras = (fluxId == null) ? (":" + fluxId) : "";
		return getType().toString() + nameExtras;
	}

	public String getFluxId() {
		return fluxId;
	}

	public void setFluxId(String fluxId) {
		this.fluxId = fluxId;
	}

	@Override
	public boolean isMaximization() {
		return false;
	}

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getShortString()
	 */
	@Override
	public String getShortString() {
		return "("+fluxId+"/ #reactions)";
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		return "FV_NR = \\frac{"+fluxId+"}{#reactions}";
	}

	@Override
	public String getBuilderString() {
		return getType() + "("+fluxId+")";
	}


}
