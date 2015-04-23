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
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.Debugger;

public class FluxValueObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String fluxId = null;
	protected boolean isMaximization = true;
	protected final double worstFitness = 0;
//	public FluxValueObjectiveFunction(String id){
//		this.fluxId = id;
//	}
	
	public FluxValueObjectiveFunction(String id, Boolean isMaximization){
		this.fluxId = id;
		this.isMaximization = isMaximization;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		FluxValueMap fluxValues = simResult.getFluxValues();
		Debugger.debug(fluxId + ": " + fluxValues.getValue(fluxId) + "\t" + simResult.getReactionList().keySet());
		return fluxValues.getValue(fluxId);
	}

	@Override
	public double getWorstFitness() {
		return worstFitness;
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.FV;
	}
	
	public String toString(){
		String objSense = (isMaximization) ? "Maximize " : "Minimize ";
		return objSense+" "+fluxId;
	}

	public String getFluxId() {
		return fluxId;
	}

	public void setFluxIndex(String fluxId) {
		this.fluxId = fluxId;
	}

	@Override
	public boolean isMaximization() {
		return isMaximization;
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
		return fluxId;
	}

	public String getDesiredId() {
		return fluxId;
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		String sense = isMaximization ? "max" : "min";
		return "FV = "+sense+"\\;\\text{"+fluxId+"}";
	}

	@Override
	public String getBuilderString() {
		return getType() + "("+fluxId+","+isMaximization+")";
	}


}
