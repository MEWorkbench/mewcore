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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CriticalGenes extends CriticalReactions {
	
	private static final long serialVersionUID = 1L;
	
	List<String> criticalGenesIds = null;

	public CriticalGenes(ISteadyStateModel model, 
			EnvironmentalConditions env, 
			SolverType solver)
	{
		super(model, env, solver);
	}
	
	public void loadCriticalGenesFromFile(String filename) throws Exception{
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		
		criticalGenesIds = new ArrayList<String>();
		
		while(br.ready()){
			String str = br.readLine().trim();
			
			if(((ISteadyStateGeneReactionModel)model).getGeneIndex(str)!=-1)
				criticalGenesIds.add(str);
		}
		
		br.close();
		fr.close();
	}

	
	public void identifyCriticalGenes() throws Exception
	{
		criticalGenesIds = new ArrayList<String>();
		
		center.addProperty(SimulationProperties.IS_MAXIMIZATION, true);
		wildTypeFluxes = center.simulate().getFluxValues();
		
		for(int i=0; i < ((ISteadyStateGeneReactionModel)model).getNumberOfGenes(); i++){
			String geneId = ((ISteadyStateGeneReactionModel)model).getGene(i).getId();
			if(isGeneCritical(geneId)){ 
				criticalGenesIds.add(geneId);
			}
		}
	}
	
	
	public boolean removeCriticalGene(String geneId){
		
		return criticalGenesIds.remove(geneId);
	}
	
	
	public boolean isGeneCritical (String geneId) throws Exception
	{
		List<String> geneKnockoutList = new ArrayList<String>();
		geneKnockoutList.add(geneId);
		
		center.setGenesKnockoutConditions(new HashSet<String>(geneKnockoutList));
		
		SteadyStateSimulationResult result = center.simulate();
		
		double wtBiomass = this.wildTypeFluxes.getValue(model.getBiomassFlux());
		
		if(result == null)
			return true;
		FluxValueMap fluxes = result.getFluxValues();
		if(fluxes.getValue(model.getBiomassFlux()) >= minimalValue*wtBiomass)
			return false;
		else return true;
	}
	
	
	public void addCriticalGene (String geneId)
	{
		criticalGenesIds.add(geneId);
	}

	public int numberCriticalGenes ()
	{
		return this.criticalGenesIds.size();
	}

	public List<String> getCriticalGenesIds() {
		return criticalGenesIds;
	}

	public void setCriticalGenesIds(List<String> criticalGenesIds) {
		this.criticalGenesIds = criticalGenesIds;
	}
	
	public void writeCriticalGenesToFile (String filePath) throws Exception
	{
		FileWriter fr = new FileWriter(filePath);
		BufferedWriter br = new BufferedWriter(fr);
	
		for(int i=0; i < numberCriticalGenes(); i++)
			br.write(criticalGenesIds.get(i)+"\n");
		
		br.close();
		fr.close();
	}
	
	@Override
	public CriticalGenes clone(){
		CriticalGenes cg = new CriticalGenes(this.model,this.getEnvConditions(),(SolverType) this.getCenter().getProperty(SimulationProperties.SOLVER));
		
		List<String> cgIDS = new ArrayList<String>();
		for(String id : this.getCriticalGenesIds())
			cgIDS.add(id);
		
		cg.setCriticalGenesIds(cgIDS);
		
		return cg;
	}
}
