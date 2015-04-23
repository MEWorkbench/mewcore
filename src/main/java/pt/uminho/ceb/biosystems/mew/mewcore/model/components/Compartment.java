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
package pt.uminho.ceb.biosystems.mew.mewcore.model.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Compartment implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
    protected double size;
    protected String id;
    protected List<String> insideCompartmentList;
    protected List<String> metaboliteList;
    
    public List<String> getMetaboliteList() {
		return metaboliteList;
	}

	public void setMetaboliteList(List<String> metaboliteList) {
		this.metaboliteList = metaboliteList;
	}

	protected boolean isExternal = false;

    
    public Compartment(String compartmentId)
    {
    	id = compartmentId;
    }
    
    public Compartment(String compartmentId, boolean isExternal){
    	id = compartmentId;
    	this.isExternal = isExternal;
    	this.size = 1.0;
    }
    
    public Compartment(String compartmentId, boolean isExternal,double size)
    {
    	id = compartmentId;
    	this.isExternal = isExternal;
    	this.size = size;
    }
    
    public Compartment(String compartmentId,double size) {
        id = compartmentId;
        this.size = size;
        metaboliteList = new ArrayList<String>();
        insideCompartmentList = new ArrayList<String>();
    }
    
    public Compartment(String compartmentID, String outside, double size){
    	id = compartmentID;
    	this.size = size;
//    	this.outside = outside;
    	this.isExternal = false;
    	metaboliteList = new ArrayList<String>();
        insideCompartmentList = new ArrayList<String>();
    }
    
    public Compartment(String compartmentID, String outside, double size,boolean isExternal){
    	id = compartmentID;
    	this.size = size;
//    	this.outside = outside;
    	this.isExternal = false;
    	metaboliteList = new ArrayList<String>();
        insideCompartmentList = new ArrayList<String>();
        this.isExternal = isExternal;
    }

    
    public Compartment() {
		// TODO Auto-generated constructor stub
	}

	public void addMetabolite(String metaboliteId) {
        metaboliteList.add(metaboliteId);
    }

    public double getSize() {
        return size;
    }
	
	public void addInsideCompartment(String compartmentID){
		insideCompartmentList.add(compartmentID);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    public boolean isExternal ()
    {
    	return isExternal;
    }

	public void setSize(int size) {
		this.size = size;
	}
    

}
