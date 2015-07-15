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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class SumOfFluxesObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long serialVersionUID = 1L;

	private boolean maximize = false;
	private Set<String> fluxesToSum= null;
	
	public SumOfFluxesObjectiveFunction(boolean maximize, Set<String> fluxesToSum){
		this.maximize = maximize;
		this.fluxesToSum= fluxesToSum;
	}
	
	public SumOfFluxesObjectiveFunction(boolean maximize, String... fluxesToSum){
		this.maximize = maximize;
		this.fluxesToSum = new HashSet<String>(Arrays.asList(fluxesToSum));
	}
	
	public SumOfFluxesObjectiveFunction(boolean maximize){
		this(maximize, (Set<String>)null);
	}
	


	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		double sumFluxes = 0.0;
		
		if(fluxesToSum==null)
			fluxesToSum = new HashSet<>(simResult.getFluxValues().keySet());

		for(String v: fluxesToSum)
			sumFluxes += Math.abs(simResult.getFluxValues().getValue(v));
		
		if(maximize)
			return sumFluxes;
		else{
			if(sumFluxes<=1)
				return 0;
			else return Math.log10(1/sumFluxes);
		}
	}

	@Override
	public double getWorstFitness() {		
		return Double.NEGATIVE_INFINITY; //NOTE: the worst fitness is always negative infinity.
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.SUM_F;
	}
	
	public String toString(){
		String objSense = (maximize) ? "Maximize " : "Minimize ";
		return objSense + "Metabolic Activity";
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

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		double ret = (maximize) ? (fit) : (1/(Math.pow(10, fit)));
		return 0;
	}

	/* (non-Javadoc)
	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getShortString()
	 */
	@Override
	public String getShortString() {
		return String.valueOf('\u03A3')+"|flux values|";
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		String n;
		if(fluxesToSum != null)
			n = "N=" + fluxesToSum.size();
		else
			n = "N";
		String sense = maximize ? "max" : "min";
		
		return sense + "\\sum_{i}^{"+n+"} v_i";
	}

	@Override
	public String getBuilderString() {
		return getType() + "("+maximize+",["+CollectionUtils.join(fluxesToSum, ",")+"])";
	}

}
