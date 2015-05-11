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


import java.util.HashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class OverrideSteadyStateModel extends AbstractOverrideSteadyStateModel {
	
	protected EnvironmentalConditions environmentalConditions = null;
	protected ReactionChangesList reactionList = null; // assuming knockouts
	
	
//	FIXME:  delete this constructor
	public OverrideSteadyStateModel(ISteadyStateModel model, EnvironmentalConditions environmentalConditions, ReactionChangesList knockoutList) {
		this.model = model;
		this.environmentalConditions = environmentalConditions;
		this.reactionList = knockoutList;
	}
	
	public OverrideSteadyStateModel(ISteadyStateModel model, EnvironmentalConditions environmentalConditions) {
		this(model, environmentalConditions, (ReactionChangesList)null);
	}
	
	public OverrideSteadyStateModel(ISteadyStateModel model, ReactionChangesList reactionList) {
		this(model, null, reactionList);
	}

	public OverrideSteadyStateModel(ISteadyStateModel model) {
		this(model, null, (ReactionChangesList)null);
	}
	
	public OverrideSteadyStateModel(ISteadyStateModel model, EnvironmentalConditions environmentalConditions, GeneticConditions geneCond) {
		
		this.model = model;
		this.environmentalConditions = environmentalConditions;
		if(geneCond!=null)
			this.reactionList = geneCond.getReactionList();
	}
	
	public void setEnvironmentalConditions(
			EnvironmentalConditions environmentalConditions) {
		this.environmentalConditions = environmentalConditions;
	}

	public void setReactionList(ReactionChangesList reactionList) {
		this.reactionList = reactionList;
	}

	@Override
	public ReactionConstraint getReactionConstraint(int reactionIndex)
	{
		String reactionId = model.getReactionId(reactionIndex);
		return getReactionConstraint(reactionId);
	}
	
	public ReactionConstraint getReactionConstraint(String reactionId) {
		
		if (reactionList != null && reactionList.containsReactionKnockout(reactionId))
			return new ReactionConstraint(0,0);
		
		if(environmentalConditions != null && environmentalConditions.containsKey(reactionId)) 
			return environmentalConditions.getReactionConstraint(reactionId);
		


		
		return model.getReactionConstraint(reactionId);
	}


	public Set<String> getOverriddenReactions() {
		
		Set<String> ret = new HashSet<String>();
		
		if(reactionList!=null)
			ret.addAll(reactionList.getReactionIds());
		if(environmentalConditions!=null)
			ret.addAll(environmentalConditions.keySet());
		return ret;
	}


}
