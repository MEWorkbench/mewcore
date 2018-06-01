package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

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

/**
 * This objective function allows the user to maximize of minimize the FVAMax or
 * FVAMin.
 * 
 * @author pmaia
 * @version 1.0
 * @since metabolic3persistent
 */
public class FVASenseObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "FVA_SENSE";
	
	public static final String	FVA_SENSE_PARAM_BIOMASS			= "Biomass";
	public static final String	FVA_SENSE_PARAM_PRODUCT			= "Product";
	public static final String	FVA_SENSE_PARAM_MAXIMIZATION	= "Maximization";
	public static final String	FVA_SENSE_PARAM_SOLVER			= "Solver";
	public static final String	FVA_SENSE_PARAM_SENSE			= "Sense";
	
	private transient SimulationSteadyStateControlCenter _cc = null;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(FVA_SENSE_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(FVA_SENSE_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(FVA_SENSE_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		myparams.put(FVA_SENSE_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		myparams.put(FVA_SENSE_PARAM_SENSE, ObjectiveFunctionParameterType.BOOLEAN);
		return Collections.unmodifiableMap(myparams);
	}
	
	public FVASenseObjectiveFunction() {
		super();
	}
	
	public FVASenseObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public FVASenseObjectiveFunction(String biomassID, String targetID, Boolean maximize, Boolean fvaMax, String solver) {
		super(biomassID, targetID, maximize, fvaMax, solver);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(FVA_SENSE_PARAM_BIOMASS, params[0]);
		setParameterValue(FVA_SENSE_PARAM_PRODUCT, params[1]);
		setParameterValue(FVA_SENSE_PARAM_MAXIMIZATION, params[2]);
		setParameterValue(FVA_SENSE_PARAM_SENSE, params[3]);
		setParameterValue(FVA_SENSE_PARAM_SOLVER, params[4]);
	}
	
	private SimulationSteadyStateControlCenter getControlCenter(ISteadyStateModel model) {
		if (_cc == null) {
			String solver = (String) getParameterValue(FVA_SENSE_PARAM_SOLVER);
			String productID = (String) getParameterValue(FVA_SENSE_PARAM_PRODUCT);
			Boolean sense = (Boolean) getParameterValue(FVA_SENSE_PARAM_SENSE);
			_cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
			_cc.setSolver(solver);
			_cc.setFBAObjSingleFlux(productID, 1.0);
			_cc.setMaximization(sense);
		}
		return _cc;
	}
	
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		String biomassID = (String) getParameterValue(FVA_SENSE_PARAM_BIOMASS);
		String productID = (String) getParameterValue(FVA_SENSE_PARAM_PRODUCT);
		
		double biomassFluxValue = simResult.getFluxValues().getValue(biomassID) * 0.99999;
		
		ISteadyStateModel model = simResult.getModel();
		
		GeneticConditions gc = simResult.getGeneticConditions();
		EnvironmentalConditions ec = new EnvironmentalConditions();
		
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
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
		
		return fvaResult.getFluxValues().getValue(productID);
	}
	
	@Override
	public double getWorstFitness() {
		return (boolean) (getParameterValue(FVA_SENSE_PARAM_MAXIMIZATION)) ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(FVA_SENSE_PARAM_MAXIMIZATION);
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return ((Boolean) getParameterValue(FVA_SENSE_PARAM_MAXIMIZATION) ? "max" : "min") + "~FVA~" + ((Boolean) getParameterValue(FVA_SENSE_PARAM_SENSE) ? "max~" : "min~") + getParameterValue(FVA_SENSE_PARAM_PRODUCT);
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getBuilderString() {
		return ID + "(" + getParameterValue(FVA_SENSE_PARAM_BIOMASS) + "," + getParameterValue(FVA_SENSE_PARAM_PRODUCT) + "," + getParameterValue(FVA_SENSE_PARAM_MAXIMIZATION) + "," + getParameterValue(FVA_SENSE_PARAM_SENSE) + ","
				+ getParameterValue(FVA_SENSE_PARAM_SOLVER) + ")";
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
