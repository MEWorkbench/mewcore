package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class GeneticConditions implements Serializable{


	private static final long serialVersionUID = 1L;
	protected GeneChangesList geneList = null;
	protected ReactionChangesList reactionList;
	protected boolean isOverUnder;
		
	
	
	public GeneticConditions(ReactionChangesList reactionList)
	{
		this.isOverUnder = isUnderOver(reactionList);
		this.reactionList = reactionList;
	}
	
	public GeneticConditions(ReactionChangesList reactionList, boolean isOverUnder)
	{
		this.isOverUnder = isOverUnder;
		this.reactionList = reactionList;
	}

	public GeneticConditions(GeneChangesList geneList, ISteadyStateGeneReactionModel model, boolean isOverUnder) throws Exception
	{
		this.geneList = geneList;
		this.isOverUnder = isOverUnder;
		if(isOverUnder)
			this.reactionList = geneList.getReactionUnderOverList(model);
		else
			this.reactionList = geneList.getReactionKnockoutList(model);		
	}

	/**
	 * Full constructor for cloning purposes
	 * 
	 * @param geneList
	 * @param reactionList
	 * @param isOverUnder
	 */
	public GeneticConditions(GeneChangesList geneList, ReactionChangesList reactionList, boolean isOverUnder){
		this.geneList = geneList;
		this.reactionList = reactionList;
		this.isOverUnder = isOverUnder;
	}

	public GeneChangesList getGeneList() {
		return geneList;
	}

	public void updateReactionsList(ISteadyStateGeneReactionModel model) throws Exception{
		if(isOverUnder)
			this.reactionList = geneList.getReactionUnderOverList(model);
		else
			this.reactionList = geneList.getReactionKnockoutList(model);
	}

	public void setGeneList(GeneChangesList geneList, ISteadyStateGeneReactionModel model) throws Exception
	{
		this.geneList = geneList;
		this.reactionList = geneList.getReactionUnderOverList(model);
	}

	
	public ReactionChangesList getReactionList() {
		return reactionList;
	}


	public void setReactionList(ReactionChangesList reactionList) {
		this.reactionList = reactionList;
	}

	public boolean isOverUnder(){
		return isOverUnder;
	}

	protected boolean isUnderOver(Map<String, Double> map){
		
		for(Double value : map.values())
			if(value>0) return true;
		return false;	
	}
	
	public boolean isGenes(){
		return geneList!=null;
	}
	
	public boolean equals(GeneticConditions conditions){
		
		if(this.isOverUnder!=conditions.isOverUnder())
			return false;
		
		if(this.reactionList!=null && (!this.reactionList.equals(conditions.getReactionList())))
			return false;
		
		if(this.geneList!=null && (!this.geneList.equals(conditions.getGeneList())))
			return false;
		
		return true;
	}
	
	public String toString(){
		List<Pair<String,Double>> pairs = new ArrayList<Pair<String,Double>>();
		if(getGeneList()!=null)
			pairs = getGeneList().getPairsList();
		else pairs = getReactionList().getPairsList();
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< pairs.size(); i++){
			Pair<String, Double> pair = pairs.get(i);
			if(i>0)
				sb.append(",");
			sb.append(pair.getValue()+"="+pair.getPairValue());
		}
			
		return sb.toString();
	}
	
	public String toStringOptions(String sep,boolean excludeExpressionLevels){
		List<Pair<String,Double>> pairs = new ArrayList<Pair<String,Double>>();
		if(getGeneList()!=null)
			pairs = getGeneList().getPairsList();
		else pairs = getReactionList().getPairsList();
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< pairs.size(); i++){
			Pair<String, Double> pair = pairs.get(i);
			if(i>0)
				sb.append(sep);
			if(!excludeExpressionLevels)
				sb.append(pair.getValue()+"="+pair.getPairValue());
			else
				sb.append(pair.getValue());
		}
			
		return sb.toString();
	}
	
	public GeneticConditions clone(){
		GeneChangesList gcl = getGeneList()==null ? null : this.getGeneList().clone();
		ReactionChangesList rcl = getReactionList().clone();
		boolean ou = isOverUnder ? true : false;		
		
		return new GeneticConditions(gcl , rcl, ou);
	}
	 
}
