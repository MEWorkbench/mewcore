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
package pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Gene;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.GeneReactionRule;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Protein;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ProteinReactionRule;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public interface ISteadyStateGeneReactionModel extends ISteadyStateModel {
	
	Integer getNumberOfGenes();
	
	IndexedHashMap<String, Gene> getGenes();
	Gene getGene(int geneIndex);
	Gene getGene(String geneId);
	Integer getGeneIndex(String geneId);
	
	///IndexedHashMap<String,Protein> getProteins();
	Protein getProtein(int proteinIndex);
	Protein getProtein(String proteinId);
	
	//TODO: tirar reactionIndex
	IndexedHashMap<String,GeneReactionRule> getGeneReactionRules();
	//GeneReactionRule getGeneReactionRule(int reactionIndex);
	GeneReactionRule getGeneReactionRule(String reactionId);
	
	//IndexedHashMap<String,ProteinReactionRule> getProteinReactionRules();
	ProteinReactionRule getProteinReactionRule(int reactionIndex);
	ProteinReactionRule getProteinReactionRule(String reactionId);
	

	ArrayList<String> getReactionsInfluencedByGene(String geneId);
	Boolean containsGeneReactionRule(String reactionId);
	
	SteadyStateGeneReactionModel removeGeneReactionsRules (List<String> reactionIds) throws Exception;
}
