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
package pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.Compartment;
import pt.uminho.ceb.biosystems.mew.core.model.components.Gene;
import pt.uminho.ceb.biosystems.mew.core.model.components.GeneReactionRule;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.Metabolite;
import pt.uminho.ceb.biosystems.mew.core.model.components.Pathway;
import pt.uminho.ceb.biosystems.mew.core.model.components.Protein;
import pt.uminho.ceb.biosystems.mew.core.model.components.ProteinReactionRule;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ModelType;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.IModelInformation;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.TreeUtils;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;

public class SteadyStateGeneReactionModel extends SteadyStateModel implements ISteadyStateGeneReactionModel, IModelInformation, Serializable{
	private static final long serialVersionUID = 1L;
	
	protected IndexedHashMap<String, Gene> genes;
	protected IndexedHashMap<String, Protein> proteins;
	protected IndexedHashMap<String, GeneReactionRule> geneReactionRules;
	protected IndexedHashMap<String, ProteinReactionRule> proteinReactionRules;
	protected HashMap<String,ArrayList<String>> geneReactionMapping = null;
	

	public SteadyStateGeneReactionModel(String modelId,
								IStoichiometricMatrix stoichiometricMatrix,
								IndexedHashMap<String, Reaction> reactions,
								IndexedHashMap<String, Metabolite> metabolites,
								Map<String, Compartment> compartments,
								IndexedHashMap<String, Pathway> pathways,
								IndexedHashMap<String, Gene> genes,
								IndexedHashMap<String, Protein> proteins,
								IndexedHashMap<String,GeneReactionRule> geneReactionRules,
								IndexedHashMap<String,ProteinReactionRule> proteinReactionRules
									) throws InvalidSteadyStateModelException {
		
		super(modelId, stoichiometricMatrix, reactions, metabolites, compartments, pathways);
		this.genes = genes;
		this.proteins = proteins;
		this.geneReactionRules = geneReactionRules;
		this.proteinReactionRules = proteinReactionRules;
		calculateGeneReactionMapping();
	}

//	public SteadyStateGeneReactionModel(String modelId,
//								IStoichiometricMatrix stoichiometricMatrix,
//								IndexedHashMap<String, Reaction> reactions,
//								IndexedHashMap<String, Metabolite> metabolites,
//								Map<String, Compartment> compartments,
//								IndexedHashMap<String, Gene> genes,
//								IndexedHashMap<String, Protein> proteins,
//								IndexedHashMap<String,GeneReactionRule> geneReactionRules,
//								IndexedHashMap<String,ProteinReactionRule> proteinReactionRules,
//								HashMap<String, ArrayList<String>> geneReactionMapping
//									) throws InvalidSteadyStateModelException {
//
//		super(modelId, stoichiometricMatrix, reactions, metabolites, compartments);
//		this.genes = genes;
//		this.proteins = proteins;
//		this.geneReactionRules = geneReactionRules;
//		this.proteinReactionRules = proteinReactionRules;
//		this.geneReactionMapping = geneReactionMapping;
//	}
//	
	

//	public SteadyStateGeneReactionModel(
//			String modelName,
//			IStoichiometricMatrix smatrix,
//			IndexedHashMap<String, Reaction> reactions,
//			IndexedHashMap<String, Metabolite> metabolites,
//			Map<String, Compartment> compartments,
//			IndexedHashMap<String, Gene> genes2,
//			Object proteins2,
//			IndexedHashMap<String, container.components.GeneReactionRule> geneReactionRules2,
//			Object proteinReactionRules2) {
//		// TODO Auto-generated constructor stub
//	}


	public Gene getGene(int geneIndex) {
		return genes.getValueAt(geneIndex);
	}
	
	public Gene getGene(String bNumber) {
		return genes.get(bNumber);
	}

	
	public IndexedHashMap<String, Gene> getGenes() {
		return genes;
	}

//	public GeneReactionRule getGeneReactionRule(int reactionIndex) {
//		return geneReactionRules.getValueAt(reactionIndex);
//	}

	public HashMap<String, ArrayList<String>> getGeneReactionMapping(){
		return geneReactionMapping;
	}

