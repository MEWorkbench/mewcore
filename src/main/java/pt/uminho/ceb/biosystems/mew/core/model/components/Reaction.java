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

import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.components.kineticlaw.KineticLaw;

/**
 * 
 * @author pmaia
 */
public class Reaction implements Serializable {

	/** for serialization/marshalling purposes */
	private static final long serialVersionUID = 1L;
	/** the id of this reaction */
	protected String id;
	/** reaction reversibility */
	protected boolean reversible;
	/** the type of this reaction. See {@link ReactionType}*/
	protected ReactionType type;	
	/** flux constraints for this reaction, i.e. upper and lower limits */
	protected ReactionConstraint constraints;	
	/** reaction name */
	protected String name;
	/** kinetic Law **/
	protected KineticLaw kineticLaw;
	
	
	/**
	 * Constructor from a reaction id and the reversibility property. The type is assumed to be <code>UNKNOWN</code>.
	 * 
	 * @param id a <code>String</code> correspondent to this reaction id.
	 */
	public Reaction(String id, boolean reversible){
		this.id = id;
		this.reversible = reversible;
		this.type = ReactionType.UNKNOWN;
		this.constraints = new ReactionConstraint();
		kineticLaw = new KineticLaw();
	}
	
	
	/**
	 * <p>Constructor from a reaction id, reversible property and limits. 
	 * <p>The type is assumed to be <code>UNKNOWN</code>.</p>
	 * 
	 * @param id a <code>String</code> correspondent to this reaction id.
	 * @param reversible a <code>boolean</code>. true if the reaction is reversible, false otherwise
	 * @param lower the lower bound of this reaction's flux
	 * @param upper the upper bound of this reaction's flux
	 */
	public Reaction(String id, boolean reversible, double lower, double upper){
		this.id = id;
		this.reversible = reversible;
		this.type = ReactionType.UNKNOWN;
		this.setConstraints(new ReactionConstraint(lower,upper));
		kineticLaw = new KineticLaw();
	}
	
	/**
	 * <p>Constructor from a reaction id, reversible property, reaction type and limits. 
	 * <p>The type is assumed to be <code>UNKNOWN</code>.</p>
	 * 
	 * @param id a <code>String</code> correspondent to this reaction id.
	 * @param reversible a <code>boolean</code>. true if the reaction is reversible, false otherwise
	 * @param type a <code>ReactionType</code>.
	 * @param lower the lower bound of this reaction's flux
	 * @param upper the upper bound of this reaction's flux
	 */
	public Reaction(String id, boolean reversible, ReactionType type, double lower, double upper){
		this.id = id;
		this.reversible = reversible;
		this.type = type;
		this.setConstraints(new ReactionConstraint(lower,upper));
		kineticLaw = new KineticLaw();
	}
	
	/**
	 * Constructor from an id and a reaction type
	 * 
	 * @param id a <code>String</code> correspondent to this reaction id.
	 * @param type a <code>ReactionType</code>.
	 */
	public Reaction(String id, boolean reversible, ReactionType type){
		this.id = id;
		this.reversible = reversible;
		this.type = type;
		kineticLaw = new KineticLaw();
	}

	public Reaction(String id, boolean isReversible,double lowerBound,
			double upperBound, KineticLaw kineticLaw){
		this.id = id;
		this.reversible = isReversible;
		this.type = ReactionType.UNKNOWN;
		this.setConstraints(new ReactionConstraint(lowerBound,upperBound));
		this.kineticLaw = kineticLaw;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public ReactionType getType() {
		if(type==null)
			type = ReactionType.UNKNOWN;
		return type;
	}

	public void setType(ReactionType type) {
		this.type = type;
	}

	public boolean isReversible() {
		return reversible;
	}

	public void setReversible(boolean reversible) {
		this.reversible = reversible;
	}
	
	public ReactionConstraint getConstraints() {
		return constraints;
	}

	public void setConstraints(ReactionConstraint constraints) {
		this.constraints = constraints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setKineticLaw(KineticLaw kineticLaw){
		this.kineticLaw = kineticLaw;
	}
	
	public KineticLaw getKineticLaw(){
		return kineticLaw;
	}

}
