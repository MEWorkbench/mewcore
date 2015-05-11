/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of
 * Biological Engineering
 * CCTC - Computer Science and Technology Center
 * University of Minho
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Public License for more details.
 * You should have received a copy of the GNU Public License
 * along with this code. If not, see http://www.gnu.org/licenses/
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.Debugger;

public class NumKnockoutsObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long	serialVersionUID	= 1L;
	
	private boolean				maximize			= false;
	
	public NumKnockoutsObjectiveFunction(Boolean maximize) {
		this.maximize = maximize;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		Debugger.debug(">>>>>>>> N kos: " + simResult.getGeneticConditions().getReactionList().getReactionKnockoutList().size());			

		int numKnockouts;
		if(simResult.getGeneticConditions().getGeneList()!=null){
			numKnockouts = simResult.getGeneticConditions().getGeneList().size();
		}
		else{
			numKnockouts = simResult.getGeneticConditions().getReactionList().size();
		}
		
		Debugger.debug("NK = "+numKnockouts);
		
		if(maximize)
			return numKnockouts;
		else{			
			return ( 1.0/ ( new Double(numKnockouts) +1 ));
		}
	}
	
	@Override
	public double getWorstFitness() {
		//		return (maximize) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		return -Double.MAX_VALUE; //NOTE: the worst fitness is always negative infinity.
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.NK;
	}
	
	public String toString() {
		String objSense = (maximize) ? "Maximize " : "Minimize ";
		return objSense + "Number of knockouts";
	}
	
	public boolean isMaximize() {
		return maximize;
	}
	
	public void setMaximize(boolean maximize) {
		this.maximize = maximize;
	}
	
	@Override
	public boolean isMaximization() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		double ret = (maximize) ? fit : ((1 / fit) - 1);
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getShortString()
	 */
	@Override
	public String getShortString() {
		return "#knockouts";
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		String sense = maximize ? "max" : "min";
		return "NK = " + sense + "\\;\\#Knockouts";
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + maximize + ")";
	}
	
}
