package pt.uminho.ceb.biosystems.mew.core.mew.simulation.mfa;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.MFAControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAApproaches;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability.MFATightBoundsResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFASystemType;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

public class TestPersistence {
	
	protected static final DecimalFormat						df	= new DecimalFormat("0.00000");
	
	public static void main(String[] args) throws Exception {
		String _modelConfFile = "files/mfafiles/ecoli_core.conf";
		String _measuredFluxesFile1 = "files/mfafiles/ecMeasurements1.csv";
		String _measuredFluxesFile2 = "files/mfafiles/ecMeasurements2.csv";
		String _fluxRatiosFile1 = "files/mfafiles/ecRatios1.txt";
		String _fluxRatiosFile2 = "files/mfafiles/ecRatios2.txt";
		String _envConditionsFile1 = "files/mfafiles/ecEnvConditions1.env";
		String _envConditionsFile2 = "files/mfafiles/ecEnvConditions2.env";
		
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
		ISteadyStateModel _model = modelConf.getModel();
		String _biomassID = modelConf.getModelBiomass();
		
		/**
		 * ENVIRONMENTAL CONDITIONS
		 */
		EnvironmentalConditions _envConditions1 = EnvironmentalConditions.readFromFile(_envConditionsFile1, ",");
		EnvironmentalConditions _envConditions2 = EnvironmentalConditions.readFromFile(_envConditionsFile2, ",");
		
		/**
		 * EXPERIMENTALLY MEASURED FLUXES
		 */
		ExpMeasuredFluxes _measuredFluxes1 = new ExpMeasuredFluxes();
		_measuredFluxes1.loadFromFile(new File(_measuredFluxesFile1), Delimiter.COMMA.toString(), false);
		
		ExpMeasuredFluxes _measuredFluxes2 = new ExpMeasuredFluxes();
		_measuredFluxes2.loadFromFile(new File(_measuredFluxesFile2), Delimiter.COMMA.toString(), false);
		
		/**
		 * FLUX RATIOS
		 */
		FluxRatioConstraintList _fluxRatios1 = (_fluxRatiosFile1 != null) ? MFATestUtil.getFluxRatiosFromFile(_fluxRatiosFile1) : null;
		FluxRatioConstraintList _fluxRatios2 = (_fluxRatiosFile2 != null) ? MFATestUtil.getFluxRatiosFromFile(_fluxRatiosFile2) : null;
		
		/**
		 * GENETIC CONDITIONS
		 */
		GeneticConditions _genConditions = new GeneticConditions(new ReactionChangesList(new String[] { "R_PTAr", "R_ALCD2x" }));
		
		String _targetID = "R_EX_lac_D_e";
		
		Double _fvaMinPercentage = 0.7;
		
		String[] _fluxesToAnalyze = new String[] {"R_EX_succ_e", "R_EX_pi_e", "R_EX_o2_e", "R_EX_nh4_e", "R_EX_lac_D_e", "R_EX_h2o_e", "R_EX_h_e", "R_EX_glu_L_e", "R_EX_gln_L_e", "R_EX_fum_e", "R_EX_fru_e", "R_EX_for_e", "R_EX_etoh_e", "R_EX_co2_e",
				"R_EX_akg_e", "R_EX_acald_e", "R_EX_ac_e" };
		
//		String[] _fluxesToAnalyze = new String[] { "R_ACALD", "R_ACKr", "R_ACONTa", "R_ACONTb", "R_ACt2r", "R_ALCD2x", "R_ATPM", "R_ATPS4r", "R_Biomass_Ecoli_core_w_GAM", "R_CO2t", "R_CS", "R_ENO", "R_ETOHt2r", "R_FBA", "R_FORti", "R_GAPD",
//		"R_GLCpts", "R_GLNS", "R_GLUDy", "R_H2Ot", "R_ICDHyr", "R_ICL", "R_MALS", "R_MDH", "R_NH4t", "R_PFK", "R_PFL", "R_PGI", "R_PGK", "R_PGM", "R_PIt2r", "R_PPC", "R_PTAr", "R_PYK", "R_RPE", "R_RPI", "R_SUCCt3", "R_TALA", "R_THD2",
//		"R_TKT1", "R_TKT2", "R_TPI" };
		
		Integer _robustnessPercentageInterval = 5;
		
		MFAControlCenter cc = new MFAControlCenter(null, null, _model, null, null, MFAApproaches.tightBounds, MFASystemType.underdetermined);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_biomassID, 1.0);
		cc.setSolver(SolverType.GLPK);
		cc.setRobustObjectiveFlux(_biomassID);
//		cc.setPercentageInterval(_robustnessPercentageInterval);
		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyze));
		
		cc.setMeasuredFluxes(_measuredFluxes1);
		cc.setFluxRatios(_fluxRatios1);
		cc.setEnvironmentalConditions(_envConditions1);
		MFATightBoundsResult res = (MFATightBoundsResult) cc.simulate();
		Map<String,ReactionConstraint> fluxBounds = res.getFluxBounds();
		System.out.println("\nstep 1");
		printSelected(fluxBounds, _fluxesToAnalyze);
		
		
		cc.setEnvironmentalConditions(_envConditions2);
		res = (MFATightBoundsResult) cc.simulate();
		fluxBounds = res.getFluxBounds();
		System.out.println("\nstep 2");
		printSelected(fluxBounds, _fluxesToAnalyze);
		
		cc.setEnvironmentalConditions(_envConditions1);
		cc.setGeneticConditions(_genConditions);
		cc.setFluxRatios(null);
		res = (MFATightBoundsResult) cc.simulate();
		fluxBounds = res.getFluxBounds();
		System.out.println("\nstep 3");
		printSelected(fluxBounds, _fluxesToAnalyze);
		
		cc.setGeneticConditions(null);
		cc.setMeasuredFluxes(_measuredFluxes2);
		cc.setFluxRatios(_fluxRatios1);
		res = (MFATightBoundsResult) cc.simulate();
		fluxBounds = res.getFluxBounds();
		System.out.println("\nstep 4");
		printSelected(fluxBounds, _fluxesToAnalyze);
		
		cc.setMeasuredFluxes(_measuredFluxes1);
		cc.setFluxRatios(_fluxRatios2);
		res = (MFATightBoundsResult) cc.simulate();
		fluxBounds = res.getFluxBounds();
		System.out.println("\nstep 5");
		printSelected(fluxBounds, _fluxesToAnalyze);

		
