package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.PARSIMONIOUS;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public class SimulationProperties {
	
	public final static String GENETIC_CONDITIONS="geneticConditions";
	public final static String ENVIRONMENTAL_CONDITIONS = "environmentalConditions";
	
	
	public final static String IS_OVERUNDER_SIMULATION = "isOverunderSimulation";
	
	public final static String IS_MAXIMIZATION = "isMaximization";
	public final static String PRODUCT_FLUX = "productFlux";
	
	
	public final static String OVERUNDER_REFERENCE_FLUXES = "overunderReferenceFluxes";
	
	
	public final static String IS_SHADOW_PRICES = "isShadowPrices";
	public final static String IS_REDUCES_COSTS = "isReducedCosts";
	
	public final static String OBJECTIVE_FUNCTION = "objectiveFunction";
	public final static String SOLVER = "solver";
	
	
	public static final String PARSIMONIUS_PROBLEM = "PARSIMONIUS_PROBLEM";
	public static final String PARSIMONIUS_OBJECTIVE_VALUE = "PARSIMONIUS_OBJECTIVE_VALUE";
	public static final String RELAX_COEF = "RELAX_COEF";
	
	public static final String WT_REFERENCE = "WT_REFERENCE";
	public static final String USE_DRAINS_IN_WT_REFERENCE = "USE_DRAINS_IN_WT_REFERENCE";
	
	public static final String ROOM_DELTA = "ROOM_DELTA";
	public static final String ROOM_EPSILON = "ROOM_EPSILON";
	public static final String ROOM_BOOLEAN_VAR_VALUES = "ROOM_BOOLEAN_VAR_VALUES";
	
	/*
	 * Methods
	 * TODO: make a Enum with the default methods
	 */
	public static final String FBA = "FBA";
	public static final String PARSIMONIUS = "pFBA";
	public static final String ROOM = "ROOM";
	public static final String MOMA = "MOMA";
	public static final String LMOMA = "LMOMA";
	public static final String NORM_LMOMA = "NLMOMA";
	
	public static final String METHOD_NAME = "METHOD_NAME";
	public static final String MAX_TURN_OBJECTIVE_FUNCTION = "MAX_TURN_OBJECTIVE_FUNCTION";
	public static final String MIN_MAX_RACIO_DIVISOR = "MIN_MAX_RACIO_DIVISOR";
	public static final String MIN_MAX_RACIO_DIVISOR_SENSE = "MIN_MAX_RACIO_DIVISOR_SENSE";
	public static final String MIN_MAX_RACIO_DIVIDEND = "MIN_MAX_RACIO_DIVIDEND";
	public static final String MIN_MAX_RACIO_DIVIDEND_SENSE = "MIN_MAX_RACIO_DIVIDEND_SENSE";
	
	public static FluxValueMap simulateWT(ISteadyStateModel model, EnvironmentalConditions envCond, SolverType solver) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException{
		
		PARSIMONIOUS<FBA> pfba = new PARSIMONIOUS<FBA>(model);
		pfba.setEnvironmentalConditions(envCond);
		pfba.setSolverType(solver);
		pfba.setProperty(IS_MAXIMIZATION, true);
		SteadyStateSimulationResult res = pfba.simulate();
		
		return res.getFluxValues();
	}
	
}
