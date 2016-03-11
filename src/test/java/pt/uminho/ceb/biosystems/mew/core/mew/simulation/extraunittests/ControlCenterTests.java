package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class ControlCenterTests {

	static private SteadyStateModel model;

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	protected static SimulationSteadyStateControlCenter cc;
	
	@BeforeClass
	static public void setData() throws Exception {
		// input Modelo
		//exception.expect(FileNotFoundException.class);
		JSBMLReader reader = new JSBMLReader(ControlCenterTests.class.getClassLoader().getResource("models/ecoli_core_model.xml").getFile(), "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
	}
	
	
	@Test
	public void FBA() throws Exception {

		cc.setMethodType(SimulationProperties.FBA);
		cc.setMaximization(true);
		cc.setSolver(SolverType.CLP);
		
		EnvironmentalConditions envCond = new EnvironmentalConditions();
		envCond.put("R_EX_o2_e", new ReactionConstraint(0, 1000));
		envCond.put("R_EX_glc_e", new ReactionConstraint(-20, 1000));
		cc.setEnvironmentalConditions(envCond);
		
		GeneticConditions geneCond = new GeneticConditions(
				new ReactionChangesList(Arrays.asList("R_FRD7"), Arrays.asList(0.0)), false);
		cc.setGeneticConditions(geneCond);
		
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println("--------------FBA--------------");
		System.out.println("OF Value: "+result.getOFvalue());
		System.out.println("OF Reaction: "+result.getOFString());
		System.out.println("Method: "+result.getMethod());
//		System.out.println("Solver: "+result.getSolver()?????);
		System.out.println("Solver type: "+result.getSolutionType());
		System.out.println("EnvCond:");
		MapUtils.prettyPrint(result.getEnvironmentalConditions());
		System.out.println("GenCond:");
		MapUtils.prettyPrint(result.getGeneticConditions().getGeneList());
		MapUtils.prettyPrint(result.getGeneticConditions().getReactionList());
	
	}
	

	@Test
	public void MOMA() throws Exception {
		cc.setMethodType(SimulationProperties.MOMA);
//		cc.setMaximization(true);
		cc.setSolver(SolverType.CLP);
		
		
		EnvironmentalConditions envCond = new EnvironmentalConditions();
		envCond.put("R_EX_glu_L_e", new ReactionConstraint(-20, 1000));
		envCond.put("R_EX_ac_e", new ReactionConstraint(-50, 1000));
		cc.setEnvironmentalConditions(envCond);
		
		GeneticConditions geneCond = new GeneticConditions(
				new ReactionChangesList(Arrays.asList("R_FUM"), Arrays.asList(0.0)), false);
		cc.setGeneticConditions(geneCond);
		
		
		
//		FBA: 0.8739215069684306
//		MOMA: 1.8369644037221817E-5
//		PFBA: 656.1953145291727
//		ROOM: 0.0
//		LMOMA: 0.0
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println("--------------MOMA--------------");
		System.out.println("OF Value: "+result.getOFvalue());
		System.out.println("OF String: "+result.getOFString());
		System.out.println("Method: "+result.getMethod());
//		System.out.println("Solver: "+result.getSolver()?????);
		System.out.println("Solver type: "+result.getSolutionType());
		System.out.println("EnvCond:");
		MapUtils.prettyPrint(result.getEnvironmentalConditions());
		System.out.println("GenCond:");
		MapUtils.prettyPrint(result.getGeneticConditions().getGeneList());
		MapUtils.prettyPrint(result.getGeneticConditions().getReactionList());
	}
	
	@Test
	public void LMOMA() throws Exception {
		cc.setMethodType(SimulationProperties.LMOMA);
//		cc.setMaximization(true);
		cc.setSolver(SolverType.CLP);
		
		
		EnvironmentalConditions envCond = new EnvironmentalConditions();
		envCond.put("R_EX_glu_L_e", new ReactionConstraint(-20, 1000));
		envCond.put("R_EX_ac_e", new ReactionConstraint(-50, 1000));
		cc.setEnvironmentalConditions(envCond);
		
		GeneticConditions geneCond = new GeneticConditions(
				new ReactionChangesList(Arrays.asList("R_FUM"), Arrays.asList(0.0)), false);
		cc.setGeneticConditions(geneCond);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println("--------------LMOMA--------------");
		System.out.println("OF Value: "+result.getOFvalue());
		System.out.println("OF String: "+result.getOFString());
		System.out.println("Method: "+result.getMethod());
//		System.out.println("Solver: "+result.getSolver()?????);
		System.out.println("Solver type: "+result.getSolutionType());
		System.out.println("EnvCond:");
		MapUtils.prettyPrint(result.getEnvironmentalConditions());
		System.out.println("GenCond:");
		MapUtils.prettyPrint(result.getGeneticConditions().getGeneList());
		MapUtils.prettyPrint(result.getGeneticConditions().getReactionList());
	}
	
	@Test
	public void ROOM() throws Exception {
		cc.setMethodType(SimulationProperties.ROOM);
//		cc.setMaximization(true);
		cc.setSolver(SolverType.CLP);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println("--------------ROOM--------------");
		System.out.println("OF Value: "+result.getOFvalue());
		System.out.println("OF String: "+result.getOFString());
		System.out.println("Method: "+result.getMethod());
//		System.out.println("Solver: "+result.getSolver()?????);
		System.out.println("Solver type: "+result.getSolutionType());
		System.out.println("EnvCond:");
		MapUtils.prettyPrint(result.getEnvironmentalConditions());
//		System.out.println("GenCond:");
//		MapUtils.prettyPrint(result.getGeneticConditions().getGeneList());
//		MapUtils.prettyPrint(result.getGeneticConditions().getReactionList());
	}
	
	@Test
	public void PFBA() throws Exception {
		cc.setMethodType(SimulationProperties.PFBA);
		cc.setMaximization(true);
		cc.setSolver(SolverType.CLP);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println("--------------PFBA--------------");
		System.out.println("OF Value: "+result.getOFvalue());
		System.out.println("OF String: "+result.getOFString());
		System.out.println("Method: "+result.getMethod());
//		System.out.println("Solver: "+result.getSolver()?????);
		System.out.println("Solver type: "+result.getSolutionType());
		System.out.println("EnvCond:");
		MapUtils.prettyPrint(result.getEnvironmentalConditions());
//		System.out.println("GenCond:");
//		MapUtils.prettyPrint(result.getGeneticConditions().getGeneList());
//		MapUtils.prettyPrint(result.getGeneticConditions().getReactionList());
	}
	
//	@Test
//	public void NormLMoma() {
//		fail("Not yet implemented");
//	}

	
	private void simulate(String method) throws Exception {
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, method);
		cc.setMaximization(true);
		cc.setSolver(SolverType.CPLEX);
		cc.simulate();
	}
}
