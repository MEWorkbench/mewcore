package pt.uminho.ceb.biosystems.mew.core.mew.simulation.sscontrolcenter;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.Assume;
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
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CLPSolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.builders.GLPKBinSolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SSSimulationControlCenterTest {
	
	public static final boolean	_debug				= true;
	public static final double	FORCE_ROOM_DELTA	= 0.0;
	public static final double	FORCE_ROOM_EPSILON	= 0.0;
	public static final double	CONST_ZERO			= 1e-8;
	public static final double	DELTA_GLPK_A		= 1e-4;
													
	public static boolean isZero(double test) {
		return Math.abs(test) < CONST_ZERO;
	}
	
	public static final MultiKeyMap<String, Map<String, double[]>>	_results						= new MultiKeyMap<String, Map<String, double[]>>();
																									
	public static final Map<String, String>						_solvers;
																	
	public static final Map<String, String>							_methods;
																	
	/**
	 * Analysis constants
	 */
	public static final String										WT_AEROBIC						= "WT_AEROBIC";
	public static final String										WT_ANAEROBIC					= "WT_ANAEROBIC";
	public static final String										RK_AEROBIC						= "RK_AEROBIC";
	public static final String										GK_AEROBIC						= "GK_AEROBIC";
	public static final String										ROU_AEROBIC						= "ROU_AEROBIC";
	public static final String										GOU_AEROBIC						= "GOU_AEROBIC";
	public static final String										FVA_TARGET_AEROBIC				= "FVA_TARGET_AEROBIC";
	public static final String										FVA_TARGET_AEROBIC_REFERENCE	= "FVA_TARGET_AEROBIC_REFERENCE";
																									
	/**
	 * Instance variables
	 */
	public static EnvironmentalConditions							envCondAerobiose, envCondAnaerobiose;
	public static GeneticConditions									genCondRK, genCondGK, genCondROU, genCondGOU;
	public static SimulationSteadyStateControlCenter				cc;
	public static ISteadyStateModel									model;
	public static String											biomassID;
	public static String											targetID;
																	
	@BeforeClass
	public static void populate() throws Exception {
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
//		ModelConfiguration modelConf = new ModelConfiguration(SSSimulationControlCenterTest.class.getClassLoader().getResource("simulation/iAF1260.conf").getFile());
		JSBMLReader reader = new JSBMLReader("./src/test/resources/models/Ec_iAF1260_flux1.xml", "1", false);
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		biomassID = model.getBiomassFlux();
		targetID = "R_EX_succ_e_";
		
		cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		//		cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
		
		List<String> reactions = new ArrayList<String>();
		reactions.add(0, "R_FUM");
		reactions.add(1, "R_SUCFUMtpp");
		
		List<String> genes = new ArrayList<String>();
		genes.add(0, "b1611");
		genes.add(1, "b1612");
		genes.add(2, "b4122");
		genes.add(3, "b4123");
		genes.add(4, "b0621");
		genes.add(5, "b4138");
		
		List<Double> reactionsExpressionsROU = new ArrayList<Double>();
		reactionsExpressionsROU.add(0, 0.1);
		reactionsExpressionsROU.add(1, 0.1);
		
		List<Double> genesExpressionsGK = new ArrayList<Double>();
		genesExpressionsGK.add(0, 0.0);
		genesExpressionsGK.add(1, 0.0);
		genesExpressionsGK.add(2, 0.0);
		genesExpressionsGK.add(3, 0.0);
		genesExpressionsGK.add(4, 0.0);
		genesExpressionsGK.add(5, 0.0);
		List<Double> genesExpressionsGOU = new ArrayList<Double>();
		genesExpressionsGOU.add(0, 0.1);
		genesExpressionsGOU.add(1, 0.1);
		genesExpressionsGOU.add(2, 0.1);
		genesExpressionsGOU.add(3, 0.1);
		genesExpressionsGOU.add(4, 0.1);
		genesExpressionsGOU.add(5, 0.1);
		
		/** Reaction knockouts */
		genCondRK = new GeneticConditions(new ReactionChangesList(reactions));
		
		/** Gene knockouts */
		genCondGK = new GeneticConditions(new GeneChangesList(genes, genesExpressionsGK), (ISteadyStateGeneReactionModel) model, false);
		genCondGK.updateReactionsList((ISteadyStateGeneReactionModel) model);
		
		/** Reaction over/under expression */
		genCondROU = new GeneticConditions(new ReactionChangesList(reactions, reactionsExpressionsROU));
		
		/** Gene over/under expression */
		genCondGOU = new GeneticConditions(new GeneChangesList(genes, genesExpressionsGOU), (ISteadyStateGeneReactionModel) model, true);
		genCondGOU.updateReactionsList((ISteadyStateGeneReactionModel) model);
		
		/** Environmental conditions - Aerobic */
		envCondAerobiose = new EnvironmentalConditions();
		envCondAerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(-20.0, 9999999.0));
		
		/** Environmental conditions - Anaerobic */
		envCondAnaerobiose = new EnvironmentalConditions();
		envCondAnaerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAnaerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(0.0, 9999999.0));
	}
	
	/*************************************************
	 * GLPK SOLVER *
	 *************************************************/
	
	/**
	 * FBA
	 */
	
	@Test
	public void test_1_1_1_GLPK_FBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_1_GLPK_FBA_WT_AEROBIC ]==================================\n");
			
		cc.setSolver(GLPKBinSolverBuilder.ID);
		cc.setEnvironmentalConditions(envCondAerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_2_GLPK_FBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_2_GLPK_FBA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_3_GLPK_FBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_3_GLPK_FBA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_4_GLPK_FBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_4_GLPK_FBA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_5_GLPK_FBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_5_GLPK_FBA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_6_GLPK_FBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_6_GLPK_FBA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_1_7_GLPK_FBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_1_7_GLPK_FBA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "FBA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * PFBA
	 */
	
	@Test
	public void test_1_2_1_GLPK_PFBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_1_GLPK_PFBA_WT_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.PFBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_2_GLPK_PFBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_2_GLPK_PFBA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_3_GLPK_PFBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_3_GLPK_PFBA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_4_GLPK_PFBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_4_GLPK_PFBA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_5_GLPK_PFBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_5_GLPK_PFBA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_6_GLPK_PFBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_6_GLPK_PFBA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_2_7_GLPK_PFBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_2_7_GLPK_PFBA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "pFBA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/** LMOMA */
	
	@Test
	public void test_1_3_1_GLPK_LMOMA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_1_GLPK_LMOMA_WT_AEROBIC ]==================================\n");
			
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_2_GLPK_LMOMA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_2_GLPK_LMOMA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_3_GLPK_LMOMA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_3_GLPK_LMOMA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_4_GLPK_LMOMA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_4_GLPK_LMOMA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_5_GLPK_LMOMA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_5_GLPK_LMOMA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_6_GLPK_LMOMA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_6_GLPK_LMOMA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_7_GLPK_LMOMA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_7_GLPK_LMOMA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_3_8_GLPK_LMOMA_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_3_8_GLPK_LMOMA_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "LMOMA").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * ROOM
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_1_4_1_GLPK_ROOM_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_1_GLPK_ROOM_WT_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.ROOM);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_2_GLPK_ROOM_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_2_GLPK_ROOM_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_3_GLPK_ROOM_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_3_GLPK_ROOM_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_4_GLPK_ROOM_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_4_GLPK_ROOM_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_5_GLPK_ROOM_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_5_GLPK_ROOM_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_6_GLPK_ROOM_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_6_GLPK_ROOM_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_7_GLPK_ROOM_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_7_GLPK_ROOM_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_4_8_GLPK_ROOM_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_4_8_GLPK_ROOM_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.ROOM);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "ROOM").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * MIMBL
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_1_5_1_GLPK_MIMBL_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_1_GLPK_MIMBL_WT_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_2_GLPK_MIMBL_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_2_GLPK_MIMBL_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_3_GLPK_MIMBL_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_3_GLPK_MIMBL_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_4_GLPK_MIMBL_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_4_GLPK_MIMBL_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_5_GLPK_MIMBL_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_5_GLPK_MIMBL_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_6_GLPK_MIMBL_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_6_GLPK_MIMBL_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_7_GLPK_MIMBL_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_7_GLPK_MIMBL_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_1_5_8_GLPK_MIMBL_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_1_5_8_GLPK_MIMBL_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("GLPK", "MIMBL").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/*************************************************
	 * CLP SOLVER *
	 *************************************************/
	
	/**
	 * FBA
	 */
	
	@Test
	public void test_2_1_1_CLP_FBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		
		cc.setMaximization(true);
		cc.setSolver(CLPSolverBuilder.ID);
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(WT_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_2_CLP_FBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(WT_ANAEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_3_CLP_FBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(RK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_4_CLP_FBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(GK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_5_CLP_FBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(ROU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_6_CLP_FBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(GOU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_1_7_CLP_FBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "FBA").get(FVA_TARGET_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	/**
	 * PFBA
	 */
	
	@Test
	public void test_2_2_1_CLP_PFBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.PFBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(WT_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_2_CLP_PFBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(WT_ANAEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_3_CLP_PFBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(RK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_4_CLP_PFBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(GK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_5_CLP_PFBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(ROU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_6_CLP_PFBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(GOU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_2_7_CLP_PFBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "pFBA").get(FVA_TARGET_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	/** LMOMA */
	
	@Test
	public void test_2_3_1_CLP_LMOMA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(WT_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_2_CLP_LMOMA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(WT_ANAEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_3_CLP_LMOMA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(RK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_4_CLP_LMOMA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(GK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_5_CLP_LMOMA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(ROU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_6_CLP_LMOMA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(GOU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_7_CLP_LMOMA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(FVA_TARGET_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_3_8_CLP_LMOMA_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "LMOMA").get(FVA_TARGET_AEROBIC_REFERENCE), actuals, DELTA_GLPK_A);
	}
	
	/**
	 * ROOM
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_2_4_1_CLP_MOMA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MOMA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(WT_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_2_CLP_MOMA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(WT_ANAEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_3_CLP_MOMA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(RK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_4_CLP_MOMA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(GK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_5_CLP_MOMA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(ROU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_6_CLP_MOMA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(GOU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_7_CLP_MOMA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(FVA_TARGET_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_4_8_CLP_MOMA_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.MOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MOMA").get(FVA_TARGET_AEROBIC_REFERENCE), actuals, DELTA_GLPK_A);
	}
	
	/**
	 * MIMBL
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_2_5_1_CLP_MIMBL_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(WT_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_2_CLP_MIMBL_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(WT_ANAEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_3_CLP_MIMBL_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(RK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_4_CLP_MIMBL_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(GK_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_5_CLP_MIMBL_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(ROU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_6_CLP_MIMBL_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(GOU_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_7_CLP_MIMBL_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(FVA_TARGET_AEROBIC), actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_2_5_8_CLP_MIMBL_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		assertArrayEquals(_results.get("CLP", "MIMBL").get(FVA_TARGET_AEROBIC_REFERENCE), actuals, DELTA_GLPK_A);
	}
	
	/*************************************************
	 * CPLEX SOLVER *
	 *************************************************/
	
	/**
	 * FBA
	 */
	
	@Test
	public void test_3_1_1_CPLEX_FBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_1_CPLEX_FBA_WT_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		
		cc.setMaximization(true);
		cc.setMethodType(SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setEnvironmentalConditions(envCondAerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_2_CPLEX_FBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_2_CPLEX_FBA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_3_CPLEX_FBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_3_CPLEX_FBA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_4_CPLEX_FBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_4_CPLEX_FBA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_5_CPLEX_FBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_5_CPLEX_FBA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_6_CPLEX_FBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_6_CPLEX_FBA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_1_7_CPLEX_FBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("FBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_1_7_CPLEX_FBA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(null);
		cc.setFBAObjSingleFlux(targetID, 1.0);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "FBA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * PFBA
	 */
	
	@Test
	public void test_3_2_1_CPLEX_PFBA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_1_CPLEX_PFBA_WT_AEROBIC ]==================================\n");
			
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.PFBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_2_CPLEX_PFBA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_2_CPLEX_PFBA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_3_CPLEX_PFBA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_3_CPLEX_PFBA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_4_CPLEX_PFBA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_4_CPLEX_PFBA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_5_CPLEX_PFBA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_5_CPLEX_PFBA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_6_CPLEX_PFBA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_6_CPLEX_PFBA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_2_7_CPLEX_PFBA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("PFBA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_2_7_CPLEX_PFBA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "pFBA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/** LMOMA */
	
	@Test
	public void test_3_3_1_CPLEX_LMOMA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_1_CPLEX_LMOMA_WT_AEROBIC ]==================================\n");
			
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_2_CPLEX_LMOMA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_2_CPLEX_LMOMA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_3_CPLEX_LMOMA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_3_CPLEX_LMOMA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_4_CPLEX_LMOMA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_4_CPLEX_LMOMA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_5_CPLEX_LMOMA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_5_CPLEX_LMOMA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_6_CPLEX_LMOMA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_6_CPLEX_LMOMA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_7_CPLEX_LMOMA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_7_CPLEX_LMOMA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_3_8_CPLEX_LMOMA_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("LMOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_3_8_CPLEX_LMOMA_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.LMOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "LMOMA").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * MOMA
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_3_4_1_CPLEX_MOMA_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_1_CPLEX_MOMA_WT_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MOMA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_2_CPLEX_MOMA_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_2_CPLEX_MOMA_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_3_CPLEX_MOMA_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_3_CPLEX_MOMA_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_4_CPLEX_MOMA_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_4_CPLEX_MOMA_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_5_CPLEX_MOMA_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_5_CPLEX_MOMA_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_6_CPLEX_MOMA_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_6_CPLEX_MOMA_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_7_CPLEX_MOMA_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_7_CPLEX_MOMA_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_4_8_CPLEX_MOMA_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MOMA"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_4_8_CPLEX_MOMA_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.MOMA);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MOMA").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * ROOM
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_3_5_1_CPLEX_ROOM_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_1_CPLEX_ROOM_WT_AEROBIC ]==================================\n");
		cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.ROOM);
		cc.setWTReference(null);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_2_CPLEX_ROOM_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_2_CPLEX_ROOM_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_3_CPLEX_ROOM_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_3_CPLEX_ROOM_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_4_CPLEX_ROOM_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_4_CPLEX_ROOM_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_5_CPLEX_ROOM_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_5_CPLEX_ROOM_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_6_CPLEX_ROOM_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_6_CPLEX_ROOM_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_7_CPLEX_ROOM_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_7_CPLEX_ROOM_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_5_8_CPLEX_ROOM_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("ROOM"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_5_8_CPLEX_ROOM_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.ROOM);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "ROOM").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * MIMBL
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void test_3_6_1_CPLEX_MIMBL_WT_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_1_CPLEX_MIMBL_WT_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(WT_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_2_CPLEX_MIMBL_WT_ANAEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_2_CPLEX_MIMBL_WT_ANAEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAnaerobiose);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(WT_ANAEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_3_CPLEX_MIMBL_RK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_3_CPLEX_MIMBL_RK_AEROBIC ]==================================\n");
			
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(RK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_4_CPLEX_MIMBL_GK_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_4_CPLEX_MIMBL_GK_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGK);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(GK_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_5_CPLEX_MIMBL_ROU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_5_CPLEX_MIMBL_ROU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondROU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(ROU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_6_CPLEX_MIMBL_GOU_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_6_CPLEX_MIMBL_GOU_AEROBIC ]==================================\n");
			
		cc.setGeneticConditions(genCondGOU);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(GOU_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_7_CPLEX_MIMBL_FVA_TARGET_AEROBIC() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_7_CPLEX_MIMBL_FVA_TARGET_AEROBIC ]==================================\n");
			
		cc.setFBAObjSingleFlux(targetID, 1.0);
		cc.setGeneticConditions(null);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(FVA_TARGET_AEROBIC);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	@Test
	public void test_3_6_8_CPLEX_MIMBL_FVA_TARGET_AEROBIC_REFERENCE() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey("MIMBL"));
		
		if (_debug)
			System.out.println("\n==================================[ test_3_6_8_CPLEX_MIMBL_FVA_TARGET_AEROBIC_REFERENCE ]==================================\n");
			
		cc.setMethodType(SimulationProperties.FBA);
		cc.setEnvironmentalConditions(envCondAerobiose);
		cc.setGeneticConditions(genCondRK);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		FluxValueMap wtref = cc.simulate().getFluxValues();
		
		cc.setMethodType(SimulationProperties.MIMBL);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.setMaximization(true);
		cc.setGeneticConditions(null);
		cc.setWTReference(null);
		cc.setUnderOverRef(null);
		cc.setWTReference(wtref);
		SteadyStateSimulationResult res = cc.simulate();
		double[] actuals = new double[3];
		double of = res.getOFvalue();
		double bio = res.getFluxValues().getValue(biomassID);
		double tar = res.getFluxValues().getValue(targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = tar;
		
		double[] expected = _results.get("CPLEX", "MIMBL").get(FVA_TARGET_AEROBIC_REFERENCE);
		
		if (_debug) {
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_GLPK_A);
	}
	
	/**
	 * MAIN METHOD USED TO GENERATE THE RESULTS
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		populate();
		
		for (String solver : _solvers.keySet()) {
			
			for (String method : _methods.keySet()) {
				
				StringBuilder sb = new StringBuilder();
				String mapName = "map_" + solver + "_" + method;
				sb.append("HashMap<String,double[]> " + mapName + "  = new HashMap<String,double[]>();\n");
				
				SimulationSteadyStateControlCenter cc = null;
				SteadyStateSimulationResult res = null;
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] WT_AEROBIC =====================================================================================================================================\n");
				/**
				 * WT_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, null, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + WT_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + WT_AEROBIC + ",null);\n");
				}
				
				//				if(res.getComplementaryInfoReactions().containsKey(SimulationProperties.WT_REFERENCE)){
				//					MapStringNum ref =  res.getComplementaryInfoReactions().get(SimulationProperties.WT_REFERENCE);
				//					
				//					BufferedWriter bw = new BufferedWriter(new FileWriter("/home/pmaia/Desktop/test_"+method+"_impl.csv"));
				//					for(String s : ref.keySet()){
				//						bw.append(s+","+ref.get(s));
				//						bw.newLine();
				//					}
				//					
				//					bw.flush();
				//					bw.close();
				//				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] WT_ANAEROBIC =====================================================================================================================================\n");
							
				/**
				 * WT_ANAEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAnaerobiose, null, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + WT_ANAEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + WT_ANAEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] RK_AEROBIC =====================================================================================================================================\n");
							
				/**
				 * RK_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, genCondRK, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + RK_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + RK_AEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] GK_AEROBIC =====================================================================================================================================\n");
							
				/**
				 * GK_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, genCondGK, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + GK_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + GK_AEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] ROU_AEROBIC =====================================================================================================================================\n");
							
				/**
				 * ROU_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, genCondROU, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + ROU_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + ROU_AEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] GOU_AEROBIC =====================================================================================================================================\n");
							
				/**
				 * GOU_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, genCondGOU, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + GOU_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + GOU_AEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] FVA_TARGET_AEROBIC =====================================================================================================================================\n");
							
				/**
				 * FVA_TARGET_AEROBIC
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, null, model, _methods.get(method));
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(targetID, 1.0);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + FVA_TARGET_AEROBIC + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + FVA_TARGET_AEROBIC + ",null);\n");
				}
				
				if (_debug)
					System.out.println("\n[" + solver + "][" + method
							+ "] FVA_TARGET_AEROBIC_REFERENCE =====================================================================================================================================\n");
							
				/**
				 * FVA_TARGET_AEROBIC_REFERENCE
				 */
				cc = new SimulationSteadyStateControlCenter(envCondAerobiose, genCondRK, model, SimulationProperties.FBA);
				cc.setSolver(_solvers.get(solver));
				cc.setMaximization(true);
				cc.setFBAObjSingleFlux(biomassID, 1.0);
				cc.setUnderOverRef(null);
				cc.setWTReference(null);
//				cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
				try {
					FluxValueMap wtref = cc.simulate().getFluxValues();
					cc = new SimulationSteadyStateControlCenter(envCondAerobiose, null, model, _methods.get(method));
					cc.setSolver(_solvers.get(solver));
					cc.setMaximization(true);
					cc.setFBAObjSingleFlux(biomassID, 1.0);
					cc.setWTReference(wtref);
					cc.setRoomProperties(FORCE_ROOM_DELTA, FORCE_ROOM_EPSILON);
					res = cc.simulate();
					double of = res.getOFvalue();
					double bio = res.getFluxValues().getValue(biomassID);
					double succ = res.getFluxValues().getValue(targetID);
					sb.append(mapName + ".put(" + FVA_TARGET_AEROBIC_REFERENCE + ",new double[]{" + of + "," + bio + "," + succ + "});\n");
				} catch (UnsupportedOperationException e) {
					sb.append(mapName + ".put(" + FVA_TARGET_AEROBIC_REFERENCE + ",null);\n");
				}
				
				sb.append("_results.put(\"" + _solvers.get(solver) + "\",\"" + _methods.get(method) + "\"," + mapName + ");\n");
				System.out.println(sb.toString() + "\n");
			}
		}
	}
	
	static {
		
		_solvers = new IndexedHashMap<String, String>();
//		_solvers.put("GLPK", GLPKBinSolverBuilder.ID);
//		_solvers.put("CLP", CLPSolverBuilder.ID);
		_solvers.put("CPLEX", CPLEX3SolverBuilder.ID);
		
		_methods = new IndexedHashMap<String, String>();
		_methods.put("FBA", SimulationProperties.FBA);
		_methods.put("PFBA", SimulationProperties.PFBA);
		_methods.put("MOMA", SimulationProperties.MOMA);
		_methods.put("LMOMA", SimulationProperties.LMOMA);
		_methods.put("ROOM", SimulationProperties.ROOM);
		_methods.put("MIMBL", SimulationProperties.MIMBL);
		
		HashMap<String, double[]> map_GLPK_FBA = new HashMap<String, double[]>();
		map_GLPK_FBA.put(WT_AEROBIC, new double[] { 1.275895306, 1.2759, 0.0 });
		map_GLPK_FBA.put(WT_ANAEROBIC, new double[] { 0.458892739, 0.458893, 0.153149 });
		map_GLPK_FBA.put(RK_AEROBIC, new double[] { 1.260004711, 1.26, 1.31131 });
		map_GLPK_FBA.put(GK_AEROBIC, new double[] { 1.260004712, 1.26, 1.31131 });
		map_GLPK_FBA.put(ROU_AEROBIC, new double[] { 1.261595344, 1.2616, 1.18005 });
		map_GLPK_FBA.put(GOU_AEROBIC, new double[] { 1.261595344, 1.2616, 1.18005 });
		map_GLPK_FBA.put(FVA_TARGET_AEROBIC, new double[] { 34.04357143, 0.0, 34.0436 });
		map_GLPK_FBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1.275895306, 1.2759, 0.0 });
		_results.put("GLPK", "FBA", map_GLPK_FBA);
		
		HashMap<String, double[]> map_GLPK_PFBA = new HashMap<String, double[]>();
		map_GLPK_PFBA.put(WT_AEROBIC, new double[] { 1112.906342, 1.27588, 0.0 });
		map_GLPK_PFBA.put(WT_ANAEROBIC, new double[] { 808.3500572, 0.458888, 0.153148 });
		map_GLPK_PFBA.put(RK_AEROBIC, new double[] { 1104.964685, 1.25999, 1.31129 });
		map_GLPK_PFBA.put(GK_AEROBIC, new double[] { 1104.964686, 1.25999, 1.31129 });
		map_GLPK_PFBA.put(ROU_AEROBIC, new double[] { 1105.759161, 1.26158, 1.18003 });
		map_GLPK_PFBA.put(GOU_AEROBIC, new double[] { 1105.759161, 1.26158, 1.18003 });
		map_GLPK_PFBA.put(FVA_TARGET_AEROBIC, new double[] { 842.5324572, 0.0, 34.0432 });
		map_GLPK_PFBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1112.906342, 1.27588, 0.0 });
		_results.put("GLPK", "pFBA", map_GLPK_PFBA);
		
		HashMap<String, double[]> map_GLPK_LMOMA = new HashMap<String, double[]>();
		map_GLPK_LMOMA.put(WT_AEROBIC, new double[] { 7.439375416E-4, 1.27588, 0.0 });
		map_GLPK_LMOMA.put(WT_ANAEROBIC, new double[] { 4.542955772E-4, 0.458888, 0.153148 });
		map_GLPK_LMOMA.put(RK_AEROBIC, new double[] { 24.86519008, 1.21298, 1.29047 });
		map_GLPK_LMOMA.put(GK_AEROBIC, new double[] { 24.86519008, 1.21298, 1.29047 });
		map_GLPK_LMOMA.put(ROU_AEROBIC, new double[] { 22.37492516, 1.21929, 1.1613 });
		map_GLPK_LMOMA.put(GOU_AEROBIC, new double[] { 22.37492516, 1.21929, 1.1613 });
		map_GLPK_LMOMA.put(FVA_TARGET_AEROBIC, new double[] { 7.439375416E-4, 1.27588, 0.0 });
		map_GLPK_LMOMA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.7526556782, 1.26, 1.31131 });
		_results.put("GLPK", "LMOMA", map_GLPK_LMOMA);
		
		HashMap<String, double[]> map_GLPK_ROOM = new HashMap<String, double[]>();
		map_GLPK_ROOM.put(WT_AEROBIC, new double[] { 0.0, 1.2366, 6.28413E-4 });
		map_GLPK_ROOM.put(WT_ANAEROBIC, new double[] { 0.0, 0.444121, 0.149592 });
		map_GLPK_ROOM.put(RK_AEROBIC, new double[] { 2.0, 1.2366, 1.28695 });
		map_GLPK_ROOM.put(GK_AEROBIC, new double[] { 2.0, 1.2366, 1.28695 });
		map_GLPK_ROOM.put(ROU_AEROBIC, new double[] { 2.0, 1.2366, 1.15412 });
		map_GLPK_ROOM.put(GOU_AEROBIC, new double[] { 2.0, 1.2366, 1.15412 });
		map_GLPK_ROOM.put(FVA_TARGET_AEROBIC, new double[] { 0.0, 1.2366, 6.28413E-4 });
		map_GLPK_ROOM.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.0, 1.26, 1.31131 });
		_results.put("GLPK", "ROOM", map_GLPK_ROOM);
		
		HashMap<String, double[]> map_GLPK_MIMBL = new HashMap<String, double[]>();
		map_GLPK_MIMBL.put(WT_AEROBIC, new double[] { 5.988716066E-4, 1.27588, 0.0 });
		map_GLPK_MIMBL.put(WT_ANAEROBIC, new double[] { 3.090446009E-4, 0.458885, 0.153148 });
		map_GLPK_MIMBL.put(RK_AEROBIC, new double[] { 10.65154563, 1.0061, 1.24075 });
		map_GLPK_MIMBL.put(GK_AEROBIC, new double[] { 10.65154563, 1.0061, 1.24075 });
		map_GLPK_MIMBL.put(ROU_AEROBIC, new double[] { 9.561440174, 1.03558, 1.12847 });
		map_GLPK_MIMBL.put(GOU_AEROBIC, new double[] { 9.561440174, 1.03558, 1.12847 });
		map_GLPK_MIMBL.put(FVA_TARGET_AEROBIC, new double[] { 5.988716066E-4, 1.27588, 0.0 });
		map_GLPK_MIMBL.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1.283301773, 1.25999, 1.31131 });
		_results.put("GLPK", "MIMBL", map_GLPK_MIMBL);
		
		HashMap<String, double[]> map_CLP_FBA = new HashMap<String, double[]>();
		map_CLP_FBA.put(WT_AEROBIC, new double[] { 1.2758953, 1.2758953, 0.0 });
		map_CLP_FBA.put(WT_ANAEROBIC, new double[] { 0.45889274, 0.45889274, 0.15314949 });
		map_CLP_FBA.put(RK_AEROBIC, new double[] { 1.2600047, 1.2600047, 1.3113058 });
		map_CLP_FBA.put(GK_AEROBIC, new double[] { 1.2600047, 1.2600047, 1.3113058 });
		map_CLP_FBA.put(ROU_AEROBIC, new double[] { 1.2615953, 1.2615953, 1.1800454 });
		map_CLP_FBA.put(GOU_AEROBIC, new double[] { 1.2615953, 1.2615953, 1.1800454 });
		map_CLP_FBA.put(FVA_TARGET_AEROBIC, new double[] { 34.043571, 0.0, 34.043571 });
		map_CLP_FBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1.2758953, 1.2758953, 0.0 });
		_results.put("CLP", "FBA", map_CLP_FBA);
		
		HashMap<String, double[]> map_CLP_PFBA = new HashMap<String, double[]>();
		map_CLP_PFBA.put(WT_AEROBIC, new double[] { 1112.9063, 1.2758825, 0.0 });
		map_CLP_PFBA.put(WT_ANAEROBIC, new double[] { 808.35007, 0.45888815, 0.15314795 });
		map_CLP_PFBA.put(RK_AEROBIC, new double[] { 1104.9647, 1.2599921, 1.3112927 });
		map_CLP_PFBA.put(GK_AEROBIC, new double[] { 1104.9647, 1.2599921, 1.3112927 });
		map_CLP_PFBA.put(ROU_AEROBIC, new double[] { 1105.7591, 1.2615827, 1.1800322 });
		map_CLP_PFBA.put(GOU_AEROBIC, new double[] { 1105.7591, 1.2615827, 1.1800322 });
		map_CLP_PFBA.put(FVA_TARGET_AEROBIC, new double[] { 842.53244, 0.0, 34.043231 });
		map_CLP_PFBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1112.9063, 1.2758825, 0.0 });
		_results.put("CLP", "pFBA", map_CLP_PFBA);
		
		HashMap<String, double[]> map_CLP_MOMA = new HashMap<String, double[]>();
		map_CLP_MOMA.put(WT_AEROBIC, new double[] { 0.06924264913877844, 1.2727822, 0.010822171 });
		map_CLP_MOMA.put(WT_ANAEROBIC, new double[] { 5.2117080502538595, 0.25463216, 0.18358941 });
		map_CLP_MOMA.put(RK_AEROBIC, new double[] { 37.124123711094356, 0.93462651, 1.1491431 });
		map_CLP_MOMA.put(GK_AEROBIC, new double[] { 66.7530131148589, 0.92005596, 1.2193544 });
		map_CLP_MOMA.put(ROU_AEROBIC, new double[] { 24.6891077276315, 1.1617342, 1.1385615 });
		map_CLP_MOMA.put(GOU_AEROBIC, new double[] { 48.06062770405208, 1.0507039, 1.0953652 });
		map_CLP_MOMA.put(FVA_TARGET_AEROBIC, new double[] { 0.06924264913877844, 1.2727822, 0.010822171 });
		map_CLP_MOMA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 3.854609331465898E-7, 1.2599821, 1.3112973 });
		_results.put("CLP", "MOMA", map_CLP_MOMA);
		
		HashMap<String, double[]> map_CLP_LMOMA = new HashMap<String, double[]>();
		map_CLP_LMOMA.put(WT_AEROBIC, new double[] { -5.4800724E-7, 1.2758826, 0.0 });
		map_CLP_LMOMA.put(WT_ANAEROBIC, new double[] { 1.8998635E-7, 0.45888814, 0.15314795 });
		map_CLP_LMOMA.put(RK_AEROBIC, new double[] { 24.865245, 1.2129814, 1.2904662 });
		map_CLP_LMOMA.put(GK_AEROBIC, new double[] { 24.865245, 1.2129814, 1.2904662 });
		map_CLP_LMOMA.put(ROU_AEROBIC, new double[] { 22.374981, 1.2192879, 1.1612965 });
		map_CLP_LMOMA.put(GOU_AEROBIC, new double[] { 22.37498, 1.2192879, 1.1612965 });
		map_CLP_LMOMA.put(FVA_TARGET_AEROBIC, new double[] { -5.4800724E-7, 1.2758826, 0.0 });
		map_CLP_LMOMA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.0026083386, 1.2600049, 1.3113027 });
		_results.put("CLP", "LMOMA", map_CLP_LMOMA);
		
		HashMap<String, double[]> map_CLP_ROOM = new HashMap<String, double[]>();
		map_CLP_ROOM.put(WT_AEROBIC, new double[] { 0.0, 1.236606, 0.001 });
		map_CLP_ROOM.put(WT_ANAEROBIC, new double[] { -2.0549477E-8, 0.44769508, 0.14941241 });
		map_CLP_ROOM.put(RK_AEROBIC, new double[] { 0.0046999048, 1.236606, 1.4002425 });
		map_CLP_ROOM.put(GK_AEROBIC, new double[] { 0.0046999057, 1.236606, 1.4002425 });
		map_CLP_ROOM.put(ROU_AEROBIC, new double[] { 1.0595072E-5, 1.236606, 1.154114 });
		map_CLP_ROOM.put(GOU_AEROBIC, new double[] { 1.0597924E-5, 1.236606, 1.154114 });
		map_CLP_ROOM.put(FVA_TARGET_AEROBIC, new double[] { 0.0, 1.236606, 0.001 });
		map_CLP_ROOM.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 2.6136683E-7, 1.2600047, 1.3113058 });
		_results.put("CLP", "ROOM", map_CLP_ROOM);
		
		HashMap<String, double[]> map_CLP_MIMBL = new HashMap<String, double[]>();
		map_CLP_MIMBL.put(WT_AEROBIC, new double[] { 2.2362513E-6, 1.2758822, 0.0 });
		map_CLP_MIMBL.put(WT_ANAEROBIC, new double[] { -2.8954081E-7, 0.45888808, 0.15314795 });
		map_CLP_MIMBL.put(RK_AEROBIC, new double[] { 10.654208, 1.0065688, 1.2419544 });
		map_CLP_MIMBL.put(GK_AEROBIC, new double[] { 10.654208, 1.0065688, 1.2419544 });
		map_CLP_MIMBL.put(ROU_AEROBIC, new double[] { 9.5637772, 1.0357306, 1.1308524 });
		map_CLP_MIMBL.put(GOU_AEROBIC, new double[] { 9.5637775, 1.0357307, 1.1308523 });
		map_CLP_MIMBL.put(FVA_TARGET_AEROBIC, new double[] { 2.2362513E-6, 1.2758822, 0.0 });
		map_CLP_MIMBL.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.0052594318, 1.2600046, 1.3113058 });
		_results.put("CLP", "MIMBL", map_CLP_MIMBL);
		
		HashMap<String, double[]> map_CPLEX_FBA = new HashMap<String, double[]>();
		map_CPLEX_FBA.put(WT_AEROBIC, new double[] { 1.275895306099577, 1.275895306050214, 0.0 });
		map_CPLEX_FBA.put(WT_ANAEROBIC, new double[] { 0.45889273905384076, 0.4588927391363803, 0.15314948608115378 });
		map_CPLEX_FBA.put(RK_AEROBIC, new double[] { 1.2600047115316502, 1.2600047114945125, 1.31130580355584 });
		map_CPLEX_FBA.put(GK_AEROBIC, new double[] { 1.2600047115463893, 1.2600047114945157, 1.3113058035558378 });
		map_CPLEX_FBA.put(ROU_AEROBIC, new double[] { 1.2615953441278234, 1.2615953441263852, 1.18004540504672 });
		map_CPLEX_FBA.put(GOU_AEROBIC, new double[] { 1.2615953437227876, 1.2615953438168515, 1.1800454072283295 });
		map_CPLEX_FBA.put(FVA_TARGET_AEROBIC, new double[] { 34.043571428571425, 0.0, 34.043571428820975 });
		map_CPLEX_FBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1.275895306099577, 1.275895306050214, 0.0 });
		_results.put("CPLEX", "FBA", map_CPLEX_FBA);
		
		HashMap<String, double[]> map_CPLEX_PFBA = new HashMap<String, double[]>();
		map_CPLEX_PFBA.put(WT_AEROBIC, new double[] { 1112.9063418078572, 1.275882547146516, 0.0 });
		map_CPLEX_PFBA.put(WT_ANAEROBIC, new double[] { 808.3500576671213, 0.45888815012645023, 0.15314795455875876 });
		map_CPLEX_PFBA.put(RK_AEROBIC, new double[] { 1104.9646855141696, 1.259992111484535, 1.3112926903036666 });
		map_CPLEX_PFBA.put(GK_AEROBIC, new double[] { 1104.964685534566, 1.2599921114992738, 1.3112926903189923 });
		map_CPLEX_PFBA.put(ROU_AEROBIC, new double[] { 1105.759161457971, 1.261582728174382, 1.1800322754362753 });
		map_CPLEX_PFBA.put(GOU_AEROBIC, new double[] { 1105.7591608974694, 1.2615827277693503, 1.1800322750146526 });
		map_CPLEX_PFBA.put(FVA_TARGET_AEROBIC, new double[] { 842.5324571464282, 0.0, 34.04323099285714 });
		map_CPLEX_PFBA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 1112.9063418078572, 1.275882547146516, 0.0 });
		_results.put("CPLEX", "pFBA", map_CPLEX_PFBA);
		
		HashMap<String, double[]> map_CPLEX_MOMA = new HashMap<String, double[]>();
		map_CPLEX_MOMA.put(WT_AEROBIC, new double[] { 5.812406826835794E-5, 1.2752565026461185, 2.5853335241773884E-4 });
		map_CPLEX_MOMA.put(WT_ANAEROBIC, new double[] { 4.76024478451931E-5, 0.4583654715794127, 0.15345229874969596 });
		map_CPLEX_MOMA.put(RK_AEROBIC, new double[] { 18.13979270914926, 1.1732002004648627, 1.2235858501708061 });
		map_CPLEX_MOMA.put(GK_AEROBIC, new double[] { 18.13979268565365, 1.1732002046471788, 1.2235858541559275 });
		map_CPLEX_MOMA.put(ROU_AEROBIC, new double[] { 14.687349709534631, 1.1834852108137073, 1.101100793080816 });
		map_CPLEX_MOMA.put(GOU_AEROBIC, new double[] { 14.687349695741725, 1.183485211396564, 1.1011007942647584 });
		map_CPLEX_MOMA.put(FVA_TARGET_AEROBIC, new double[] { 5.812406826835794E-5, 1.2752565026461185, 2.5853335241773884E-4 });
		map_CPLEX_MOMA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 4687.5878654864755, 0.044545889842008565, 0.21547824294125384 });
		_results.put("CPLEX", "MOMA", map_CPLEX_MOMA);
		
		HashMap<String, double[]> map_CPLEX_LMOMA = new HashMap<String, double[]>();
		map_CPLEX_LMOMA.put(WT_AEROBIC, new double[] { 0.0, 1.275882547146516, 0.0 });
		map_CPLEX_LMOMA.put(WT_ANAEROBIC, new double[] { 0.0, 0.45888815012645023, 0.15314795455875876 });
		map_CPLEX_LMOMA.put(RK_AEROBIC, new double[] { 24.86524873798018, 1.2129814009448798, 1.2904661951982985 });
		map_CPLEX_LMOMA.put(GK_AEROBIC, new double[] { 24.865248737980835, 1.2129814009448832, 1.290466195198309 });
		map_CPLEX_LMOMA.put(ROU_AEROBIC, new double[] { 22.374983620884734, 1.2192879407192982, 1.1612965493738585 });
		map_CPLEX_LMOMA.put(GOU_AEROBIC, new double[] { 22.37498362088718, 1.2192879407192425, 1.1612965493740353 });
		map_CPLEX_LMOMA.put(FVA_TARGET_AEROBIC, new double[] { 0.0, 1.275882547146516, 0.0 });
		map_CPLEX_LMOMA.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.0, 1.2600047114945125, 1.31130580355584 });
		_results.put("CPLEX", "LMOMA", map_CPLEX_LMOMA);
		
		HashMap<String, double[]> map_CPLEX_ROOM = new HashMap<String, double[]>();
		map_CPLEX_ROOM.put(WT_AEROBIC, new double[] { -1.8626450938956528E-8, 1.275882547146516, 4.6566128730773926E-10 });
		map_CPLEX_ROOM.put(WT_ANAEROBIC, new double[] { -1.3969824272813132E-8, 0.45888815012645023, 0.15314795525724306 });
		map_CPLEX_ROOM.put(RK_AEROBIC, new double[] { 77.00006907605642, 0.965558391533048, 1.8658463890522876 });
		map_CPLEX_ROOM.put(GK_AEROBIC, new double[] { 79.00005346762933, 0.9479185536500848, 1.7766204626650082 });
		map_CPLEX_ROOM.put(ROU_AEROBIC, new double[] { 79.0000621331509, 0.9966218398038076, 1.6790751653184095 });
		map_CPLEX_ROOM.put(GOU_AEROBIC, new double[] { 78.00006206765735, 0.9964745235866552, 1.679330570865985 });
		map_CPLEX_ROOM.put(FVA_TARGET_AEROBIC, new double[] { -1.8626450938956528E-8, 1.275882547146516, 4.6566128730773926E-10 });
		map_CPLEX_ROOM.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { -1.977831866955662E-15, 1.2600047115077224, 1.311305802920069 });
		_results.put("CPLEX", "ROOM", map_CPLEX_ROOM);
		
		HashMap<String, double[]> map_CPLEX_MIMBL = new HashMap<String, double[]>();
		map_CPLEX_MIMBL.put(WT_AEROBIC, new double[] { 0.0, 1.2758825471465165, 0.0 });
		map_CPLEX_MIMBL.put(WT_ANAEROBIC, new double[] { 0.0, 0.45888815012645023, 0.15314795455875876 });
		map_CPLEX_MIMBL.put(RK_AEROBIC, new double[] { 10.655761779411023, 1.0064090696514594, 1.2395418221192773 });
		map_CPLEX_MIMBL.put(GK_AEROBIC, new double[] { 10.65576177941206, 1.0064090696514212, 1.2395418221193293 });
		map_CPLEX_MIMBL.put(ROU_AEROBIC, new double[] { 9.564785572568875, 1.03536627611176, 1.1292016770640996 });
		map_CPLEX_MIMBL.put(GOU_AEROBIC, new double[] { 9.564785572568644, 1.035366276111771, 1.1292016770640512 });
		map_CPLEX_MIMBL.put(FVA_TARGET_AEROBIC, new double[] { 0.0, 1.2758825471465165, 0.0 });
		map_CPLEX_MIMBL.put(FVA_TARGET_AEROBIC_REFERENCE, new double[] { 0.0, 1.2600047115040134, 1.31130580355584 });
		_results.put("CPLEX", "MIMBL", map_CPLEX_MIMBL);
	}
}
