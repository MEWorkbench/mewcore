package pt.uminho.ceb.biosystems.mew.mewcore.matlab.cobra;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraGeometricFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraLinearMOMAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraMOMAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.simulation.RavenFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.simulation.TigerFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class SimulationTests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	
	SteadyStateModel model;
	EnvironmentalConditions envCond;
	GeneticConditions geneCond;
	
	@Before
	public void init() throws Exception{
		//JSBMLReader reader = new JSBMLReader("files/models/Ec_iJR904.xml", "1",false);
		JSBMLReader reader = new JSBMLReader(getFile("models/ecoli_core_model.xml"), "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
		envCond = new EnvironmentalConditions("NoO2");
		envCond.addReactionConstraint("R_EX_o2_e", new ReactionConstraint(0, 0));
		
		geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList("R_EX_o2_e")));
	}

	//@Test
	public void CobraFBA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
		
		
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_FBA");
		
		
		cc.setMaximization(true);
		//cc.addProperty("KOList", new HashSet<String>(Arrays.asList("ola", "nova")));
		
		SteadyStateSimulationResult result = cc.simulate();
		
		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("---------- COBRA FBA ----------");
		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}
	
	//@Test
	public void CobraMOMA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_MOMA", CobraMOMAFormulation.class);
		
		
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_MOMA");
		
		cc.setMaximization(true);
		
//		SteadyStateSimulationResult result = cc.simulate();
//		
//		double d = result.getFluxValues().get(model.getBiomassFlux());
//		System.out.println("---------- COBRA MOMA ----------");
//		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}
	
	//@Test
	public void CobraLinearMOMA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_LMOMA", CobraLinearMOMAFormulation.class);
		
		
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_LMOMA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("---------- COBRA LMOMA ----------");
		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}
	
	//@Test
	public void CobraGeometricFBA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_GEOFBA", CobraGeometricFBAFormulation.class);
		
		
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_GEOFBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("---------- COBRA GEOMETRIC FBA----------");
		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}
	
	
	
	
	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///                                           RAVEN                                     ///
	///////////////////////////////////////////////////////////////////////////////////////////
	//@Test
	public void RavenFBA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_RAVEN_FBA", RavenFBAFormulation.class);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_RAVEN_FBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("---------- RAVEN FBA ----------");
		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///                                           TIGER                                     ///
	///////////////////////////////////////////////////////////////////////////////////////////
	//@Test
	public void TigerFBA() throws Exception {
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_TIGER_FBA", TigerFBAFormulation.class);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_TIGER_FBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("---------- TIGER FBA ----------");
		System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
	}

}
