package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.extraunittests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.PARSIMONIOUS;

public class PARSIMONIOUSSimulationKO {
	
	protected SimulationSteadyStateControlCenter cc;
	protected SteadyStateModel model;

	@Before
	public void setData() throws Exception {
		// input Modelo
		//exception.expect(FileNotFoundException.class);
		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Desktop/Models/Ec_iAF1260.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		// Define Genetic Conditions
		//setGeneticConditions();
		
		// Define Environmental Conditions
		//setEnvironmentalConditions();

		
	}
	
	
	private ArrayList<EnvironmentalConditions> getListKOs(){
		
		ArrayList<EnvironmentalConditions> listKOs = new ArrayList<EnvironmentalConditions>();
		
		EnvironmentalConditions envKo1 = new EnvironmentalConditions();
		envKo1.put("R_2MAHMP", new ReactionConstraint(0,0));
		envKo1.put("R_ACKr", new ReactionConstraint(0,0));
		envKo1.put("R_ATPS4rpp", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo2 = new EnvironmentalConditions();
		envKo2.put("R_IPDDI", new ReactionConstraint(0,0));
		envKo2.put("R_METSOXR1", new ReactionConstraint(0,0));
		envKo2.put("R_METSOXR2", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo3 = new EnvironmentalConditions();
		envKo3.put("R_2MAHMP", new ReactionConstraint(0,0));
		envKo3.put("R_ABTA", new ReactionConstraint(0,0));
		envKo3.put("R_IPDDI", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo4 = new EnvironmentalConditions();
		envKo4.put("R_FDH4pp", new ReactionConstraint(0,0));
		envKo4.put("R_IDOND", new ReactionConstraint(0,0));
		envKo4.put("R_IPDDI", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo5 = new EnvironmentalConditions();
		envKo5.put("R_2MAHMP", new ReactionConstraint(0,0));
		envKo5.put("R_DRPA", new ReactionConstraint(0,0));
		envKo5.put("R_IPDDI", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo6 = new EnvironmentalConditions();
		envKo6.put("R_CYTBO3_4pp", new ReactionConstraint(0,0));
		envKo6.put("R_DRPA", new ReactionConstraint(0,0));
		envKo6.put("R_IPDDI", new ReactionConstraint(0,0));
		
		EnvironmentalConditions envKo7 = new EnvironmentalConditions();
		envKo7.put("R_ALCD2x", new ReactionConstraint(0,0));
		envKo7.put("R_DRPA", new ReactionConstraint(0,0));
		envKo7.put("R_IPDDI", new ReactionConstraint(0,0));
		
		

		EnvironmentalConditions envKo8 = new EnvironmentalConditions();
		envKo8.put("R_EDA", new ReactionConstraint(0,0));
		envKo8.put("R_IPDDI", new ReactionConstraint(0,0));
		envKo8.put("R_VALTA", new ReactionConstraint(0,0));
		

		EnvironmentalConditions envKo9 = new EnvironmentalConditions();
		envKo9.put("R_ACKr", new ReactionConstraint(0,0));
		envKo9.put("R_CYTBO3_4pp", new ReactionConstraint(0,0));
		envKo9.put("R_FUM", new ReactionConstraint(0,0));
		

		EnvironmentalConditions envKo10 = new EnvironmentalConditions();
		envKo10.put("R_DRPA", new ReactionConstraint(0,0));
		envKo10.put("R_EDA", new ReactionConstraint(0,0));
		envKo10.put("R_FUM", new ReactionConstraint(0,0));
		
		
		EnvironmentalConditions envKo11 = new EnvironmentalConditions();
		envKo11.put("R_EDA", new ReactionConstraint(0,0));
		envKo11.put("R_FUM", new ReactionConstraint(0,0));
		envKo11.put("R_LDH_D", new ReactionConstraint(0,0));
		
	
		EnvironmentalConditions envKo12 = new EnvironmentalConditions();
		envKo12.put("R_ALDD2y", new ReactionConstraint(0,0));
		envKo12.put("R_FUM", new ReactionConstraint(0,0));
		envKo12.put("R_LDH_D", new ReactionConstraint(0,0));
		

		EnvironmentalConditions envKo13 = new EnvironmentalConditions();
		envKo13.put("R_FRD3", new ReactionConstraint(0,0));
		envKo13.put("R_FUM", new ReactionConstraint(0,0));
		envKo13.put("R_LDH_D", new ReactionConstraint(0,0));
		

		EnvironmentalConditions envKo14 = new EnvironmentalConditions();
		envKo14.put("R_ALCD19", new ReactionConstraint(0,0));
		envKo14.put("R_FUM", new ReactionConstraint(0,0));
		envKo14.put("R_LDH_D", new ReactionConstraint(0,0));
		

		EnvironmentalConditions envKo15 = new EnvironmentalConditions();
		envKo15.put("R_ALCD2x", new ReactionConstraint(0,0));
		envKo15.put("R_LDH_D", new ReactionConstraint(0,0));
		envKo15.put("R_PFL", new ReactionConstraint(0,0));
				
		
		

		
		
//		listKOs.add(envKo1);
//		listKOs.add(envKo2);
//		listKOs.add(envKo3);
//		listKOs.add(envKo4);
//		listKOs.add(envKo5);
//		listKOs.add(envKo6);
//		listKOs.add(envKo7);
		
//		listKOs.add(envKo8);
//		listKOs.add(envKo9);
//		listKOs.add(envKo10);
//		listKOs.add(envKo11);
//		listKOs.add(envKo12);
//		listKOs.add(envKo13);
//		listKOs.add(envKo14);
//		listKOs.add(envKo15);
		
		
		
		return listKOs;
	}
	
	private ArrayList<EnvironmentalConditions> getListKOs(String file)
	{
		ArrayList<EnvironmentalConditions> listKOs = new ArrayList<EnvironmentalConditions>();
		
		try{
			FileInputStream fis = new FileInputStream(file);
		 
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String[] reactions;
			String line = null;
			while ((line = br.readLine()) != null) {
				reactions = line.replaceAll("\\s+","").split(",");
				EnvironmentalConditions eC = new EnvironmentalConditions();
				for(int i = 0; i < reactions.length; i++)
				{
					eC.put("R_"+reactions[i], new ReactionConstraint(0.0,0.0));
				}
				
				listKOs.add(eC);
			}
		 
			br.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return listKOs;
	}
	
	@Test
	public void PFBAWithKO() throws Exception
	{

		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Desktop/Models/Ec_iAF1260.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		ArrayList<EnvironmentalConditions> listKos = getListKOs("/home/hgiesteira/Desktop/Outros/ReactionsList");
		
		
//		ArrayList<EnvironmentalConditions> listKos = getListKOs();
		envKO = new EnvironmentalConditions();
		for (EnvironmentalConditions eC : listKos) {
			
			envKO = eC;  
			
			System.out.println("\n**"+CollectionUtils.join(envKO.keySet(), " | ")+"**");
			
			
			setEnvironmentalConditions();
			
			for (String rec : eC.keySet()) {
				if(model.getReaction(rec) == null)
					System.out.println(rec + " NOT IN MODEL!");
			}
	
			//System.out.println("\n*Normal*");
			PFBASimulation();
			
			//System.out.println("\n*Control Center*");
			CCPFBASimulation();
			
//			for (String str : envConditions.keySet()) {
//				System.out.println(str);
//			}
		
			//System.out.println("------------------//--------------------------");
			
			CCPFBASimulationMax();
			
			System.out.println(valorBiomass + "\t" + valorSucc + "\t" + valorMinSucc+ "\t" + valorMaxSucc);
		}
		
		
//		for (String string : result.getFluxValues().keySet()) {
//			System.out.println(string + " " + result.getFluxValues().get(string));
//		}
		
		
		
//		for (String string : model.getReactions().keySet()) {
//			System.out.println(string);
//		}
		
	}
	
	private void PFBASimulation() throws Exception{
		PARSIMONIOUS method = new PARSIMONIOUS(model);
		
		
		
		method.setProperty(SimulationProperties.SOLVER, SolverType.CLP);
		//method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, "R_EX_succ_e_");
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, "R_EX_succ_e_");
		method.setEnvironmentalConditions(envConditions);
		
//		for (String env : method.getEnvironmentalConditions().keySet()) {
//			System.out.println(env + " LowerB: " + 
//					method.getEnvironmentalConditions().get(env).getLowerLimit()+ " UpperB: "+
//							method.getEnvironmentalConditions().get(env).getUpperLimit());
//		}
		
		
		
		SteadyStateSimulationResult result = method.simulate();
		
		//System.out.println();
		//System.out.println(result.getOFString() + ": " + result.getOFvalue());
		//System.out.println();
		//System.out.println("Succinate Production: "+result.getFluxValues().get("R_EX_succ_e_"));
		
		
		valorSucc = result.getFluxValues().get("R_EX_succ_e_");
		//System.out.println("Biomass: "+result.getFluxValues().get("R_Ec_biomass_iAF1260_core_59p81M"));
		valorBiomass = result.getFluxValues().get("R_Ec_biomass_iAF1260_core_59p81M");
	}
	
	double valorSucc = 0.0;
	double valorMaxSucc = 0.0;
	double valorMinSucc = 0.0;
	double valorBiomass = 0.0;
	private void CCPFBASimulation() throws Exception{

//		envConditions.put("R_Ec_biomass_iAF1260_core_59p81M", new ReactionConstraint(valorBiomass, valorBiomass));
//		SimulationSteadyStateControlCenter ccBase = new SimulationSteadyStateControlCenter(envConditions, null, model, SimulationProperties.FBA);
//		ccBase.setFBAObjSingleFlux("R_EX_succ_e_", 1.0);
//		ccBase.setSolver(SolverType.CLP);
//		ccBase.setMaximization(true);
//		SteadyStateSimulationResult resultBase = ccBase.simulate();
//		
//		System.out.println();
////		System.out.println(result.getOFString() + ": " + result.getOFvalue());
////		System.out.println();
//		System.out.println("Succinate Production Max: "+resultBase.getFluxValues().get("R_EX_succ_e_"));
//		
		
		envConditions.put("R_Ec_biomass_iAF1260_core_59p81M", new ReactionConstraint(valorBiomass, valorBiomass));
		List<String> listReaction = new ArrayList<String>();
		listReaction.add("R_2MAHMP");
		listReaction.add("R_G6PDH2r");
		listReaction.add("R_GLCDpp");
		listReaction.add("R_PGM");
		listReaction.add("R_RPI");
		
		GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(listReaction));
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envConditions, null, model, SimulationProperties.FBA);
		cc.setSolver(SolverType.CLP);
		cc.setMaximization(false);
		cc.setFBAObjSingleFlux("R_EX_succ_e_", 1.0);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		
		valorMinSucc = result.getFluxValues().get("R_EX_succ_e_");
		//System.out.println();
//		System.out.println(result.getOFString() + ": " + result.getOFvalue());
//		System.out.println();
		//System.out.println("Succinate Production: "+result.getFluxValues().get("R_EX_succ_e_"));
		
		//System.out.println("Biomass: "+result.getFluxValues().get("R_Ec_biomass_iAF1260_core_59p81M"));
		
	}
	
	private void CCPFBASimulationMax() throws Exception{

		envConditions.put("R_Ec_biomass_iAF1260_core_59p81M", new ReactionConstraint(valorBiomass, valorBiomass));
		SimulationSteadyStateControlCenter ccBase = new SimulationSteadyStateControlCenter(envConditions, null, model, SimulationProperties.FBA);
		ccBase.setFBAObjSingleFlux("R_EX_succ_e_", 1.0);
		ccBase.setSolver(SolverType.CLP);
		ccBase.setMaximization(true);
		SteadyStateSimulationResult resultBase = ccBase.simulate();
		
		//System.out.println();
//		System.out.println(result.getOFString() + ": " + result.getOFvalue());
//		System.out.println();
		//System.out.println("Succinate Production Max: "+resultBase.getFluxValues().get("R_EX_succ_e_"));
		
		valorMaxSucc = resultBase.getFluxValues().get("R_EX_succ_e_");
		
	}
	
	
	protected EnvironmentalConditions envCond, envKO, envConditions;
	private void setEnvironmentalConditions()
	{
		// input overunderReactions
		envCond = new EnvironmentalConditions();
		envCond.put("R_EX_o2_e_", new ReactionConstraint(-20, 999999.0));
		envCond.put("R_EX_glc_e_", new ReactionConstraint(-20, 999999.0));
		//envCond.put("R_Ec_biomass_iAF1260_core_59p81M", new ReactionConstraint(1.2758825, 1.2758825));

		
		// input koreactions
//		envKO = new EnvironmentalConditions();
//
//		
////		envKO.put("R_2MAHMP", new ReactionConstraint(0, 0));
////		envKO.put("R_ACKr", new ReactionConstraint(0, 0));
////		envKO.put("R_ATPS4rpp", new ReactionConstraint(0, 0));
//		envKO.put("R_IPDDI", new ReactionConstraint(0, 0));
//		envKO.put("R_METSOXR1", new ReactionConstraint(0, 0));
//		envKO.put("R_METSOXR2", new ReactionConstraint(0, 0));
		
				

		envConditions = envCond;
		envConditions.putAll(envKO);
		
		
	}
	
	
//	@Test
	public void getReactionAndBounds() throws Exception
	{
//		for (String name : model.getReactions().keySet()) {
//			Reaction reac = model.getReaction(name);
// 			System.out.println(name + " " + reac.getConstraints().getLowerLimit() + " " + reac.getConstraints().getUpperLimit());
//		
//			
//		}
		
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(SolverType.CPLEX);
		cc.setMaximization(true);
		//cc.setFBAObjSingleFlux("R_EX_succ_e_", 1.0);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		//MapUtils.prettyPrint(result.getFluxValues());
		
		List sortedKeys=new ArrayList(result.getFluxValues().keySet());
		Collections.sort(sortedKeys);
		
		for (int i = 0; i < sortedKeys.size(); i++) {
			Reaction rec = model.getReaction(sortedKeys.get(i).toString());
			System.out.println(sortedKeys.get(i) + " " + result.getFluxValues().get(sortedKeys.get(i).toString()));
		}
	}

}
