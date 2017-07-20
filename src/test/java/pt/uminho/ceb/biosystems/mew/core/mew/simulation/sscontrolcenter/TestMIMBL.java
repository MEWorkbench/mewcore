package pt.uminho.ceb.biosystems.mew.core.mew.simulation.sscontrolcenter;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cern.colt.Arrays;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

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
		
		JSBMLReader reader = new JSBMLReader("./src/test/resources/models/Ec_iAF1260_flux1.xml", "1", false);
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		biomassID = model.getBiomassFlux();
		targetID = "R_EX_succ_e_";
		
		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
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
		cc.setSolver(CPLEX3SolverBuilder.ID);
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
