package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

/**
 * 
 * @author pmaia Feb 12, 2014
 */
public class FVAObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long		serialVersionUID	= 1L;
	private static final boolean	_debug				= false;
	public static final String		ID					= "FVA";
	
	public static final String								FVA_PARAM_BIOMASS		= "Biomass";
	public static final String								FVA_PARAM_PRODUCT		= "Product";
	public static final String								FVA_PARAM_MAXIMIZATION	= "Maximization";
	
	private transient SimulationSteadyStateControlCenter	_cc						= null;

	
	static{
		Map<String,ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(FVA_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(FVA_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(FVA_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public FVAObjectiveFunction(){super();}
	
	public FVAObjectiveFunction(Map<String,Object> configuration) throws InvalidObjectiveFunctionConfiguration{
		super(configuration);
	}
	
	public FVAObjectiveFunction(String biomassID, String targetID, Boolean maximize) {
		super(biomassID,targetID,maximize);
	}	
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(FVA_PARAM_BIOMASS, params[0]);
		setParameterValue(FVA_PARAM_PRODUCT, params[1]);
		setParameterValue(FVA_PARAM_MAXIMIZATION, params[2]);
	}
		
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(FVA_PARAM_BIOMASS);
		String productID = (String) getParameterValue(FVA_PARAM_PRODUCT);
		
		double biomassFluxValue = simResult.getFluxValues().getValue(biomassID) * 0.99999;
		
		if (_cc == null) {
			initControlCenter(simResult);
		}
		
		GeneticConditions gc = simResult.getGeneticConditions();
		
		EnvironmentalConditions ec = new EnvironmentalConditions();
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
		ec.addReactionConstraint(biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
		
		_cc.setEnvironmentalConditions(ec);
		_cc.setGeneticConditions(gc);
		_cc.setFBAObjSingleFlux(productID, 1.0);
		
		SteadyStateSimulationResult fvaResult = null;
		try {
			fvaResult = (SteadyStateSimulationResult) _cc.simulate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double value = fvaResult.getFluxValues().getValue(productID);
		if (Double.isNaN(value)) value = 0;
		//		System.out.println("[FVA] testing ["+gc.toStringOptions(",", true)+"]: fva="+value);
		return value;
	}
	
	private void initControlCenter(SteadyStateSimulationResult simResult) {
		Boolean maximize = (Boolean) getParameterValue(FVA_PARAM_MAXIMIZATION);
		String biomassID = (String) getParameterValue(FVA_PARAM_BIOMASS);
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: initializing control center");
		_cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), simResult.getGeneticConditions(), simResult.getModel(), SimulationProperties.FBA);
		_cc.setSolver(SolverType.CPLEX3);
		_cc.setMaximization(maximize);
		_cc.setFBAObjSingleFlux(biomassID, 1.0);
		
		try {
			if (_debug) System.out.print("[" + getClass().getSimpleName() + "]:... warming up using [" + biomassID + "]... ");
			SteadyStateSimulationResult res = _cc.simulate(); // warm-up
			if (_debug) System.out.println(res.getOFString() + "=" + res.getOFvalue() + " ...done!");
		} catch (Exception e) {
		
		}
	}
	
	@Override
	public double getWorstFitness() {
		return (boolean) (getParameterValue(FVA_PARAM_MAXIMIZATION)) ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public boolean isMaximization() {
		return (boolean) getParameterValue(FVA_PARAM_MAXIMIZATION);
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return "FVA~" + ((boolean) getParameterValue(FVA_PARAM_MAXIMIZATION)? "max~" : "min~") + getParameterValue(FVA_PARAM_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getBuilderString() {
		return ID + "(" + getParameterValue(FVA_PARAM_BIOMASS)+ "," + getParameterValue(FVA_PARAM_PRODUCT)+ "," + getParameterValue(FVA_PARAM_MAXIMIZATION)+ ")";
	}
	
	@Override
	public String getLatexFormula() {
		return null;
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
}
