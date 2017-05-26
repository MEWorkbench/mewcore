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
package pt.uminho.ceb.biosystems.mew.core.simulation.fva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.FVAZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;

public class FBAFluxVariabilityAnalysisNew implements IFluxVariabilityAnalysis {

	protected ISteadyStateModel 		model;
	protected EnvironmentalConditions 	envConditions;
	protected GeneticConditions 		geneticConditions;
	protected SolverType 				solverType;

	protected SimulationSteadyStateControlCenter simulCenter;

	protected FluxValueMap referenceFD = null;

	public FBAFluxVariabilityAnalysisNew(ISteadyStateModel model, EnvironmentalConditions envConditions, GeneticConditions geneticConditions,
			SolverType solverType) throws Exception{
		this(model, envConditions, geneticConditions, solverType, false);
	}
	
	public FBAFluxVariabilityAnalysisNew(ISteadyStateModel model, EnvironmentalConditions envConditions, GeneticConditions geneticConditions,
			SolverType solverType, boolean isPFBAReference) throws Exception {
		
		this(model, envConditions, geneticConditions, solverType, calculateReference(model, envConditions, geneticConditions, solverType, isPFBAReference));
	}

	
	static FluxValueMap calculateReference(ISteadyStateModel model, EnvironmentalConditions envConditions, GeneticConditions geneticConditions,
			SolverType solverType, boolean isPFBAReference){
		
		AbstractSSBasicSimulation<LPProblem> fba = new FBA(model);
		fba.setEnvironmentalConditions(envConditions);
		fba.setGeneticConditions(geneticConditions);
		fba.setSolverType(solverType);
		fba.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		
		if(isPFBAReference){
			PFBA<AbstractSSBasicSimulation<LPProblem>> pfba = new PFBA<>(model);
			pfba.setInitProblem(fba);
			pfba.setSolverType(solverType);
			fba = pfba;
		}
		return fba.simulate().getFluxValues();
	}
	
	public FBAFluxVariabilityAnalysisNew(ISteadyStateModel model, EnvironmentalConditions envConditions, GeneticConditions geneticConditions,
			SolverType solverType, FluxValueMap reference) throws Exception {
		this.model = model;
		this.envConditions = envConditions;
		this.geneticConditions = geneticConditions;
		this.solverType = solverType;

		this.referenceFD = reference;
	}

	public FBAFluxVariabilityAnalysisNew(ISteadyStateModel model, SolverType solverType) throws Exception {
		this(model, null, null, solverType);
	}

