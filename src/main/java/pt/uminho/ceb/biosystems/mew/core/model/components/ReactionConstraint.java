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
package pt.uminho.ceb.biosystems.mew.core.model.components;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;

public class ReactionConstraint implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final double MINUS_INFINITY = -Double.MAX_VALUE;
	public static final double PLUS_INFINITY = Double.MAX_VALUE;
	
	
	protected double upperLimit;
	protected double lowerLimit;
	
	public ReactionConstraint(){
		upperLimit = PLUS_INFINITY;
		lowerLimit = MINUS_INFINITY;
	}
	
	public ReactionConstraint(ReactionConstraint rc){
		upperLimit = rc.getUpperLimit();
		lowerLimit = rc.getLowerLimit();
	}
	
	public ReactionConstraint(ReactionConstraintCI reactionConstraintCI) {
		this.lowerLimit = reactionConstraintCI.getLowerLimit();
		this.upperLimit = reactionConstraintCI.getUpperLimit();
	}
	
	public ReactionConstraint(double lowerLimit,double upperLimit){
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}
	
	public double getLowerLimit(){
		return lowerLimit;
	}
	
	public double getUpperLimit(){
		return upperLimit;
	}

	public void setUpperLimit(double upperLimit){
		this.upperLimit = upperLimit;
	}
	
	public void setLowerLimit(double lowerLimit){
		this.lowerLimit = lowerLimit;
	}
	
	public String toString(){
		return "["+lowerLimit +", "+upperLimit+"]";
		
	}
}
