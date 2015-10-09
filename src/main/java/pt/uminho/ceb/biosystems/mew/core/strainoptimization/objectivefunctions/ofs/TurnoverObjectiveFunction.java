package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;

/**
 * Max/min turnover of a specific metabolite production (here production is
 * always assumed)
 * 
 * 
 * @author pmaia
 * @date May 7, 2014
 * @version 0.1
 * @since Analysis
 */
public class TurnoverObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ID = "TURN";
	
	public static final String	TURN_PARAM_TARGET_METABOLITE	= "TargetMetabolite";
	public static final String	TURN_PARAM_BIOMASS				= "Biomass";
	public static final String	TURN_PARAM_MIN_BIOMASS			= "MinBiomass";
	public static final String	TURN_PARAM_MAXIMIZATION			= "Maximization";
	
	protected Map<String, Double>	_reactions	= null;
	protected boolean				_init		= false;
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(TURN_PARAM_TARGET_METABOLITE, ObjectiveFunctionParameterType.METABOLITE);
		myparams.put(TURN_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(TURN_PARAM_MIN_BIOMASS, ObjectiveFunctionParameterType.DOUBLE);
		myparams.put(TURN_PARAM_MAXIMIZATION, ObjectiveFunctionParameterType.BOOLEAN);
		return Collections.unmodifiableMap(myparams);
	}
	
	public TurnoverObjectiveFunction() {
		super();
	}
	
	public TurnoverObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public TurnoverObjectiveFunction(String metaboliteID, String biomassID, Double minBiomass, Boolean isMaximization) {
		super(metaboliteID, biomassID, minBiomass, isMaximization);
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(TURN_PARAM_TARGET_METABOLITE, params[0]);
		setParameterValue(TURN_PARAM_BIOMASS, params[1]);
		setParameterValue(TURN_PARAM_MIN_BIOMASS, params[2]);
		setParameterValue(TURN_PARAM_MAXIMIZATION, params[3]);
	}
	
	public void init(ISteadyStateModel model) {
		
		String metID = (String) getParameterValue(TURN_PARAM_TARGET_METABOLITE);
		_reactions = new HashMap<String, Double>();
		
		int metaIndex = model.getMetaboliteIndex(metID);
		for (String reaction : model.getReactions().keySet()) {
			int reacIndex = model.getReactionIndex(reaction);
			double stoich = model.getStoichiometricValue(metaIndex, reacIndex);
			if (stoich != 0) _reactions.put(reaction, stoich);
		}
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		String biomassID = (String) getParameterValue(TURN_PARAM_BIOMASS);
		Double minBiomass = (Double) getParameterValue(TURN_PARAM_MIN_BIOMASS);
		
		if (!_init) {
			init(simResult.getModel());
		}
		
		if (simResult.getFluxValues().get(biomassID) < minBiomass)
			return getWorstFitness();
		else {
			double fit = 0.0;
			for (String reactionID : _reactions.keySet()) {
				double value = simResult.getFluxValues().get(reactionID) * _reactions.get(reactionID);
				if (value > 0) fit += value;
			}
			
			return fit;
		}
	}
	
	public Map<String, Double> consumingProducing(SteadyStateSimulationResult simResult, boolean producing) {
		if (!_init) {
			init(simResult.getModel());
		}
		
		Map<String, Double> toret = new HashMap<String, Double>();
		
		for (String reactionID : _reactions.keySet()) {
			double value = simResult.getFluxValues().get(reactionID) * _reactions.get(reactionID);
			if (producing && value > 0) {
				toret.put(reactionID, value);
			} else if (!producing && value < 0) {
				toret.put(reactionID, value);
			}
		}
		
		return toret;
	}
	
	@Override
	public double getWorstFitness() {
		return isMaximization() ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public boolean isMaximization() {
		return (Boolean) getParameterValue(TURN_PARAM_MAXIMIZATION);
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	public String toString() {
		String objSense = (isMaximization()) ? "Maximize " : "Minimize ";
		return objSense + " " + getParameterValue(TURN_PARAM_TARGET_METABOLITE);
	}
	
	@Override
	public String getShortString() {
		return (String) getParameterValue(TURN_PARAM_TARGET_METABOLITE);
	}
	
	@Override
	public String getLatexString() {
		return "$" + getLatexFormula() + "$";
	}
	
	@Override
	public String getLatexFormula() {
		String sense = isMaximization() ? "max" : "min";
		return "TURN = " + sense + "\\;\\text{" + getParameterValue(TURN_PARAM_TARGET_METABOLITE) + "}";
	}
	
	@Override
	public String getBuilderString() {
		return "TURN (" + getParameterValue(TURN_PARAM_TARGET_METABOLITE) + "," + getParameterValue(TURN_PARAM_BIOMASS) + "," + getParameterValue(TURN_PARAM_MIN_BIOMASS) + "," + isMaximization() + ")";
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
