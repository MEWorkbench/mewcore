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
package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.dspp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class DSPP_BPCYObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "DSPP_BPCY";
	public static final double	MIN_PRECISION		= 0.000000000001;
	
	public static final String	DSPP_BPCY_PARAM_BIOMASS		= "Biomass";
	public static final String	DSPP_BPCY_PARAM_PRODUCT		= "Product";
	public static final String	DSPP_BPCY_PARAM_SUBSTRATE	= "Substrate";
	
	double					substrateValue;
	protected final double	worstFitness	= Double.NEGATIVE_INFINITY;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters() {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(DSPP_BPCY_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(DSPP_BPCY_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(DSPP_BPCY_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		return Collections.unmodifiableMap(myparams);
	}
	
	public DSPP_BPCYObjectiveFunction() {
		super();
	}
	
	public DSPP_BPCYObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public DSPP_BPCYObjectiveFunction(String biomassId, String desiredFluxId, String substrateId) {
		super(biomassId, desiredFluxId, substrateId);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(DSPP_BPCY_PARAM_BIOMASS, params[0]);
		setParameterValue(DSPP_BPCY_PARAM_PRODUCT, params[1]);
		setParameterValue(DSPP_BPCY_PARAM_SUBSTRATE, params[2]);
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		FluxValueMap fluxValuesOuter = simResult.getFluxValues();
		FluxValueMap fluxValuesInner = (FluxValueMap) simResult.getComplementaryInfoReactions().get(SimulationProperties.WT_REFERENCE);
		
		String biomassID = (String) getParameterValue(DSPP_BPCY_PARAM_BIOMASS);
		String productID = (String) getParameterValue(DSPP_BPCY_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(DSPP_BPCY_PARAM_SUBSTRATE);
		
		Debugger.debug(biomassID + "/" + productID + "/" + substrateID);
		double biomassValue = Math.abs(fluxValuesInner.getValue(biomassID));
		double desiredFlux = Math.abs(fluxValuesOuter.getValue(productID));
		if (substrateID != null)
			substrateValue = Math.abs(fluxValuesOuter.getValue(substrateID));
		else
			substrateValue = 1.0;
			
		if (substrateValue < MIN_PRECISION) return Double.NaN;
		
		double fitness = (biomassValue * desiredFlux) / substrateValue;
		Debugger.debug(biomassID + ":" + biomassValue + "|" + productID + ":" + desiredFlux + "|" + substrateID + ":" + substrateValue + "|F:" + fitness);
		return fitness;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		return "BPCY : (" + getParameterValue(DSPP_BPCY_PARAM_BIOMASS) + " x " + getParameterValue(DSPP_BPCY_PARAM_PRODUCT) + ") / " + getParameterValue(DSPP_BPCY_PARAM_SUBSTRATE);
	}
	
	public String getBiomass() {
		return (String) getParameterValue(DSPP_BPCY_PARAM_BIOMASS);
	}
	
	public String getDesiredFlux() {
		return (String) getParameterValue(DSPP_BPCY_PARAM_PRODUCT);
	}
	
	public String getSubstrate() {
		return (String) getParameterValue(DSPP_BPCY_PARAM_SUBSTRATE);
	}
	
	public double getSubstrateValue() {
		return substrateValue;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return getID();
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "BPCY=max \\frac{\\text{" + getParameterValue(DSPP_BPCY_PARAM_BIOMASS) + "} \\times \\text{" + getParameterValue(DSPP_BPCY_PARAM_PRODUCT) + "} } {\\text{" + getParameterValue(DSPP_BPCY_PARAM_SUBSTRATE) + "}}";
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(DSPP_BPCY_PARAM_BIOMASS) + "," + getParameterValue(DSPP_BPCY_PARAM_PRODUCT) + "," + getParameterType(DSPP_BPCY_PARAM_SUBSTRATE) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}

}
