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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.nullspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAApproaches;

public class MFANullSpaceSolution implements Serializable{
	
	private static final long serialVersionUID = 4842287087483519452L;
	
	protected List<FluxSet> fluxsetList;
	protected NullSpaceMethod nullSpaceMethod;

	
	public MFANullSpaceSolution(List<FluxSet> fluxsetList, NullSpaceMethod nullSpaceMethod) {
		this.fluxsetList = fluxsetList;
		this.nullSpaceMethod = nullSpaceMethod;
	}
	
	
	/** Convert the flux values of the ith solution to a steady state solution data type */
	public SteadyStateSimulationResult convertNullSpaceSolution(ISteadyStateModel model, int solutionIndex){
		return (solutionIndex<fluxsetList.size()) ? convertNullSpaceSolution(model, fluxsetList.get(solutionIndex)) : null;
	}
	
	/** Convert the flux values to a steady state solution data type */
	public SteadyStateSimulationResult convertNullSpaceSolution(ISteadyStateModel model, FluxSet solution){
		
		FluxValueMap fluxValues = new FluxValueMap();
		
		for(int f=0; f<solution.getNumberFluxes(); f++)
		{
			Flux flux = solution.getFlux(f);
			int fluxIndex = flux.getIndex();
			String rId = model.getReaction(fluxIndex).getId();
			double fluxValue = flux.getValue();
			fluxValues.put(rId, fluxValue);
		}
		
		for(int f=0; f<solution.getNumberMeasuredFluxes(); f++)
		{
			MeasuredFlux measuredFlux = solution.getMeasuredFlux(f);
			int fluxIndex = measuredFlux.getIndex();
			String rId = model.getReaction(fluxIndex).getId();
			double fluxValue = measuredFlux.getExpectedValue();
			fluxValues.put(rId, fluxValue);
		}
		return new SteadyStateSimulationResult(model, null, null,MFAApproaches.nullSpace.getPropertyDescriptor(), fluxValues, "No Solver was used", Double.NaN, "Null Space OF", null);
	}
	
	public int getNumberOfSolutions(){
		return fluxsetList.size();
	}
	
	public FluxSet getFluxSet(int index){
		return fluxsetList.get(index);
	}
	
	public FluxSet getPrincipalSolution(){
		return (fluxsetList==null) ? null : fluxsetList.get(0);
	}
	
	public List<FluxSet> getAllSolutions(){
		return fluxsetList;
	}
	
	public List<FluxSet> getAlternativeSolutions(){
		if(fluxsetList==null || fluxsetList.size()<2)
			return null;
		List<FluxSet> alternativeSolutions = new ArrayList<FluxSet>();
		for(int i=1; i<fluxsetList.size(); i++)
			alternativeSolutions.add(fluxsetList.get(i));
		return alternativeSolutions;
	}
	
	public NullSpaceMethod getNullSpaceMethod(){return nullSpaceMethod;}
}
