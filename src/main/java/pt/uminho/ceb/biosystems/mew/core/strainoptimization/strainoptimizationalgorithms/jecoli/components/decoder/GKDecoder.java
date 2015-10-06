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
package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.SetRepresentation;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;

public class GKDecoder extends RKDecoder {
	
	private static final long serialVersionUID = 1L;

	public GKDecoder(ISteadyStateGeneReactionModel model) {
		super(model);
	}
	
	public GKDecoder(ISteadyStateGeneReactionModel model, List<Integer> notAllowedGeneKnockouts) {
		super(model, notAllowedGeneKnockouts);
	}
	
	public int getNumberVariables() {
		if (this.notAllowedKnockouts == null)
			return ((ISteadyStateGeneReactionModel) model).getNumberOfGenes();
		else
			return ((ISteadyStateGeneReactionModel) model).getNumberOfGenes() - notAllowedKnockouts.size();
	}
	
	public int getInitialNumberVariables() {
		return ((ISteadyStateGeneReactionModel) model).getNumberOfGenes();
	}
	
	public void createNotAllowedGenesFromIds(List<String> notAllowedGeneIds) throws Exception {
		notAllowedKnockouts = new ArrayList<Integer>(notAllowedGeneIds.size());
		
		Iterator<String> it = notAllowedGeneIds.iterator();
		while (it.hasNext()) {
			
			String nextId = it.next();
			
			notAllowedKnockouts.add(((ISteadyStateGeneReactionModel) model).getGeneIndex(nextId));
		}
		createInternalDecodeTable();
	}
	
	public List<Integer> decodeGeneKnockouts(TreeSet<Integer> genome) throws Exception {
		List<Integer> geneKnockoutList = new ArrayList<Integer>();
		
		Iterator<Integer> it = genome.iterator();
		while (it.hasNext())
			geneKnockoutList.add(convertValue(it.next()));
		
		return geneKnockoutList;
	}
	
	public GeneticConditions decode(IRepresentation solution) throws Exception {
		TreeSet<Integer> genome = ((SetRepresentation) solution).getGenome();
		
		List<Integer> geneKnockoutList = decodeGeneKnockouts(genome);
		
		GeneChangesList gcl = new GeneChangesList(geneKnockoutList, (ISteadyStateGeneReactionModel) model);		
		
		return new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) model, false);
	}
}
