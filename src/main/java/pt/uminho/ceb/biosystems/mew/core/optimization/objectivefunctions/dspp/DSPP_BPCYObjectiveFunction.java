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
package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.dspp;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.ObjectiveFunctionType;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class DSPP_BPCYObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long	serialVersionUID	= 1L;
	public static final double	MIN_PRECISION		= 0.000000000001;
	
	String						biomassId;
	String						desiredFluxId;
	String						substrateId			= null;
	double						substrateValue;
	protected final double		worstFitness		= Double.NEGATIVE_INFINITY;
	
	public DSPP_BPCYObjectiveFunction(String biomassId, String desiredFluxId, double substrateValue) {
		this.biomassId = biomassId;
		this.desiredFluxId = desiredFluxId;
		this.substrateValue = substrateValue;
	}
	
	public DSPP_BPCYObjectiveFunction(String biomassId, String desiredFluxId, String substrateId) {
		this.biomassId = biomassId;
		this.desiredFluxId = desiredFluxId;
		this.substrateId = substrateId;
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		FluxValueMap fluxValuesLMOMA = simResult.getFluxValues();
		FluxValueMap fluxValuesPFBA = (FluxValueMap) simResult.getComplementaryInfoReactions().get(SimulationProperties.WT_REFERENCE);
		
		Debugger.debug(biomassId + "/" + desiredFluxId + "/" + substrateId);
		double biomassValue = Math.abs(fluxValuesPFBA.getValue(biomassId));
		double desiredFlux = Math.abs(fluxValuesLMOMA.getValue(desiredFluxId));
		if (substrateId != null) 
			substrateValue = Math.abs(fluxValuesLMOMA.getValue(substrateId));
		else
			substrateValue = 1.0;
		
		if(substrateValue<MIN_PRECISION)
			return Double.NaN;
		
		double fitness = (biomassValue * desiredFlux) / substrateValue;
		Debugger.debug(biomassId + ":" + biomassValue + "|" + desiredFluxId + ":" + desiredFlux + "|" + substrateId + ":" + substrateValue + "|F:" + fitness);
		return fitness;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.DSPP_BPCY;
	}
	
	public String toString() {
		return "BPCY : (" + biomassId + " x " + desiredFluxId + ") / " + substrateId;
	}
	
	public String getBiomass() {
		return biomassId;
	}
	
	public void setBiomass(String biomassId) {
		this.biomassId = biomassId;
	}
	
	public String getDesiredFlux() {
		return desiredFluxId;
	}
	
	public void setDesiredFlux(String desiredFluxId) {
		this.desiredFluxId = desiredFluxId;
	}
	
	public String getSubstrate() {
		return substrateId;
	}
	
	public void setSubstrate(String substrateId) {
		this.substrateId = substrateId;
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
	public String getShortString() {
		return "BPCY";
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "BPCY=max \\frac{\\text{" + biomassId + "} \\times \\text{" + desiredFluxId + "} } {\\text{" + substrateId + "}}";
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + biomassId + "," + desiredFluxId + "," + substrateId + ")";
	}
	
}
