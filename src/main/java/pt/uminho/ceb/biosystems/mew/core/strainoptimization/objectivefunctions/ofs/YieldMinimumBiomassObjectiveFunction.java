/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of
 * Biological Engineering
 * CCTC - Computer Science and Technology Center
 * University of Minho
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Public License for more details.
 * You should have received a copy of the GNU Public License
 * along with this code. If not, see http://www.gnu.org/licenses/
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.math.MathUtils;

public class YieldMinimumBiomassObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "YIELD";
	
	public static final String	YIELD_PARAM_BIOMASS					= "Biomass";
	public static final String	YIELD_PARAM_PRODUCT					= "Product";
	public static final String	YIELD_PARAM_MIN_BIOMASS_PERCENTAGE	= "MinBiomassPercentage";
	public static final String	YIELD_PARAM_SOLVER					= "Solver";
	
	protected final double	worstFitness		= 0.0d;
	protected double		minimumBiomassValue	= 0.0;
	protected boolean		biomassComputed		= false;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(YIELD_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(YIELD_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(YIELD_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		return Collections.unmodifiableMap(myparams);
	}
	
	public YieldMinimumBiomassObjectiveFunction() {
		super();
	}
	
	public YieldMinimumBiomassObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public YieldMinimumBiomassObjectiveFunction(String biomassId, String desiredFluxId, Double minimumBiomass, SolverType solver) {
		super(biomassId, desiredFluxId, minimumBiomass, solver);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(YIELD_PARAM_BIOMASS, params[0]);
		setParameterValue(YIELD_PARAM_PRODUCT, params[1]);
		setParameterValue(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE, params[2]);
		setParameterValue(YIELD_PARAM_SOLVER, params[3]);
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(YIELD_PARAM_BIOMASS);
		String productID = (String) getParameterValue(YIELD_PARAM_PRODUCT);
		if (!biomassComputed) {
			computeBiomassValue(simResult);
		}
		
		FluxValueMap fluxValues = simResult.getFluxValues();
		double biomassValue = fluxValues.getValue(biomassID);
		double desiredFlux = fluxValues.getValue(productID);
		
		double fitness = 0.0;
		if (biomassValue >= minimumBiomassValue) {
			fitness = desiredFlux;
		}
		
		Debugger.debug("B: " + biomassValue + "\tD: " + desiredFlux + "\tmin: " + minimumBiomassValue);
		return fitness;
	}
	
	private void computeBiomassValue(SteadyStateSimulationResult simResult) {
		SolverType solver = (SolverType) getParameterValue(YIELD_PARAM_SOLVER);
		String biomassID = (String) getParameterValue(YIELD_PARAM_BIOMASS);
		Double minBiomassPercentage = (Double) getParameterValue(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE);
		
		ISteadyStateModel model = simResult.getModel();
		String simulationMethod = simResult.getMethod();
		EnvironmentalConditions env = simResult.getEnvironmentalConditions();
		
		SimulationSteadyStateControlCenter simulationControlCenter = new SimulationSteadyStateControlCenter(env, null, model, simulationMethod);
		simulationControlCenter.setSolver(solver);
		simulationControlCenter.setMaximization(true);
		simulationControlCenter.setFBAObjSingleFlux(biomassID, 1.0);
		
		SteadyStateSimulationResult solution = null;
		
		try {
			solution = simulationControlCenter.simulate();
		} catch (Exception e) {
			Debugger.debug("YieldMinimumBiomassObjectiveFunction: could not compute reference biomass value");
			e.printStackTrace();
		}
		
		double biomass = 0.0;
		if (solution != null) {
			biomass = solution.getFluxValues().getValue(biomassID);
			minimumBiomassValue = biomass * minBiomassPercentage;
		}
		
		System.out.println("\nwt biomass= " + biomass + " | min biomass= " + minimumBiomassValue + " | min percent= " + minBiomassPercentage);
		biomassComputed = true;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		Double minBiomassPercentage = (Double) getParameterValue(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE);
		String productID = (String) getParameterValue(YIELD_PARAM_PRODUCT);
		double percentage = MathUtils.round(minBiomassPercentage * 100, 2);
		return "YIELD> percentage=" + percentage + ";target=" + productID;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return getID();
	}
	
	public String getDesiredId() {
		return (String) getParameterValue(YIELD_PARAM_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		Double minBiomassPercentage = (Double) getParameterValue(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE);
		double percentage = MathUtils.round(minBiomassPercentage * 100, 2);
		return "$YIELD = max (\\text{" + getParameterValue(YIELD_PARAM_PRODUCT) + "}); biomass \\ge " + percentage + "\\% wt$";
		
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(YIELD_PARAM_BIOMASS) + "," + getParameterValue(YIELD_PARAM_PRODUCT) + "," + getParameterValue(YIELD_PARAM_MIN_BIOMASS_PERCENTAGE) + "," + getParameterValue(YIELD_PARAM_SOLVER) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
