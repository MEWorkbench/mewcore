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
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.evalutionfunction;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.aggregation.IAggregationFunction;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;

public interface IStrainOptimizationEvaluationFunction extends IEvaluationFunction<IRepresentation>{

	public ISteadyStateDecoder getDecoder();
	
	public void setSolver(SolverType solver);
	
//	public void setMethodType(String methodType);
//	
//	public String getMethodType();
		
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions);

	public int getNumberOfObjectives();

	public void setNumberOfObjectives(int numberOfObjectives);

	public Object getSimulationProperty(String propertyKey);
	
	public void setSimulationProperty(String key, Object value);
	
	public Double[] evaluateMO(IRepresentation solution) throws Exception;
	
//	public SimulationSteadyStateControlCenter getSimulationControlCenter();
		
	public void setOverUnderReferenceDistribution(Map<String,Double> reference);

	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException;

	public IEvaluationFunction<IRepresentation> deepCopy() throws Exception;
	
	public List<IObjectiveFunction> getObjectiveFunctions();
	
	public void setFitnessAggregation(IAggregationFunction agg);
		
}
