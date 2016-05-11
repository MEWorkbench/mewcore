package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

/**
 * This objective function allows the user to choose wether to maximize or minimize the FVAMin of
 * FVAMax. Furthermore, it allows to specify the percentage of robustness of the solution with
 * respect to the WT biomass, the minimum value for the biomass as well as a minimum value for the
 * product-substrate yield (PSYield).
 * 
 * @author pmaia
 * @date May 11, 2016
 * @version 0.1
 * @since
 */
public class FVASenseMinPSYieldObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long								serialVersionUID							= 1L;
	public static final String								ID											= "FVA_SENSE_MIN_PSYIELD";
																										
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_BIOMASS			= "Biomass";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT			= "Product";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_SUBSTRATE		= "Substrate";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION	= "Maximization";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_SOLVER			= "Solver";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_SENSE			= "Sense";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_MIN_ROBUSTNESS	= "MinumumRobustness";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_MIN_BIOMASS		= "MinimumBiomass";
	public static final String								FVA_SENSE_MIN_PSYIELD_PARAM_MIN_PSYIELD		= "MinimumProductSubstrateYield";
																										
	private transient SimulationSteadyStateControlCenter	_cc											= null;
																										
	public Map<String, ObjectiveFunctionParameterType> loadParameters() {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_SENSE, ObjectiveFunctionParameterType.BOOLEAN);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_ROBUSTNESS, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_BIOMASS, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_PSYIELD, ObjectiveFunctionParameterType.DOUBLE);
		return Collections.unmodifiableMap(myparams);
	}
	
	public FVASenseMinPSYieldObjectiveFunction() {
		super();
	}
	
	public FVASenseMinPSYieldObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public FVASenseMinPSYieldObjectiveFunction(
			String biomassID,
			String targetID,
			String substrateID,
			Boolean maximize,
			Boolean fvaMax,
			SolverType solver,
			Double minimumRobustness,
			Double minimumBiomass,
			Double minimumPSYield) {
		super(biomassID, targetID, substrateID, maximize, fvaMax, solver, minimumRobustness, minimumBiomass, minimumPSYield);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_BIOMASS, params[0]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT, params[1]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SUBSTRATE, params[2]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION, params[3]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SENSE, params[4]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SOLVER, params[5]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_ROBUSTNESS, params[6]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_BIOMASS, params[7]);
		setParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_PSYIELD, params[8]);
	}
	
	private SimulationSteadyStateControlCenter getControlCenter(ISteadyStateModel model) {
		if (_cc == null) {
			SolverType solver = (SolverType) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SOLVER);
			String productID = (String) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT);
			Boolean sense = (Boolean) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SENSE);
			_cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
			_cc.setSolver(solver);
			_cc.setFBAObjSingleFlux(productID, 1.0);
			_cc.setMaximization(sense);
		}
		return _cc;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_BIOMASS);
		String productID = (String) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SUBSTRATE);
		Double minBiomass = (Double) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_BIOMASS);
		Double minPSYield = (Double) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_PSYIELD);
		Double minRobustness = (Double) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MIN_ROBUSTNESS);
		
		double biomassFluxValue = simResult.getFluxValues().getValue(biomassID) * minRobustness;
		
		ISteadyStateModel model = simResult.getModel();
		
		GeneticConditions gc = simResult.getGeneticConditions();
		EnvironmentalConditions ec = new EnvironmentalConditions();
		
		if (simResult.getEnvironmentalConditions() != null)
			ec.putAll(simResult.getEnvironmentalConditions());
		ec.addReactionConstraint(biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
		
		getControlCenter(model).setEnvironmentalConditions(ec);
		getControlCenter(model).setGeneticConditions(gc);
		
		SteadyStateSimulationResult fvaResult = null;
		try {
			fvaResult = (SteadyStateSimulationResult) getControlCenter(model).simulate();
			//			System.out.println(fvaResult.getEnvironmentalConditions().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Double result = fvaResult.getFluxValues().getValue(productID);
		
		if (!Double.isNaN(result)) {
			
			Double biomassValue = simResult.getFluxValues().getValue(biomassID);
			Double substrateValue = simResult.getFluxValues().getValue(substrateID);
			Double psYield = result / substrateValue;
			
			if (biomassValue >= minBiomass && psYield >= minPSYield) {
				return result;
			}
		}
		
		return getWorstFitness();
	}
	
	@Override
	public double getWorstFitness() {
		return (boolean) (getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION)) ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION);
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return ((Boolean) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION) ? "max" : "min") + "~FVA~" + ((Boolean) getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SENSE) ? "max~" : "min~")
				+ getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getBuilderString() {
		return ID + "(" + getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_BIOMASS) + "," + getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_PRODUCT) + ","
				+ getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_MAXIMIZATION) + ","
				+ getParameterValue(FVA_SENSE_MIN_PSYIELD_PARAM_SENSE) + ","
				+ getParameterType(FVA_SENSE_MIN_PSYIELD_PARAM_SOLVER) + ")";
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