//		MFAControlCenter cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, MFAApproaches.robustnessAnalysis, MFASystemType.underdetermined);
//		cc.setMaximization(true);
//		cc.setFBAObjSingleFlux(_biomassID, 1.0);
//		cc.setSolver(SolverType.CPLEX3);
//		cc.setRobustObjectiveFlux(_biomassID);
//		cc.setPercentageInterval(_robustnessPercentageInterval);
//		cc.setSelectedFluxes(Arrays.asList(_fluxesToAnalyze));
//				
//		
//		cc.setMeasuredFluxes(_measuredFluxes1);
//		cc.setFluxRatios(_fluxRatios1);
//		cc.setEnvironmentalConditions(_envConditions1);
//		MFARobustnessResult res = (MFARobustnessResult) cc.simulate();
//		Map<String,double[]> fluxBounds = res.getFluxObjectiveValues();
//		System.out.println("\nstep 1");
//		printRobustnessLong(fluxBounds);

		
//		cc.setEnvironmentalConditions(_envConditions2);
//		res = (MFARobustnessResult) cc.simulate();
//		fluxBounds = res.getFluxObjectiveValues();//		
//		System.out.println("\nstep 2");
//		printRobustness(fluxBounds);
//
//		
//		cc.setEnvironmentalConditions(_envConditions1);
//		cc.setGeneticConditions(_genConditions);
//		cc.setFluxRatios(null);
//		res = (MFARobustnessResult) cc.simulate();
//		fluxBounds = res.getFluxObjectiveValues();
//		System.out.println("\nstep 3");
//		printRobustness(fluxBounds);
//
//		
//		cc.setGeneticConditions(null);
//		cc.setMeasuredFluxes(_measuredFluxes2);
//		cc.setFluxRatios(_fluxRatios1);
//		res = (MFARobustnessResult) cc.simulate();
//		fluxBounds = res.getFluxObjectiveValues();
//		System.out.println("\nstep 4");
//		printRobustness(fluxBounds);
//
//		
//		cc.setMeasuredFluxes(_measuredFluxes1);
//		cc.setFluxRatios(_fluxRatios2);
//		res = (MFARobustnessResult) cc.simulate();
//		fluxBounds = res.getFluxObjectiveValues();
//		System.out.println("\nstep 5");
//		printRobustness(fluxBounds);
		
		
		
		
//		MFAControlCenter cc = new MFAControlCenter(_envConditions1, null, _model, _measuredFluxes1, _fluxRatios1, MFAApproaches.parsimonious, MFASystemType.underdetermined);
//		cc.setMaximization(true);
//		cc.setFBAObjSingleFlux(_biomassID, 1.0);
//		cc.setSolver(SolverType.CPLEX3);
//				
//		SteadyStateSimulationResult res = cc.simulate();
//		double of = res.getOFvalue();
//		double bio = res.getFluxValues().getValue(_biomassID);
//		System.out.println("\nstep 1\t"+of+"\t"+bio);
//				
//		
//		cc.setEnvironmentalConditions(_envConditions2);
//		res = cc.simulate();	
//		System.out.println("\nstep 2\t"+res.getOFvalue()+"\t"+res.getFluxValues().get(_biomassID));
//
//		
//		cc.setEnvironmentalConditions(_envConditions1);
//		cc.setGeneticConditions(_genConditions);
//		cc.setFluxRatios(null);
////		cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/mfafiles/persistent.mps");
//		res = cc.simulate();
//		System.out.println("\nstep 3\t"+res.getOFvalue()+"\t"+res.getFluxValues().get(_biomassID)+"\t"+res.getFluxValues().get(_targetID));
//
//		
//		cc.setGeneticConditions(null);
//		cc.setMeasuredFluxes(_measuredFluxes2);
//		cc.setFluxRatios(_fluxRatios1);
//		res = cc.simulate();
//		System.out.println("\nstep 4\t"+res.getOFvalue()+"\t"+res.getFluxValues().get(_biomassID));
//
//		
//		cc.setMeasuredFluxes(_measuredFluxes1);
//		cc.setFluxRatios(_fluxRatios2);
//		res =  cc.simulate();
//		System.out.println("\nstep 5\t"+res.getOFvalue()+"\t"+res.getFluxValues().get(_biomassID));
		
	}	
	
	public static void printSelected(Map<String,ReactionConstraint> fluxbounds, String[] fluxesToAnalyze){
		for(String s : fluxesToAnalyze){
			ReactionConstraint rc = fluxbounds.get(s);
			System.out.println(s+": "+df.format(rc.getLowerLimit())+", "+df.format(rc.getUpperLimit()));
		}
	}	
	
	public static String getFluxObjectiveValues(Map<String,double[]> fluxObjectives,String[] fluxesToAnalyze){
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
	
	public static void printRobustness(Map<String,double[]> fluxes){
		TreeMap<String,double[]> fl = new TreeMap<String, double[]>();
		fl.putAll(fluxes);
		for(String s : fl.keySet())
			System.out.println(s+" = "+Arrays.toString(fl.get(s)));
	}
	
	public static void printRobustnessLong(Map<String, double[]> fluxes) {
		TreeMap<String, double[]> fl = new TreeMap<String, double[]>();
		fl.putAll(fluxes);
		for (String s : fl.keySet()){
			System.out.println(s);
			double[] vals = fl.get(s);
			for(double d : vals)
				System.out.println("\t"+df.format(d));
		}
	}
	
}
