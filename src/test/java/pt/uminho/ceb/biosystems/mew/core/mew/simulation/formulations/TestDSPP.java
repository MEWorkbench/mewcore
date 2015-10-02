package pt.uminho.ceb.biosystems.mew.core.mew.simulation.formulations;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.SimulationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.DSPP_LMOMA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FVAObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.dspp.DSPP_BPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class TestDSPP {
	
	public static final String	owncloud		= System.getenv("OWNCLOUD");
	public static final String	baseDir			= owncloud + "/documents/INVISTA/INVISTA_20150820_model2_v3_7ahpt_fru_h2co2_EXCEP2_alternatives";
	public static final String	baseDirTargets	= baseDir + "/targets/";
	
	public static final String confFile = baseDir + "/configurations/conf_20150820_1130_LMOMA.conf";
	
	public static void main(String[] args) throws Exception {
		
		SimulationConfiguration conf = new SimulationConfiguration(confFile);
		ISteadyStateModel model = conf.getModel();
		
		GeneticConditions gc = new GeneticConditions(new ReactionChangesList(new String[] { "R_PGMT", "R_PYC", "R_HIBD", "R_TKT1", "R_GLYCODHq", "R_PPC", "R_HISAL", "R_NDPK3", "R_PPS", "R_TALA", "R_PRK", "R_ICL", "R_GLYCODHmq" }));
		EnvironmentalConditions condStage1 = EnvironmentalConditions.readFromFile("/home/pmaia/ownCloud/documents/INVISTA/INVISTA_20150820_model2_v3_7ahpt_fru_h2co2_EXCEP2_alternatives/conditions/envcond#modelv3_7AHPT#[fructose_aerobic].env", ",");
		EnvironmentalConditions condStage2 = condStage1.copy();
//		condStage2.getReactionConstraint("R_EX_o2_").setLowerLimit(-1.1);
		condStage2.addReactionConstraint("R_EX_pi_", new ReactionConstraint(-0.001, 1000.0));
		
		
		
		FVAObjectiveFunction fvaMax = new FVAObjectiveFunction(conf.getModelBiomass(), "R_step_drain_7AHPT", true);
		FluxValueObjectiveFunction fv = new FluxValueObjectiveFunction("R_step_drain_7AHPT", true);
		FluxValueObjectiveFunction bio = new FluxValueObjectiveFunction(conf.getModelBiomass(), true);
		
		DSPP_BPCYObjectiveFunction dsppBPCY = new DSPP_BPCYObjectiveFunction(conf.getModelBiomass(), "R_step_drain_7AHPT", "R_EX_fru_");
		
		PFBA<FBA> pfba = new PFBA<>(model);
		pfba.setEnvironmentalConditions(condStage1);
		pfba.setGeneticConditions(gc);
		pfba.setSolverType(SolverType.CPLEX3);
		pfba.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		
		SteadyStateSimulationResult resPFBA = pfba.simulate();
		
		DSPP_LMOMA dspp = new DSPP_LMOMA(model);
		dspp.setEnvironmentalConditions(condStage1);
		dspp.setGeneticConditions(gc);
		dspp.setSolverType(SolverType.CPLEX3);
		dspp.setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		dspp.setProperty(SimulationProperties.DSPP_FIRST_STAGE_ENV_COND,condStage2);
		
		SteadyStateSimulationResult resDSPP = dspp.simulate();
		
		
		System.out.println("fvamax PFBA = "+fvaMax.evaluate(resPFBA));
		System.out.println("fvamax DSSP = "+fvaMax.evaluate(resDSPP));
		System.out.println("flux value PFBA = "+fv.evaluate(resPFBA));
		System.out.println("flux value DSSP = "+fv.evaluate(resDSPP));
		System.out.println("biomass PFBA = "+bio.evaluate(resPFBA));
		System.out.println("biomass DSPP = "+bio.evaluate(resDSPP));
		System.out.println("of DSPP = "+resDSPP.getOFvalue());
		System.out.println("DSPP BPCY="+dsppBPCY.evaluate(resDSPP));
		MapUtils.prettyPrint(resPFBA.getNetConversionMap(false));
		MapUtils.prettyPrint(resDSPP.getNetConversionMap(false));
		
		
	}
	
}
