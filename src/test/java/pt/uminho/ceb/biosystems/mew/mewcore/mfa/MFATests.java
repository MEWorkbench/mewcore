package pt.uminho.ceb.biosystems.mew.mewcore.mfa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFAControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions.InvalidExpressionException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.fluxratios.grammar.ValidateFluxRatios;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAApproaches;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.robustness.MFARobustnessResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.variability.MFAFvaResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.variability.MFATightBoundsResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties.MFASystemType;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.MathmlRatiosParser;

public class MFATests {
	
	protected ISteadyStateModel model;
//	private static SolverType solver = SolverType.GLPK;
//	private static SolverType solver = SolverType.CLP;
	private static SolverType solver = SolverType.CPLEX;
	
	private static boolean MAXIMIZATION = true; 
	
	private static ExpMeasuredFluxes measuredFluxes1, measuredFluxesSC, measuredFluxesEC;
	EnvironmentalConditions envCond;
	GeneticConditions geneCond;
	
	private static FluxRatioConstraintList fluxRatiosConstraint1, fluxRatiosConstraintFile;
	private static Map<String, String[]> ratios;
	
	protected static final double TOLERANCE = 1e-5;
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	private final Map<String, Map<String, ReactionConstraint>> boundsMap = Collections.unmodifiableMap(
		    new HashMap<String, Map<String, ReactionConstraint>>() {{
		    	
		    	put("TightBounds", new HashMap<String, ReactionConstraint>() {{
		    		put("R_EX_succ_e", new ReactionConstraint(0.0, 7.999999999999998));
		    		put("R_ADK1", new ReactionConstraint(0.0, 46.61));
		    		put("R_GLUSy", new ReactionConstraint(0.0, 46.61));
		    		put("R_GLUDy", new ReactionConstraint(-6.9476538868294595, 46.61));
		    		put("R_FORt2", new ReactionConstraint(0.0, 186.44));
		    		put("R_FUM", new ReactionConstraint(0.0, 0.0));
		    		put("R_PDH", new ReactionConstraint(0.0, 40.0));
		    		put("R_GLUt2r", new ReactionConstraint(-6.511627906976742, 0.0));
		    		put("R_EX_o2_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_ALCD2x", new ReactionConstraint(-40.0, 0.0));
		    		put("R_ICDHyr", new ReactionConstraint(0.0, 6.511627906976742));
		    		put("R_PYRt2r", new ReactionConstraint(0.0, 0.0));
		    		put("R_EX_for_e", new ReactionConstraint(0.0, 40.0));
		    		put("R_EX_co2_e", new ReactionConstraint(-0.8847802254301684, 40.0));
		    		put("R_EX_h2o_e", new ReactionConstraint(-20.0, 1.862871424667469));
		    		put("R_SUCDi", new ReactionConstraint(0.0, 1000.0));
		    		put("R_EX_fru_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_FRD7", new ReactionConstraint(0.0, 1000.0));
		    		put("R_NADH16", new ReactionConstraint(0.0, 0.0));
		    		put("R_PTAr", new ReactionConstraint(0.0, 20.0));
		    		put("R_TPI", new ReactionConstraint(14.285714285714285, 20.0));
		    		put("R_ATPM", new ReactionConstraint(8.39, 55.0));
		    		put("R_GLNabc", new ReactionConstraint(0.0, 0.0));
		    		put("R_PPCK", new ReactionConstraint(0.0, 18.66666666666666));
		    		put("R_PGI", new ReactionConstraint(2.857142857142858, 20.0));
		    		put("R_Biomass_Ecoli_core_w_GAM", new ReactionConstraint(0.0, 0.4952035738681193));
		    		put("R_EX_nh4_e", new ReactionConstraint(-7.0064676288824135, 0.0));
		    		put("R_PGK", new ReactionConstraint(-40.0, -34.285714285714285));
		    		put("R_MALt2_2", new ReactionConstraint(0.0, 0.0));
		    		put("R_PGM", new ReactionConstraint(-40.0, -33.85193176719225));
		    		put("R_ACALDt", new ReactionConstraint(-40.0, 0.0));
		    		put("R_PGL", new ReactionConstraint(0.0, 17.142857142857142));
		    		put("R_H2Ot", new ReactionConstraint(-1.862871424667469, 20.0));
		    		put("R_EX_acald_e", new ReactionConstraint(0.0, 40.0));
		    		put("R_ATPS4r", new ReactionConstraint(-51.61, 10.0));
		    		put("R_EX_fum_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_EX_pi_e", new ReactionConstraint(-1.821705387188651, 0.0));
		    		put("R_EX_glu_L_e", new ReactionConstraint(0.0, 6.511627906976742));
		    		put("R_ETOHt2r", new ReactionConstraint(-40.0, 0.0));
		    		put("R_SUCOAS", new ReactionConstraint(-4.912280701754386, 0.0));
		    		put("R_GAPD", new ReactionConstraint(34.285714285714285, 40.0));
		    		put("R_FUMt2_2", new ReactionConstraint(0.0, 0.0));
		    		put("R_CO2t", new ReactionConstraint(-40.0, 0.8847802254301684));
		    		put("R_G6PDH2r", new ReactionConstraint(0.0, 17.142857142857142));
		    		put("R_ICL", new ReactionConstraint(0.0, 7.999999999999998));
		    		put("R_O2t", new ReactionConstraint(0.0, 0.0));
		    		put("R_EX_gln_L_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_FORti", new ReactionConstraint(0.0, 226.44));
		    		put("R_ACONTa", new ReactionConstraint(0.0, 9.302325581395348));
		    		put("R_FRUpts2", new ReactionConstraint(0.0, 0.0));
		    		put("R_FBA", new ReactionConstraint(14.285714285714285, 20.0));
		    		put("R_ACONTb", new ReactionConstraint(0.0, 9.302325581395348));
		    		put("R_AKGDH", new ReactionConstraint(0.0, 4.912280701754386));
		    		put("R_PPC", new ReactionConstraint(0.0, 18.66666666666666));
		    		put("R_NADTRHD", new ReactionConstraint(0.0, 93.22));
		    		put("R_EX_etoh_e", new ReactionConstraint(0.0, 40.0));
		    		put("R_PFL", new ReactionConstraint(0.0, 40.0));
		    		put("R_PFK", new ReactionConstraint(14.285714285714286, 66.61));
		    		put("R_ACt2r", new ReactionConstraint(-20.0, 0.0));
		    		put("R_GLUN", new ReactionConstraint(0.0, 46.61));
		    		put("R_TKT1", new ReactionConstraint(-0.08859191936500654, 5.7142857142857135));
		    		put("R_TKT2", new ReactionConstraint(-0.2673604095313978, 5.7142857142857135));
		    		put("R_EX_ac_e", new ReactionConstraint(0.0, 20.0));
		    		put("R_GLCpts", new ReactionConstraint(20.0, 20.0));
		    		put("R_SUCCt2_2", new ReactionConstraint(0.0, 62.146666666666675));
		    		put("R_SUCCt3", new ReactionConstraint(0.0, 62.146666666666675));
		    		put("R_GLNS", new ReactionConstraint(0.0, 46.61));
		    		put("R_EX_h_e", new ReactionConstraint(0.0, 60.99255774611417));
		    		put("R_EX_lac_D_e", new ReactionConstraint(0.0, 40.0));
		    		put("R_D_LACt2", new ReactionConstraint(-40.0, 0.0));
		    		put("R_PIt2r", new ReactionConstraint(0.0, 1.821705387188651));
		    		put("R_GND", new ReactionConstraint(0.0, 17.142857142857142));
		    		put("R_ENO", new ReactionConstraint(33.85193176719225, 40.0));
		    		put("R_ME1", new ReactionConstraint(0.0, 1.1428571428571404));
		    		put("R_ME2", new ReactionConstraint(0.0, 1.6326530612244867));
		    		put("R_CS", new ReactionConstraint(0.0, 9.302325581395348));
		    		put("R_NH4t", new ReactionConstraint(0.0, 7.0064676288824135));
		    		put("R_EX_akg_e", new ReactionConstraint(0.0, 5.6));
		    		put("R_LDH_D", new ReactionConstraint(-40.0, 0.0));
		    		put("R_MDH", new ReactionConstraint(0.0, 8.000000000000002));
		    		put("R_THD2", new ReactionConstraint(0.0, 93.22));
		    		put("R_AKGt2r", new ReactionConstraint(-5.6, 0.0));
		    		put("R_ACKr", new ReactionConstraint(-20.0, 0.0));
		    		put("R_CYTBD", new ReactionConstraint(0.0, 0.0));
		    		put("R_ACALD", new ReactionConstraint(-40.0, 0.0));
		    		put("R_MALS", new ReactionConstraint(0.0, 7.999999999999998));
		    		put("R_RPI", new ReactionConstraint(-5.7142857142857135, 0.0));
		    		put("R_PPS", new ReactionConstraint(0.0, 46.61));
		    		put("R_PYK", new ReactionConstraint(12.44380679709657, 66.61));
		    		put("R_RPE", new ReactionConstraint(-0.3559523288964042, 11.428571428571427));
		    		put("R_TALA", new ReactionConstraint(-0.08859191936500654, 5.7142857142857135));
		    		put("R_FBP", new ReactionConstraint(0.0, 46.61));
		    	}});
		    	
		    	put("MFVA", new HashMap<String, ReactionConstraint>() {{
		    		put("R_EX_succ_e", new ReactionConstraint(0.46462841854769843, 0.5105983948083506));
		    		put("R_ADK1", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_GLUSy", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_GLUDy", new ReactionConstraint(-3.8135108811850476, 1.7816512274181662));
		    		put("R_FORt2", new ReactionConstraint(0.0, 16.78548632580967));
		    		put("R_FUM", new ReactionConstraint(-0.5105983948083506, 0.0));
		    		put("R_PDH", new ReactionConstraint(0.0, 33.43034001310296));
		    		put("R_GLUt2r", new ReactionConstraint(-1.3987905271508012, 0.0));
		    		put("R_EX_glc_e", new ReactionConstraint(-20.0, -18.47404669765367));
		    		put("R_EX_o2_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_ALCD2x", new ReactionConstraint(-32.473716562155104, -10.163384447000439));
		    		put("R_EX_mal_L_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_ICDHyr", new ReactionConstraint(0.5012876007711118, 2.1427062276645295));
		    		put("R_PYRt2r", new ReactionConstraint(-5.595162108603233, 0.0));
		    		put("R_EX_for_e", new ReactionConstraint(1.2859985368563192, 35.18096696850698));
		    		put("R_EX_co2_e", new ReactionConstraint(-1.4228845468124303, 32.13555999913609));
		    		put("R_EX_pyr_e", new ReactionConstraint(0.0, 5.595162108603233));
		    		put("R_EX_h2o_e", new ReactionConstraint(-13.436473108227796, 3.512495040081477));
		    		put("R_SUCDi", new ReactionConstraint(0.0, 1000.0));
		    		put("R_EX_fru_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_FRD7", new ReactionConstraint(0.0, 1000.0));
		    		put("R_NADH16", new ReactionConstraint(0.0, 0.5105983948083506));
		    		put("R_PTAr", new ReactionConstraint(0.0, 16.71517000655148));
		    		put("R_TPI", new ReactionConstraint(16.985883760001304, 19.537834112070605));
		    		put("R_PPCK", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_GLNabc", new ReactionConstraint(0.0, 0.0));
		    		put("R_ATPM", new ReactionConstraint(8.39, 12.586371581452418));
		    		put("R_PGI", new ReactionConstraint(13.135382143784401, 19.90475117419772));
		    		put("R_Biomass_Ecoli_core_w_GAM", new ReactionConstraint(0.46462841854769843, 0.5105983948083506));
		    		put("R_EX_nh4_e", new ReactionConstraint(-3.932316367807694, -2.53352584065689));
		    		put("R_PGK", new ReactionConstraint(-38.764878274974656, -34.988738458712845));
		    		put("R_MALt2_2", new ReactionConstraint(0.0, 0.0));
		    		put("R_PGM", new ReactionConstraint(-38.0697941608273, -34.29365434456548));
		    		put("R_ACALDt", new ReactionConstraint(-6.71419453032388, 0.0));
		    		put("R_PGL", new ReactionConstraint(0.0, 6.769369030413323));
		    		put("R_H2Ot", new ReactionConstraint(-3.512495040081477, 13.436473108227796));
		    		put("R_EX_acald_e", new ReactionConstraint(0.0, 6.71419453032388));
		    		put("R_ATPS4r", new ReactionConstraint(-15.676520903516241, 2.90413199365625));
		    		put("R_EX_fum_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_EX_pi_e", new ReactionConstraint(-1.8783383149814572, -1.7092285633114173));
		    		put("R_EX_glu_L_e", new ReactionConstraint(0.0, 1.3987905271508012));
		    		put("R_ETOHt2r", new ReactionConstraint(-32.473716562155104, -10.163384447000439));
		    		put("R_SUCOAS", new ReactionConstraint(-0.5009939802257317, 0.0));
		    		put("R_GAPD", new ReactionConstraint(34.988738458712845, 38.764878274974656));
		    		put("R_FUMt2_2", new ReactionConstraint(0.0, 0.0));
		    		put("R_CO2t", new ReactionConstraint(-32.13555999913609, 1.4228845468124303));
		    		put("R_G6PDH2r", new ReactionConstraint(0.0, 6.769369030413323));
		    		put("R_ICL", new ReactionConstraint(0.0, 0.4989827111750826));
		    		put("R_EX_gln_L_e", new ReactionConstraint(0.0, 0.0));
		    		put("R_O2t", new ReactionConstraint(0.0, 0.0));
		    		put("R_FORti", new ReactionConstraint(1.2859985368563192, 51.50182487576896));
		    		put("R_ACONTa", new ReactionConstraint(0.5012876007711118, 2.1427062276645295));
		    		put("R_FRUpts2", new ReactionConstraint(0.0, 0.0));
		    		put("R_FBA", new ReactionConstraint(16.985883760001304, 19.537834112070605));
		    		put("R_ACONTb", new ReactionConstraint(0.5012876007711118, 2.1427062276645295));
		    		put("R_AKGDH", new ReactionConstraint(0.0, 0.5009939802257317));
		    		put("R_PPC", new ReactionConstraint(1.3314391961902845, 9.975721859820752));
		    		put("R_NADTRHD", new ReactionConstraint(0.0, 8.392743162904836));
		    		put("R_EX_etoh_e", new ReactionConstraint(10.163384447000439, 32.473716562155104));
		    		put("R_PFL", new ReactionConstraint(1.2859985368563192, 35.18096696850698));
		    		put("R_PFK", new ReactionConstraint(16.985883760001304, 23.734205693523016));
		    		put("R_ACt2r", new ReactionConstraint(-16.71517000655148, 0.0));
		    		put("R_GLUN", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_TKT1", new ReactionConstraint(-0.091346052831214, 2.173334319392934));
		    		put("R_TKT2", new ReactionConstraint(-0.275672073357028, 2.0056034602972144));
		    		put("R_EX_ac_e", new ReactionConstraint(0.0, 16.71517000655148));
		    		put("R_GLCpts", new ReactionConstraint(18.47404669765367, 20.0));
		    		put("R_SUCCt2_2", new ReactionConstraint(0.0, 5.595162108603233));
		    		put("R_SUCCt3", new ReactionConstraint(0.46462841854769843, 6.059790527150938));
		    		put("R_GLNS", new ReactionConstraint(0.11880548662264648, 4.315177068075066));
		    		put("R_EX_h_e", new ReactionConstraint(11.535701450018548, 61.847549231774494));
		    		put("R_EX_lac_D_e", new ReactionConstraint(0.0, 11.190324217206381));
		    		put("R_D_LACt2", new ReactionConstraint(-11.190324217206381, 0.0));
		    		put("R_PIt2r", new ReactionConstraint(1.7092285633114173, 1.8783383149814572));
		    		put("R_GND", new ReactionConstraint(0.0, 6.769369030413323));
		    		put("R_ENO", new ReactionConstraint(34.29365434456548, 38.0697941608273));
		    		put("R_ME1", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_ME2", new ReactionConstraint(0.0, 8.179654245082766));
		    		put("R_CS", new ReactionConstraint(0.5012876007711118, 2.1427062276645295));
		    		put("R_NH4t", new ReactionConstraint(2.53352584065689, 3.932316367807694));
		    		put("R_EX_akg_e", new ReactionConstraint(0.0, 1.4596075065921497));
		    		put("R_LDH_D", new ReactionConstraint(-11.190324217206381, 0.0));
		    		put("R_MDH", new ReactionConstraint(-8.644282663630463, 0.4989827111750826));
		    		put("R_THD2", new ReactionConstraint(0.0, 16.35930849016553));
		    		put("R_AKGt2r", new ReactionConstraint(-1.4596075065921497, 0.0));
		    		put("R_ACKr", new ReactionConstraint(-16.71517000655148, 0.0));
		    		put("R_CYTBD", new ReactionConstraint(0.0, 0.0));
		    		put("R_ACALD", new ReactionConstraint(-32.473716562155104, -10.163384447000439));
		    		put("R_MALS", new ReactionConstraint(0.0, 0.4989827111750826));
		    		put("R_RPI", new ReactionConstraint(-2.590431250723203, -0.3339749072520856));
		    		put("R_PPS", new ReactionConstraint(0.0, 4.196371581452418));
		    		put("R_PYK", new ReactionConstraint(7.852883688938441, 20.22890951547361));
		    		put("R_RPE", new ReactionConstraint(-0.3670181261882419, 4.178937779690145));
		    		put("R_TALA", new ReactionConstraint(-0.091346052831214, 2.173334319392934));
		    		put("R_FBP", new ReactionConstraint(0.0, 4.196371581452418));
		    	}});
		    	
		    }});	

	private final Map<String, Double> resultMaps = Collections.unmodifiableMap(
		    new HashMap<String, Double>() {{
		    	
		    	put("FBA", 0.495203573);
		    	put("PFBA", 657.1787403);
		    	put("MFAQP", -334.38007369);
		    	put("MFARobustnessAnalysis", 0.49506793);
		    	put("ReactioKOFBA", 0.45788944);
		    	put("GeneKOFBA", 0.49520357);
		    }});
		    
	
	@Before
	public void setData() throws Exception {
		// input Model
		JSBMLReader reader = new JSBMLReader(getFile("mfa/files/ecoli_core_model.xml"), "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		// EnvCond
		envCond = new EnvironmentalConditions();
		envCond.addReactionConstraint("R_EX_o2_e", new ReactionConstraint(0.0, 1000));
		envCond.addReactionConstraint("R_EX_glc_e", new ReactionConstraint(-20.0, 0));
		
		//
		measuredFluxesEC = new ExpMeasuredFluxes();
		measuredFluxesEC.loadFromFile(new File(getFile("mfa/files/ecMeasurements.csv")), ",", true);
		
		// Measured Fluxes1
		measuredFluxes1 = new ExpMeasuredFluxes();
		measuredFluxes1.put("R_EX_glc_e", -20.0, 0.0);
		measuredFluxes1.put("R_EX_mal_L_e", 0.0, 0.0);
		measuredFluxes1.put("R_EX_pyr_e", 0.0, 0.0);
		
		fluxRatiosConstraintFile = new FluxRatioConstraintList();
		MathmlRatiosParser parser = new MathmlRatiosParser();
		parser.parseRatioExpressions(new File(getFile("mfa/files/scRatiosMathML.xml")));
		fluxRatiosConstraintFile = parser.getRatioConstraints();
		
		// Measured FluxesSC
		measuredFluxesSC = new ExpMeasuredFluxes();
		measuredFluxesSC.loadFromFile(new File(getFile("mfa/files/scMeasurements.csv")), ",", true);
		
		ratios = new HashMap<String, String[]>();
		ratios.put("oaaPep", new String[]{"R_PPC / R_PPC + R_MDH", "OAA from PEP"});
		ratios.put("pepOaa", new String[]{"R_MALS / R_MALS - R_ME1 + R_PPC + R_FUM", "PEP from GLX"});
		
		List<Object[]> rs = new ArrayList<Object[]>();
		rs.add(new Object[]{"oaaPep", 0.7, "="});
		rs.add(new Object[]{"pepOaa", 0.35, "<="});
		confRatios(rs);
	}
	
	private static void confRatios(List<Object[]> ratioValues){

		fluxRatiosConstraint1 = new FluxRatioConstraintList();
		
		for(Object[] rData : ratioValues)
		{
			String[] expDescription = ratios.get((String) rData[0]);
			String exp = expDescription[0] + rData[2] + rData[1];
			Map<String, Double> fluxesCoeffs = null;
			
			try {
				fluxesCoeffs = ValidateFluxRatios.validate(exp);
			} 
			catch (InvalidExpressionException e) {
				e.printStackTrace();
				return;
			}
			
			FluxRatioConstraint ratio = new FluxRatioConstraint(expDescription[1], exp, fluxesCoeffs);
			fluxRatiosConstraint1.add(ratio);
		}
	}
			
	private void printFluxMapAndOF(SteadyStateSimulationResult result)
	{
		MapUtils.prettyPrint(result.getFluxValues());
		
		System.out.println("OFString : " + result.getOFString());
		System.out.println("OFValue : " + result.getOFvalue());
	}
	
	@Test
	public void FBA() throws Exception {
		System.out.println("\n---------------------FBA---------------------");
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, measuredFluxes1, fluxRatiosConstraint1, MFAApproaches.linearProgramming, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setFBAObjSingleFlux(((ISteadyStateModel) model).getBiomassFlux(), 1.0);
		
		SteadyStateSimulationResult result = mfaCC.simulate();
			
		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++FBA++++++++++++++++++++\n");
	}
	
	@Test
	public void PFBA() throws Exception {
		System.out.println("\n---------------------PFBA---------------------");
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, measuredFluxes1, fluxRatiosConstraint1, MFAApproaches.parsimonious, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setFBAObjSingleFlux(((ISteadyStateModel) model).getBiomassFlux(), 1.0);
		
		SteadyStateSimulationResult result = mfaCC.simulate();
			
		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++PFBA++++++++++++++++++++\n");
	}
	
	@Test
	public void TightBounds()  throws Exception{
		System.out.println("\n---------------------Tight Bounds---------------------");
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, measuredFluxes1, fluxRatiosConstraint1, MFAApproaches.tightBounds, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		
		MFATightBoundsResult result = (MFATightBoundsResult) mfaCC.simulate();
			
		//boundsMapGenerator(new Object() {}.getClass().getEnclosingMethod().getName(), result.getFluxBounds());
		boundsValidation(new Object() {}.getClass().getEnclosingMethod().getName(), result.getFluxBounds());
		
		System.out.println("++++++++++++++++++++Tight Bounds++++++++++++++++++++\n");
	}

	@Test
	public void MFVA() throws Exception {
		System.out.println("\n---------------------MFVA---------------------");
		// In the tutorial there were used measured fluxes and flux ratios constraints
		// but then some lower bounds higher than upper bounds
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, null, null, MFAApproaches.fva, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setMinimumPercentage(0.9);
		mfaCC.setMinimumPercentageFlux(((ISteadyStateModel) model).getBiomassFlux());
		MFAFvaResult result = (MFAFvaResult) mfaCC.simulate();
			
		//boundsMapGenerator(new Object() {}.getClass().getEnclosingMethod().getName(), result.getFluxBounds());
		boundsValidation(new Object() {}.getClass().getEnclosingMethod().getName(), result.getFluxBounds());
		
		System.out.println("++++++++++++++++++++MFVA++++++++++++++++++++\n");
	}

	@Test
	public void MFAQP() throws Exception {
		System.out.println("\n---------------------MFAQP---------------------");
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, measuredFluxesEC, fluxRatiosConstraint1, MFAApproaches.quadraticProgramming, MFASystemType.underdetermined);
		
		if(!solver.isQP())
		{
			System.out.println("************************************************************************");
			System.out.println("Choose QP solver, CPLEX will be used instead!");
			mfaCC.setSolver(SolverType.CPLEX);
			System.out.println("************************************************************************");
		}
		else
			mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		
		SteadyStateSimulationResult result = mfaCC.simulate();

		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++MFAQP++++++++++++++++++++\n");
	}
	
	@Test
	public void MFARobustnessAnalysis() throws Exception {
		System.out.println("\n---------------------MFARobustnessAnalysis---------------------");
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, null, model, measuredFluxesEC, fluxRatiosConstraint1, MFAApproaches.robustnessAnalysis, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setFBAObjSingleFlux(((ISteadyStateModel) model).getBiomassFlux(), 1.0);
		
		mfaCC.setSelectedFluxes(Arrays.asList(new String[]{"R_ACONTa", "R_ACONTb", "R_ENO", 
				"R_PFK", "R_PGK", "R_THD2", "R_TKT1", "R_TKT2", "R_TPI"}));
		
		mfaCC.setPercentageInterval(5);
		
		MFARobustnessResult result = (MFARobustnessResult) mfaCC.simulate();
			
		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++MFARobustnessAnalysis++++++++++++++++++++\n");
	}
	
	@Test
	public void ReactioKOFBA() throws Exception {
		System.out.println("\n---------------------ReactioKOFBA---------------------");
		geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList(new String[]{"R_ACKr", "R_Act2r"})));
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, geneCond, model, measuredFluxes1, fluxRatiosConstraint1, MFAApproaches.linearProgramming, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setFBAObjSingleFlux(((ISteadyStateModel) model).getBiomassFlux(), 1.0);
		
		SteadyStateSimulationResult result = mfaCC.simulate();
			
		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++ReactioKOFBA++++++++++++++++++++\n");
	}
	
	@Test
	public void GeneKOFBA() throws Exception {
		System.out.println("\n---------------------GeneKOFBA---------------------");
		GeneChangesList geneChangesListKO = new GeneChangesList();
		geneChangesListKO.put("b0114", 0.0);
		geneChangesListKO.put("b0115", 0.0);
		geneChangesListKO.put("b0116", 0.0);
		geneChangesListKO.put("b3403", 0.0);
		try {
			geneCond = new GeneticConditions(geneChangesListKO, (ISteadyStateGeneReactionModel)model, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		MFAControlCenter mfaCC = new MFAControlCenter(envCond, geneCond, model, measuredFluxes1, fluxRatiosConstraint1, MFAApproaches.linearProgramming, MFASystemType.underdetermined);
		
		mfaCC.setSolver(solver);
		mfaCC.setMaximization(MAXIMIZATION);
		mfaCC.setFBAObjSingleFlux(((ISteadyStateModel) model).getBiomassFlux(), 1.0);
		
		SteadyStateSimulationResult result = mfaCC.simulate();
			
		//printFluxMapAndOF(result);
		
		assertEquals(resultMaps.get(new Object() {}.getClass().getEnclosingMethod().getName()), result.getOFvalue(), TOLERANCE);
		System.out.println("++++++++++++++++++++GeneKOFBA++++++++++++++++++++\n");
	}
	
	private void boundsValidation(String methodName, Map<String, ReactionConstraint> fluxBounds)
	{
		assertEquals("Total number of bounds is incoherent", boundsMap.get(methodName).size(), fluxBounds.size());
		for (String reaction : boundsMap.get(methodName).keySet()) {
			
			if(!fluxBounds.containsKey(reaction))
				assertEquals("Reaction missing", reaction, "");
				
			assertEquals("Lower bound different in " + reaction, fluxBounds.get(reaction).getLowerLimit(), boundsMap.get(methodName).get(reaction).getLowerLimit(), TOLERANCE);
			
			assertEquals("Upper bound different in " + reaction, fluxBounds.get(reaction).getUpperLimit(), boundsMap.get(methodName).get(reaction).getUpperLimit(), TOLERANCE);	
		}
	}
	
	// Generate a Bounds Map by method and add to the boundsMap for Thight Bounds and MFVA
	private void boundsMapGenerator(String methodName, Map<String, ReactionConstraint> boundsMap) {
		
		StringBuilder sb = new StringBuilder();
		//String mapName = "map_" + solver + "_" + methodName;
		//sb.append("HashMap<String,ReactionConstraint> " + mapName + "  = new HashMap<String,ReactionConstraint>();\n");
		
		sb.append("put(\"" + methodName + "\", new HashMap<String, ReactionConstraint>() {{\n");
		for (String reactionName : boundsMap.keySet())
			sb.append("\tput(\"" + reactionName + "\", new ReactionConstraint("+boundsMap.get(reactionName).getLowerLimit()+", "+boundsMap.get(reactionName).getUpperLimit()+"));\n");
		sb.append("}});\n");
		
		System.out.println(sb);
		
	}
}

