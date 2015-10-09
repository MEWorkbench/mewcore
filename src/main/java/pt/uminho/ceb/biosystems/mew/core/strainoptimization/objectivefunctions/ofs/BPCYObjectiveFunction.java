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

public class BPCYObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String	ID						= "BPCY";
	public static final double	MIN_PRECISION			= 0.000000000001;
	public static final String	BPCY_PARAM_BIOMASS		= "Biomass";
	public static final String	BPCY_PARAM_PRODUCT		= "Product";
	public static final String	BPCY_PARAM_SUBSTRATE	= "Substrate";
	protected final double		worstFitness			= Double.NEGATIVE_INFINITY;
	protected double			substrateValue;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		HashMap<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(BPCY_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(BPCY_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(BPCY_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		return Collections.unmodifiableMap(myparams);
	}
	
	public BPCYObjectiveFunction() {
		super();
	}
	
	public BPCYObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public BPCYObjectiveFunction(String biomassId, String desiredFluxId, String substrateId) {
		super(biomassId, desiredFluxId, substrateId);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(BPCY_PARAM_BIOMASS, params[0]);
		setParameterValue(BPCY_PARAM_PRODUCT, params[1]);
		setParameterValue(BPCY_PARAM_SUBSTRATE, params[2]);
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		FluxValueMap fluxValues = simResult.getFluxValues();
		String biomassId = (String) getParameterValue(BPCY_PARAM_BIOMASS);
		String desiredFluxId = (String) getParameterValue(BPCY_PARAM_PRODUCT);
		String substrateId = (String) getParameterValue(BPCY_PARAM_SUBSTRATE);
		Debugger.debug(biomassId + "/" + desiredFluxId + "/" + substrateId);
		double biomassValue = Math.abs(fluxValues.getValue(biomassId));
		double desiredFlux = Math.abs(fluxValues.getValue(desiredFluxId));
		if (substrateId != null)
			substrateValue = Math.abs(fluxValues.getValue(substrateId));
		else
			substrateValue = 1.0;
			
		if (substrateValue < MIN_PRECISION) return Double.NaN;
		
		double fitness = (biomassValue * desiredFlux) / substrateValue;
		Debugger.debug(biomassId + ":" + biomassValue + "|" + desiredFluxId + ":" + desiredFlux + "|" + substrateId + ":" + substrateValue + "|F:" + fitness);
		return fitness;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		return "BPCY : (" + getParameterValue(BPCY_PARAM_BIOMASS) + " x " + getParameterValue(BPCY_PARAM_PRODUCT) + ") / " + getParameterValue(BPCY_PARAM_SUBSTRATE);
	}
	
	public String getBiomass() {
		return (String) getParameterValue(BPCY_PARAM_BIOMASS);
	}
	
	public void setBiomass(String biomassId) {
		setParameterValue(BPCY_PARAM_BIOMASS, biomassId);
	}
	
	public String getDesiredFlux() {
		return (String) getParameterValue(BPCY_PARAM_PRODUCT);
	}
	
	public void setDesiredFlux(String desiredFluxId) {
		setParameterValue(BPCY_PARAM_PRODUCT, desiredFluxId);
	}
	
	public String getSubstrate() {
		return (String) getParameterValue(BPCY_PARAM_SUBSTRATE);
	}
	
	public void setSubstrate(String substrateId) {
		setParameterValue(BPCY_PARAM_SUBSTRATE, substrateId);
	}
	
	public double getSubstrateValue() {
		return substrateValue;
	}
	
	public void setSubstrateValue(double substrateValue) {
		this.substrateValue = substrateValue;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getShortString()
	 */
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "BPCY=max \\frac{\\text{" + getParameterValue(BPCY_PARAM_BIOMASS) + "} \\times \\text{" + getParameterValue(BPCY_PARAM_PRODUCT) + "} } {\\text{" + getParameterValue(BPCY_PARAM_SUBSTRATE) + "}}";
	}
	
	@Override
	public String getBuilderString() {
		return "BPCY (" + getParameterValue(BPCY_PARAM_BIOMASS) + "," + getParameterValue(BPCY_PARAM_PRODUCT) + "," + getParameterValue(BPCY_PARAM_SUBSTRATE) + ")";
	}
	
	@Override
	public String getShortString() {
		return getID();
	}
	
}
