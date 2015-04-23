package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.extraunittests;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.FBA;

public abstract class FBASimulationKO extends AbstractSimulationTest{
	
	protected SimulationSteadyStateControlCenter cc;
	protected SteadyStateModel model;
	
	//protected EnvironmentalConditions envCond, envKO, envConditions;

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
		setEnvironmentalConditions();

		
	}
	
//	private void setEnvironmentalConditions()
//	{
//		// input overunderReactions
//		envCond = new EnvironmentalConditions();
//		envCond.put("R_EX_o2_e", new ReactionConstraint(-20, 1000));
//		envCond.put("R_EX_glc_e", new ReactionConstraint(-20, 1000));
//						
//
//		// input koreactions
//		envKO = new EnvironmentalConditions();
//		envKO.put("R_ACKr", new ReactionConstraint(0, 0));
//		envKO.put("R_ACt2r", new ReactionConstraint(0, 0));
//		envKO.put("R_H2Ot", new ReactionConstraint(0, 0));
//		
//		envConditions = envCond;
//		envConditions.putAll(envKO);
//		
//	}
	
//	protected void setParameters()
//	{
//		cc.setSolver(SolverType.CLP);
//		cc.setMaximization(true);
//	}
//	
//	@Test
//	public void FBAWithKO() {
//		
//		
//		cc = new SimulationSteadyStateControlCenter(envConditions, null, model, SimulationProperties.FBA);
//		
//		setParameters();
//		
//		try {
//			System.out.println(cc.simulate().getOFvalue());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	@Test
	public void FBAWithKO() throws Exception
	{

		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Desktop/Models/Ec_iAF1260.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		setEnvironmentalConditions();

		System.out.println("\n*******Normal*******");
		FBASimulation();
		
		System.out.println("\n*******Control Center*******");
		CCFBASimulation();
		
		
//		for (String string : result.getFluxValues().keySet()) {
//			System.out.println(string + " " + result.getFluxValues().get(string));
//		}
		
		
		
//		for (String string : model.getReactions().keySet()) {
//			System.out.println(string);
//		}
		
	}
	
	private void FBASimulation() throws Exception{
		method = new FBA(model);
		
		method.setProperty(SimulationProperties.SOLVER, SolverType.CLP);
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		method.setEnvironmentalConditions(envConditions);
		
//		for (String env : method.getEnvironmentalConditions().keySet()) {
//			System.out.println(env + " LowerB: " + 
//					method.getEnvironmentalConditions().get(env).getLowerLimit()+ " UpperB: "+
//							method.getEnvironmentalConditions().get(env).getUpperLimit());
//		}
		
		
		SteadyStateSimulationResult result = method.simulate();
		
		System.out.println();
		System.out.println(result.getOFString() + ": " + result.getOFvalue());
		System.out.println();
		System.out.println("Succinate Production: "+result.getFluxValues().get("R_EX_succ_e_"));
		
		System.out.println("Acetate Production: "+result.getFluxValues().get("R_EX_ac_e_"));
	}
	
	private void CCFBASimulation() throws Exception{


		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envConditions, null, model, "FBA");
		cc.setSolver(SolverType.CLP);
		cc.setMaximization(true);
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println();
		System.out.println(result.getOFString() + ": " + result.getOFvalue());
		System.out.println();
		System.out.println("Succinate Production: "+result.getFluxValues().get("R_EX_succ_e_"));
		
		System.out.println("Acetate Production: "+result.getFluxValues().get("R_EX_ac_e_"));
		
	}
	
	
	protected EnvironmentalConditions envCond, envKO, envConditions;
	private void setEnvironmentalConditions()
	{
		// input overunderReactions
		envCond = new EnvironmentalConditions();
		envCond.put("R_EX_o2_e_", new ReactionConstraint(-20, 999999.0));
		envCond.put("R_EX_glc_e_", new ReactionConstraint(-20, 999999.0));
						

		// input koreactions
		envKO = new EnvironmentalConditions();
//		envKO.put("R_NADH16pp", new ReactionConstraint(0, 0));
//		envKO.put("R_PGM", new ReactionConstraint(0, 0));
		
//		envKO.put("R_FUM", new ReactionConstraint(0, 0));
//		envKO.put("R_LDH_D", new ReactionConstraint(0, 0));
//		
//		envKO.put("R_EDA", new ReactionConstraint(0, 0));
		
				

		envConditions = envCond;
		envConditions.putAll(envKO);
		
		
	}

	@Override
	protected String getMethodString() {
		// TODO Auto-generated method stub
		return SimulationProperties.FBA;
	}

	@Override
	protected Map<String, Double> getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isMaximization() {
		// TODO Auto-generated method stub
		return true;
	}

}
