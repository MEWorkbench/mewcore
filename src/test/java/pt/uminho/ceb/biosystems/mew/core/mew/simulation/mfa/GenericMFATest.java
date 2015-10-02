package pt.uminho.ceb.biosystems.mew.core.mew.simulation.mfa;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.MFAControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAApproaches;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.robustness.MFARobustnessResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability.MFAFvaResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability.MFATightBoundsResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFASystemType;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenericMFATest {
	
	public static final Pattern _methodPattern = Pattern.compile(".*test_(.*)\\(.*");
	public static final boolean	_debug			= true;
	public static final double	CONST_ZERO		= 1e-8;
	public static final double	DELTA_SOLVER	= 1e-5;
	
	public static boolean isZero(double test) {
		return Math.abs(test) < CONST_ZERO;
	}
	
	public static final MultiKeyMap<String, Map<String, double[]>>	_results		= new MultiKeyMap<String, Map<String, double[]>>();
	
	public static Map<String, SolverType>							_solvers;
	
	public static Map<String, MFAApproaches>						_methods;
	
	/**
	 * Analysis constants
	 */
	public static final String										WT_ENV1_FM1_FR1	= "WT_ENV1_FM1_FR1";
	
	public static final String										WT_ENV2_FM1_FR1	= "WT_ENV2_FM1_FR1";
	
	public static final String										RK_ENV1_FM1_FR0	= "RK_ENV1_FM1_FR0";
	
	public static final String										WT_ENV1_FM2_FR1	= "WT_ENV1_FM2_FR1";
	
	public static final String										WT_ENV1_FM1_FR2	= "WT_ENV1_FM1_FR2";
	
	/**
	 * Instance variables
	 */
	public static EnvironmentalConditions							_envConditions1, _envConditions2;
	public static GeneticConditions									_genConditions;
	public static MFAControlCenter									_cc;
	public static ISteadyStateModel									_model;
	public static ExpMeasuredFluxes									_measuredFluxes1, _measuredFluxes2;
	public static FluxRatioConstraintList							_fluxRatios1, _fluxRatios2;
	public static String											_biomassID;
	public static String											_targetID;
	
	public static String											_modelConfFile;
	public static String											_measuredFluxesFile1, _measuredFluxesFile2;
	public static String											_fluxRatiosFile1, _fluxRatiosFile2;
	public static String											_envConditionsFile1, _envConditionsFile2;
	
	/** for FVA **/
	public static double											_fvaMinPercentage;
	public static String											_fvaMinFlux;
	public static String[]											_fluxesToAnalyze;
	
	/** for robustness analysis */
	public static int												_robustnessPercentageInterval;
	public static String[]											_fluxesToAnalyzeRobustness;
	
	//	@BeforeClass
	public static void populate() throws Exception {
		
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		/**
		 * MODEL
		 */
		ModelConfiguration modelConf = new ModelConfiguration(_modelConfFile);
		_model = modelConf.getModel();
		_biomassID = modelConf.getModelBiomass();
		
		/**
		 * ENVIRONMENTAL CONDITIONS
		 */
		_envConditions1 = EnvironmentalConditions.readFromFile(_envConditionsFile1, ",");
		_envConditions2 = EnvironmentalConditions.readFromFile(_envConditionsFile2, ",");
		
		/**
		 * EXPERIMENTALLY MEASURED FLUXES
		 */
		_measuredFluxes1 = new ExpMeasuredFluxes();
		_measuredFluxes1.loadFromFile(new File(_measuredFluxesFile1), Delimiter.COMMA.toString(), false);
		
		_measuredFluxes2 = new ExpMeasuredFluxes();
		_measuredFluxes2.loadFromFile(new File(_measuredFluxesFile2), Delimiter.COMMA.toString(), false);
		
		/**
		 * FLUX RATIOS
		 */
		_fluxRatios1 = (_fluxRatiosFile1 != null) ? MFATestUtil.getFluxRatiosFromFile(_fluxRatiosFile1) : null;
		_fluxRatios2 = (_fluxRatiosFile2 != null) ? MFATestUtil.getFluxRatiosFromFile(_fluxRatiosFile2) : null;
		
		/**
		 * MFA CONTROL CENTER
		 */
		_cc = new MFAControlCenter(_envConditions1, _genConditions, _model, _measuredFluxes1, _fluxRatios1, MFAApproaches.linearProgramming, MFASystemType.underdetermined);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
	}
	
	/**************************************************************************************************
	 ************************************************************************************************** 
	 ***** 										G L P K 										  *****
	 ************************************************************************************************** 
	 **************************************************************************************************/	
	@Test
	public void test_1_1_1_GLPK_LP_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, MFAApproaches.linearProgramming, MFASystemType.underdetermined);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
		_cc.setSolver(SolverType.GLPK);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_1_2_GLPK_LP_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_LP).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_1_3_GLPK_LP_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_LP).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_1_4_GLPK_LP_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_LP).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_1_5_GLPK_LP_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_2_1_GLPK_PARSIMONIOUS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setApproach(MFAApproaches.parsimonious);
		_cc.setFluxRatios(_fluxRatios1);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_2_2_GLPK_PARSIMONIOUS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_2_3_GLPK_PARSIMONIOUS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_PARSIMONIOUS).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_2_4_GLPK_PARSIMONIOUS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_2_5_GLPK_PARSIMONIOUS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setFluxRatios(_fluxRatios2);
		_cc.setMeasuredFluxes(_measuredFluxes1);
