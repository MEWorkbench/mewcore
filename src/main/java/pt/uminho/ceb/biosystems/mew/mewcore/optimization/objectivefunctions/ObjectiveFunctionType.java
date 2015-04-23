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

import java.lang.reflect.Constructor;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.Evaluator;

public enum ObjectiveFunctionType {
	
	EN{
		@Override
		public String toString(){
			return "ENSEMBLE";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					IObjectiveFunction.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return EnsembleObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "";
		}
	},
	BPCY{
		@Override
		public String toString(){
			return "BPCY: Biomass-Product Coupled Yield";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					String.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return BPCYObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(biomassId, desiredFluxId, substrateId)";
		}
	},
	YIELD{
		@Override
		public String toString(){
			return "YIELD: Product flux with Minimum Biomass";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					Double.class,
					SolverType.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return YieldMinimumBiomassObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(biomassId, desiredFluxId, minimumBiomass, solver)";
		}
	},
	PYIELD{
		@Override
		public String toString(){
			return "PYIELD: Product Yield with Minimum Biomass";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					String.class,
					Double.class,
					Double.class,
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return ProductYieldObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "ProductYieldObjectiveFunction(biomassId, productId, substrateId, minUptakeFlux, minBiomassFlux )";
		}
	},
	FV{
		@Override
		public String toString(){
			return "Max/Min of Reaction Flux value";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					Boolean.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return FluxValueObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(id, isMaximization)";
		}
		
	},
	FV_NR{
		@Override
		public String toString(){
			return "Max/Min of Reaction Flux value / number new reactions";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return FluxValueNumberReactionsObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			// TODO Auto-generated method stub
			return "(fluxId)";
		}
	},
	NK{
		@Override
		public String toString(){
			return "Max/Min Number of Knockouts";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					Boolean.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return NumKnockoutsObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(Boolean maximize)";
		}
	},
	SUM_F{
		@Override
		public String toString(){
			return "Max/Min Sum of Flux Measures";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					Boolean.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return SumOfFluxesObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(maximize, fluxesToSum)";
		}
	},
	WBP{
		@Override
		public String toString(){
			return "Weighted Biomass Product";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					String.class,
					Double.class,
					Double.class
			};
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return WeightedBPObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(biomassId, desiredFluxId, maxBiomass, maxProduct, alpha)";
		}
	}

//	WFVA{
//		@Override
//		public String toString(){
//			return "Weighted Flux Variability Analysis";
//		}
//
//	}
	, WEIGHTED_YIELD {
		@Override
		public String toString() {
			return"Weighted Yield Variability Analysis";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					double.class,
					SolverType.class,
					double.class
			};
			
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return WeightedYIELDObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(biomassId, desiredFluxId, alpha, lpSolver, minBiomassValue)";
		}
	
	}
	, WEIGHTED_BPCY {
		@Override
		public String toString() {
			return"Weighted Yield Variability Analysis";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					double.class,
					SolverType.class
			};
			
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return WeightedBPCYObjectiveFunction.class;
		}

		@Override
		protected String toStringAtgumentsHelp() {
			return "(biomassId, desiredFluxId, alpha, solver)";
		}
	
	}
	;

	@Override
	abstract public String toString();

	protected abstract Class<?>[] getArgumentsClasses();

	protected abstract Class<?> getObjectiveFunctionClass();
	
	protected abstract String toStringAtgumentsHelp();

	public IObjectiveFunction getObjectiveFunction(String... args) throws InvalidObjectiveFunctionConfiguration {

		Class<?> klazz = getObjectiveFunctionClass();
		Class<?>[] argsClasses = getArgumentsClasses();
		IObjectiveFunction of;
		try {

			Object[] initArgs = new Object[args.length];
			for(int i = 0; i < args.length; i++)
				initArgs[i] = Evaluator.evaluate(args[i], argsClasses[i]);

			Constructor<?> constructor = klazz.getConstructor(argsClasses);
			Object unTypedOF = constructor.newInstance(initArgs);
			of = IObjectiveFunction.class.cast(unTypedOF);
		} catch (Exception e) {
			throw new InvalidObjectiveFunctionConfiguration(args, argsClasses, klazz, e);
		}

		return of;	
	}
	
	public IObjectiveFunction getObjectiveFunction(Object... initArgs) throws InvalidObjectiveFunctionConfiguration {

		Class<?> klazz = getObjectiveFunctionClass();
		Class<?>[] argsClasses = getArgumentsClasses();
		IObjectiveFunction of;
		try {

			Constructor<?> constructor = klazz.getConstructor(argsClasses);
			Object unTypedOF = constructor.newInstance(initArgs);
			of = IObjectiveFunction.class.cast(unTypedOF);
		} catch (Exception e) {
			throw new InvalidObjectiveFunctionConfiguration(initArgs, argsClasses, klazz, e);
		}

		return of;	
	}

}
