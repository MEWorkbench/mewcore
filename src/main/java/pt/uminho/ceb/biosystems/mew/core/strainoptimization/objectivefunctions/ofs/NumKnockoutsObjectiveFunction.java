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
package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class NumKnockoutsObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "NK";
	
	public static final String NK_PARAM_MAXIMIZATION = "Maximization";
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(NK_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public NumKnockoutsObjectiveFunction() {
		super();
	}
	
	public NumKnockoutsObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public NumKnockoutsObjectiveFunction(Boolean maximize) {
		super(maximize);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(NK_PARAM_MAXIMIZATION, params[0]);
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		Boolean maximize = (Boolean) getParameterValue(NK_PARAM_MAXIMIZATION);
		
		Debugger.debug(">>>>>>>> N kos: " + simResult.getGeneticConditions().getReactionList().getReactionKnockoutList().size());
		
		int numKnockouts;
		if (simResult.getGeneticConditions().getGeneList() != null) {
			numKnockouts = simResult.getGeneticConditions().getGeneList().size();
		} else {
			numKnockouts = simResult.getGeneticConditions().getReactionList().size();
		}
		
		Debugger.debug("NK = " + numKnockouts);
		
		if (maximize)
			return numKnockouts;
		else {
			return (1.0 / (new Double(numKnockouts) + 1));
		}
	}
	
	@Override
	public double getWorstFitness() {
		return -Double.MAX_VALUE; //NOTE: the worst fitness is always negative infinity.
	}
	
	public String toString() {
		String objSense = (Boolean) (getParameterValue(NK_PARAM_MAXIMIZATION)) ? "Maximize " : "Minimize ";
		return objSense + "Number of knockouts";
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(NK_PARAM_MAXIMIZATION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		double ret = (Boolean) (getParameterValue(NK_PARAM_MAXIMIZATION)) ? fit : ((1 / fit) - 1);
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
		String sense = (Boolean) getParameterValue(NK_PARAM_MAXIMIZATION) ? "max" : "min";
		return "NK = " + sense + "\\;\\#Knockouts";
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(NK_PARAM_MAXIMIZATION) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
