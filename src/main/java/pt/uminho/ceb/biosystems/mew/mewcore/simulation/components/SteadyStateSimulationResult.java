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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;


public class SteadyStateSimulationResult implements Serializable{
	
	private static final long serialVersionUID = 1L;

	protected ISteadyStateModel model;
	protected EnvironmentalConditions environmentalConditions;
	protected GeneticConditions geneticConditions;
	protected String method; 
	protected FluxValueMap fluxValues;
	protected Map<String,MapStringNum> complementaryInfoReactions = null;
	protected Map<String,MapStringNum> complementaryInfoMetabolites = null;
	protected String solverOutput;
	protected Double OFvalue;
	protected String OFString;
	protected LPSolutionType solutionType;

	public LPSolutionType getSolutionType() {
		return solutionType;
	}

	public void setSolutionType(LPSolutionType solutionType) {
		this.solutionType = solutionType;
	}

	public SteadyStateSimulationResult(ISteadyStateModel model,
			String method, FluxValueMap fluxValues)
	{
		this.model = model;
		this.method = method;
		this.fluxValues = fluxValues;
	}
	
	public SteadyStateSimulationResult(ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions,
			String method, FluxValueMap fluxValues,
			String solverOutput, 
			Double oFvalue, 
			String oFString,
			LPSolutionType solutionType) {
		super();
		this.model = model;
		this.environmentalConditions = environmentalConditions;
		this.geneticConditions = geneticConditions;
		this.method = method;
		this.fluxValues = fluxValues;
		this.solverOutput = solverOutput;
		OFvalue = oFvalue;
		OFString = oFString;
		this.solutionType = solutionType;
	}

	public ISteadyStateModel getModel() {
		return model;
	}

	public EnvironmentalConditions getEnvironmentalConditions() {
		return environmentalConditions;
	}

	public GeneticConditions getGeneticConditions() {
		return geneticConditions;
	}

	public String getMethod() {
		return method;
	}

	public FluxValueMap getFluxValues() {
		return fluxValues;
	}

	public Map<String, MapStringNum> getComplementaryInfoReactions() {
		return complementaryInfoReactions;
	}

	public Map<String, MapStringNum> getComplementaryInfoMetabolites() {
		return complementaryInfoMetabolites;
	}

	public String getSolverOutput() {
		return solverOutput;
	}

	public double getOFvalue() {
		return OFvalue;
	}

	public String getOFString() {
		return OFString;
	}

	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}

	public void setEnvironmentalConditions(
			EnvironmentalConditions environmentalConditions) {
		this.environmentalConditions = environmentalConditions;
	}

	public void setGeneticConditions(GeneticConditions geneticConditions) {
		this.geneticConditions = geneticConditions;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setFluxValues(FluxValueMap fluxValues) {
		this.fluxValues = fluxValues;
	}

	public void setComplementaryInfoReactions(
			Map<String, MapStringNum> complementaryInfoReactions) {
		this.complementaryInfoReactions = complementaryInfoReactions;
	}

	public void setComplementaryInfoMetabolites(
			Map<String, MapStringNum> complementaryInfoMetabolites) {
		this.complementaryInfoMetabolites = complementaryInfoMetabolites;
	}

	public void setSolverOutput(String solverOutput) {
		this.solverOutput = solverOutput;
	}

	public void setOFvalue(double oFvalue) {
		OFvalue = oFvalue;
	}

	public void setOFString(String oFString) {
		OFString = oFString;
	}

	public ReactionChangesList getReactionList(){
		return geneticConditions.getReactionList();
	}

	public GeneChangesList getGeneList(){
		return geneticConditions.getGeneList();
	}
	
	public String getNetConversion(){
		return getNetConversion(false);
	}
	
	public void addComplementaryInfoReactions(String infoId,MapStringNum infoData){
		if(complementaryInfoReactions==null)
			complementaryInfoReactions = new HashMap<String, MapStringNum>();
		
		complementaryInfoReactions.put(infoId, infoData);
	}
	
	public void addComplementaryInfoMetabolites(String infoId,MapStringNum infoData){
		if(complementaryInfoMetabolites==null)
			complementaryInfoMetabolites = new HashMap<String, MapStringNum>();
		
		complementaryInfoMetabolites.put(infoId, infoData);
	}
	
	public String getNetConversion(boolean includeBiomass)
	{
		String left="";
		String right="";
		
		boolean firstLeft = true;
		boolean firstRight = true;
		
		if(includeBiomass && model.getBiomassFlux() == null)
			return null;
		
		for(int i=0; i < model.getNumberOfReactions(); i++)
		{
			Reaction r = model.getReaction(i);
			double fluxValue = fluxValues.get(r.getId()); 
			if (fluxValue!= 0.0 && r.getType().equals(ReactionType.DRAIN) )
			{
				int metaboliteIndex = model.getMetaboliteFromDrainIndex(i);
				if (fluxValue < 0) {
					if (firstLeft) firstLeft = false;
					else left += " + ";
					left += (-1*fluxValue);
					left += " ";
					left += model.getMetaboliteId(metaboliteIndex);
				}
				else 
				{
					if (firstRight) firstRight = false;
					else right += " + ";
					right += fluxValue + " ";
					right += model.getMetaboliteId(metaboliteIndex);
				}
			}			
		}
		
		if (includeBiomass)
		{
			Reaction rb = model.getReaction(model.getBiomassFlux());
			String rid = model.getBiomassFlux();
			double biomassFlux = fluxValues.get(rid);
			if ( biomassFlux > 0)
			{
				if (firstRight) firstRight = false;
				else right += " + ";
				right += biomassFlux + " ";
				right += rb.getId();
			}
		}
		
		return (left+ " --> " + right);
	}
	
	public Map<String, Double> getNetConversionMap(boolean includeBiomass)
	{
		Map<String, Double> ret = new HashMap<String, Double>();
		
		if(includeBiomass && model.getBiomassFlux() == null)
			return null;
		
		for(int i=0; i < model.getNumberOfReactions(); i++)
		{
			Reaction r = model.getReaction(i);
			
			
			
			
			Double fluxValue = fluxValues.get(r.getId()); 
			if (fluxValue!= null && fluxValue!= 0.0 && r.getType().equals(ReactionType.DRAIN))
			{
				int metaboliteIndex = model.getMetaboliteFromDrainIndex(i);
				ret.put(model.getMetaboliteId(metaboliteIndex), fluxValue);
			}
			
			if(fluxValue == null)
				System.out.println("flux problem: " + r.getId());
		}
		
		if (includeBiomass)
		{
			Reaction rb = model.getReaction(model.getBiomassFlux());
			String rid = model.getBiomassFlux();
			double biomassFlux = fluxValues.get(rid);
			ret.put(rid, biomassFlux);
			
		}
		
		return ret;
	}
	

// TODO ... NOT HERE 	
//	Map<String,MapStringNum> getComplementaryInfoFromLPSolution(LPSolution solution)
//	{
		// TO DO 
//		return null;
//	}
}
