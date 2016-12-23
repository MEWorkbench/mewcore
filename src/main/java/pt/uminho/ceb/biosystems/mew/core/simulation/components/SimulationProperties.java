package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

public class SimulationProperties {
	
	public final static String	METHOD_ID							= "methodID";
	public final static String	MODEL								= "model";
	public final static String	GENETIC_CONDITIONS					= "geneticConditions";
	public final static String	ENVIRONMENTAL_CONDITIONS			= "environmentalConditions";
	public final static String	IS_OVERUNDER_SIMULATION				= "isOverunderSimulation";
	public final static String	OVERUNDER_REFERENCE_FLUXES			= "overunderReferenceFluxes";
	public final static String	OVERUNDER_2STEP_APPROACH			= "overunder2stepApproach";
	public final static String	IS_MAXIMIZATION						= "isMaximization";
	public final static String	PRODUCT_FLUX						= "productFlux";
	public final static String	IS_SHADOW_PRICES					= "isShadowPrices";
	public final static String	IS_REDUCED_COSTS					= "isReducedCosts";
	public final static String	OBJECTIVE_FUNCTION					= "objectiveFunction";
	public final static String	SOLVER								= "solver";
	public static final String	PARSIMONIOUS_PROBLEM				= "PARSIMONIOUS_PROBLEM";
	public static final String	PARSIMONIOUS_OBJECTIVE_VALUE		= "PARSIMONIOUS_OBJECTIVE_VALUE";
	public static final String	RELAX_COEF							= "RELAX_COEF";
	public static final String	WT_REFERENCE						= "WT_REFERENCE";
	public static final String	USE_DRAINS_IN_WT_REFERENCE			= "USE_DRAINS_IN_WT_REFERENCE";
	public static final String	ROOM_DELTA							= "ROOM_DELTA";
	public static final String	ROOM_EPSILON						= "ROOM_EPSILON";
	public static final String	ROOM_BOOLEAN_VAR_VALUES				= "ROOM_BOOLEAN_VAR_VALUES";
	public static final String	ROOM_SPECIAL_CONSTRAINTS			= "ROOM_SPECIAL_CONSTRAINTS";
	public static final String	DEBUG_SOLVER_MODEL					= "DEBUG_SOLVER_MODEL";
																	
	/*
	 * Methods
	 * TODO: make a Enum with the default methods
	 */
	public static final String	FBA									= "FBA";
	public static final String	PFBA								= "pFBA";
	public static final String	ROOM								= "ROOM";
	public static final String	MOMA								= "MOMA";
	public static final String	LMOMA								= "LMOMA";
	public static final String	NORM_LMOMA							= "NLMOMA";
	public static final String	MIMBL								= "MIMBL";
	public static final String	DSPP_LMOMA							= "DSPP_LMOMA";
																	
	/** turnovers */
	public static final String	METHOD_NAME							= "METHOD_NAME";
	public static final String	MAX_TURN_OBJECTIVE_FUNCTION			= "MAX_TURN_OBJECTIVE_FUNCTION";
																	
	/** min/max ratios */
	public static final String	MIN_MAX_RACIO_DIVISOR				= "MIN_MAX_RACIO_DIVISOR";
	public static final String	MIN_MAX_RACIO_DIVISOR_SENSE			= "MIN_MAX_RACIO_DIVISOR_SENSE";
	public static final String	MIN_MAX_RACIO_DIVIDEND				= "MIN_MAX_RACIO_DIVIDEND";
	public static final String	MIN_MAX_RACIO_DIVIDEND_SENSE		= "MIN_MAX_RACIO_DIVIDEND_SENSE";
																	
	/**
	 * Added by pmaia
	 */
	public static final String	PARSIMONIOUS_OF_CONSTRAINT			= "PARSIMONIOUS_OF_CONSTRAINT";
	public static final String	OF_NEW_VARS							= "OF_NEW_VARS";
	public static final String	OF_ASSOCIATED_VARIABLES				= "OF_ASSOCIATED_VARIABLES";
	public static final String	OF_ASSOCIATED_CONSTRAINTS			= "OF_ASSOCIATED_CONSTRAINTS";
																	
	/***********************
	 * Turnover properties *
	 **********************/
	public static final String	USE_2OPT							= "USE_2OPT";
																	
	/** Identify the turnover reference */
	public static final String	TURNOVER_WT_REFERENCE				= "TURNOVER_WT_REFERENCE";
																	
	/** Identify the turnover solution */
	public static final String	TURNOVER_MAP_SOLUTION				= "TURNOVER_MAP_SOLUTION";
																	
