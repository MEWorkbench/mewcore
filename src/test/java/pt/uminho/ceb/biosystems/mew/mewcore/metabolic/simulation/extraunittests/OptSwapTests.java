package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.extraunittests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class OptSwapTests {
	
	protected SimulationSteadyStateControlCenter cc;
	protected SteadyStateModel model;
	protected Set<String> allSwaps;

	@Before
	public void setData() throws Exception {
		// input Modelo
		//exception.expect(FileNotFoundException.class);
		JSBMLReader reader = new JSBMLReader("./files/models/iJO1366_swaps.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.PARSIMONIUS);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux("R_Ec_biomass_iJO1366_core_53p95M", 1.0);
		
		// Define Genetic Conditions
		setGeneticConditions();
		
		// Define Environmental Conditions
		setEnvironmentalConditions();

		
	}
	
	private void setEnvironmentalConditions() {
		EnvironmentalConditions envCond = new EnvironmentalConditions();
		envCond.put("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(-20, 1000));
		envCond.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-20, 1000));
		cc.setEnvironmentalConditions(envCond);

	}

	private void setGeneticConditions() {
		Set<String> kos = new HashSet<String>();
		
		allSwaps = new HashSet<String>();
		
		try{
			FileInputStream fis = new FileInputStream("./files/extraFiles/swapList");
		 
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			while ((line = br.readLine()) != null)
				allSwaps.add(line);
		 
			br.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		try{
			FileInputStream fis = new FileInputStream("./files/extraFiles/KOList");
		 
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			while ((line = br.readLine()) != null) {
				kos.add(line.replace("=0.0", "").replace(",", ""));
//				reactions = line.replaceAll("\\s+","").split(",");
//				EnvironmentalConditions eC = new EnvironmentalConditions();
//				for(int i = 0; i < reactions.length; i++)
//				{
//					eC.put("R_"+reactions[i], new ReactionConstraint(0.0,0.0));
//				}
//				
//				listKOs.add(eC);
			}
		 
			br.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		for (String swap : allSwaps)			
			if(!kos.contains(swap))
				kos.add(swap.replace("_swap", ""));
		
		
		ReactionChangesList reactionList = new ReactionChangesList(kos);

		GeneticConditions geneCond = new GeneticConditions(reactionList);
		
		try {
			cc.setGeneticConditions(geneCond);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void runPFBA() {
		
		try {
			SteadyStateSimulationResult result = cc.simulate();
			
			System.out.println("BIOMASS: " +result.getFluxValues().get("R_Ec_biomass_iJO1366_core_53p95M"));
			System.out.println("SUCCINATE: " +result.getFluxValues().get("R_EX_succ_LPAREN_e_RPAREN_"));
			
			
			EnvironmentalConditions newEnvCond = new EnvironmentalConditions();
			double biomassValue = result.getFluxValues().get("R_Ec_biomass_iJO1366_core_53p95M");
			double succValue = result.getFluxValues().get("R_EX_succ_LPAREN_e_RPAREN_");
			newEnvCond.addReactionConstraint("R_Ec_biomass_iJO1366_core_53p95M", new ReactionConstraint(biomassValue-biomassValue*0.95, 1000));
			//newEnvCond.addReactionConstraint("R_Ec_biomass_iJO1366_core_53p95M", new ReactionConstraint(0.93*0.9999999, 1000));
			//newEnvCond.addReactionConstraint("R_Ec_biomass_iJO1366_core_53p95M", new ReactionConstraint(succValue/2, 1000));
			newEnvCond.put("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(-20, 1000));
			newEnvCond.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-20, 1000));
			cc.setEnvironmentalConditions(newEnvCond);
			
			System.out.println("---------------------- // ---------------------------");
			cc.setFBAObjSingleFlux("R_EX_succ_LPAREN_e_RPAREN_", 1.0);
			cc.setMaximization(true);
			cc.setMethodType(SimulationProperties.FBA);
			result = cc.simulate();
			System.out.println("BIOMASS: " +result.getFluxValues().get("R_Ec_biomass_iJO1366_core_53p95M"));
			System.out.println("SUCCINATE: " +result.getFluxValues().get("R_EX_succ_LPAREN_e_RPAREN_"));
			
			System.out.println("---------------------- // ---------------------------");
			cc.setFBAObjSingleFlux("R_EX_succ_LPAREN_e_RPAREN_", 1.0);
			cc.setMaximization(false);
			cc.setMethodType(SimulationProperties.FBA);
			result = cc.simulate();
			System.out.println("BIOMASS: " +result.getFluxValues().get("R_Ec_biomass_iJO1366_core_53p95M"));
			System.out.println("SUCCINATE: " +result.getFluxValues().get("R_EX_succ_LPAREN_e_RPAREN_"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
