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
package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.Gene;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.list.ListStrings;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.Environment;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.IEnvironment;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.BooleanValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DoubleValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Mean;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Minimum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Variable;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.VariableDouble;

public class GeneChangesList extends MapStringNum implements Serializable
{
	
	private static final long serialVersionUID = 1L;

	public GeneChangesList() {
		super();
	}
	

	public GeneChangesList(List<String> genes, List<Double> expression){
		super();
		int size=genes.size();
		
		for(int i=0;i<size;i++)
			addGene(genes.get(i), expression.get(i));
		
	}
	

	public GeneChangesList(Collection<String> genes){
		super();
		
		for(String id : genes)
			addGene(id, 0.0);
		
	}

	public GeneChangesList(Collection<Integer> listIndexes, ISteadyStateGeneReactionModel model)
	{
		super();
		for(Integer index: listIndexes)
			addGene(index, 0.0, model);
	}
	
	public void setFromListPairsIndexValue (List<Pair<Integer,Double>> geneIndexes, ISteadyStateGeneReactionModel model) {
	
		for(Pair<Integer,Double> pair: geneIndexes)
			addGene(pair,model);
	}

	
	public void setFromListPairsIdValue (List<Pair<String,Double>> regulations){
		
		for(Pair<String,Double> pair : regulations)
			addGene(pair);
	}
	
// codigo a seguir nao sei para que e necessario
	
//	public GeneUnderOverList(ReactionUnderOverList reactionList, ISteadyStateGeneReactionModel model)
//	{
//		this();
//		Iterator<String> it = reactionList.getReactionIds().iterator();
//		
//		while(it.hasNext()){
//			
//			String reactionId = it.next();
//			if(model.containsGeneReactionRule(reactionId)){
//				
//				GeneReactionRule reactionRule = model.getGeneReactionRule(reactionId);
//				ArrayList<String> genesID = ContainerConverter.withdrawVariablesInRule(reactionRule.getRule());
//				Environment<IValue> environment = new Environment<IValue>();
//				
//				for(int i=0;  i<genesID.size() ;i++)
//					environment.associate(genesID.get(i), new BooleanValue(true));
//
//				if(genesID.size() == 1){ 
//					add(genesID.get(0));
//				}
//				else
//				{
//					for(int i = 0; i < genesID.size(); i++){
//						
//						if(i>0)
//							environment.associate(genesID.get(i-1), new BooleanValue(true));
//						
//						environment.associate(genesID.get(i), new BooleanValue(false));
//						boolean exp = reactionRule.getRule().evaluate(environment).getBooleanValue();
//						if(exp){
//							
//							if(!containsKey(genesID.get(i)))
//								add(genesID.get(i));
//						}
//						else{
//							
//							if(!contains(genesID.get(i)))
//								add(genesID.get(i));
//						
//							break;
//						}
//					}
//				}
//			}
//			else{
//				System.out.println("Reaction " + reactionId + " has no rule");
//			}
//		}
//
//	}
//	
	
	public void loadFromString (String strList, String separator)
	{
		String[] genesPairs = strList.trim().split(separator);
		
		for(String genePair : genesPairs)
		{	
			genePair.trim();
			String[] geneRate = genePair.split(",");
			addGene(geneRate[0],Double.valueOf(geneRate[1]));
		}
	}

	public void loadGeneKnockoutsFromString (String strList, String separator)
	{
		ListStrings geneIds = new ListStrings();
		geneIds.fromString(strList, separator);
		
		int size= geneIds.size();
		
		for(int i=0;i<size;i++)
			addGene(geneIds.get(i), 0.0);	
	}

	
	public Set<String> getGeneIds() {
		return keySet();
	}

	public List<String> getGeneKnockoutList ()
	{
		List<String> res = new ArrayList<String>();
		
		for(String s: keySet())
		{
			if(getGeneExpression(s) == 0.0)
				res.add(s);
		}
		
		return res;
	}
	
