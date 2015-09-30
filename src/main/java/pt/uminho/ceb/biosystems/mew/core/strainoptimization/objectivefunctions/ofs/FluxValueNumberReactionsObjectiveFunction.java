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

import pt.uminho.ceb.biosystems.mew.core.simulation.components.AddReactionsSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;

// Flux value divided by the number of reactions of genome
public class FluxValueNumberReactionsObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String	ID					= "FV_NR";
	public static final String	FV_NR_PARAM_PRODUCT	= "Product";
	
	protected final double worstFitness = 0;
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(FV_NR_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public FluxValueNumberReactionsObjectiveFunction() {
		super();
	}
	
	public FluxValueNumberReactionsObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public FluxValueNumberReactionsObjectiveFunction(String fluxId) {
		super(fluxId);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(FV_NR_PARAM_PRODUCT, params[0]);
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		String productId = (String) getParameterValue(FV_NR_PARAM_PRODUCT);
		FluxValueMap fluxValues = simResult.getFluxValues();
		int numReactions = ((AddReactionsSimulationResult) simResult).getAddReactiontList().size();
		return fluxValues.getValue(productId) / numReactions;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		String nameExtras = (getParameterValue(FV_NR_PARAM_PRODUCT) == null) ? (":" + getParameterValue(FV_NR_PARAM_PRODUCT)) : "";
		return getID() + nameExtras;
	}
	
	public String getFluxId() {
		return (String) getParameterValue(FV_NR_PARAM_PRODUCT);
	}
	
	@Override
	public boolean isMaximization() {
		return false;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return "(" + getParameterValue(FV_NR_PARAM_PRODUCT) + "/ #reactions)";
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "FV_NR = \\frac{" + getParameterValue(FV_NR_PARAM_PRODUCT) + "}{#reactions}";
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(FV_NR_PARAM_PRODUCT) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
}
