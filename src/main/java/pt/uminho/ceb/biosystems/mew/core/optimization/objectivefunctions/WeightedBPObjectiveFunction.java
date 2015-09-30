package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;
///*
//\ * Copyright 2010
// * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
// * CCTC - Computer Science and Technology Center
// *
// * University of Minho 
// * 
// * This is free software: you can redistribute it and/or modify 
// * it under the terms of the GNU Public License as published by 
// * the Free Software Foundation, either version 3 of the License, or 
// * (at your option) any later version. 
// * 
// * This code is distributed in the hope that it will be useful, 
// * but WITHOUT ANY WARRANTY; without even the implied warranty of 
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
// * GNU Public License for more details. 
// * 
// * You should have received a copy of the GNU Public License 
// * along with this code. If not, see http://www.gnu.org/licenses/ 
// * 
// * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
// */
//package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;
//
//import java.io.Serializable;
//
//import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
//import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
//import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
//
//public class WeightedBPObjectiveFunction implements IObjectiveFunction, Serializable {
//	
//	private static final long serialVersionUID = 1L;
//
//	protected final double worstFitness = Double.NEGATIVE_INFINITY;
//
//	String biomassId; 
//	String desiredFluxId;
//	protected double maxBiomass;
//	protected double maxProduct;
//	protected double alpha;
//	
//	public WeightedBPObjectiveFunction(String biomassId, String desiredFluxId, Double maxBiomass, Double maxProduct, Double alpha) {
//		this.biomassId = biomassId;
//		this.desiredFluxId = desiredFluxId;
//		this.maxBiomass = maxBiomass;
//		this.maxProduct = maxProduct;
//		this.alpha = alpha;
//	}
//
//	public double evaluate(SteadyStateSimulationResult simResult){
//		
//		FluxValueMap fluxValues = simResult.getFluxValues();
//		
//		double biomassValue = Math.abs(fluxValues.getValue(biomassId));		
//		double desiredFlux = Math.abs(fluxValues.get(desiredFluxId));
//		
//		double NG = biomassValue/maxBiomass;
//		double NP = desiredFlux/maxProduct;
//			
//		double fitness = Math.pow(NG,alpha)*Math.pow(NP, (1.0-alpha));
//		
//		return fitness;
//	}
//
//	@Override
//	public double getWorstFitness() {
//		return worstFitness;
//	}
//
//
//	@Override
//	public ObjectiveFunctionType getType() {
//		return ObjectiveFunctionType.WBP;
//	}
//	
//	public String toString(){
//		return getType().toString() ;
//	}
//
//	@Override
//	public boolean isMaximization() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getUnnormalizedFitness(double)
//	 */
//	@Override
//	public double getUnnormalizedFitness(double fit) {
//		return fit;
//	}
//
//	/* (non-Javadoc)
//	 * @see metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction#getShortString()
//	 */
//	@Override
//	public String getShortString() {
//		return toString();
//	}
//
//	public String getDesiredId() {
//		return desiredFluxId;
//	}
//
//	@Override
//	public String getLatexString() {
//		return "$"+getLatexFormula()+"$";
//	}
//	
//	@Override
//	public String getLatexFormula() {
//		return "WBPO= \\left(\\frac{\\text{"+biomassId+"}}{"+maxBiomass+"}\\right)^"+alpha 
//				  + " \\times "
//				  + " \\left(\\frac{\\text{"+desiredFluxId+"}}{"+maxProduct+"}\\rigth)^{(1-"+alpha + ")}";
//	}
//
//	@Override
//	public String getBuilderString() {
//		return getType() + "("+biomassId+","+desiredFluxId+","+maxBiomass+","+maxProduct+","+alpha+")";
//	}
//
//	
//}