	public List<Pair<Integer,Double>> getIndexesList(ISteadyStateGeneReactionModel model) throws NonExistentIdException
	{
		List<Pair<Integer, Double>> indexList = new ArrayList<Pair<Integer,Double>>();
		
		for(String id: keySet())
			indexList.add(new Pair<Integer, Double>(model.getGeneIndex(id), get(id)));
		
		return indexList;
	}
	
	public List<Pair<String,Double>> getPairsList ()
	{
		List<Pair<String, Double>> indexList = new ArrayList<Pair<String,Double>>();
		
		for(String id: keySet())
			indexList.add(new Pair<String, Double>(id, get(id)));
		
		return indexList;
	}
	
	public void addGene(String geneID, double fluxRate){
		put(geneID, fluxRate);
		
	}
	
	public void addGene(int geneIndex,double geneRate, ISteadyStateGeneReactionModel model){
		String geneID = model.getGene(geneIndex).getId();
		addGene(geneID, geneRate);
	}
	
	public void addGene(Pair<Integer,Double> genePair,ISteadyStateGeneReactionModel model){
		addGene(genePair.getValue(),genePair.getPairValue(),model);
	}
	
	public void addGene(Pair<String,Double> genePair){
		addGene(genePair.getValue(),genePair.getPairValue());
	}

	public void addGeneKnockout (String geneId)
	{
		addGene(geneId, 0.0);
	}
	
	public void loadGeneKnockoutsFromFile(String filePath) throws Exception{
		FileReader f = new FileReader(filePath);
		BufferedReader r = new BufferedReader(f);
		
		String gene;
		while(r.ready()){
			gene = r.readLine().trim();
			
			String[] geneRate = gene.split(",");
			this.addGene(geneRate[0].trim(),Double.valueOf(geneRate[1]));
		}
		
		r.close();
		f.close();
			
	}
	
	public boolean containsGene (String geneId)
	{
		return containsKey(geneId); 
	}
	
	public boolean containsGeneKnockout (String geneId)
	{
		if (containsGene(geneId))
			return getGeneExpression(geneId)==0.0;
		else return false;

	}
	
	public void removeGene (String geneId)
	{
		remove(geneId);
	}
	
	
	public Double getGeneExpression(String geneID)
	{
		return get(geneID);
	}
	
