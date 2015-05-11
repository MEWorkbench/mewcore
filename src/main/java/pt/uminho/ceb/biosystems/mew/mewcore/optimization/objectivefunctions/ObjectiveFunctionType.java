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
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.Evaluator;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public enum ObjectiveFunctionType {
	
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
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
		protected String toStringArgumentsHelp() {
			return "(biomassId, desiredFluxId, maxBiomass, maxProduct, alpha)";
		}
	},
	WEIGHTED_YIELD {
		@Override
		public String toString() {
			return"Weighted Yield Variability Analysis";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					Double.class,
					SolverType.class,
					Double.class
			};
			
			return argumentsClasses;
		}

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return WeightedYIELDObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(biomassId, desiredFluxId, alpha, lpSolver, minBiomassValue)";
		}
	
	}, 
	WEIGHTED_BPCY {
		@Override
		public String toString() {
			return"Weighted Yield Variability Analysis";
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
			return WeightedBPCYObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(biomassId, desiredFluxId, alpha, solver)";
		}
	
	},
	FVA {
		@Override
		public String toString() {
			return"Flux Variability Analysis";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					Boolean.class
			};
			
			return argumentsClasses;
		}		

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return FVAObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(biomassId, desiredFluxId, isMaximization)";
		}
	
	},
	FVASENSE {
		@Override
		public String toString() {
			return"Flux Variability Analysis with sense";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					Boolean.class,
					Boolean.class,
					SolverType.class
			};
			
			return argumentsClasses;
		}		

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return FVASenseObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(biomassId, desiredFluxId, isMaximization, isFVAMaximization, solverType)";
		}
	
	},
	CYIELD {
		@Override
		public String toString() {
			return"Carbon yield";
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
			return CarbonYieldObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(substrateID, desiredFluxId, modelConfigurationFile)";
		}
	
	},
	PMTCYIELD {
		@Override
		public String toString() {
			return" Percentage of Max. Theor. Carbon yield";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					String.class,
					SolverType.class
			};
			
			return argumentsClasses;
		}		

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return PercentMaxTheoreticalCarbonYieldObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(substrateID, desiredFluxId, modelConfigurationFile, solver)";
		}
	
	}
	,
	TURN {
		@Override
		public String toString() {
			return"Turnover";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					String.class,
					String.class,
					Double.class,
					Boolean.class
			};
			
			return argumentsClasses;
		}		

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return TurnoverObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "(metaboliteID, biomassID, minBiomass, isMaximization)";
		}
	},
	MIN_ALTERN {
		@Override
		public String toString() {
			return "Minimize alternatives";
		}
		
		@Override
		protected Class<?>[] getArgumentsClasses() {
			Class<?>[] argumentsClasses = {
					Set.class,
					String.class,
					String.class,
					String.class,
					SolverType.class,
					Boolean.class
			};
			
			return argumentsClasses;
		}		

		@Override
		protected Class<?> getObjectiveFunctionClass() {
			return MinimizeAlternativesObjectiveFunction.class;
		}

		@Override
		protected String toStringArgumentsHelp() {
			return "([alternative1, alternative2, ..., alternativeN], desiredFluxId, modelConfigurationFile, solver, useCountsInsteadOfCarbons, maximize)";
		}
	
	}	
	;

	@Override
	abstract public String toString();

	protected abstract Class<?>[] getArgumentsClasses();

	protected abstract Class<?> getObjectiveFunctionClass();
	
	protected abstract String toStringArgumentsHelp();

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
	
//	public static void main(String ... args){
//		
//	}

}
