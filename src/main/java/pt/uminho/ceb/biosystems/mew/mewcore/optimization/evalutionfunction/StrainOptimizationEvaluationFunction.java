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
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.evalutionfunction;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

/**
 * The Strain Optimization Evaluation Function
 * 
 * @author mrocha, pmaia
 */
public class StrainOptimizationEvaluationFunction extends AbstractMultiobjectiveEvaluationFunction<IRepresentation> implements IStrainOptimizationEvaluationFunction {
	
	private static final long						serialVersionUID	= 1L;
	
	protected final boolean							debug				= false;
	
	protected List<IObjectiveFunction>				objectiveFunctions;
	
	protected ISteadyStateModel						model;
	
	protected ISteadyStateDecoder					decoder;
	
	protected SimulationSteadyStateControlCenter	controlCenter;
	
	protected int									numberOfObjectives	= 1;
	
	protected SolverType							solver;
	
	protected String								simulationMethod;
	
	protected FluxValueMap							referenceFD;
	
	public StrainOptimizationEvaluationFunction(
			ISteadyStateModel model,
			ISteadyStateDecoder decoder,
			List<IObjectiveFunction> objectiveFunctions,
			EnvironmentalConditions envConds,
			SolverType solver,
			String simulationMethod,
			FluxValueMap referenceFD,
			boolean isMaximization) throws Exception {
		super(true);
		this.model = model;
		this.objectiveFunctions = objectiveFunctions;
		this.decoder = decoder;
		this.solver = solver;
		this.simulationMethod = simulationMethod;
		this.referenceFD = referenceFD;
		this.numberOfObjectives = objectiveFunctions.size();
		
		this.controlCenter = new SimulationSteadyStateControlCenter(envConds, null, model, simulationMethod);
		this.controlCenter.setSolver(solver);
		this.controlCenter.setMaximization(isMaximization);
		if (this.referenceFD != null) {
			this.controlCenter.setWTReference(this.referenceFD);
		}
	}
	
	public StrainOptimizationEvaluationFunction(ISteadyStateModel model, ISteadyStateDecoder decoder, List<IObjectiveFunction> objectiveFunctions, EnvironmentalConditions envConds, SolverType solver, String simulationMethod) throws Exception {
		this(model, decoder, objectiveFunctions, envConds, solver, simulationMethod, null, true);
	};
	
	public ISteadyStateDecoder getDecoder() {
		return decoder;
	}
	
	public void setSolver(SolverType solver) {
		controlCenter.setSolver(solver);
	}
	
	public void setMethodType(String methodType) {
		controlCenter.setMethodType(methodType);
	}
	
	public String getMethodType() {
		return controlCenter.getMethodType();
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		controlCenter.setEnvironmentalConditions(environmentalConditions);
	}
	
	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}
	
	public void setNumberOfObjectives(int numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
	}
	
	public Object getSimulationProperty(String propertyKey) {
		return controlCenter.getProperty(propertyKey);
	}
	
	public void setSimulationProperty(String key, Object value) {
		controlCenter.setSimulationProperty(key, value);
	}
	
	@Override
	public Double[] evaluateMO(IRepresentation solution) {
		Double[] resultList = new Double[objectiveFunctions.size()];
		
		GeneticConditions gc = null;
		try {
			gc = decoder.decode(solution);
		} catch (Exception e1) {
			if (debug) System.out.println("StrainOptimizationEvaluationFunction.evaluteMO: decoder exception: \n" + e1.toString());
		}
		controlCenter.setGeneticConditions(gc);
		
		if (debug) {
			System.out.println("\n===[SOL IN]===");
			if (gc.getGeneList() == null) {
				for (Pair<String, Double> pair : gc.getReactionList().getPairsList())
					System.out.println(pair.getValue() + " = " + pair.getPairValue());
			} else {
				for (Pair<String, Double> pair : gc.getGeneList().getPairsList())
					System.out.println(pair.getValue() + " = " + pair.getPairValue());
			}
			System.out.println("===[SOL OUT]===");
		}
		
		SteadyStateSimulationResult result = null;
		
		try {
			result = controlCenter.simulate();
			if (debug) System.out.println("Biomass: " + result.getFluxValues().get(model.getBiomassFlux()));
		} catch (Exception e) {
			//			e.printStackTrace();
			if (debug) System.out.println("StrainOptimizationEvaluationFunction.evaluateMO: Simulation exception: \n" + e.toString());
		}
		
		Double fitness = null;
		if (result != null && (result.getSolutionType().equals(LPSolutionType.OPTIMAL) || result.getSolutionType().equals(LPSolutionType.FEASIBLE))) {
			int size = objectiveFunctions.size();
			for (int i = 0; i < size; i++) {
				IObjectiveFunction of = objectiveFunctions.get(i);
				try {
					fitness = of.evaluate(result);
					if (debug) System.out.println("Fitness(" + i + "): " + fitness);
				} catch (Exception e) {
					if (debug) e.printStackTrace();
				}
				resultList[i] = fitness;
			}
			if (debug) System.out.println(result.getSolutionType());
			
		} else {
			int size = objectiveFunctions.size();
			for (int i = 0; i < size; i++) {
				//NOTE: this may not be correct for clashing OFs, i.e., max vs min. Should be evaluated separately for each OF. (of.isMaximization ?)
				IObjectiveFunction of = objectiveFunctions.get(i);
				resultList[i] = of.getWorstFitness();
				if (debug) System.out.println("fit: " + i + "\t" + of.getWorstFitness());
			}
		}
		
		return resultList;
	}
	
	public SimulationSteadyStateControlCenter getSimulationControlCenter() {
		return this.controlCenter;
	}
	
	public void setOverUnderReferenceDistribution(Map<String, Double> reference) {
		this.setSimulationProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, reference);
	}
	
	@Override
	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
	}
	
	@Override
	public IEvaluationFunction<IRepresentation> deepCopy() throws Exception {
		return new StrainOptimizationEvaluationFunction(this.model, this.decoder, this.objectiveFunctions, controlCenter.getEnvironmentalConditions(), solver, simulationMethod, referenceFD, isMaximization);
	}
	
	/**
	 * @return the objectiveFunctions
	 */
	public List<IObjectiveFunction> getObjectiveFunctions() {
		return objectiveFunctions;
	}
	
}
