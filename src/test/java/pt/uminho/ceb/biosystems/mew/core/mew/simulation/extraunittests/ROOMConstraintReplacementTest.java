package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ROOMConstraintReplacementTest {
	
	@Test
	public void test_A_() throws Exception{
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 0.000000001);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis",2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis",true);
		CplexParamConfiguration.setBooleanParam("PreInd",true);
		CplexParamConfiguration.setIntegerParam("HeurFreq",-1);
		ModelConfiguration modelConf = new ModelConfiguration("files/iAF1260_full/iAF1260.conf");
		ISteadyStateModel model = modelConf.getModel();
		String biomassID = modelConf.getModelBiomass();
		String targetID = "R_EX_succ_e_";
		
		
		EnvironmentalConditions envCondAerobiose = new EnvironmentalConditions();
		envCondAerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(-20.0, 9999999.0));
		
		EnvironmentalConditions envCondAnaerobiose = new EnvironmentalConditions();
		envCondAnaerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAnaerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(0.0, 9999999.0));
		
		List<String> reactions = new ArrayList<String>();
		reactions.add(0, "R_FUM");
		reactions.add(1, "R_SUCFUMtpp");
		GeneticConditions genCondRK = new GeneticConditions(new ReactionChangesList(reactions));
		
		List<String> genes = new ArrayList<String>();
		genes.add(0, "b1611");
		genes.add(1, "b1612");
		genes.add(2, "b4122");
		genes.add(3, "b4123");
		genes.add(4, "b0621");
		genes.add(5, "b4138");
		List<Double> genesExpressionsGK = new ArrayList<Double>();
		genesExpressionsGK.add(0, 0.0);
		genesExpressionsGK.add(1, 0.0);
		genesExpressionsGK.add(2, 0.0);
		genesExpressionsGK.add(3, 0.0);
		genesExpressionsGK.add(4, 0.0);
		genesExpressionsGK.add(5, 0.0);
		GeneticConditions genCondGK = new GeneticConditions(new GeneChangesList(genes, genesExpressionsGK), (ISteadyStateGeneReactionModel) model, false);
		genCondGK.updateReactionsList((ISteadyStateGeneReactionModel) model);
		
	
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCondAerobiose, null, model, SimulationProperties.ROOM);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.addProperty(SimulationProperties.ROOM_DELTA, 0.0);
		cc.addProperty(SimulationProperties.ROOM_EPSILON, 0.0);
		
		try {
			System.out.println("================================================ NEW A ================================================ ");
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testA_new.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("\nOF = " + of + "/ BIO = " + bio + " TARG = " + succ+"\n");
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {
			System.out.println("================================================ NEW B ================================================ ");
			cc.setEnvironmentalConditions(envCondAnaerobiose);
//			cc.setGeneticConditions(genCondGK);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testB_new.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("\nOF = " + of + "/ BIO = " + bio + " TARG = " + succ+"\n");
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {			
			System.out.println("================================================ NEW C ================================================ ");
			cc.setGeneticConditions(genCondRK);
			cc.setEnvironmentalConditions(envCondAerobiose);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testC_new.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("\nOF = " + of + "/ BIO = " + bio + " TARG = " + succ+"\n");
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {
			System.out.println("================================================ NEW D ================================================ ");
			cc.setGeneticConditions(genCondGK);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testD_new.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("OF = " + of + "/ BIO = " + bio + " TARG = " + succ);
		} catch (UnsupportedOperationException e) {
		
		}
	}
	
	@Test
	public void test_B_() throws Exception{
		
//		OF = -1.3969824272813132E-8/ BIO = 0.45888815012645023 TARG = 0.15314795525724306

		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 0.000000001);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis",2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis",true);
		CplexParamConfiguration.setBooleanParam("PreInd",true);
		CplexParamConfiguration.setIntegerParam("HeurFreq",-1);
		ModelConfiguration modelConf = new ModelConfiguration("files/iAF1260_full/iAF1260.conf");
		ISteadyStateModel model = modelConf.getModel();
		String biomassID = modelConf.getModelBiomass();
		String targetID = "R_EX_succ_e_";
		
		
		EnvironmentalConditions envCondAerobiose = new EnvironmentalConditions();
		envCondAerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(-20.0, 9999999.0));
		
		EnvironmentalConditions envCondAnaerobiose = new EnvironmentalConditions();
		envCondAnaerobiose.addReactionConstraint("R_EX_glc_e_", new ReactionConstraint(-20.0, 9999999.0));
		envCondAnaerobiose.addReactionConstraint("R_EX_o2_e_", new ReactionConstraint(0.0, 9999999.0));
		
		List<String> reactions = new ArrayList<String>();
		reactions.add(0, "R_FUM");
		reactions.add(1, "R_SUCFUMtpp");
		GeneticConditions genCondRK = new GeneticConditions(new ReactionChangesList(reactions));
		
		List<String> genes = new ArrayList<String>();
		genes.add(0, "b1611");
		genes.add(1, "b1612");
		genes.add(2, "b4122");
		genes.add(3, "b4123");
		genes.add(4, "b0621");
		genes.add(5, "b4138");
		List<Double> genesExpressionsGK = new ArrayList<Double>();
		genesExpressionsGK.add(0, 0.0);
		genesExpressionsGK.add(1, 0.0);
		genesExpressionsGK.add(2, 0.0);
		genesExpressionsGK.add(3, 0.0);
		genesExpressionsGK.add(4, 0.0);
		genesExpressionsGK.add(5, 0.0);
		GeneticConditions genCondGK = new GeneticConditions(new GeneChangesList(genes, genesExpressionsGK), (ISteadyStateGeneReactionModel) model, false);
		genCondGK.updateReactionsList((ISteadyStateGeneReactionModel) model);
	
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCondAerobiose, null, model, SimulationProperties.ROOM);
		cc.setSolver(SolverType.CPLEX);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassID, 1.0);
		cc.addProperty(SimulationProperties.ROOM_DELTA, 0.0);
		cc.addProperty(SimulationProperties.ROOM_EPSILON, 0.00);
		
		try {
			System.out.println("================================================ OLD A ================================================ ");
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testA_old.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("OF = " + of + "/ BIO = " + bio + " TARG = " + succ);
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {
			System.out.println("================================================ OLD B ================================================ ");
			cc.setEnvironmentalConditions(envCondAnaerobiose);
//			cc.setGeneticConditions(genCondGK);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testB_old.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("OF = " + of + "/ BIO = " + bio + " TARG = " + succ);
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {
			System.out.println("================================================ OLD C ================================================ ");
			cc.setGeneticConditions(genCondRK);
			cc.setEnvironmentalConditions(envCondAerobiose);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testC_old.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("OF = " + of + "/ BIO = " + bio + " TARG = " + succ);
		} catch (UnsupportedOperationException e) {
		
		}
		
		try {
			System.out.println("================================================ OLD D ================================================ ");
			cc.setGeneticConditions(genCondGK);
			cc.addProperty(SimulationProperties.DEBUG_SOLVER_MODEL, "files/ROOM_TESTS/testD_old.mps");
			SteadyStateSimulationResult res = cc.simulate();
			double of = res.getOFvalue();
			double bio = res.getFluxValues().getValue(biomassID);
			double succ = res.getFluxValues().getValue(targetID);
			System.out.println("OF = " + of + "/ BIO = " + bio + " TARG = " + succ);
		} catch (UnsupportedOperationException e) {
		
		}
	}
	
}