	// gives maximum or minimum value for a flux (no constraints)
	public double optimumFlux(String fluxId, boolean maximize) {
		getSimulCenter().setFBAObjSingleFlux(fluxId, 1.0);
		getSimulCenter().setMaximization(maximize);

		double value = Double.NaN;
		try {
			SteadyStateSimulationResult res = getSimulCenter().simulate();
			value = res.getFluxValues().get(fluxId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return value;
	}

	public double[] limitsFlux(String fluxId) {
		double[] res = new double[2];

		res[0] = optimumFlux(fluxId, false);
		res[1] = optimumFlux(fluxId, true);

		return res;
	}

	// gives maximum or minimum value for a flux given a constraint on the
	// biomass
	// (biomass given as percentage of wild type)
	public double optimumFlux(String fluxId, double minimumPercBiomass, boolean maximize) throws Exception {
		String biomassId = model.getBiomassFlux();
		double wtBiomass = referenceFD.get(biomassId);
		Double biomassLowerLimit = minimumPercBiomass * wtBiomass;

		if (biomassLowerLimit.equals(Double.NaN))
			biomassLowerLimit = 0.0;

		ReactionConstraint oldConstraints = model.getReactionConstraint(biomassId);
		ReactionConstraint newConstraints = new ReactionConstraint(biomassLowerLimit, oldConstraints.getUpperLimit());

		EnvironmentalConditions conditions;

		if (this.getSimulCenter().getEnvironmentalConditions() != null)
			conditions = this.getSimulCenter().getEnvironmentalConditions().copy();
		else
			conditions = new EnvironmentalConditions();

		conditions.addReactionConstraint(biomassId, newConstraints);

		getSimulCenter().setFBAObjSingleFlux(fluxId, 1.0);
		getSimulCenter().setEnvironmentalConditions(conditions);
		getSimulCenter().setMaximization(maximize);

		SteadyStateSimulationResult res = getSimulCenter().simulate();

		if (res == null) {
			return 0.0;
		} else {
			return res.getFluxValues().get(fluxId);
		}
	}

	public double[] limitsFlux(String fluxId, double minPercBiomass) throws Exception {
		double[] res = new double[2];

		res[0] = optimumFlux(fluxId, minPercBiomass, false);
		res[1] = optimumFlux(fluxId, minPercBiomass, true);

		return res;
	}

	public Map<String, double[]> limitsAllFluxes(double minimumPercBiomass) throws Exception {
		Map<String, double[]> res = new HashMap<String, double[]>();

		for (int i = 0; i < model.getNumberOfReactions(); i++) {

			String reactionId = model.getReactionId(i);

			double r[] = limitsFlux(reactionId, minimumPercBiomass);

			System.out.println(reactionId + "\t" + r[0] + "\t" + r[1]);

			res.put(reactionId, r);

		}
		return res;
	}
	
	/**
	 * Based on a pivot flux and a step number gives the min/max variation of a list of fluxes
	 * @param pivotFlux
	 * Flux that will be the center of the variation - Most of the times is the Biomass Flux
	 * @param targetFluxes
	 * List of fluxes that will be analyze along the pivot flux variation
	 * @param pivotNumStep
	 * Number of steps that represents the pivot flux variation
	 * @return
	 * A Map that for each flux have another Map that for each pivot step has a third map with two keys (Min and Max) that matches the flux values.
	 * <br>The String is the fluxID.
	 * <br>The first Double is for steps.
	 * <br>The Boolean represents the minimization and maximization.
	 * <br>The second Double corresponds to the Min/Max flux values
	 * @throws Exception
	 */
	public Map<String, Map<Double, SteadyStateSimulationResult[]>> fluxVariation(String pivotFlux, List<String> targetFluxes, int pivotNumStep, EnvironmentalConditions envCond, GeneticConditions geneCond) throws Exception {
		
		Map<String, Map<Double, SteadyStateSimulationResult[]>> mapToRet = new LinkedHashMap<>();
		
		for (String flux : targetFluxes)
			mapToRet.put(flux, fluxVariation(pivotFlux, flux, pivotNumStep, envCond, geneCond));
		
		return mapToRet;
	}
	
	
	/**
	 * Based on a pivot flux and a step number gives the min/max variation of a specific flux.
	 * @param pivotFlux
	 * Flux that will be the center of the variation - Most of the times is the Biomass Flux.
	 * @param targetFlux
	 * Flux that will be analyze along the pivot flux variation.
	 * @param pivotNumStep
	 * Number of steps that represents the pivot flux variation.
	 * @return
	 * A Map that for each pivot step has a second map with two keys (Min and Max) that matches the flux values.
	 * <br>The first Double is for steps.
	 * <br>The Boolean represents the minimization and maximization.
	 * <br>The second Double the correspondent Min/Max flux values.
	 * @throws Exception
	 */
	public Map<Double, SteadyStateSimulationResult[]> fluxVariation(String pivotFlux, String targetFlux, int pivotNumStep, EnvironmentalConditions envCond, GeneticConditions geneCond) throws Exception {
		
		Map<Double, SteadyStateSimulationResult[]> mapToRet = new LinkedHashMap<>();
		
		if(model.getReactions().containsKey(pivotFlux) && model.getReactions().containsKey(targetFlux)){
			
			// Get Min and Max Pivot Flux Values
			setEnvConditions(envCond);
			setGeneticConditions(geneCond);
			Double[] minMaxPair = {run(model, false, pivotFlux).getOFvalue(), run(model, true, pivotFlux).getOFvalue()};
			double minPivotOF = minMaxPair[0];
			double maxPivotOF = minMaxPair[1];
			
			// Obtain value for each step based on min/max pivot values and number of steps
			double eachStepValue = Math.abs(maxPivotOF - minPivotOF) / pivotNumStep;
			
			double actualStepValue = minPivotOF;
			int actualStep = 0;
			
			// Define new EnvCond with actual step value
			if(envCond == null) envCond = new EnvironmentalConditions();
			EnvironmentalConditions restrictions = envCond.copy();
			
			// for each step perform min/max flux simulations
			while(actualStep <= pivotNumStep){
				
				ReactionConstraint rc = new ReactionConstraint(actualStepValue, actualStepValue);
				restrictions.addReactionConstraint(pivotFlux, rc);
				defineRestrictions(restrictions);

				// Perform simulations
				SteadyStateSimulationResult[] simResults = {run(model, false, targetFlux), run(model, true, targetFlux)};
				
				// Add to Map
				mapToRet.put(actualStepValue, simResults);
				
				// Increment step value and iteration
				actualStepValue += eachStepValue;
				actualStep++;
				
				// Set previous envCond
				setEnvConditions(envCond);
			}
		}
		
		return mapToRet;
		
	}
	
	protected Double[] getMinMaxSimulation(String flux) throws Exception {
		double minOF = run(model, false, flux).getOFvalue();
		double maxOF = run(model, true, flux).getOFvalue();
		
		Double[] minMax = {minOF, maxOF};
		
		return minMax;
	}
	
	public double[] tightBounds(String fluxId) throws Exception {
		return limitsFlux(fluxId, 1.0);
	}

	/**
	 * For a given flux calculates maximum value, for each value of biomass,
	 * from 0% to 100% using interval defined by 2nd argument
	 */
	public double[] maxValuesFluxAllBiomasses(String fluxId, double interval) throws Exception {
		double[] res = new double[(int) (1.0 / interval) + 1];

		double biomassPerc = 0.0;

		for (int i = 0; i < res.length; i++) {
			res[i] = optimumFlux(fluxId, biomassPerc, true);
			biomassPerc += interval;
		}

		return res;
	}

	public FluxValueMap getReferenceFluxes() {

		return referenceFD;
	}

	public void setReferenceFluxes(FluxValueMap referenceFluxes) {
		this.referenceFD = referenceFluxes;
	}

	public EnvironmentalConditions getEnvConditions() {
		return getSimulCenter().getEnvironmentalConditions();
	}

	public void setEnvConditions(EnvironmentalConditions envConditions) throws Exception {
		getSimulCenter().setEnvironmentalConditions(envConditions);

		SteadyStateSimulationResult res = getSimulCenter().simulate();
		referenceFD = res.getFluxValues();

	}

	public GeneticConditions getGeneticConditions() {
		return getSimulCenter().getGeneticConditions();
	}

	public void setGeneticConditions(GeneticConditions gc) throws Exception {
		getSimulCenter().setGeneticConditions(gc);

		SteadyStateSimulationResult res = getSimulCenter().simulate();
		referenceFD = res.getFluxValues();
	}

	public FVAZeroValueFluxes identifyFVAZeroFluxes() throws Exception {

		List<String> zeroF = new ArrayList<String>();

		for (String fId : referenceFD.keySet()) {

			double wtF = referenceFD.get(fId);

			if (wtF == 0.0) {
				double[] limits = limitsFlux(fId, 0.0);
				if (limits[0] == 0 && limits[1] == 0)
					zeroF.add(fId);
				System.out.println(fId + " = [" + limits[0] + "," + limits[1] + "]");
			}
		}

		FVAZeroValueFluxes zerovalues = new FVAZeroValueFluxes(zeroF, this.getEnvConditions());
		return zerovalues;
	}

	public FVAZeroValueFluxes identifyFVAZeroFluxesIgnore(Set<String> toIgnore) throws Exception {
		List<String> zeroF = new ArrayList<String>();

		for (String fId : referenceFD.keySet()) {
			if (toIgnore == null || !toIgnore.contains(fId)) {
				double wtF = referenceFD.get(fId);

				if (wtF == 0.0) {
					double[] limits = limitsFlux(fId, 0.0);
					if (limits[0] == 0 && limits[1] == 0)
						zeroF.add(fId);
					// System.out.println(fId + " = [" + limits[0] + "," +
					// limits[1] + "]");
				}
			}
		}

		FVAZeroValueFluxes zerovalues = new FVAZeroValueFluxes(zeroF, this.getEnvConditions());
		return zerovalues;
	}

	public SimulationSteadyStateControlCenter getSimulCenter() {
		if (simulCenter == null) {
			simulCenter = new SimulationSteadyStateControlCenter(envConditions, geneticConditions, model, SimulationProperties.FBA);
			simulCenter.setSolver(solverType);
			simulCenter.setMaximization(true);
		}
		return simulCenter;
	}

	@Override
	public void defineRestrictions(EnvironmentalConditions restrictions) {
		getSimulCenter().setEnvironmentalConditions(restrictions);
	}

	@Override
	public SteadyStateSimulationResult run(ISteadyStateModel model, boolean isMaximization, String targetFlux) throws Exception {
		
		getSimulCenter().setFBAObjSingleFlux(targetFlux, 1.0);
		getSimulCenter().setMaximization(isMaximization);
		
		SteadyStateSimulationResult result = getSimulCenter().simulate(); 
		return result;
	}

}
