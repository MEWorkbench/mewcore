package pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.optimization;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.CobraStrainOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter.ConnectionFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public class OptSwapFormulation extends ConnectionFormulation {
	
	String modelNameFromOptFlux;
	
	public OptSwapFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
		super(model, new MatlabConnection());
		modelNameFromOptFlux = model.getId();
	}
	
	@Override
	protected Map<String, Object> createConverterParameteres() {
		
		Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, properties.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS));
		
		prop.put(CobraStrainOptimizationProperties.TIME_LIMIT, properties.get(CobraStrainOptimizationProperties.TIME_LIMIT));
		prop.put(CobraStrainOptimizationProperties.PRODUCT_FLUX, properties.get(CobraStrainOptimizationProperties.PRODUCT_FLUX));
		prop.put(CobraStrainOptimizationProperties.SELECTED_RXNS, properties.get(CobraStrainOptimizationProperties.SELECTED_RXNS));
		prop.put(CobraStrainOptimizationProperties.MAX_MODIFICATIONS, properties.get(CobraStrainOptimizationProperties.MAX_MODIFICATIONS));
		prop.put("MAX_SWAP", properties.get("MAX_SWAP"));
		prop.put("NUM_INTERVENTIONS", properties.get("NUM_INTERVENTIONS"));
		prop.put("CRITICAL_RXNS", properties.get("CRITICAL_RXNS"));
		prop.put("USE_COBRA_SOLVER", properties.get("USE_COBRA_SOLVER"));
		
		return prop;
	}
	
	@Override
	protected void initPropsKeys() {
		super.initPropsKeys();
		possibleProperties.add(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	@Override
	public void prepareMatlabEnvironment() {
		try {
		
//			Set<String> test = ManagerExceptionUtils.testCast(properties, Set.class, "KOList", true);
//			
//			for (String string : test) {
//				System.out.println(string);
//			}
			
			converter.runCommand("opt = struct();");
			
			Set<String> allReactions = new LinkedHashSet<>(model.getReactions().keySet());
			
			if (properties.containsKey(CobraStrainOptimizationProperties.SELECTED_RXNS)) {
				Set<String> selectedRxns = (Set<String>) properties.get(CobraStrainOptimizationProperties.SELECTED_RXNS);
				converter.sendStringList("selectedRxnList", selectedRxns.toArray(new String[selectedRxns.size()]));
				converter.runCommand("opt.knockableRxns = selectedRxnList.';");
				allReactions.removeAll(selectedRxns);
			}
			
			int maxKOs = -1;
			if (properties.containsKey(CobraStrainOptimizationProperties.MAX_MODIFICATIONS))
				maxKOs = (int) properties.get(CobraStrainOptimizationProperties.MAX_MODIFICATIONS);
			converter.sendInteger("opt.knockoutNum", maxKOs);
			
			int maxSwaps = -1;
			if (properties.containsKey("MAX_SWAP"))
				maxSwaps = (int) properties.get("MAX_SWAP");
			converter.sendInteger("opt.swapNum", maxSwaps);
			
			int numInterventions = 1;
			if (maxKOs > -1 && maxSwaps > -1)
				numInterventions = maxKOs + maxSwaps;
			if (properties.containsKey("NUM_INTERVENTIONS"))
				numInterventions = (int) properties.get("NUM_INTERVENTIONS");
			converter.sendInteger("opt.interventionNum", numInterventions);
			
//			Define objective product reaction
			String productFlux = model.getBiomassFlux();
			if (properties.containsKey(CobraStrainOptimizationProperties.PRODUCT_FLUX))
				productFlux = (String) properties.get(CobraStrainOptimizationProperties.PRODUCT_FLUX);
			converter.sendString("opt.targetRxn", productFlux);
			allReactions.remove(productFlux);
			
			converter.runCommand("opt.dhRxns = {};");
			
//			Define possible constraints
//			if(properties.containsKey(OptimizationProperties.CONSTRAINED_REACTIONS)){
//				Set<ConstrainedReaction> constrainedReactionsList = new LinkedHashSet<ConstrainedReaction>((Set)properties.get(OptimizationProperties.CONSTRAINED_REACTIONS));
//				List<String> rxnList = new ArrayList<String>();
//				List<Double> valuesList = new ArrayList<Double>();
////				String[] rxnList = new String[constrainedReactionsList.size()+1];
////				Double[] valuesList = new Double[constrainedReactionsList.size()+1];
//				String senseList = "";
//				//int i = 0;
//				boolean hasBiomassConstraint = false;
//				for (ConstrainedReaction constrainedReaction : constrainedReactionsList) {
//					if(constrainedReaction.getReactionID().equals(model.getBiomassFlux()))
//						hasBiomassConstraint = true;
//					rxnList.add(constrainedReaction.getReactionID());
//					valuesList.add(constrainedReaction.getConstraintValue());
////					rxnList[i] = constrainedReaction.getReactionID();
////					valuesList[i] = constrainedReaction.getConstraintValue();
//					senseList += ConstrainedReaction.parseConstraintSense(constrainedReaction);
//					allReactions.remove(constrainedReaction.getReactionID());
//					//i++;
//				}
//				if(!hasBiomassConstraint)
//				{
//					rxnList.add(model.getBiomassFlux());
//					valuesList.add((double)properties.get(OptimizationProperties.MIN_GROWTH));
//					senseList += "G";
//				}
//				allReactions.remove(model.getBiomassFlux());
//				
//				converter.sendStringList("constrOpt.rxnList", rxnList.toArray(new String[rxnList.size()]));
//				converter.sendDoubleList("constrOpt.values", new ArrayList(Arrays.asList(valuesList)));
//				converter.sendString("constrOpt.sense", senseList);
//			}
//			else{
//				
//				List<String> rxnList = new ArrayList<String>();
//				rxnList.add(model.getBiomassFlux());
//				List<Double> valuesList = new ArrayList<Double>();
//				valuesList.add((double)properties.get(OptimizationProperties.MIN_GROWTH));
//				String senseList = "G";
//				
//				converter.sendStringList("constrOpt.rxnList", rxnList.toArray(new String[rxnList.size()]));
//				converter.sendDoubleList("constrOpt.values", new ArrayList(Arrays.asList(valuesList)));
//				converter.sendString("constrOpt.sense", senseList);
//			}
			
			// Define TimeLimit
			int timeLimit = 3600;
			if (properties.containsKey(CobraStrainOptimizationProperties.TIME_LIMIT))
				timeLimit = (int) properties.get(CobraStrainOptimizationProperties.TIME_LIMIT);
			converter.sendInteger("opt.solverParams.maxTime", timeLimit);
			
			int useCobraSolver = 1;
			if (properties.containsKey("USE_COBRA_SOLVER"))
				useCobraSolver = (int) properties.get("USE_COBRA_SOLVER");
			converter.sendInteger("opt.useCobraSolver", useCobraSolver);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Erro do lado do Matlab. Method: prepareMatlabEnvironment");
			e.printStackTrace();
		}
	}
	
	@Override
	public void executeSimulationCommand() {
		getConverter().runFunction("optSwap",
				Arrays.asList(new String[] { modelNameFromOptFlux, "opt" }),
				Arrays.asList(new String[] { "optSwapResult" }));
	}
	
	@Override
	public SteadyStateSimulationResult parseMatlabOutput() {
		SteadyStateSimulationResult result = new SteadyStateSimulationResult(model, "", null);
		
		try {
//			
//			double[] fluxes = getConverter().getVariableDoubleList("optKnockSol.fluxes");
//			String[] rxns = getConverter().getVariableStringList(modelNameFromOptFlux+".rxns");
////			
//			String[] koRxnsList = getConverter().getVariableStringList("optKnockSol"+".rxnList");
////			
//			FluxValueMap fluxValues = new FluxValueMap();
//			for (int i = 0; i < rxns.length; i++)
//				fluxValues.put(rxns[i], fluxes[i]);
////			
//			
//			getConverter().runFunction("testOptKnockSol",
//					Arrays.asList(new String[]{modelNameFromOptFlux, "options.targetRxn", "optKnockSol.rxnList"}),
//					Arrays.asList(new String[]{"growthRate","minProd","maxProd"}));
//
//			
//			
//			
////		
////			
//			result.setOFvalue((getConverter().getVariableDoubleList("optSwapResult.knockoutRxns"))[0]);
////			result.setOFvalue((getConverter().getVariableDoubleList("totalFluxDiff"))[0]);
//			result.setSolverOutput(getConverter().getVariableString("optSwapResult.solver"));
//			result.setOFString("Max: " + getConverter().getVariableString("opt.targetRxn"));
//			result.setFluxValues(fluxValues);
//			EnvironmentalConditions kos = new EnvironmentalConditions();
//			for (String reaction : koRxnsList) {
//				kos.put(reaction, new ReactionConstraint(0, 0));
//			}
//			
//			result.setEnvironmentalConditions((EnvironmentalConditions)properties.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS));
//			result.setGeneticConditions(new GeneticConditions(new ReactionChangesList(Arrays.asList(koRxnsList))));
////			
//			result.setMethod("OptSwap");
//			
			
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro do lado do Matlab. Method: parseMatlabOutput");
			return null;
		}
	}
	
//	@Override
//	public SteadyStateOptimizationResult parseMatlabOptimizationOutput(
//			SteadyStateSimulationResult simulationResult) {
//		
//		SteadyStateOptimizationResult optimizationResult = new SteadyStateOptimizationResult(simulationResult);
//		
//		//optimizationResult.setBiomassValue(biomassValue)
//		
//		
//		return null;
//	}
	
	public static void main(String[] args) {
		ConstrainedReaction react = new ConstrainedReaction("T", 10.0, "<");
		System.out.println(ConstrainedReaction.parseConstraintSense(react));
	}
	
	@Override
	public void clearAllProperties() {
	}
	
}
