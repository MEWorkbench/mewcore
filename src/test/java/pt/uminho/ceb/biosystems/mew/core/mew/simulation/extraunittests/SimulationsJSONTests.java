package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSONReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CLPSolverBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class SimulationsJSONTests {
	
	protected static String JSON_FOLDER = "./../biocomponents/src/test/resources/JsonModels/JSON/";
	protected static String WRITE_FOLDER = "./../biocomponents/src/test/resources/JsonModels/Write/";
	
	protected SteadyStateModel modelJSON;
	protected SteadyStateModel modelJSONOriginal;
	
	protected Container contJSON;
	protected Container contJSONOriginal;
	
	String modelName;
	
	
	@Before
	public void setData() throws Exception {
		modelName = "e_coli_core";
//		modelName = "iAF1260";
//		modelName = "iMM904";
//		modelName = "iCHOv1";
		
		
		JSONReader readerJson = new JSONReader(WRITE_FOLDER +modelName + ".json", modelName, true);
		contJSON = new Container(readerJson);
		modelJSON = (SteadyStateModel) ContainerConverter.convert(contJSON);
		
		JSONReader readerJsonOriginal = new JSONReader(JSON_FOLDER +modelName + ".json", modelName, true);
		contJSONOriginal = new Container(readerJsonOriginal);
		modelJSONOriginal = (SteadyStateModel) ContainerConverter.convert(contJSONOriginal);
		
	}
	
	protected EnvironmentalConditions getEC(String modelName){
		EnvironmentalConditions toRet = new EnvironmentalConditions();
		
		switch (modelName) {
		case "e_coli_core":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(8.39, 10000.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-10.0, 10000.0));
			break;

		case "iAF1260":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(8.39, 8.39));
//			toRet.addReactionConstraint("CAT", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_cbl1_e", new ReactionConstraint(-0.01, 10000.0));
//			toRet.addReactionConstraint("SPODMpp", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-8.0, 10000.0));
//			toRet.addReactionConstraint("FHL", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("SPODM", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_o2_e", new ReactionConstraint(-18.5, 10000.0));
			break;
			
		case "iMM904":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(1.0, 1.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-10.0, 10000.0));
