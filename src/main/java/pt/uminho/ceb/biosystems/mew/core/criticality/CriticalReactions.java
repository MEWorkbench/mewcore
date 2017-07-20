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
package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public class CriticalReactions implements Serializable {
	
	private static final long serialVersionUID = 1L;

	double minimalValue = 0.05; // value that determines if gene is critical; given as percentage of biomass
	
	protected ISteadyStateModel model;

	protected SimulationSteadyStateControlCenter center = null;

	protected FluxValueMap wildTypeFluxes = null;
	
	protected List<String> criticalReactionIds = null;

	
	public CriticalReactions(){	}	
	
	public CriticalReactions(ISteadyStateModel model, 
			EnvironmentalConditions env, 
			String solver)
	{
		this.model = model;
		center = new SimulationSteadyStateControlCenter(env, null, model, SimulationProperties.FBA);
		center.setSolver(solver);
		center.setMaximization(true);
	}	
	
	public EnvironmentalConditions getEnvConditions(){
		return center.getEnvironmentalConditions();
	}


	public void setEnvConditions(EnvironmentalConditions envConditions) throws Exception  {
		center.setEnvironmentalConditions(envConditions);
	}


	public void loadCriticalReactionsFromFile(String filename) throws Exception
	{
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		
		criticalReactionIds = new ArrayList<String>();
		
		while(br.ready())
		{
			String str = br.readLine().trim();
			
			if(!criticalReactionIds.contains(str) && model.getReactionIndex(str)!=-1)
				criticalReactionIds.add(str);
		}
		
		br.close();
		fr.close();
		
	}

	public boolean removeCriticalReactions(String reactionId){
		return criticalReactionIds.remove(reactionId);
	}
	
	public void identifyCriticalReactions() throws Exception
	{		
		center.addProperty(SimulationProperties.IS_MAXIMIZATION, true);
		wildTypeFluxes = center.simulate().getFluxValues();
		
		criticalReactionIds = new ArrayList<String>();
		
		for(int i=0; i < model.getNumberOfReactions(); i++)
		{
			String reactionId = model.getReactionId(i);
			System.out.println("crit ["+i+"/"+model.getNumberOfReactions()+"]");
			if(wildTypeFluxes.getValue(reactionId) != 0.0 && isReactionCritical(reactionId))
				criticalReactionIds.add(reactionId);
		}
	}
	
	public void identifyCriticalReactionIgnoring(Set<String> ignore) throws Exception {
		center.addProperty(SimulationProperties.IS_MAXIMIZATION, true);
		wildTypeFluxes = center.simulate().getFluxValues();
		
		criticalReactionIds = new ArrayList<String>();
		
		for (int i = 0; i < model.getNumberOfReactions(); i++) {
			String reactionId = model.getReactionId(i);
			
			if ((ignore==null || !ignore.contains(reactionId)) && wildTypeFluxes.getValue(reactionId) != 0.0 && isReactionCritical(reactionId)){
				criticalReactionIds.add(reactionId);
			}
		}
	}

	
	public void identifyCriticalReactions(String targetId) throws Exception
	{
		center.setFBAObjSingleFlux(targetId, 1.0);
		wildTypeFluxes = center.simulate().getFluxValues();
				
		criticalReactionIds = new ArrayList<String>();
		
		for(int i=0; i < model.getNumberOfReactions(); i++)
		{
			String reactionId = model.getReactionId(i);
			if(wildTypeFluxes.getValue(reactionId) != 0.0 && isReactionCritical(reactionId, targetId)){
				criticalReactionIds.add(reactionId);				
			}
		}
	}

	
	public boolean isReactionCritical (String reactionId) throws Exception{
		
		List<String> knockoutList = new ArrayList<String>();
		knockoutList.add(reactionId);
		
		if (this.wildTypeFluxes == null)
			wildTypeFluxes = center.simulate().getFluxValues();
		
		center.setReactionsKnockoutConditions(new HashSet<String>(knockoutList));
		double wtBiomass = this.wildTypeFluxes.get(model.getBiomassFlux());
		
		SteadyStateSimulationResult result = center.simulate();
		
		if(result == null)	return true;
		
		FluxValueMap fluxes = result.getFluxValues();

		if(fluxes.getValue(model.getBiomassFlux()) >= minimalValue*wtBiomass)
			return false;
		else 
			return true;
			
	}
	
	public boolean isReactionCritical (String reactionId, String targetId) throws Exception
	{
		List<String> knockoutList = new ArrayList<String>();
		knockoutList.add(reactionId);

		center.setFBAObjSingleFlux(targetId, 1.0);

		if (this.wildTypeFluxes == null)
			wildTypeFluxes = center.simulate().getFluxValues();
		double wtTargetFlux = wildTypeFluxes.getValue(targetId);

		center.setReactionsKnockoutConditions(new HashSet<String>(knockoutList));						
		SteadyStateSimulationResult result = center.simulate();
		if(result == null) return true;
		
		FluxValueMap fluxes = result.getFluxValues();

		if(fluxes.getValue(targetId) >= minimalValue*wtTargetFlux)
			return false;
		else 
			return true;		
	}
	
	public void addCriticalReaction (String reactionId)
	{
		getCriticalReactionIds().add(reactionId);
	}
	
	public int numberCriticalReactions ()
	{
		return criticalReactionIds.size();
	}
	
	public ISteadyStateModel getModel() {
		return model;
	}

	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}

	public String getSimulationMethod() {
		return center.getMethodType();
	}

	public void setSimulationMethod(String methodType) {
		this.center.setMethodType(methodType);
	}

	public List<String> getCriticalReactionIds() {
		if(criticalReactionIds == null)
			criticalReactionIds = new ArrayList<String>();
		return criticalReactionIds;
	}

	public void setCriticalReactionIds(List<String> criticalReactionIds) {
		this.criticalReactionIds = criticalReactionIds;
	}
	
	public void addCriticalReactionIds(List<String> criticalReactionIds) {
		for(String id: criticalReactionIds)
			if(!getCriticalReactionIds().contains(id))
				getCriticalReactionIds().add(id);
	}
	
	public void setTransportReactionsAsCritical(){
		List<String> tr = model.identifyTransportReactionsFromStoichiometry();
		addCriticalReactionIds(tr); //FIXME: use container to improve performance
	}
	
	public void setDrainReactionsAsCritical(){
		List<String> drain = model.identifyDrainReactionsFromStoichiometry();		
		addCriticalReactionIds(drain); //FIXME: use container to improve performance
	}
	
	public void writeCriticalReactionsToFile (String filePath) throws Exception
	{
		FileWriter fr = new FileWriter(filePath);
		BufferedWriter br = new BufferedWriter(fr);
	
		for(int i=0; i < numberCriticalReactions(); i++)
			br.write(criticalReactionIds.get(i)+"\n");
		
		br.close();
		fr.close();
	}
	
	public void setMinimalValue (double value)
	{
		this.minimalValue = value;
	}
	
	
	@Override
	public CriticalReactions clone(){
		
		CriticalReactions cr = new CriticalReactions(this.model,this.getEnvConditions(),(String) this.getCenter().getProperty(SimulationProperties.SOLVER));
		
		cr.setMinimalValue(this.minimalValue);		
		cr.setWildTypeFluxes(this.getWildTypeFluxes());
		
		List<String> crIDS = new ArrayList<String>();
		for(String id: this.getCriticalReactionIds())
			crIDS.add(id);
		
		cr.setCriticalReactionIds(crIDS);
		
		return cr;
	}

	/**
	 * @return the center
	 */
	public SimulationSteadyStateControlCenter getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(SimulationSteadyStateControlCenter center) {
		this.center = center;
	}

	/**
	 * @return the wildTypeFluxes
	 */
	public FluxValueMap getWildTypeFluxes() {
		return wildTypeFluxes;
	}

	/**
	 * @param wildTypeFluxes the wildTypeFluxes to set
	 */
	public void setWildTypeFluxes(FluxValueMap wildTypeFluxes) {
		this.wildTypeFluxes = wildTypeFluxes;
	}

}