//		_cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/mfafiles/persistent.mps");
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	
	@Test
	public void test_1_3_1_GLPK_FVA_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setApproach(MFAApproaches.fva);
		_cc.setFluxRatios(_fluxRatios1);		
		_cc.setMinimumPercentage(_fvaMinPercentage);
		_cc.setMinimumPercentageFlux(_fvaMinFlux);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_3_2_GLPK_FVA_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions2);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_FVA).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_3_3_GLPK_FVA_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_FVA).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_3_4_GLPK_FVA_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_FVA).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_3_5_GLPK_FVA_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_4_1_GLPK_TIGHTBOUNDS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setApproach(MFAApproaches.tightBounds);
		_cc.setFluxRatios(_fluxRatios1);
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_4_2_GLPK_TIGHTBOUNDS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions2);

		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV2_FM1_FR1);		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_4_3_GLPK_TIGHTBOUNDS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_TIGHTBOUMDS).get(RK_ENV1_FM1_FR0);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_4_4_GLPK_TIGHTBOUNDS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM2_FR1);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_4_5_GLPK_TIGHTBOUNDS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("GLPK", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR2);			
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_5_1_GLPK_ROBUSTNESS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setApproach(MFAApproaches.robustnessAnalysis);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setRobustObjectiveFlux(_biomassID);
		_cc.setPercentageInterval(_robustnessPercentageInterval);
		_cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
		 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);				
		double[] expected = _results.get("GLPK", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_5_2_GLPK_ROBUSTNESS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions2);

		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("GLPK", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV2_FM1_FR1);		 
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_5_3_GLPK_ROBUSSTNESS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("GLPK", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(RK_ENV1_FM1_FR0);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_5_4_GLPK_ROBUSTNESS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);				
		double[] expected = _results.get("GLPK", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM2_FR1);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_1_5_5_GLPK_ROBUSTNESS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("GLPK"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("GLPK", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR2);		 			 				
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	/**************************************************************************************************
	 ************************************************************************************************** 
	 ***** 										C L P 											  *****
	 ************************************************************************************************** 
	 **************************************************************************************************/
	
	
	@Test
	public void test_2_1_1_CLP_LP_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setApproach(MFAApproaches.linearProgramming);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setSolver(SolverType.CLP);
		_cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyze));
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_1_2_CLP_LP_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_LP).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_1_3_CLP_LP_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_LP).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_1_4_CLP_LP_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_LP).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_1_5_CLP_LP_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_2_1_CLP_QP_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setApproach(MFAApproaches.quadraticProgramming);
		_cc.setFluxRatios(_fluxRatios1);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_QP).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_2_2_CLP_QP_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_QP).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_2_3_CLP_QP_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_QP).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_2_4_CLP_QP_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_QP).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_2_5_CLP_QP_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_QP).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	
	@Test
	public void test_2_3_1_CLP_PARSIMONIOUS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setApproach(MFAApproaches.parsimonious);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_3_2_CLP_PARSIMONIOUS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_3_3_CLP_PARSIMONIOUS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_PARSIMONIOUS).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_3_4_CLP_PARSIMONIOUS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_3_5_CLP_PARSIMONIOUS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	
	@Test
	public void test_2_4_1_CLP_FVA_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setApproach(MFAApproaches.fva);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMinimumPercentage(_fvaMinPercentage);
		_cc.setMinimumPercentageFlux(_fvaMinFlux);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_4_2_CLP_FVA_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_FVA).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_4_3_CLP_FVA_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_FVA).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_4_4_CLP_FVA_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_FVA).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_4_5_CLP_FVA_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CLP", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_5_1_CLP_TIGHTBOUNDS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setApproach(MFAApproaches.tightBounds);
		_cc.setMaximization(true);
		_cc.setFluxRatios(_fluxRatios1);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_5_2_CLP_TIGHTBOUNDS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV2_FM1_FR1);		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_5_3_CLP_TIGHTBOUNDS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_TIGHTBOUMDS).get(RK_ENV1_FM1_FR0);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_5_4_CLP_TIGHTBOUNDS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM2_FR1);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_5_5_CLP_TIGHTBOUNDS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CLP", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR2);			
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_6_1_CLP_ROBUSTNESS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setApproach(MFAApproaches.robustnessAnalysis);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setRobustObjectiveFlux(_biomassID);
		_cc.setPercentageInterval(_robustnessPercentageInterval);
		_cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
		 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CLP", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_6_2_CLP_ROBUSTNESS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions2);

		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);	
		double[] expected = _results.get("CLP", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV2_FM1_FR1);		 
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_6_3_CLP_ROBUSSTNESS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CLP", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(RK_ENV1_FM1_FR0);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_6_4_CLP_ROBUSTNESS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CLP", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM2_FR1);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_2_6_5_CLP_ROBUSTNESS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CLP"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CLP", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR2);		 			 				
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	/**************************************************************************************************
	 ************************************************************************************************** 
	 ***** 										C P L E X 									      *****
	 ************************************************************************************************** 
	 ************************************************************************************************** 
	 * 
	 ** @throws Exception
	 */
	
	@Test
	public void test_3_1_1_CPLEX_LP_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setApproach(MFAApproaches.linearProgramming);
		_cc.setSolver(SolverType.CPLEX3);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyze));
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_1_2_CPLEX_LP_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_LP).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_1_3_CPLEX_LP_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_LP).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_1_4_CPLEX_LP_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_LP).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_1_5_CPLEX_LP_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_LP));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_LP).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_2_1_CPLEX_QP_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setApproach(MFAApproaches.quadraticProgramming);
		_cc.setFluxRatios(_fluxRatios1);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_QP).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_2_2_CPLEX_QP_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_QP).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_2_3_CPLEX_QP_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_QP).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_2_4_CPLEX_QP_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_QP).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_2_5_CPLEX_QP_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_QP));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_QP).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	
	@Test
	public void test_3_3_1_CPLEX_PARSIMONIOUS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setApproach(MFAApproaches.parsimonious);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_3_2_CPLEX_PARSIMONIOUS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_3_3_CPLEX_PARSIMONIOUS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[3];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		double targ = result.getFluxValues().get(_targetID);
		actuals[0] = of;
		actuals[1] = bio;
		actuals[2] = targ;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_PARSIMONIOUS).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_3_4_CPLEX_PARSIMONIOUS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_3_5_CPLEX_PARSIMONIOUS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_PARSIMONIOUS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		SteadyStateSimulationResult result = _cc.simulate();
		
		double[] actuals = new double[2];
		double of = result.getOFvalue();
		double bio = result.getFluxValues().get(_biomassID);
		actuals[0] = of;
		actuals[1] = bio;
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_PARSIMONIOUS).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	
	@Test
	public void test_3_4_1_CPLEX_FVA_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setApproach(MFAApproaches.fva);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMinimumPercentage(_fvaMinPercentage);
		_cc.setMinimumPercentageFlux(_fvaMinFlux);
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_4_2_CPLEX_FVA_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_FVA).get(WT_ENV2_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_4_3_CPLEX_FVA_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_FVA).get(RK_ENV1_FM1_FR0);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_4_4_CPLEX_FVA_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_FVA).get(WT_ENV1_FM2_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_4_5_CPLEX_FVA_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_FVA));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFAFvaResult result = (MFAFvaResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[2];
		actuals[0] = result.getObjectiveFluxValue();
		actuals[1] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_FVA).get(WT_ENV1_FM1_FR2);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_5_1_CPLEX_TIGHTBOUNDS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setApproach(MFAApproaches.tightBounds);
		_cc.setMaximization(true);
		_cc.setFluxRatios(_fluxRatios1);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_5_2_CPLEX_TIGHTBOUNDS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV2_FM1_FR1);		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_5_3_CPLEX_TIGHTBOUNDS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_TIGHTBOUMDS).get(RK_ENV1_FM1_FR0);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_5_4_CPLEX_TIGHTBOUNDS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM2_FR1);	
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_5_5_CPLEX_TIGHTBOUNDS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_TIGHTBOUMDS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFATightBoundsResult result =  (MFATightBoundsResult) _cc.simulate();
		
		Map<String,ReactionConstraint> fluxBounds = result.getFluxBounds(); 
		double[] analyzedFluxBounds = getLowerUpper(fluxBounds);
		double[] actuals = new double[1];
		actuals[0] = fluxBounds.get(_biomassID).getLowerLimit();
		actuals = concatDoubleArrays(actuals, analyzedFluxBounds);
		 		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_TIGHTBOUMDS).get(WT_ENV1_FM1_FR2);			
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_6_1_CPLEX_ROBUSTNESS_WT_ENV1_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setApproach(MFAApproaches.robustnessAnalysis);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setRobustObjectiveFlux(_biomassID);
		_cc.setPercentageInterval(_robustnessPercentageInterval);
		_cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
		 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR1);
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_6_2_CPLEX_ROBUSTNESS_WT_ENV2_FM1_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions2);

		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);		
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV2_FM1_FR1);		 
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_6_3_CPLEX_ROBUSSTNESS_RK_ENV1_FM1_FR0() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setEnvironmentalConditions(_envConditions1);
		_cc.setGeneticConditions(_genConditions);
		_cc.setFluxRatios(null);
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(RK_ENV1_FM1_FR0);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_6_4_CPLEX_ROBUSTNESS_WT_ENV1_FM2_FR1() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setGeneticConditions(null);
		_cc.setFluxRatios(_fluxRatios1);
		_cc.setMeasuredFluxes(_measuredFluxes2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM2_FR1);		 		
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
	@Test
	public void test_3_6_5_CPLEX_ROBUSTNESS_WT_ENV1_FM1_FR2() throws Exception {
		Assume.assumeTrue(_solvers.containsKey("CPLEX"));
		Assume.assumeTrue(_methods.containsKey(MFAProperties.MFA_ROBUSTNESSANALYSIS));
		_cc.setMeasuredFluxes(_measuredFluxes1);
		_cc.setFluxRatios(_fluxRatios2);
		
		MFARobustnessResult result =  (MFARobustnessResult) _cc.simulate();
 		
		double[] actuals = getFluxObjectiveValuesConcat(result.getFluxObjectiveValues(),_fluxesToAnalyzeRobustness);			
		double[] expected = _results.get("CPLEX", MFAProperties.MFA_ROBUSTNESSANALYSIS).get(WT_ENV1_FM1_FR2);		 			 				
		
		if (_debug) {
			String method = Thread.currentThread().getStackTrace()[1].toString();
			Matcher m = _methodPattern.matcher(method);
			if(m.matches())
				System.out.println("\n======[ "+m.group(1)+" ]=====================================================================================================================================");
			System.out.println("Expected=\t" + Arrays.toString(expected));
			System.out.println("Actuals=\t" + Arrays.toString(actuals));
			MapUtils.prettyPrint(_cc.getMethodPropertiesMap());
		}
		
		assertArrayEquals(expected, actuals, DELTA_SOLVER);
	}
	
		
	
	/**
	 * METHOD FOR RESULT GENERATION
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		populate();
		
		for (String solver : _solvers.keySet()) {			
			for (String method : _methods.keySet()) {								
				switch (method) {
					case MFAProperties.MFA_LP:
						doLPQP(solver, method);
						break;
					case MFAProperties.MFA_PARSIMONIOUS:
						doLPQP(solver, method);
						break;
					case MFAProperties.MFA_QP:
						doLPQP(solver, method);
						break;
					case MFAProperties.MFA_FVA:
						doFVA(solver, method);
						break;
					case MFAProperties.MFA_TIGHTBOUMDS:
						doTightBounds(solver,method);
						break;
					case MFAProperties.MFA_ROBUSTNESSANALYSIS:
						doRobustness(solver,method);
						break;					
					default:
						break;
				}	
			}
		}
	}
	
	
	
	private static void doRobustness(String solver, String method) throws Exception {
		StringBuilder sb = new StringBuilder();
		String mapName = "map_" + solver + "_" + method;
		sb.append("HashMap<String,double[]> " + mapName + "  = new HashMap<String,double[]>();\n");
		
		MFAControlCenter cc = null;
		MFARobustnessResult res = null;						
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setRobustObjectiveFlux(_biomassID);
		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		try {
			res = (MFARobustnessResult) cc.simulate();
			Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",new double[]{" + getFluxObjectiveValues(fluxBounds,_fluxesToAnalyzeRobustness)+"});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",null);\n");
		}
		
			
		
		
		/***************************************************
		 * 					WT_ENV2_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV2_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions2, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setRobustObjectiveFlux(_biomassID);
		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		try {
			res =  (MFARobustnessResult) cc.simulate();
			Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
			
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",new double[]{" + getFluxObjectiveValues(fluxBounds,_fluxesToAnalyzeRobustness)+"});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					RK_ENV1_FM1_FR0
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] RK_ENV1_FM1_FR0 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, _genConditions, _model, _measuredFluxes1, null, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setRobustObjectiveFlux(_biomassID);
		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		try {
			res =  (MFARobustnessResult) cc.simulate();
			Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
			
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",new double[]{" + getFluxObjectiveValues(fluxBounds,_fluxesToAnalyzeRobustness)+"});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM2_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM2_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes2, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setRobustObjectiveFlux(_biomassID);
		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		try {
			res =  (MFARobustnessResult) cc.simulate();
			Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
			
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",new double[]{" + getFluxObjectiveValues(fluxBounds,_fluxesToAnalyzeRobustness)+"});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR2
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR2 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios2, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setRobustObjectiveFlux(_biomassID);
		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyzeRobustness));
		try {
			res =  (MFARobustnessResult) cc.simulate();
			Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",new double[]{" + getFluxObjectiveValues(fluxBounds,_fluxesToAnalyzeRobustness)+"});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",null);\n");
		}
		
		sb.append("_results.put(\"" + _solvers.get(solver) + "\",\"" + method + "\"," + mapName + ");\n");
		System.out.println(sb.toString() + "\n");
	}



	private static void doTightBounds(String solver, String method) throws Exception {
		StringBuilder sb = new StringBuilder();
		String mapName = "map_" + solver + "_" + method;
		sb.append("HashMap<String,double[]> " + mapName + "  = new HashMap<String,double[]>();\n");
		
		MFAControlCenter cc = null;
		MFATightBoundsResult res = null;						
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFATightBoundsResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double bio = fluxBounds.get(_biomassID).getLowerLimit();
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",new double[]{" + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",null);\n");
		}
		
			
		
		
		/***************************************************
		 * 					WT_ENV2_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV2_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions2, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res =  (MFATightBoundsResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",new double[]{" + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					RK_ENV1_FM1_FR0
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] RK_ENV1_FM1_FR0 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, _genConditions, _model, _measuredFluxes1, null, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res =  (MFATightBoundsResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",new double[]{" + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM2_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM2_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes2, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res =  (MFATightBoundsResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",new double[]{" + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR2
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR2 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios2, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFATightBoundsResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",new double[]{" + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",null);\n");
		}
		
		sb.append("_results.put(\"" + _solvers.get(solver) + "\",\"" + method + "\"," + mapName + ");\n");
		System.out.println(sb.toString() + "\n");
	}



	/**
	 * COMPUTE FVA CASE STUDIES
	 * 
	 * @param solver
	 * @param method
	 * @throws Exception
	 */
	public static void doFVA(String solver, String method) throws Exception{
		StringBuilder sb = new StringBuilder();
		String mapName = "map_" + solver + "_" + method;
		sb.append("HashMap<String,double[]> " + mapName + "  = new HashMap<String,double[]>();\n");
		
		MFAControlCenter cc = null;
		MFAFvaResult res = null;						
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFAFvaResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double of = res.getObjectiveFluxValue();
			double bio = fluxBounds.get(_biomassID).getLowerLimit();
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",new double[]{" + of + "," + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",null);\n");
		}
		
			
		
		
		
		/***************************************************
		 * 					WT_ENV2_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV2_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions2, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFAFvaResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double of = res.getObjectiveFluxValue();
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",new double[]{" + of + "," + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					RK_ENV1_FM1_FR0
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] RK_ENV1_FM1_FR0 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, _genConditions, _model, _measuredFluxes1, null, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFAFvaResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double of = res.getObjectiveFluxValue();
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",new double[]{" + of + "," + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM2_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM2_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes2, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFAFvaResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double of = res.getObjectiveFluxValue();
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",new double[]{" + of + "," + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR2
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR2 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios2, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		cc.setMinimumPercentage(_fvaMinPercentage);
		cc.setMinimumPercentageFlux(_fvaMinFlux);
		try {
			res = (MFAFvaResult) cc.simulate();
			Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds(); 
			double of = res.getObjectiveFluxValue();
			double bio = fluxBounds.get(_biomassID).getLowerLimit(); 
			
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",new double[]{" + of + "," + bio + "," + getLowerUpperString(fluxBounds)+ "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",null);\n");
		}
		
		sb.append("_results.put(\"" + _solvers.get(solver) + "\",\"" + method + "\"," + mapName + ");\n");
		System.out.println(sb.toString() + "\n");
	}
	
	
	
	
	
	
	
	/**
	 * COMPUTE LP, PARSIMONIOUS AND QP CASE STUDIES 
	 * 
	 * @param solver
	 * @param method
	 * @throws Exception
	 */
	public static void doLPQP(String solver, String method) throws Exception{
		
		StringBuilder sb = new StringBuilder();
		String mapName = "map_" + solver + "_" + method;
		sb.append("HashMap<String,double[]> " + mapName + "  = new HashMap<String,double[]>();\n");
		
		MFAControlCenter cc = null;
		SteadyStateSimulationResult res = null;						
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		try {
			res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(_biomassID);
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",new double[]{" + of + "," + bio + "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR1 + ",null);\n");
		}
		
			
		
		
		
		/***************************************************
		 * 					WT_ENV2_FM1_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV2_FM1_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions2, null, _model, _measuredFluxes1, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		try {
			res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(_biomassID);
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",new double[]{" + of + "," + bio + "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV2_FM1_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					RK_ENV1_FM1_FR0
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] RK_ENV1_FM1_FR0 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, _genConditions, _model, _measuredFluxes1, null, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		try {
			res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(_biomassID);
			double targ = res.getFluxValues().getValue(_targetID);
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",new double[]{" + of + "," + bio + "," + targ + "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + RK_ENV1_FM1_FR0 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM2_FR1
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM2_FR1 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes2, _fluxRatios1, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		try {
			res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(_biomassID);
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",new double[]{" + of + "," + bio + "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM2_FR1 + ",null);\n");
		}
		
		
		
		
		/***************************************************
		 * 					WT_ENV1_FM1_FR2
		 **************************************************/
		if (_debug) System.out.println("\n[" + solver + "][" + method + "] WT_ENV1_FM1_FR2 =====================================================================================================================================\n");
		cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios2, _methods.get(method), MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(_solvers.get(solver));
		try {
			res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(_biomassID);
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",new double[]{" + of + "," + bio + "});\n");
		} catch (UnsupportedOperationException e) {
			sb.append(mapName + ".put(" + WT_ENV1_FM1_FR2 + ",null);\n");
		}
		
		sb.append("_results.put(\"" + _solvers.get(solver) + "\",\"" + method + "\"," + mapName + ");\n");
		System.out.println(sb.toString() + "\n");
	}
	
	public static double[] getLowerUpper(Map<String,ReactionConstraint> fluxBounds){
		double [] allFluxes = null;
		for(int i=0;i<_fluxesToAnalyze.length;i++){
			double[] ibounds = new double[2]; 
			ibounds[0] = fluxBounds.get(_fluxesToAnalyze[i]).getLowerLimit();
			ibounds[1] = fluxBounds.get(_fluxesToAnalyze[i]).getUpperLimit();
			allFluxes = concatDoubleArrays(allFluxes, ibounds);
		}
		return allFluxes;
	}
	
	public static String getLowerUpperString(Map<String,ReactionConstraint> fluxBounds){		
		String ret = "";
		for(int i=0;i<_fluxesToAnalyze.length;i++){
			String lower = Double.toString(fluxBounds.get(_fluxesToAnalyze[i]).getLowerLimit());
			String upper = Double.toString(fluxBounds.get(_fluxesToAnalyze[i]).getUpperLimit());
			ret+= (lower+","+upper);
			if(i<_fluxesToAnalyze.length-1)
				ret+=",";
		}
		return ret;
	}
	
	public static String getFluxObjectiveValues(Map<String,double[]> fluxObjectives,String [] fluxesToAnalyze){
		return StringUtils.concat(",", getFluxObjectiveValuesConcat(fluxObjectives,fluxesToAnalyze));
	}
	
	public static double[] getFluxObjectiveValuesConcat(Map<String,double[]> fluxObjectives,String[] fluxesToAnalyze){
		double[] fluxesAllReactions = null;
		for(int i=0; i< fluxesToAnalyze.length; i++){
			String r = fluxesToAnalyze[i];
			double[] fluxes= fluxObjectives.get(r);
			fluxesAllReactions = concatDoubleArrays(fluxesAllReactions, fluxes);
		}
		return fluxesAllReactions;
	}
	
	public static double[] concatDoubleArrays(double[] arr1, double[] arr2){
		if(arr1==null)
			return arr2;
		if(arr2==null)
			return arr1;
		
		double[] toret = new double[arr1.length+arr2.length];
		for(int i=0; i<arr1.length; i++){
			toret[i] = arr1[i];
		}
		for(int i=0; i<arr2.length;i++){
			toret[arr1.length+i] = arr2[i];
		}
		
		return toret;
	}
}