	public GeneReactionRule getGeneReactionRule(String reactionId) {
		return geneReactionRules.get(reactionId);
	}

	
	public IndexedHashMap<String, GeneReactionRule> getGeneReactionRules() {
		return geneReactionRules;
	}

	public Protein getProtein(int proteinIndex) {
		return proteins.getValueAt(proteinIndex);
	}

	public Protein getProtein(String proteinId) {
		return proteins.get(proteinId);
	}

	public ProteinReactionRule getProteinReactionRule(int reactionIndex) {
		return proteinReactionRules.getValueAt(reactionIndex);
	}

	public ProteinReactionRule getProteinReactionRule(String reactionId) {
		return proteinReactionRules.get(reactionId);
	}

	public IndexedHashMap<String, ProteinReactionRule> getProteinReactionRules() {
		return proteinReactionRules;
	}

	public IndexedHashMap<String, Protein> getProteins() {
		return proteins;
	}

	
	public Integer getNumberOfGenes() {
		return genes.size();
	}

	public Integer getGeneIndex(String geneId) {
		return genes.getIndexOf(geneId);
	}

	public ArrayList<String> getReactionsInfluencedByGene(String geneId){
		return geneReactionMapping.get(geneId);
	}
	
	
	private void calculateGeneReactionMapping(){
		this.geneReactionMapping = new HashMap<String,ArrayList<String>>(getNumberOfReactions());
		
		for(String reactionId : geneReactionRules.keySet()){
			
			AbstractSyntaxTree<DataTypeEnum, IValue> ruleTree = geneReactionRules.get(reactionId).getRule();
			if (ruleTree.getRootNode() != null){

				List<String> geneIds = TreeUtils.withdrawVariablesInRule(ruleTree);
				for(String geneId : geneIds){
					if(geneReactionMapping.containsKey(geneId)){
						ArrayList<String> reactionDependent = geneReactionMapping.get(geneId);
						reactionDependent.add(reactionId);
					}else{
						ArrayList<String> reactionDependent = new ArrayList<String>();
						reactionDependent.add(reactionId);
						geneReactionMapping.put(geneId, reactionDependent);
					}
				}
				
			}
			
		}	
	}


	public Boolean containsGeneReactionRule(String reactionId) {
		return this.geneReactionRules.containsKey(reactionId);
	}

	public ArrayList<String> getReactionsAssociate(String geneId) {
		return geneReactionMapping.get(geneId);
	}

	@Override
	public SteadyStateGeneReactionModel removeGeneReactionsRules(
			List<String> reactionIds) throws Exception {
		
		IndexedHashMap<String,GeneReactionRule> originalGeneReactionsRules = 
			this.getGeneReactionRules();
		
		IndexedHashMap<String,Gene> originalGenes = this.getGenes();
		
		IndexedHashMap<String,GeneReactionRule> newGeneReactionsRules = 
			new IndexedHashMap<String, GeneReactionRule>();
		IndexedHashMap<String,Gene> newGenes = 
			new IndexedHashMap<String, Gene>();
		
		for(String reaction: geneReactionRules.keySet()){
			if(!reactionIds.contains(reaction)){
//				String rule = originalGeneReactionsRules.get(reaction).getRule().toString();
//				newGeneReactionsRules.put(reaction, new GeneReactionRule(rule));
				
				newGeneReactionsRules.put(reaction, originalGeneReactionsRules.get(reaction));
				
				ArrayList<String> genesId = TreeUtils.withdrawVariablesInRule(originalGeneReactionsRules.get(reaction).getRule());
				for(int g =0; g< genesId.size();g++)
					if(!newGenes.containsKey(genesId.get(g))){
						Gene orig = originalGenes.get(genesId.get(g));
//						newGenes.put(genesId.get(g), new Gene(orig.getId(),orig.getName()));
						newGenes.put(genesId.get(g), orig);
					}
			}
		}
		
		SteadyStateGeneReactionModel newModel = new SteadyStateGeneReactionModel(this.id,this.getStoichiometricMatrix(),this.getReactions(),this.metaboliteMap,this.compartmentMap,this.pathwayMap,
				newGenes,null,newGeneReactionsRules,null);
		
		return newModel;
	}
	
	@Override
	public ModelType getModelType() {
		return ModelType.GENE_REACTION_STEADY_STATE_MODEL;
	}
}