	public ReactionChangesList getReactionUnderOverList(ISteadyStateGeneReactionModel model)
	{
		ReactionChangesList result = new ReactionChangesList();
		IEnvironment<IValue> environment = new Environment<IValue>();
		ArrayList<String> possibleInfluenced = new ArrayList<String>();
		HashMap<String, Double> reactionFluxRate = new HashMap<String, Double>();
		
		int numberOfGenes = model.getNumberOfGenes();
		for(int j=0; j<numberOfGenes ; j++)
		{	
			Gene gene = model.getGene(j);			
			if(containsKey(gene.getId())){
				
				Double expressionRate = getGeneExpression(gene.getId());
				
				environment.associate(gene.getId(),new DoubleValue(expressionRate));
				ArrayList<String> reactions = model.getReactionsInfluencedByGene(gene.getId());

					for(int k =0; k < reactions.size(); k++)
					{
						if(!possibleInfluenced.contains(reactions.get(k)))
						{
							String reactionID = reactions.get(k);
							possibleInfluenced.add(reactionID);
							reactionFluxRate.put(reactionID,expressionRate);
						}
					}

			}
			else environment.associate(gene.getId(),new DoubleValue(1.0));
		}
		
		for(int i = 0; i < possibleInfluenced.size(); i++)
		{
			String reactionId = possibleInfluenced.get(i);
			AbstractSyntaxTree<DataTypeEnum, IValue> geneRule = null;
			
			if(model.containsGeneReactionRule(reactionId))
				geneRule = model.getGeneReactionRule(reactionId).getRule();
	
			double expressionValue = evaluateEnvironment(environment, geneRule);
			
			if(geneRule!= null && expressionValue!=1.0){
				result.addReaction(reactionId, expressionValue);
			}
		}
		
		
		return result;
	}
	
/*	Nao sei se sera necessario .... */
	public ReactionChangesList getReactionKnockoutList (ISteadyStateGeneReactionModel model)
	{
		List<String> result = new ArrayList<String>();
		
		IEnvironment<IValue> environment = new Environment<IValue>();
		ArrayList<String> possibleInfluenced = new ArrayList<String>();
		
		for(int i =0; i < model.getNumberOfGenes(); i++)
		{	
			Gene gene = model.getGene(i);	

			if(containsGeneKnockout(gene.getId())){
				environment.associate(gene.getId(),new BooleanValue(false));
				ArrayList<String> reactions = model.getReactionsInfluencedByGene(gene.getId());
					for(int k =0; k < reactions.size(); k++){
						if(!possibleInfluenced.contains(reactions.get(k)))
							possibleInfluenced.add(reactions.get(k));
					}
			}
			else environment.associate(gene.getId(),new BooleanValue(true));
		}
		
		for(int i =0; i < possibleInfluenced.size(); i++)
		{
			String reactionId = possibleInfluenced.get(i);
			AbstractSyntaxTree<DataTypeEnum, IValue> geneRule = null;
			
			if(model.containsGeneReactionRule(reactionId))
				geneRule = model.getGeneReactionRule(reactionId).getRule();
			
			if(geneRule!= null && !((Boolean) geneRule.evaluate(environment).getValue())){
				result.add(reactionId);
			}	
		}
		
		return new ReactionChangesList(result);
	}


	
	public double evaluateEnvironment(IEnvironment<IValue> environment, AbstractSyntaxTree<DataTypeEnum, IValue> geneTree){
		AbstractSyntaxTree<DataTypeEnum,IValue> convertedTree = convertGeneTree(geneTree, environment);
		return (double) convertedTree.evaluate(environment).getValue();
	}
	
	public AbstractSyntaxTree<DataTypeEnum, IValue> convertGeneTree(AbstractSyntaxTree<DataTypeEnum, IValue> geneRuleTree, IEnvironment<IValue> environment){
		AbstractSyntaxTreeNode<DataTypeEnum, IValue> root = convertGeneRule(geneRuleTree.getRootNode(), environment); 
		
		return new AbstractSyntaxTree<DataTypeEnum, IValue>(root); 
	}
	
	public AbstractSyntaxTreeNode<DataTypeEnum, IValue> convertGeneRule(AbstractSyntaxTreeNode<DataTypeEnum, IValue> geneRule, IEnvironment<IValue> environment){
		AbstractSyntaxTreeNode<DataTypeEnum, IValue> geneRegulationRule = null;
		
		if(geneRule instanceof And){
			geneRegulationRule = new Minimum(convertGeneRule(geneRule.getChildAt(0),environment), convertGeneRule(geneRule.getChildAt(1),environment));
		}
		
		if(geneRule instanceof Or){
			geneRegulationRule = new Mean(convertGeneRule(geneRule.getChildAt(0),environment), convertGeneRule(geneRule.getChildAt(1),environment));
		}
		
		if(geneRule instanceof Variable){
			String geneID = ((Variable)geneRule).toString();
			double geneExpressionValue = (double) environment.find(geneID).getValue();
			geneRegulationRule = new VariableDouble(geneID, geneExpressionValue);
		}
		
		return geneRegulationRule;
 	}
	
	
	public String getStringRepresentation() {
		String stringRepresentation = "";
		
		for(String s:keySet()){
			stringRepresentation += s +","+  get(s) +";\n";
		}
		
		return stringRepresentation;
	}
	

	
	public GeneChangesList clone(){
		return (GeneChangesList)(super.clone());
	}
	

}
