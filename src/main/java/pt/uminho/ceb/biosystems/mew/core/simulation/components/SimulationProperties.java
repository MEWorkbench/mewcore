package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class SimulationProperties {
	
	public final static String	GENETIC_CONDITIONS					= "geneticConditions";
	public final static String	ENVIRONMENTAL_CONDITIONS			= "environmentalConditions";
	
	public final static String	IS_OVERUNDER_SIMULATION				= "isOverunderSimulation";
	public final static String	OVERUNDER_REFERENCE_FLUXES			= "overunderReferenceFluxes";
	public final static String	OVERUNDER_2STEP_APPROACH			= "overunder2stepApproach";
	
	public final static String	IS_MAXIMIZATION						= "isMaximization";
	public final static String	PRODUCT_FLUX						= "productFlux";
	
	public final static String	IS_SHADOW_PRICES					= "isShadowPrices";
	public final static String	IS_REDUCES_COSTS					= "isReducedCosts";
	
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
	
	/** min/max ratios*/
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
	 *   DSPP properties   *
	 **********************/
	public static final String DSPP_FIRST_STAGE_ENV_COND = "DSPP_FIRST_STAGE_ENV_CONDITION";
	
	
	
	
	public static FluxValueMap simulateWT(ISteadyStateModel model, EnvironmentalConditions envCond, SolverType solver) throws Exception {
		
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
			try {
				turnover = getTurnOverMetabolite(model, fluxes, metId);
			} catch (NonExistentIdException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret.put(metId, turnover);
		}
		
		return ret;
	}
	
	private static Double getTurnOverMetabolite(ISteadyStateModel model, Map<String, Double> fluxes, String metabolite_id) throws NonExistentIdException {
		double result = 0.0;
		
		int met_idx = model.getMetaboliteIndex(metabolite_id);
		
		for (int i = 0; i < fluxes.size(); i++) {
			
			String r = model.getReactionId(i);
			
			double stoiq_val = model.getStoichiometricValue(met_idx, i);
			double sim_value = fluxes.get(r);
			
			double valueToCompare = sim_value * stoiq_val;
			
			if (valueToCompare > 0) result += valueToCompare;
			
		}
		return result;
		
	}
	
}
