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
package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.nullspace;

import java.io.Serializable;

public class MeasuredFlux implements Serializable{
	private static final long serialVersionUID = -7304497841334245058L;
	
	protected int fluxIndex;
	protected double measuredValue;
	protected double expectedValue;
	
	public MeasuredFlux(int fluxIndex,double expectedValue,double measuredValue) {
		this.fluxIndex = fluxIndex;
		this.measuredValue = measuredValue;
		this.expectedValue = expectedValue;
	}

	public int getIndex() {
		return fluxIndex;
	}

	public double getMeasuredValue() {
		return measuredValue;
	}

	public double getExpectedValue() {
		return expectedValue;
	}
	
	
	

}
