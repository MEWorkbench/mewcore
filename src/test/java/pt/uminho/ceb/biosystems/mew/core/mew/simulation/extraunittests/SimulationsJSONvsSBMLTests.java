package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLLevel3Reader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSONReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class SimulationsJSONvsSBMLTests {
	
	protected static String JSON_FOLDER = "./../biocomponents/src/test/resources/JsonModels/JSON/";
	protected static String SBML_FOLDER = "./../biocomponents/src/test/resources/JsonModels/SBML/";
	
	protected SteadyStateModel modelSBML;
	protected SteadyStateModel modelJSON;
	
	protected Container contSBML;
	protected Container contJSON;
	
	String modelName;
	
	
	GeneticConditions reactionsKO = null;
	GeneticConditions reactionsOU = null;
	GeneticConditions genesSBMLKO = null;
	GeneticConditions genesJSONKO = null;
	GeneticConditions genesSBMLOU = null;
	GeneticConditions genesJSONOU = null;
	
	
	@Before
	public void setData() throws Exception {
		modelName = "e_coli_core";
//		modelName = "iAF1260";
//		modelName = "iMM904";
//		modelName = "iCHOv1";
		
		JSBMLLevel3Reader readerSBML = new JSBMLLevel3Reader(SBML_FOLDER + modelName + ".xml", "1", false);
		JSONReader readerJson = new JSONReader(JSON_FOLDER + modelName + ".json", modelName, true);
		
		contSBML = new Container(readerSBML);
		modelSBML = (SteadyStateModel) ContainerConverter.convert(contSBML);
		
		contJSON = new Container(readerJson);
		modelJSON = (SteadyStateModel) ContainerConverter.convert(contJSON);

		reactionsKO = new GeneticConditions(new ReactionChangesList(Arrays.asList("R_SUCDi", "R_G6PDH2r", "R_EX_ac_e")));
		genesSBMLKO = new GeneticConditions(new GeneChangesList(Arrays.asList("G_s0001", "G_b3732", "G_b2463", "G_b1852")), (ISteadyStateGeneReactionModel) modelSBML, false);
		genesJSONKO = new GeneticConditions(new GeneChangesList(Arrays.asList("s0001", "b3732", "b2463", "b1852")), (ISteadyStateGeneReactionModel) modelJSON, false);
		
		reactionsOU = new GeneticConditions(new ReactionChangesList(
												Arrays.asList("R_THD2", "R_FUM", "R_EX_akg_e", "R_TALA", "R_MDH"), 
												Arrays.asList(0.125, 0.0, 0.0, 0.0, 0.0)), 
											true);
		genesSBMLOU = new GeneticConditions(new GeneChangesList(
							Arrays.asList("G_b0721", "G_b0767", "G_b1849"), 
							Arrays.asList(0.03125, 0.0, 0.0625)), 
							(ISteadyStateGeneReactionModel) modelSBML, true);
		genesJSONOU = new GeneticConditions(new GeneChangesList(
							Arrays.asList("b0721", "b0767", "b1849"), 
							Arrays.asList(0.03125, 0.0, 0.0625)), 
							(ISteadyStateGeneReactionModel) modelJSON, true);
		
	}
	
	protected EnvironmentalConditions getEC(String modelName){
		EnvironmentalConditions toRet = new EnvironmentalConditions();
		
//		switch (modelName) {
//		case "e_coli_core":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(8.39, 10000.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-10.0, 10000.0));
//			break;
//
//		case "iAF1260":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(8.39, 8.39));
//			toRet.addReactionConstraint("CAT", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_cbl1_e", new ReactionConstraint(-0.01, 10000.0));
//			toRet.addReactionConstraint("SPODMpp", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-8.0, 10000.0));
//			toRet.addReactionConstraint("FHL", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("SPODM", new ReactionConstraint(0.0, 0.0));
//			toRet.addReactionConstraint("EX_o2_e", new ReactionConstraint(-18.5, 10000.0));
//			break;
//			
//		case "iMM904":
//			toRet.addReactionConstraint("ATPM", new ReactionConstraint(1.0, 1.0));
//			toRet.addReactionConstraint("EX_glc__D_e", new ReactionConstraint(-10.0, 10000.0));
//			toRet.addReactionConstraint("EX_o2_e", new ReactionConstraint(-2.0, 10000.0));
//			break;	
//		}
		
		return toRet;
	}
	
	@Test
	public void simulateEcoliCoreFBA() throws Exception{
		
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(getEC(modelName), null, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		double jsonSuccValue = jsonSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + jsonSuccValue);
		
		
		SimulationSteadyStateControlCenter ccSBML = new SimulationSteadyStateControlCenter(null, null, modelSBML, SimulationProperties.FBA);
		ccSBML.setMethodType(SimulationProperties.FBA);
		ccSBML.setMaximization(true);
		ccSBML.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult sbmlSim = ccSBML.simulate();

		String sbmlOF = sbmlSim.getOFString();
		
		double sbmlOFValue = sbmlSim.getOFvalue();
		
		double sbmlSuccValue = sbmlSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + sbmlSuccValue);
		
		
		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + sbmlOF, jsonOF.equals(sbmlOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + sbmlOFValue, jsonOFValue == sbmlOFValue);
		
		Assert.assertTrue("Different succinate value: " + jsonSuccValue + " vs " + sbmlSuccValue, jsonSuccValue == sbmlSuccValue);
		
	}
	
	@Test
	public void simulateEcoliCoreFBA_RK() throws Exception{
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(null, reactionsKO, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		double jsonSuccValue = jsonSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + jsonSuccValue);
		
		
		SimulationSteadyStateControlCenter ccSBML = new SimulationSteadyStateControlCenter(null, reactionsKO, modelSBML, SimulationProperties.FBA);
		ccSBML.setMethodType(SimulationProperties.FBA);
		ccSBML.setMaximization(true);
		ccSBML.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult sbmlSim = ccSBML.simulate();

		String sbmlOF = sbmlSim.getOFString();
		
		double sbmlOFValue = sbmlSim.getOFvalue();
		
		double sbmlSuccValue = sbmlSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + sbmlSuccValue);
		
		
		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + sbmlOF, jsonOF.equals(sbmlOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + sbmlOFValue, jsonOFValue == sbmlOFValue);
		
		Assert.assertTrue("Different succinate value: " + jsonSuccValue + " vs " + sbmlSuccValue, jsonSuccValue == sbmlSuccValue);
	}
	
	@Test
	public void simulateEcoliCoreFBA_ROU() throws Exception{
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(null, reactionsOU, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		double jsonSuccValue = jsonSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + jsonSuccValue);
		
		
		SimulationSteadyStateControlCenter ccSBML = new SimulationSteadyStateControlCenter(null, reactionsOU, modelSBML, SimulationProperties.FBA);
		ccSBML.setMethodType(SimulationProperties.FBA);
		ccSBML.setMaximization(true);
		ccSBML.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult sbmlSim = ccSBML.simulate();

		String sbmlOF = sbmlSim.getOFString();
		
		double sbmlOFValue = sbmlSim.getOFvalue();
		
		double sbmlSuccValue = sbmlSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + sbmlSuccValue);
		
		
		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + sbmlOF, jsonOF.equals(sbmlOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + sbmlOFValue, jsonOFValue == sbmlOFValue);
		
		Assert.assertTrue("Different succinate value: " + jsonSuccValue + " vs " + sbmlSuccValue, jsonSuccValue == sbmlSuccValue);
	}
	
	@Test
	public void simulateEcoliCoreFBA_GK() throws Exception{
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(null, genesJSONKO, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		double jsonSuccValue = jsonSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + jsonSuccValue);
		
		
		SimulationSteadyStateControlCenter ccSBML = new SimulationSteadyStateControlCenter(null, genesSBMLKO, modelSBML, SimulationProperties.FBA);
		ccSBML.setMethodType(SimulationProperties.FBA);
		ccSBML.setMaximization(true);
		ccSBML.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult sbmlSim = ccSBML.simulate();

		String sbmlOF = sbmlSim.getOFString();
		
		double sbmlOFValue = sbmlSim.getOFvalue();
		
		double sbmlSuccValue = sbmlSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + sbmlSuccValue);
		
		
		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + sbmlOF, jsonOF.equals(sbmlOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + sbmlOFValue, jsonOFValue == sbmlOFValue);
		
		Assert.assertTrue("Different succinate value: " + jsonSuccValue + " vs " + sbmlSuccValue, jsonSuccValue == sbmlSuccValue);
	}
	
	@Test
	public void simulateEcoliCoreFBA_GOU() throws Exception{
		
		SimulationSteadyStateControlCenter ccJSON = new SimulationSteadyStateControlCenter(null, genesJSONOU, modelJSON, SimulationProperties.FBA);
		ccJSON.setMethodType(SimulationProperties.FBA);
		ccJSON.setMaximization(true);
		ccJSON.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult jsonSim = ccJSON.simulate();
		
		String jsonOF = jsonSim.getOFString();
		
		double jsonOFValue = jsonSim.getOFvalue();
		
		double jsonSuccValue = jsonSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + jsonSuccValue);
		
		
		SimulationSteadyStateControlCenter ccSBML = new SimulationSteadyStateControlCenter(null, genesSBMLOU, modelSBML, SimulationProperties.FBA);
		ccSBML.setMethodType(SimulationProperties.FBA);
		ccSBML.setMaximization(true);
		ccSBML.setSolver(SolverType.CPLEX3);
		
		SteadyStateSimulationResult sbmlSim = ccSBML.simulate();

		String sbmlOF = sbmlSim.getOFString();
		
		double sbmlOFValue = sbmlSim.getOFvalue();
		
		double sbmlSuccValue = sbmlSim.getFluxValues().get("R_EX_succ_e");
		
		System.out.println(jsonOF + "\t" + jsonOFValue + "\tR_EX_succ_e: " + sbmlSuccValue);
		

		Assert.assertTrue("Different objective function: " + jsonOF + " vs " + sbmlOF, jsonOF.equals(sbmlOF));
		
		Assert.assertTrue("Different objective function value: " + jsonOFValue + " vs " + sbmlOFValue, jsonOFValue == sbmlOFValue);
		
		Assert.assertTrue("Different succinate value: " + jsonSuccValue + " vs " + sbmlSuccValue, jsonSuccValue == sbmlSuccValue);
	}
	
	@Test
	public void compareModels(){
		
		int jsonRSize = modelJSON.getReactions().size();
		int sbmlRSize = modelSBML.getReactions().size();
		
		int jsonMSize = modelJSON.getMetabolites().size();
		int sbmlMSize = modelSBML.getMetabolites().size();
		
		Set<String> sbmlReacts = new HashSet<String>(modelSBML.getReactions().keySet());
		Set<String> jsonReacts = new HashSet<String>(modelJSON.getReactions().keySet());
		
		for (String s : modelJSON.getReactions().keySet()) {
			Reaction reaction = modelSBML.getReaction("R_"+s);
			if(reaction != null){
				System.out.println("JSON: " + s + " vs SBML: " + reaction.getId());
				sbmlReacts.remove(reaction.getId());
				jsonReacts.remove(s);
			}else{
				System.err.println("UNKNOWN: " + s);
			}
			
		}
		
		
		System.out.println(jsonRSize + " vs " + sbmlRSize);
		System.out.println(jsonMSize + " vs " + sbmlMSize);
		
		
		System.out.println("JSON: " + jsonReacts);
		System.out.println("SBML: " + sbmlReacts);
		
		Assert.assertTrue("Different reaction ids", (jsonReacts.size() == sbmlReacts.size()) && (jsonReacts.size() == 0));
		
		for (String s : modelJSON.getReactions().keySet()) {
			Reaction reactionSBML = modelSBML.getReaction("R_"+s);
			Reaction reactionJSON = modelJSON.getReaction(s);
			
			ReactionConstraint sbmlConstraints = reactionSBML.getConstraints();
			ReactionConstraint jsonConstraints = reactionJSON.getConstraints();
			
//			System.out.println(reactionSBML.getConstraints() + " vs "+ reactionJSON.getConstraints() + "\t"+ reactionSBML.getConstraints().equals(reactionJSON.getConstraints()));
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
			Map<String, StoichiometryValueCI> sbmlReactants = contSBML.getReaction("R_"+s).getReactants();
			
			
			if(jsonReactants.size() != sbmlReactants.size()){
				System.err.println("DIFF REACTANTS: " + s + " JSON: " + jsonReactants.size() + " SBML: " + sbmlReactants.size());
				MapUtils.prettyPrint(jsonReactants);
				MapUtils.prettyPrint(sbmlReactants);
			}
			
			for (String react : jsonReactants.keySet()) {
				StoichiometryValueCI jsonStoich = jsonReactants.get(react);
				StoichiometryValueCI sbmlStoich = sbmlReactants.get("M_"+react);
//				System.out.println("REACTANT " + react + ": " + jsonStoich + " vs " + sbmlStoich);
				if(!jsonStoich.getStoichiometryValue().equals(sbmlStoich.getStoichiometryValue())){
					System.err.println("DIFF REACTANT " + s +"\t" + react + "\t" + jsonStoich.getStoichiometryValue() + " vs " + sbmlStoich.getStoichiometryValue());
				}
				
			}
			
			Map<String, StoichiometryValueCI> jsonProducts = contJSON.getReaction(s).getProducts();
			Map<String, StoichiometryValueCI> sbmlProducts = contSBML.getReaction("R_"+s).getProducts();
			
			if(jsonProducts.size() != sbmlProducts.size()){
				System.err.println("DIFF PRODUCTS: " + s + " JSON: " + jsonProducts.size() + " SBML: " + sbmlProducts.size());
				MapUtils.prettyPrint(jsonProducts);
				MapUtils.prettyPrint(sbmlProducts);
			}
			
			for (String prod : jsonProducts.keySet()) {
				StoichiometryValueCI jsonStoich = jsonProducts.get(prod);
				StoichiometryValueCI sbmlStoich = sbmlProducts.get("M_"+prod);
//				System.out.println("PRODUCT " + prod + ": " + jsonStoich + " vs " + sbmlStoich);
				if(!jsonStoich.getStoichiometryValue().equals(sbmlStoich.getStoichiometryValue())){
					System.err.println("DIFF PRODUCT " + s +"\t" + prod + "\t" + jsonStoich.getStoichiometryValue() + " vs " + sbmlStoich.getStoichiometryValue());
				}
			}
		}
		
		
//		System.out.println("JSON Columns: " + modelJSON.getStoichiometricMatrix().columns() + " Rows: " +modelJSON.getStoichiometricMatrix().rows());
//		
//		for (int i = 0; i < modelJSON.getStoichiometricMatrix().columns(); i++) {
//			Double json = modelJSON.getStoichiometricValue(0, i);
//			Double sbml = modelSBML.getStoichiometricValue(0, i);
//			
//			if(!json.equals(sbml)){
//				System.err.println("DIFF: " + i + "\t" + json + " vs " + sbml + "\t" +modelJSON.getReaction(i).getId() + "\t" + modelJSON.getMetaboliteId(0));
//			}
//		}
//		
//		
//		
//		System.out.println("SBML Columns: " + modelSBML.getStoichiometricMatrix().columns() + " Rows: " +modelJSON.getStoichiometricMatrix().rows());
		
	}

}
