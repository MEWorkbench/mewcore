package pt.uminho.ceb.biosystems.mew.mewcore.matlab.cobra;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.CobraMethods;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraGeometricFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraLinearMOMAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraMOMAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class SingleTests {
	
	SteadyStateModel model;
	
	@Before
	public void init() throws Exception{
		//JSBMLReader reader = new JSBMLReader("files/models/Ec_iJR904.xml", "1",false);
		JSBMLReader reader = new JSBMLReader("files/models/ecoli_core_model.xml", "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
//		envCond = new EnvironmentalConditions("NoO2");
//		envCond.addReactionConstraint("R_EX_o2_e", new ReactionConstraint(0, 0));
//		
//		geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList("R_EX_o2_e")));
	}

	@Test
	public void FBA() {
		try{
			SimulationSteadyStateControlCenter.registerMethod(CobraMethods.COBRAFBA, CobraFBAFormulation.class);
			
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, CobraMethods.COBRAFBA);
			
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
			
			SteadyStateSimulationResult resultCobra = cc.simulate();
			
			System.out.println(resultCobra.getOFString() +": "+ resultCobra.getOFvalue());	
			
			
			cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
			
			SteadyStateSimulationResult resultCobra2 = cc.simulate();
			
			System.out.println(resultCobra2.getOFString() +": "+ resultCobra2.getOFvalue());	
			
		}catch(Exception e){
			System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			e.printStackTrace();
		}
	}
	
	@Test
	public void GeometricFBA() {
		try{
			SimulationSteadyStateControlCenter.registerMethod(CobraMethods.COBRAGEOMETRICFBA, CobraGeometricFBAFormulation.class);
			
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, CobraMethods.COBRAGEOMETRICFBA);
			
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
			
			SteadyStateSimulationResult resultCobra = cc.simulate();
			
			System.out.println(resultCobra.getOFString() +": "+ resultCobra.getOFvalue());	
			
			
			cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
			
			SteadyStateSimulationResult resultCobra2 = cc.simulate();
			
			System.out.println(resultCobra2.getOFString() +": "+ resultCobra2.getOFvalue());	
			
		}catch(Exception e){
			System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void LinearMOMA() {
		try{
			SimulationSteadyStateControlCenter.registerMethod(CobraMethods.COBRALMOMA, CobraLinearMOMAFormulation.class);
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList("R_EX_o2_e")));
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, CobraMethods.COBRALMOMA);
			
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
			
			SteadyStateSimulationResult resultCobra = cc.simulate();
			
			System.out.println(resultCobra.getOFString() +": "+ resultCobra.getOFvalue());	
			
			
			cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
			
			SteadyStateSimulationResult resultCobra2 = cc.simulate();
			
			System.out.println(resultCobra2.getOFString() +": "+ resultCobra2.getOFvalue());	
			
		}catch(Exception e){
			System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void MOMA() {
		try{
			SimulationSteadyStateControlCenter.registerMethod(CobraMethods.COBRAMOMA, CobraMOMAFormulation.class);
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList("R_EX_o2_e")));
			
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, CobraMethods.COBRAMOMA);
			
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
			
			SteadyStateSimulationResult resultCobra = cc.simulate();
			
			System.out.println(resultCobra.getOFString() +": "+ resultCobra.getOFvalue());	
			
			
			cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
			
			SteadyStateSimulationResult resultCobra2 = cc.simulate();
			
			System.out.println(resultCobra2.getOFString() +": "+ resultCobra2.getOFvalue());	
			
		}catch(Exception e){
			System.out.println("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			e.printStackTrace();
		}
	}

}
