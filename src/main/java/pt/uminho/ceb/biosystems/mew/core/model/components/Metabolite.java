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


public class Metabolite implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final boolean DEFAULT_CONSTANT=false;
	private static final boolean DEFAULT_BCONDITIONS=false;
	
	
	protected String id;
	protected String name;
	protected Compartment compartment;
	protected boolean constant;
	protected boolean boundaryCondition;
	
	
	public Metabolite(String id, String name, Compartment compartment,boolean isConstant,boolean boundaryCondition){
		this.id = id;
		this.name = name;
		this.compartment = compartment;
		this.constant = isConstant;
		this.boundaryCondition = boundaryCondition;
	}
	
	public Metabolite(){
		this(null, null, null, DEFAULT_CONSTANT, DEFAULT_BCONDITIONS);
	}
	
	public Metabolite(String id){
		this(id, null, null, DEFAULT_CONSTANT, DEFAULT_BCONDITIONS);
	}
	
	public Metabolite(String id, String name){
		this(id, name, null, DEFAULT_CONSTANT, DEFAULT_BCONDITIONS);
	}
	
	public Metabolite(String id, String name, Compartment compartment){
		this(id, name, compartment, DEFAULT_CONSTANT, DEFAULT_BCONDITIONS);
	}
	
	public Metabolite(String id, String name, Compartment compartment, boolean isBoundary){
		this(id, name, compartment, DEFAULT_CONSTANT, isBoundary);
	}
	
	public boolean isBoundaryCondition() {
		return boundaryCondition;
	}

	public void setBoundaryCondition(boolean boundaryCondition) {
		this.boundaryCondition = boundaryCondition;
	}

	public boolean isConstant(){
		return constant;
	}
	
	public void setConstant(boolean isConstant){
		constant = isConstant;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Compartment getCompartment() {
		return compartment;
	}

	public void setCompartment(Compartment compartment) {
		this.compartment = compartment;
	}
	
	public boolean isExternal(){
		return compartment.isExternal();
	}
	

}
