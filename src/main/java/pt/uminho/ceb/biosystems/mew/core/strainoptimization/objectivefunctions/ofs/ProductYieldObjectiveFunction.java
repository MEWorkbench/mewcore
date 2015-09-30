package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.core.utils.Debugger;

public class ProductYieldObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "PYIELD";
	
	public static final String	PYIELD_PARAM_BIOMASS		= "Biomass";
	public static final String	PYIELD_PARAM_PRODUCT		= "Product";
	public static final String	PYIELD_PARAM_SUBSTRATE		= "Substrate";
	public static final String	PYIELD_PARAM_MIN_UPTAKE		= "MinUptake";
	public static final String	PYIELD_PARAM_MIN_BIOMASS	= "MinBiomass";
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(PYIELD_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(PYIELD_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(PYIELD_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		myparams.put(PYIELD_PARAM_MIN_UPTAKE, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(PYIELD_PARAM_MIN_BIOMASS, ObjectiveFunctionParameterType.DOUBLE);
		parameters = Collections.unmodifiableMap(myparams);
	}
	
	public ProductYieldObjectiveFunction() {
		super();
	}
	
	public ProductYieldObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public ProductYieldObjectiveFunction(String biomassId, String productId, String substrateId, Double minUptakeFlux, Double minBiomassFlux) {
		super(biomassId, productId, substrateId, minUptakeFlux, minBiomassFlux);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(PYIELD_PARAM_BIOMASS, params[0]);
		setParameterValue(PYIELD_PARAM_PRODUCT, params[1]);
		setParameterValue(PYIELD_PARAM_SUBSTRATE, params[2]);
		setParameterValue(PYIELD_PARAM_MIN_UPTAKE, params[3]);
		setParameterValue(PYIELD_PARAM_MIN_BIOMASS, params[4]);
	}
	
	@Override
	public double getWorstFitness() {
		return 0;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(PYIELD_PARAM_BIOMASS);
		String productID = (String) getParameterValue(PYIELD_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(PYIELD_PARAM_SUBSTRATE);
		Double minUptake = (Double) getParameterValue(PYIELD_PARAM_MIN_UPTAKE);
		Double minBiomass = (Double) getParameterValue(PYIELD_PARAM_MIN_BIOMASS);
		
		FluxValueMap fvm = simResult.getFluxValues();
		double biomassFlux = fvm.get(biomassID);
		double productFlux = fvm.get(productID);
		double substrateFlux = Math.abs(fvm.get(substrateID));
		
		double fitness;
		if (biomassFlux < minBiomass)
			fitness = getWorstFitness();
		else if (substrateFlux < minUptake)
			fitness = getWorstFitness();
		else
			fitness = productFlux / substrateFlux;
			
		Debugger.debug("B:" + biomassFlux + "\tP:" + productFlux + "\tS:" + substrateFlux + "\tYIELD:" + fitness);
		return fitness;
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
		return "PYIELD~target=" + getParameterValue(PYIELD_PARAM_PRODUCT) + ";minUptake=" + getParameterValue(PYIELD_PARAM_MIN_UPTAKE) + ";minBiomass=" + getParameterValue(PYIELD_PARAM_MIN_BIOMASS);
	}
	
	public String getDesiredId() {
		return (String) getParameterValue(PYIELD_PARAM_PRODUCT);
	}
	
	public String getSubstrateId() {
		return (String) getParameterValue(PYIELD_PARAM_SUBSTRATE);
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		return "PYIELD = \\frac{\\text{" + getParameterValue(PYIELD_PARAM_PRODUCT) + "}}{\\text{" + getParameterValue(PYIELD_PARAM_SUBSTRATE) + "}}";
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(PYIELD_PARAM_BIOMASS) + "," + getParameterValue(PYIELD_PARAM_PRODUCT) + "," + getParameterValue(PYIELD_PARAM_SUBSTRATE) + "," + getParameterValue(PYIELD_PARAM_MIN_UPTAKE) + ","
				+ getParameterValue(PYIELD_PARAM_MIN_BIOMASS) + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
