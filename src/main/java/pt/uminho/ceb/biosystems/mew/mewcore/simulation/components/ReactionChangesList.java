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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.list.ListStrings;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class ReactionChangesList extends MapStringNum implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public ReactionChangesList() {
		super();
	}
	

	//FIXME: the expression should be a Map<String, Double>
	public ReactionChangesList(List<String> reactions, List<Double> expression){
		super();
		int size=reactions.size();
		
		for(int i=0;i<size;i++)
			addReaction(reactions.get(i), expression.get(i));		
	}
	
	public ReactionChangesList(Collection<String> reactions){
		super();
		
		for(String id: reactions)
			addReaction(id, 0.0);	
	}
	
	//FIXME: the reactions should be a Set<String> or a Collection<String> 
//	careful with size 
	// creates from list of knockouts
	public ReactionChangesList (List<String> reactions){
		super();
		int size=reactions.size();
		
		for(int i=0;i<size;i++)
			addReaction(reactions.get(i), 0.0);	
	}
	

	//FIXME: the reactions should be a Set<String>
	@Deprecated
//	USE: ReactionChangesList(Map<String, Double> expressions)
	public ReactionChangesList(List<Integer> reactions, ISteadyStateModel model)
	{
		super();
		
		for(int i=0; i < reactions.size(); i++)
			addReaction(model.getReactionId(reactions.get(i)), 0.0);

	}
	
	public ReactionChangesList(Map<String, Double> expressions) {
		super();
		putAll(expressions);
	}


	public void setFromListPairsIndexValue (List<Pair<Integer,Double>> reactionIndexes, ISteadyStateModel model) {		
		for(Pair<Integer,Double> pair : reactionIndexes)
			addReaction(pair.getValue(),pair.getPairValue(), model);
	}
	
	public void setFromListpairsIdValue (List<Pair<String,Double>> regulations){
		
		for(Pair<String,Double> pair : regulations)
			addReaction(pair);
	}
	
	
	public void loadFromString (String strList, String separator)
	{
		strList.trim();
		String[] reactionsPairs = strList.split(separator);
		
		for(String reactionPair: reactionsPairs)
		{
			reactionPair.trim();
			String[] reactionFlux = reactionPair.split(",");
			addReaction(reactionFlux[0],Double.valueOf(reactionFlux[1]));
		}
	}

	public void loadReactionKnockoutsFromString (String strList, String separator)
	{
		ListStrings reactionIds = new ListStrings();
		reactionIds.fromString(strList, separator);
		
		int size= reactionIds.size();
		
		for(int i=0;i<size;i++)
			addReaction(reactionIds.get(i), 0.0);	
	}
	
	public String getStringRepresentation()
	{
				
		return getStringRepresentation(",", ";\n");
	}
	
	public String getStringRepresentation(String sepIdtoExpresion, String sepExpressions)
	{
		String stringRepresentation = "";
		
		for(String reactionId : keySet()) {
			stringRepresentation += reactionId +sepIdtoExpresion+ get(reactionId)+sepExpressions;
		}
		
		return stringRepresentation;
	}
	
	public Set<String> getReactionIds() {
		return new HashSet<String>(this.keySet());
	}
	
	public Set<Integer> getReactionIndexes(ISteadyStateModel model) throws NonExistentIdException
	{
		Set<Integer> indexList = new TreeSet<Integer>();
		
		for(String reactionID : keySet())
			indexList.add(model.getReactionIndex(reactionID));
		
		return indexList;
	}
	
	public List<String> getReactionKnockoutList ()
	{
		List<String> res = new ArrayList<String>();
		
		for(String s: keySet())
		{
			if(getReactionFlux(s) == 0.0)
				res.add(s);
		}
		
		return res;
	}
	
	public List<Integer> getReactionKnockoutIndexes (ISteadyStateModel model) throws Exception
	{
		List<Integer> res = new ArrayList<Integer>();
		
		for(String s: keySet())
		{
			if(getReactionFlux(s) == 0.0)
				res.add(model.getReactionIndex(s));
		}
		
		
		return res;
	}
	
	public boolean allKnockouts ()
	{
		boolean res = true;
		
		for(String s: keySet())
		{
			if(getReactionFlux(s) != 0.0)
				res = false;
		}
		
		return res;
	}
	
	public void addReaction (String reactionId, double fluxRate)
	{
		put(reactionId, fluxRate);
	}
	
	public void addReactionKnockout (String reactionId)
	{
		put(reactionId, 0.0);
	}
	
	public void addReaction(Pair<Integer,Double> reactionPair,ISteadyStateModel model)
	{
		addReaction(reactionPair.getValue(),reactionPair.getPairValue(),model);
	}
	
	public void addReaction(Pair<String,Double> reactionPair)
	{
		addReaction(reactionPair.getValue(),reactionPair.getPairValue());
	}
	
	public void addReaction(int reactionIndex,double fluxrate,ISteadyStateModel model)
	{
		String reactionID = model.getReactionId(reactionIndex);
		
		addReaction(reactionID, fluxrate);
	}
	
	public boolean containsReaction (String reactionId)
	{
		return containsKey(reactionId); 
	}

	public boolean containsReactionKnockout (String reactionId)
	{
		if (containsReaction(reactionId))
			return getReactionFlux(reactionId)==0.0;
		else return false;
	}

	
	public void removeReaction(String reactionId)
	{
		remove(reactionId);
	}
	
	public double getReactionFlux (String reactionId)
	{
		return get(reactionId);
	}
	
	public List<Pair<Integer,Double>> getIndexesList(ISteadyStateModel model) throws NonExistentIdException
	{
		List<Pair<Integer, Double>> indexList = new ArrayList<Pair<Integer,Double>>();
		
		for(String id: keySet())
			indexList.add(new Pair<Integer, Double>(model.getReactionIndex(id), get(id)));
		
		return indexList;
	}
	
	
	public List<Pair<String,Double>> getPairsList ()
	{
		List<Pair<String, Double>> indexList = new ArrayList<Pair<String,Double>>();
		
		for(String id: keySet())
			indexList.add(new Pair<String, Double>(id, get(id)));
		
		return indexList;
	}
	
	
	public void loadReactionUnderOverFromFile(String filePath) throws Exception{

		FileReader f = new FileReader(filePath);
		BufferedReader r = new BufferedReader(f);
		
		String reaction;
		while(r.ready())
		{
			reaction = r.readLine().trim();
			
			String[] reactionFlux = reaction.split(",");
			this.addReaction(reactionFlux[0],Double.parseDouble(reactionFlux[1]));
		}
		
		r.close();
		f.close();
			
	}	
	
	public void loadKnockoutsFromFile(String filePath) throws Exception{

		File fr = new File(filePath);
		FileReader f = new FileReader(fr);
		BufferedReader r = new BufferedReader(f);
		
		String reaction;
		while(r.ready()){
			reaction = r.readLine().trim();
			this.addReaction(reaction, 0.0);
		}
		
		r.close();
		f.close();
			
	}	
}
