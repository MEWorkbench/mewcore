package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;

public class WeightedBPObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ID = "WBP";
	
	public static final String	WBP_PARAM_BIOMASS		= "Biomass";
	public static final String	WBP_PARAM_PRODUCT		= "Product";
	public static final String	WBP_PARAM_MAX_BIOMASS	= "MaxBiomass";
	public static final String	WBP_PARAM_MAX_PRODUCT	= "MaxProduct";
	public static final String	WBP_PARAM_ALPHA			= "Alpha";
	
	protected final double worstFitness = Double.NEGATIVE_INFINITY;
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(WBP_PARAM_MAX_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(WBP_PARAM_MAX_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(WBP_PARAM_MAX_BIOMASS, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(WBP_PARAM_MAX_PRODUCT, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(WBP_PARAM_ALPHA, ObjectiveFunctionParameterType.DOUBLE);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public WeightedBPObjectiveFunction() {
		super();
	}
	
	public WeightedBPObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public WeightedBPObjectiveFunction(String biomassId, String desiredFluxId, Double maxBiomass, Double maxProduct, Double alpha) {
		super(biomassId, desiredFluxId, maxBiomass, maxProduct, alpha);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(WBP_PARAM_BIOMASS, params[0]);
		setParameterValue(WBP_PARAM_PRODUCT, params[1]);
		setParameterValue(WBP_PARAM_MAX_BIOMASS, params[2]);
		setParameterValue(WBP_PARAM_MAX_PRODUCT, params[3]);
		setParameterValue(WBP_PARAM_ALPHA, params[4]);
	}
	
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(WBP_PARAM_BIOMASS);
		String productID = (String) getParameterValue(WBP_PARAM_MAX_PRODUCT);
		Double maxBiomass = (Double) getParameterValue(WBP_PARAM_MAX_BIOMASS);
		Double maxProduct = (Double) getParameterValue(WBP_PARAM_MAX_PRODUCT);
		Double alpha = (Double) getParameterValue(WBP_PARAM_ALPHA);
		
		FluxValueMap fluxValues = simResult.getFluxValues();
		
		double biomassValue = Math.abs(fluxValues.getValue(biomassID));
		double desiredFlux = Math.abs(fluxValues.get(productID));
		
		double NG = biomassValue / maxBiomass;
		double NP = desiredFlux / maxProduct;
		
		double fitness = Math.pow(NG, alpha) * Math.pow(NP, (1.0 - alpha));
		
		return fitness;
	}
	
	@Override
	public double getWorstFitness() {
		return worstFitness;
	}
	
	public String toString() {
		return getID();
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return toString();
	}
	
	public String getDesiredId() {
		return (String) getParameterValue(WBP_PARAM_MAX_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "WBPO= \\left(\\frac{\\text{" + getParameterValue(WBP_PARAM_BIOMASS) + "}}{" + getParameterValue(WBP_PARAM_MAX_BIOMASS) + "}\\right)^" + getParameterValue(WBP_PARAM_ALPHA) + " \\times " + " \\left(\\frac{\\text{"
				+ getParameterValue(WBP_PARAM_MAX_PRODUCT) + "}}{" + getParameterValue(WBP_PARAM_MAX_PRODUCT) + "}\\rigth)^{(1-" + getParameterValue(WBP_PARAM_ALPHA) + ")}";
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(WBP_PARAM_BIOMASS) + "," + getParameterValue(WBP_PARAM_PRODUCT) + "," + getParameterValue(WBP_PARAM_MAX_BIOMASS) + "," + getParameterValue(WBP_PARAM_MAX_PRODUCT) + ","
				+ getParameterValue(WBP_PARAM_ALPHA) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