	/** Identify the flux distribution for the first optimization */
	public static final String	MIMBL_FIRST_OPTIMIZATION_FLUXVALUE	= "MIMBL_FIRST_OPTIMIZATION_FLUXVALUE";
																	
	/***********************
	 * DSPP properties *
	 **********************/
	public static final String	DSPP_FIRST_STAGE_ENV_COND			= "DSPP_FIRST_STAGE_ENV_CONDITION";
																	
	/***********************
	 * TDPS properties *
	 ***********************/
	public static final String	FV_LMOMA							= "FV_LMOMA";
	public static final String	W_LMOMA								= "W_LMOMA";
	public static final String	R_calculator						= "R_calculator";
	public static final String	TDPS								= "TDPS";
	public static final String	TDPS_LMOMA							= "TDPS_LMOMA";
	public static final String	TDPS_FBA							= "TDPS_FBA";
	public static final String	ER_FBA								= "ER_FBA";
	public static final String	ER_LMOMA							= "ER_LMOMA";
	public static final String	ER_MIMBL							= "ER_MIMBL";
																	
	/** TDPS2 - Maia */
	public static final String	TDPS2								= "TDPS2";
	public static final String	TDPS3								= "TDPS3";
	public static final String	TDPS4								= "TDPS4";
	public static final String	TDPS_PENALTY						= "TDPS_PENALTY";
	public static final String	TDPS_REMOVE_METABOLITES				= "TDPS_REMOVE_METABOLITES";
	public static final String	TDPS_UNBOUNDED_METABOLITES			= "TDPS_UNBOUNDED_METABOLITES";
	public static final String	TDPS_DRAINS							= "TDPS_DRAINS";
																	
	public static FluxValueMap simulateWT(ISteadyStateModel model, EnvironmentalConditions envCond, SolverType solver) {
		
		PFBA<FBA> pfba = new PFBA<FBA>(model);
		pfba.setEnvironmentalConditions(envCond);
		pfba.setSolverType(solver);
		pfba.setProperty(IS_MAXIMIZATION, true);
		SteadyStateSimulationResult res = pfba.simulate();
		
		return res.getFluxValues();
	}
	
	public static Map<String, Double> getTurnOverCalculation(ISteadyStateModel model, Map<String, Double> fluxes) {
		
		Map<String, Double> ret = new HashMap<String, Double>();
		
		for (int i = 0; i < model.getNumberOfMetabolites(); i++) {
			
			String metId = model.getMetaboliteId(i);
			double turnover = 0;
			turnover = getTurnOverMetabolite(model, fluxes, i);
			ret.put(metId, turnover);
		}
		
		return ret;
	}
	
	public static Map<Integer, Double> getTurnOverCalculationByIndex(ISteadyStateModel model, Map<String, Double> fluxes, Set<Integer> ignoreMetabolites) {
		Map<Integer, Double> ret = new HashMap<Integer, Double>();
		
		for (int i = 0; i < model.getNumberOfMetabolites(); i++) {
			
			double turnover = 0;
			if (!ignoreMetabolites.contains(i)) {
				turnover = getTurnOverMetabolite(model, fluxes, i);
			}
			ret.put(i, turnover);
		}
		
		return ret;
	}
	
	public static Map<Integer, Double> getTurnOverCalculationByIndex(ISteadyStateModel model, Map<String, Double> fluxes) {
		return getTurnOverCalculationByIndex(model, fluxes, new HashSet<Integer>());
	}
	
	private static Double getTurnOverMetabolite(ISteadyStateModel model, Map<String, Double> fluxes, Integer met_idx) {
		double result = 0.0;
		
		for (int i = 0; i < fluxes.size(); i++) {
			
			String r = model.getReactionId(i);
			
			double stoiq_val = model.getStoichiometricValue(met_idx, i);
			double sim_value = fluxes.get(r);
			
			double valueToCompare = sim_value * stoiq_val;
			
			if (valueToCompare > 0)
				result += valueToCompare;
				
		}
		return result;
	}
	
	public static void computeConsumersProducersPerIndex(ISteadyStateModel model, Map<Integer, Collection<Integer>> producers, Map<Integer, Collection<Integer>> consumers, Set<Integer> metabolites2ignore) {
		
		for (int met = 0; met < model.getNumberOfMetabolites(); met++) {
			
			producers.put(met, new ArrayList<Integer>());
			consumers.put(met, new ArrayList<Integer>());
			
			for (int reac = 0; reac < model.getNumberOfReactions(); reac++) {
				
				double stoich = model.getStoichiometricValue(met, reac);
				
				//dont include unbounded metabolites
				if ((stoich > 0) && (!metabolites2ignore.contains(met))) {
					producers.get(met).add(reac);
				}
				//dont include unbounded metabolites
				if ((stoich < 0) && (!metabolites2ignore.contains(met))) {
					consumers.get(met).add(reac);
				}
			}
		}
	}
	
