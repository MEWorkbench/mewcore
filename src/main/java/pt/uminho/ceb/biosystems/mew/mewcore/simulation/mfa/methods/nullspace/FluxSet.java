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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace;

import java.io.Serializable;
import java.util.List;

public class FluxSet implements Serializable{
	private static final long serialVersionUID = 853439988253162434L;
	
	protected List<MeasuredFlux> measuredFluxList;
	protected List<Flux> fluxtList;
	protected double[][] sensitivityMatrix;
	
	/** Beta vector (free variables, computed from the measured fluxes) that originates the flux distribution of the solution */
	protected double[] betaVector;
	
	
	public FluxSet(List<MeasuredFlux> measuredFluxList, List<Flux> fluxtList,double[][] sensitivityMatrix) {
		this.measuredFluxList = measuredFluxList;
		this.fluxtList = fluxtList;
		this.sensitivityMatrix = sensitivityMatrix;
	}

	public FluxSet(List<MeasuredFlux> alternativeMeasuredFluxList,List<Flux> alternativeFluxList) {
		measuredFluxList = alternativeMeasuredFluxList;
		fluxtList = alternativeFluxList;
		sensitivityMatrix = null;
	}
	
	public FluxSet(List<MeasuredFlux> alternativeMeasuredFluxList, List<Flux> alternativeFluxList, double[] betaVector) {
		measuredFluxList = alternativeMeasuredFluxList;
		fluxtList = alternativeFluxList;
		this.betaVector = betaVector;
		sensitivityMatrix = null;
	}

	public void addFluxValue(int fluxIndex,double fluxValue){
		fluxtList.add(new Flux(fluxIndex,fluxValue));
	}
	
	public void addMeasuredFlux(int fluxIndex,double measuredValue,double expectedValue){
		measuredFluxList.add(new MeasuredFlux(fluxIndex,measuredValue,expectedValue));
	}
	
	public Flux getFlux(int position){
		return fluxtList.get(position);
	}
	
	public MeasuredFlux getMeasuredFlux(int position){
		return measuredFluxList.get(position);
	}
	
	public Flux getFluxByIndex(int fluxIndex){
		int i = 0;
		
		for(Flux flux:fluxtList)
			if(fluxIndex == flux.getIndex())
				return flux;
			else
				i++;
		
		return null;
	}
	
	public MeasuredFlux getMeasuredFluxByIndex(int fluxIndex){
		int i = 0;
		
		for(MeasuredFlux measuredFlux:measuredFluxList)
			if(fluxIndex == measuredFlux.getIndex())
				return measuredFlux;
			else
				i++;
		
		return null;
	}
	
	public int getTotalNumberOfFluxes(){
		return fluxtList.size()+measuredFluxList.size();
	}
	
	public int getNumberMeasuredFluxes(){
		return measuredFluxList.size();
	}
	
	public int getNumberFluxes(){
		return fluxtList.size();
	}
	
	public boolean hasSensitivityMatrix(){
		return sensitivityMatrix != null;
	}
	
	public double[][] getSensitivityMatrix(){
		return sensitivityMatrix;
	}

	public double[] getBetaVector() {return betaVector;}
	public void setBetaVector(double[] betaVector) {this.betaVector = betaVector;}
}
