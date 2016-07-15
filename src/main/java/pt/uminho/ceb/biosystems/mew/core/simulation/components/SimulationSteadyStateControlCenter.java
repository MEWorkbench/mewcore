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
package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.DSPP_LMOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.LMOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.MOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.NormLMoma;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.ROOM;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps.TDPS;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps.TDPS2;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover.MiMBl;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;

public class SimulationSteadyStateControlCenter extends AbstractSimulationSteadyStateControlCenter {
	
	private static final long serialVersionUID = 1L;
	
	protected static SimulationMethodsFactory factory;
	
	protected boolean forceRecreate = false;
	
	static {
		
		LinkedHashMap<String, Class<?>> mapMethods = new LinkedHashMap<String, Class<?>>();
		
		mapMethods.put(SimulationProperties.FBA, FBA.class);
		mapMethods.put(SimulationProperties.PFBA, PFBA.class);
		mapMethods.put(SimulationProperties.MOMA, MOMA.class);
		mapMethods.put(SimulationProperties.LMOMA, LMOMA.class);
		mapMethods.put(SimulationProperties.NORM_LMOMA, NormLMoma.class);
		mapMethods.put(SimulationProperties.ROOM, ROOM.class);
		mapMethods.put(SimulationProperties.MIMBL, MiMBl.class);
		mapMethods.put(SimulationProperties.DSPP_LMOMA, DSPP_LMOMA.class); 	//dual stage phenotype prediction	
		mapMethods.put(SimulationProperties.TDPS, TDPS.class);				//turnover dependent phenotype simulation - rui's method
		mapMethods.put(SimulationProperties.TDPS2, TDPS2.class);			//turnover dependent phenotype simulation - rui's method
		
		factory = new SimulationMethodsFactory(mapMethods);
	}
	
	public SimulationSteadyStateControlCenter(Map<String, Object> simulationConfiguration) {
		super(simulationConfiguration);
		SolverType solver = (SolverType) simulationConfiguration.get(SimulationProperties.SOLVER);
		Boolean isMaximization = (Boolean) simulationConfiguration.get(SimulationProperties.IS_MAXIMIZATION);
		Boolean overUnder2StepApproach = (Boolean) simulationConfiguration.get(SimulationProperties.OVERUNDER_2STEP_APPROACH);
		FluxValueMap wtReference = (FluxValueMap) simulationConfiguration.get(SimulationProperties.WT_REFERENCE);
		FluxValueMap ouReference = (FluxValueMap) simulationConfiguration.get(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
	
		setSolver(solver);
		setMaximization(isMaximization);
		setOverUnder2StepApproach(overUnder2StepApproach);
		setWTReference(wtReference);
		setUnderOverRef(ouReference);
	}
	
	
	public SimulationSteadyStateControlCenter(EnvironmentalConditions environmentalConditions, GeneticConditions geneticConditions, ISteadyStateModel model, String methodType) {
		
		super(environmentalConditions, geneticConditions, model, methodType);
	}
	
	public SteadyStateSimulationResult simulate() throws Exception {
		if (isUnderOverSimulation() && getUnderOverRef() == null && !isUnderOver2StepApproach()) {
			addUnderOverRef();
		}
		if (lastMethod == null || forceRecreate) {
			lastMethod = getFactory().getMethod(this.methodType, methodProperties, model);
			if(lastMethod!=null && IConvexSteadyStateSimulationMethod.class.isAssignableFrom(lastMethod.getClass())){
				((IConvexSteadyStateSimulationMethod)lastMethod).addPropertyChangeListener(this);				
			}
			forceRecreate = false;
		} else {
			lastMethod.putAllProperties(methodProperties);
		}
		return lastMethod.simulate();
	}
	
	public void setMethodType(String methodType) {
		if (!this.getMethodType().equals(methodType)) {
			forceRecreate = true;
		}
		super.setMethodType(methodType);
	}
	
	@Override
	public SimulationMethodsFactory getFactory() {
		return factory;
	}
	
	public Boolean isMaximization() {
		return (Boolean) (getProperty(SimulationProperties.IS_MAXIMIZATION));
	}
	
	public void setMaximization(Boolean isMaximization) {
		addProperty(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	public void setProductFlux(String productFlux) {
		addProperty(SimulationProperties.PRODUCT_FLUX, productFlux);
	}
	
	public void setSolver(SolverType solverType) {
		addProperty(SimulationProperties.SOLVER, solverType);
	}
	
	public void addUnderOverRef() throws Exception {
		FluxValueMap ref = SimulationProperties.simulateWT(model, getEnvironmentalConditions(), getSolverType());
		setUnderOverRef(ref);
	}
	
	private SolverType getSolverType() {
		return (SolverType) getProperty(SimulationProperties.SOLVER);
	}
	
	public void addWTReference() throws Exception {
		Map<String, Double> ref = SimulationProperties.simulateWT(model, getEnvironmentalConditions(), getSolverType());
		setWTReference(ref);
	}
	
	public void setReactionsKnockoutConditions(Set<String> knockoutIds) {
		
		ReactionChangesList reactionList = new ReactionChangesList(knockoutIds);
		GeneticConditions genConditions = new GeneticConditions(reactionList, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void setGenesKnockoutConditions(Set<String> knockoutIds) {
		GeneChangesList geneList = new GeneChangesList(knockoutIds);
		GeneticConditions genConditions = new GeneticConditions(geneList, (ISteadyStateGeneReactionModel) model, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void setRoomProperties(Double delta, Double epsilon) {
		addProperty(SimulationProperties.ROOM_DELTA, delta);
		addProperty(SimulationProperties.ROOM_EPSILON, epsilon);
	}
	
	public void setOverUnder2StepApproach(Boolean ou2stepApproach) {

		addProperty(SimulationProperties.OVERUNDER_2STEP_APPROACH, ou2stepApproach);
	}
	
	static public void registMethod(String methodId, Class<?> klass) throws RegistMethodException, NoConstructorMethodException {
		factory.addSimulationMethod(methodId, klass);
	}
	
	public void setFBAObj(Map<String, Double> obj) {
		addProperty(SimulationProperties.OBJECTIVE_FUNCTION, obj);
	}
	
	public void setFBAObjSingleFlux(String fluxId, Double objValueCoef) {
		Map<String, Double> m = new HashMap<String, Double>();
		m.put(fluxId, 1.0);
		setFBAObj(m);
	}
	
	public static Set<String> getRegisteredMethods() {
		return factory.getRegisteredMethods();
	}
	
	public static Class<? extends LPProblem> getProblemTypeFromMethod(String method) {
		return factory.getProblemTypeFromMethod(method);
	}
	
	public static void registerMethod(String id, Class<?> method) {
		factory.registerMethod(id, method);
	}
	
	/**
	 * <p>
	 * Method must be used with caution. When using a persistent solver, calling
	 * this option will inhibit the performance improvement granted by the
	 * persistence option.
	 * <p>
	 * However, if building and releasing
	 * <code>SimulationSteadyStateControlCenter</code> multiple times, this is
	 * necessary in order to prevent degeneration of memory from non-native java
	 * calls (e.g., C calls from CPLEX)
	 */
	public void forceSolverCleanup() {
		if (lastMethod != null && IConvexSteadyStateSimulationMethod.class.isAssignableFrom(lastMethod.getClass())){
			((IConvexSteadyStateSimulationMethod)lastMethod).forceSolverCleanup();			
		}
	}
}
