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

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;

public class UnderOverSingleReference extends OverrideSteadyStateModel {
	
	protected FluxValueMap referenceValues = null;
	
	protected double alpha = 0.001; // safe to remove ??
	

	/*
	 * Instantiates OverrideSteadyStateModelUnderOverExpression
	 * 
	 * @param underOverExpression
	 * @param referenceValues
	 * @param envCond
	 * @param model
	 */
	public UnderOverSingleReference(ISteadyStateModel model, EnvironmentalConditions envCond, ReactionChangesList reactionList, FluxValueMap references) {
		super(model,envCond, reactionList);
		this.referenceValues = references;
	}
	
	public UnderOverSingleReference(ISteadyStateModel model, ReactionChangesList reactionList, FluxValueMap references){
		this(model, null, reactionList, references);
	}
	
	public UnderOverSingleReference(ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions, FluxValueMap reference) {
		
		this(model, environmentalConditions, geneticConditions.getReactionList(), reference);
	}

	public ReactionConstraint getReactionConstraint(String reactionId)
	{	
		ReactionConstraint ret = model.getReactionConstraint(reactionId);
		Double referenceFlux = referenceValues.getValue(reactionId);
		
		if(environmentalConditions != null && environmentalConditions.containsKey(reactionId))
			ret = environmentalConditions.getReactionConstraint(reactionId);
		
		if(referenceFlux != null && reactionList != null &&  reactionList.containsReaction(reactionId))
			ret = underOverConstraint(reactionId, referenceFlux, ret);
		
		return ret;
	}
	
	public ReactionConstraint underOverConstraint(String reactionId, double referenceFlux, ReactionConstraint rc){
				
		if(referenceFlux >= 0)
			return positiveReference(reactionId, referenceFlux, rc);
		else
			return negativeReference(reactionId, referenceFlux, rc);
	}

	public ReactionConstraint positiveReference(String reactionId, double referenceFlux, ReactionConstraint rc)
	{
		double p = reactionList.getReactionFlux(reactionId);
		double lowerLimit,upperLimit;
		double delta = alpha * referenceFlux;
		
		boolean isOverExpression = p > 1;
		
		if(isOverExpression){
			lowerLimit = p * (referenceFlux - delta);
			upperLimit = rc.getUpperLimit();
		}else{
			lowerLimit = 0;
			upperLimit = (referenceFlux + delta) * p;
		}
		
//		System.out.println(reactionId + "\t" + referenceFlux + "\t" +p+"\t"+ lowerLimit + "\t"+ upperLimit);
		return new ReactionConstraint(lowerLimit, upperLimit);
	}
	
	public ReactionConstraint negativeReference(String reactionId, double referenceFlux, ReactionConstraint rc){
		
		double p = reactionList.getReactionFlux(reactionId);
		double lowerLimit,upperLimit;
		double delta = alpha * referenceFlux;
		
		boolean isOverExpression = p > 1;
		
		if(isOverExpression){
			lowerLimit = rc.getLowerLimit();
			upperLimit = (referenceFlux - delta) * p;
		}else{
			lowerLimit = p * (referenceFlux + delta);
			upperLimit = 0;
		}
		
		
//		System.out.println(reactionId + "\t" + referenceFlux + "\t"+p+"\t" + lowerLimit +"\t"+ upperLimit);
		
		return new ReactionConstraint(lowerLimit, upperLimit);
	}
	

}
