package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.sscontrolcenter;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import cern.colt.Arrays;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMIMBL {
	
	private static ISteadyStateModel	model;
	private static String	biomassID;
	private static String	targetID;
	private static SimulationSteadyStateControlCenter	cc;
	private static EnvironmentalConditions	envCondAerobiose;
	private static EnvironmentalConditions	envCondAnaerobiose;

	@BeforeClass
	public static void before() throws Exception{
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		ModelConfiguration modelConf = new ModelConfiguration("files/iAF1260_full/iAF1260.conf");
		model = modelConf.getModel();
		biomassID = modelConf.getModelBiomass();
		targetID = "R_EX_succ_e_";
		
		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);					
	
		/** Environmental conditions - Aerobic */
		envCondAerobiose = new EnvironmentalConditions();
		envCondAerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(-20.0, 9999999.0));
		
		/** Environmental conditions - Anaerobic */
		envCondAnaerobiose = new EnvironmentalConditions();
		envCondAnaerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAnaerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(0.0, 9999999.0));
	}
	
	@Test
	public void test_1() throws Exception{
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setEnvironmentalConditions(envCondAerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		System.out.println("Actuals=\t" + Arrays.toString(actuals));
		MapUtils.prettyPrint(cc.getMethodPropertiesMap());				
	}
	
	@Test
	public void test_2() throws Exception{
//		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.MIMBL);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		System.out.println("Actuals=\t" + Arrays.toString(actuals));
		MapUtils.prettyPrint(cc.getMethodPropertiesMap());				
	}
	
}
