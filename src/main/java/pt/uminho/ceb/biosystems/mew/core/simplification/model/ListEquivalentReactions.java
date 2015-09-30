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
package pt.uminho.ceb.biosystems.mew.core.simplification.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class ListEquivalentReactions implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	Set<String> listIds; 
	
	public ListEquivalentReactions()
	{
		listIds = new LinkedHashSet<String>();
	}
	
	public void addReaction (String reactionId)
	{
		listIds.add(reactionId);
	}

	public void addReaction (int reactionIndex, ISteadyStateModel model)
	{
		listIds.add(model.getReactionId(reactionIndex));
	}

	public void joinLists (ListEquivalentReactions otherList)
	{
		listIds.addAll(otherList.getListIds());
	}
	
	
	public boolean containsReaction(String reactionId)
	{
		return listIds.contains(reactionId);
	}
	
	public boolean containsReaction(int reactionIndex, ISteadyStateModel model)
	{
		return listIds.contains(model.getReactionId(reactionIndex));
	}

	public void removeReaction (String reactionId)
	{
		listIds.remove(reactionId);
	}
	
	public int numberReactions ()
	{
		return listIds.size();
	}
	
	public Set<String> getListIds() {
		return listIds;
	}

	public void setListIds(Set<String> listIds) {
		this.listIds = listIds;
	}

	
	public String toString ()
	{		
		return CollectionUtils.join(listIds, " <> ");
	}
	
}
