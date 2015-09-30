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

import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class FluxValueObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "FV_NR";
	
	public static final String	FV_NR_PARAM_PRODUCT			= "Product";
	public static final String	FV_NR_PARAM_MAXIMIZATION	= "Maximization";
	protected final double		worstFitness				= 0;
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(FV_NR_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(FV_NR_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public FluxValueObjectiveFunction() {
		super();
	}
	
	public FluxValueObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public FluxValueObjectiveFunction(String id, Boolean isMaximization) {
		super(id, isMaximization);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(FV_NR_PARAM_PRODUCT, params[0]);
		setParameterValue(FV_NR_PARAM_MAXIMIZATION, params[1]);
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		String productID = (String) getParameterValue(FV_NR_PARAM_PRODUCT);
		FluxValueMap fluxValues = simResult.getFluxValues();
		Double fluxValue = fluxValues.getValue(productID);
		Debugger.debug(productID + ": " + fluxValue + "\t" + simResult.getReactionList().keySet());
		return fluxValue;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		String objSense = (boolean) (getParameterValue(FV_NR_PARAM_MAXIMIZATION)) ? "Maximize " : "Minimize ";
		return objSense + " " + getParameterValue(FV_NR_PARAM_PRODUCT);
	}
	
	public String getFluxId() {
		return (String) getParameterValue(FV_NR_PARAM_PRODUCT);
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(FV_NR_PARAM_MAXIMIZATION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#
	 * getShortString()
	 */
	@Override
	public String getShortString() {
		return ID;
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		String sense = (boolean) getParameterValue(FV_NR_PARAM_MAXIMIZATION) ? "max" : "min";
		return "FV = " + sense + "\\;\\text{" + getParameterValue(FV_NR_PARAM_PRODUCT) + "}";
	}
	
	@Override
	public String getBuilderString() {
		return ID + "(" + getParameterValue(FV_NR_PARAM_PRODUCT) + "," + getParameterValue(FV_NR_PARAM_MAXIMIZATION) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