	public static void computeProductsReactantsPerIndex(ISteadyStateModel model, Map<Integer, Collection<Integer>> products, Map<Integer, Collection<Integer>> reactants, Set<Integer> metabolites2ignore) {
		
		for (int reac = 0; reac < model.getNumberOfReactions(); reac++) {
			
			products.put(reac, new ArrayList<Integer>());
			reactants.put(reac, new ArrayList<Integer>());
			
			for (int met = 0; met < model.getNumberOfMetabolites(); met++) {
				
				double stoich = model.getStoichiometricValue(met, reac);
				
				//dont include unbounded metabolites
				if ((stoich > 0) && (!metabolites2ignore.contains(met))) {
					products.get(reac).add(met);
					
				}
				//dont include unbounded metabolites
				if ((stoich < 0) && (!metabolites2ignore.contains(met))) {
					reactants.get(reac).add(met);
					
				}
			}
		}
	}
	
	static public Map<String, Integer> splitReactionsMILP(MILPProblem problem, ISteadyStateModel model, IOverrideReactionBounds overrideBounds, Set<Integer> reaction2ignore) throws LinearProgrammingTermAlreadyPresentException {
		Map<String,Integer> newVariables = new HashMap<>();
		
		for(int i=0; i<model.getNumberOfReactions(); i++){
			if(!reaction2ignore.contains(i)){
				String reactionID = model.getReactionId(i);
				ReactionConstraint constr = overrideBounds.getReactionConstraint(reactionID);
				splitSingleReactionMILP(problem, newVariables, reactionID, i, constr.getLowerLimit(), constr.getUpperLimit());
			}
		}
		
		return newVariables;
	}
	
	static public void splitSingleReactionMILP(MILPProblem problem, Map<String, Integer> newVariables, String reactionName, int reactionIdx, double lowerLimit, double upperLimit) throws LinearProgrammingTermAlreadyPresentException {
		final String idPositive = "TORV_" + reactionName + "(" + reactionIdx + ")_PST";
		final String idNegative = "TORV_" + reactionName + "(" + reactionIdx + ")_NGT";
		
		//split reversible reactions into a positive Vp and a negative Vn half reaction
		Map<String, Integer> newVars = L1VarTerm.splitNegAndPosVariable(problem, reactionIdx, idPositive, idNegative, lowerLimit, upperLimit);
		newVariables.putAll(newVars);
		
		//get the split variables' indexes	
		int vpn = newVariables.get(idPositive);
		int vnn = newVariables.get(idNegative);
		
		//get the number of variables
		int bp = problem.getNumberVariables();
		int bn = bp + 1;
		
		problem.addIntVariable("y" + reactionIdx, 0, 1);
		problem.addIntVariable("w" + reactionIdx, 0, 1);
		//add the boolean variables to the var mappings
		newVariables.put("y" + reactionIdx, bp);
		newVariables.put("w" + reactionIdx, bn);
		
		//create positive row
		LPProblemRow binaryP = new LPProblemRow();
		binaryP.addTerm(vpn, 1); // create: Vp< 1000 * Bp ; If the boolean variable is 1, Vn is lower than 1000,if it is 0, Vp has to be zero
		binaryP.addTerm(bp, -1000);
		LPConstraint MILPpos = new LPConstraint(LPConstraintType.LESS_THAN, binaryP, 0);
		problem.addConstraint(MILPpos);
		
		//create negative row
		LPProblemRow binaryN = new LPProblemRow();
		binaryN.addTerm(vnn, 1); // create: Vn > -1000 * Bn ; If the boolean variable is 1, Vn is higher than -1000,if it is 0, Vn has to be zero
		binaryN.addTerm(bn, -1000);
		LPConstraint MILPneg = new LPConstraint(LPConstraintType.LESS_THAN, binaryN, 0);
		problem.addConstraint(MILPneg);
		
		//create: Bp+Bn<1; With this constraint the sum of the boolean variables has to be lower or equal to 1
		//which means they cannot both be equal to 1 and Vp and Vn will never be active at the same time
		LPProblemRow binaryS = new LPProblemRow();
		binaryS.addTerm(bp, 1);
		binaryS.addTerm(bn, 1);		
		LPConstraint MILPsum = new LPConstraint(LPConstraintType.LESS_THAN, binaryS, 1);
		problem.addConstraint(MILPsum);
		
	}
	
}