//			toRet.addReactionConstraint("EX_o2_e", new ReactionConstraint(-2.0, 10000.0));
			break;	
		}
		
		return toRet;
	}
	
	@Test
	public void simulateEcoliCoreFBA() throws Exception{
		
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(getEC(modelName), null, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(CLPSolverBuilder.ID);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		System.out.println(jsonOF + "\t" + jsonOFValue);
		
		
		SimulationSteadyStateControlCenter ccJSONOriginal = new SimulationSteadyStateControlCenter(getEC(modelName), null, modelJSONOriginal, SimulationProperties.FBA);
		ccJSONOriginal.setMethodType(SimulationProperties.FBA);
		ccJSONOriginal.setMaximization(true);
		ccJSONOriginal.setSolver(CLPSolverBuilder.ID);
		
		SteadyStateSimulationResult jsonSimOrig = ccJSONOriginal.simulate();
		
		String originalOF = jsonSimOrig.getOFString();
		
		double originalOFValue = jsonSimOrig.getOFvalue();
		
		System.out.println(originalOF + "\t" + originalOFValue);
		
		
		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + originalOF, jsonOF.equals(originalOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + originalOFValue, jsonOFValue == originalOFValue);
		
		
	}
	
	@Test
	public void compareJSONModels(){
		
		Set<String> jsonReactsOrig = new HashSet<String>(modelJSONOriginal.getReactions().keySet());
		Set<String> jsonReacts = new HashSet<String>(modelJSON.getReactions().keySet());
		
		for (String s : modelJSONOriginal.getReactions().keySet()) {
			Reaction reaction = modelJSON.getReaction(s);
			if(reaction != null){
				System.out.println("JSON: " + s + " vs ORIGINAL: " + reaction.getId());
				jsonReactsOrig.remove(s);
				jsonReacts.remove(s);
			}else{
				System.err.println("UNKNOWN: " + s);
			}
			
		}
		
		System.out.println(jsonReacts.size() + "\t" + jsonReactsOrig.size());
		
		Assert.assertTrue("Different reaction ids", (jsonReacts.size() == jsonReactsOrig.size()) && (jsonReactsOrig.size() == 0));
		
		for (String s : modelJSON.getReactions().keySet()) {
			Reaction reactionSBML = modelJSONOriginal.getReaction(s);
			Reaction reactionJSON = modelJSON.getReaction(s);
			
			ReactionConstraint sbmlConstraints = reactionSBML.getConstraints();
			ReactionConstraint jsonConstraints = reactionJSON.getConstraints();
			
			double lbSbml = sbmlConstraints.getLowerLimit();
			double ubSbml = sbmlConstraints.getUpperLimit();
			
			double lbjson = jsonConstraints.getLowerLimit();
			double ubjson = jsonConstraints.getUpperLimit();
			
			boolean lbDiff = false;
			if(lbSbml != lbjson){
				lbDiff = true;
			}
			
			boolean ubDiff = false;
			if(ubSbml != ubjson){
				ubDiff = true;
			}
			
			if(ubDiff || lbDiff){
				System.err.println("Differences: " + s + "\t" + reactionSBML.getConstraints() + " vs "+ reactionJSON.getConstraints());
			}
			
			
			Map<String, StoichiometryValueCI> jsonReactants = contJSON.getReaction(s).getReactants();
			Map<String, StoichiometryValueCI> jsonReactantsOriginal = contJSONOriginal.getReaction(s).getReactants();
			
			
			if(jsonReactants.size() != jsonReactantsOriginal.size()){
				System.err.println("DIFF REACTANTS: " + s + " JSON: " + jsonReactants.size() + " ORIGINAL: " + jsonReactantsOriginal.size());
				MapUtils.prettyPrint(jsonReactants);
				MapUtils.prettyPrint(jsonReactantsOriginal);
			}
			
			for (String react : jsonReactants.keySet()) {
				StoichiometryValueCI jsonStoich = jsonReactants.get(react);
				StoichiometryValueCI jsonStoichOriginal = jsonReactantsOriginal.get(react);
				if(!jsonStoich.getStoichiometryValue().equals(jsonStoichOriginal.getStoichiometryValue())){
					System.err.println("DIFF REACTANT " + s +"\t" + react + "\t" + jsonStoich.getStoichiometryValue() + " vs " + jsonStoichOriginal.getStoichiometryValue());
				}
				
			}
			
			Map<String, StoichiometryValueCI> jsonProducts = contJSON.getReaction(s).getProducts();
			Map<String, StoichiometryValueCI> jsonProductsOriginal = contJSONOriginal.getReaction(s).getProducts();
			
			if(jsonProducts.size() != jsonProductsOriginal.size()){
				System.err.println("DIFF PRODUCTS: " + s + " JSON: " + jsonProducts.size() + " ORIGINAL: " + jsonProductsOriginal.size());
				MapUtils.prettyPrint(jsonProducts);
				MapUtils.prettyPrint(jsonProductsOriginal);
			}
			
			for (String prod : jsonProducts.keySet()) {
				StoichiometryValueCI jsonStoich = jsonProducts.get(prod);
				StoichiometryValueCI jsonStoichOriginal = jsonProductsOriginal.get(prod);
				if(!jsonStoich.getStoichiometryValue().equals(jsonStoichOriginal.getStoichiometryValue())){
					System.err.println("DIFF PRODUCT " + s +"\t" + prod + "\t" + jsonStoich.getStoichiometryValue() + " vs " + jsonStoichOriginal.getStoichiometryValue());
				}
			}
		}
		
	}

}
