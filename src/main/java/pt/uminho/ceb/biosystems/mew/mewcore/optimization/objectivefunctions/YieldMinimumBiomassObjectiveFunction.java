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
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.math.MathUtils;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.Debugger;

public class YieldMinimumBiomassObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long	serialVersionUID			= 1L;
	
	String						biomassId;
	String						desiredFluxId;
	protected final double		worstFitness				= 0.0d;
	protected SolverType		solver;
	
	protected double			minimumBiomassPercentage	= 0.1;
	protected double			minimumBiomassValue			= 0.0;
	protected boolean			biomassComputed				= false;
	
	public YieldMinimumBiomassObjectiveFunction(
			String biomassId,
			String desiredFluxId,
			Double minimumBiomass,
			SolverType solver) {
		this.biomassId = biomassId;
		this.desiredFluxId = desiredFluxId;
		this.minimumBiomassPercentage = minimumBiomass;
		this.solver = solver;
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		if (!biomassComputed)
			computeBiomassValue(simResult);
		
		FluxValueMap fluxValues = simResult.getFluxValues();
		double biomassValue = fluxValues.getValue(biomassId);
		double desiredFlux = fluxValues.getValue(desiredFluxId);
		
		double fitness = 0.0;
		if (biomassValue >= minimumBiomassValue)
			fitness = desiredFlux;
//		else {
//			fitness = (desiredFlux * biomassValue * 0.001);
//		}
		
		Debugger.debug("B: " + biomassValue + "\tD: " + desiredFlux + "\tmin: " + minimumBiomassValue);
		return fitness;
	}
	
	private void computeBiomassValue(SteadyStateSimulationResult simResult) {
		ISteadyStateModel model = simResult.getModel();
		String simulationMethod = simResult.getMethod();
		EnvironmentalConditions env = simResult.getEnvironmentalConditions();
		
		SimulationSteadyStateControlCenter simulationControlCenter = new SimulationSteadyStateControlCenter(env, null,
				model, simulationMethod);
		simulationControlCenter.setSolver(solver);
		simulationControlCenter.setMaximization(true);
		simulationControlCenter.setFBAObjSingleFlux(biomassId, 1.0);
		
		SteadyStateSimulationResult solution = null;
		
		try {
			solution = simulationControlCenter.simulate();
		} catch (Exception e) {
			Debugger.debug("YieldMinimumBiomassObjectiveFunction: could not compute reference biomass value");
			e.printStackTrace();
		}
		
		double biomass = 0.0;
		if (solution != null) {
			biomass = solution.getFluxValues().getValue(biomassId);
			minimumBiomassValue = biomass * minimumBiomassPercentage;
		}
		
		System.out.println("\nwt biomass= " + biomass + " | min biomass= " + minimumBiomassValue + " | min percent= "
				+ minimumBiomassPercentage);
		biomassComputed = true;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.YIELD;
	}
	
	public String toString() {
		double percentage = MathUtils.round(minimumBiomassPercentage * 100, 2);
		return "YIELD> percentage=" + percentage + ";target=" + desiredFluxId;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getUnnormalizedFitness(double)
	 */
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metabolic.optimization.objectivefunctions.interfaces.IObjectiveFunction
	 * #getShortString()
	 */
	@Override
	public String getShortString() {
		return getType().toString();
	}
	
	public String getDesiredId() {
		return desiredFluxId;
	}
	
	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		double percentage = MathUtils.round(minimumBiomassPercentage * 100, 2);
		return "$YIELD = max (\\text{" + desiredFluxId + "}); biomass \\ge " + percentage + "\\% wt$";

	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + biomassId + "," + desiredFluxId + "," + minimumBiomassPercentage + "," + solver + ")";
	}
	
}
