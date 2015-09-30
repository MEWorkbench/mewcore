//package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;
//
//import java.io.Serializable;
//import java.util.HashMap;
//import java.util.Map;
//
//import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
//import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
//import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
//
///**
// * Max/min turnover of a specific metabolite production (here production is
// * always assumed)
// * 
// * 
// * @author pmaia
// * @date May 7, 2014
// * @version 0.1
// * @since Analysis
// */
//public class TurnoverObjectiveFunction implements IObjectiveFunction, Serializable {
//	
//	private static final long		serialVersionUID	= 1L;
//	
//	protected String				_metaboliteID		= null;
//	protected String				_biomassID			= null;
//	protected double				_minBiomass			= 0.1;
//	protected boolean				_isMaximization		= true;
//	
//	protected Map<String, Double>	_reactions			= null;
//	protected boolean				_init				= false;
//	
//	public TurnoverObjectiveFunction(String metaboliteID, String biomassID, Double minBiomass, Boolean isMaximization) {
//		_metaboliteID = metaboliteID;
//		_biomassID = biomassID;
//		_minBiomass = minBiomass;
//		_isMaximization = isMaximization;
//	}
//	
//	public void init(ISteadyStateModel model) {
//		_reactions = new HashMap<String, Double>();
//		
//		int metaIndex = model.getMetaboliteIndex(_metaboliteID);
//		for (String reaction : model.getReactions().keySet()) {
//			int reacIndex = model.getReactionIndex(reaction);
//			double stoich = model.getStoichiometricValue(metaIndex, reacIndex);
//			if (stoich != 0) _reactions.put(reaction, stoich);
//		}
//	}
//	
//	@Override
//	public double evaluate(SteadyStateSimulationResult simResult) {
//		if (!_init) init(simResult.getModel());
//		
//		if (simResult.getFluxValues().get(_biomassID) < _minBiomass)
//			return getWorstFitness();
//		else {
//			double fit = 0.0;
//			for (String reactionID : _reactions.keySet()) {
//				double value = simResult.getFluxValues().get(reactionID) * _reactions.get(reactionID);
//				if (value > 0) fit += value;
//			}
//			
//			return fit;
//		}
//	}
//	
//	public Map<String, Double> consumingProducing(SteadyStateSimulationResult simResult, boolean producing) {
//		if (!_init) init(simResult.getModel());
//		Map<String, Double> toret = new HashMap<String, Double>();
//		
//		for (String reactionID : _reactions.keySet()) {
//			double value = simResult.getFluxValues().get(reactionID) * _reactions.get(reactionID);			
//			if (producing && value > 0){
//				toret.put(reactionID, value);
//			}else if(!producing && value < 0){
//				toret.put(reactionID, value);
//			}
//		}
//		
//		return toret;
//	}
//	
//	@Override
//	public double getWorstFitness() {
//		return isMaximization() ? -Double.MAX_VALUE : Double.MAX_VALUE;
//	}
//	
//	@Override
//	public ObjectiveFunctionType getType() {
//		return ObjectiveFunctionType.TURN;
//	}
//	
//	@Override
//	public boolean isMaximization() {
//		return _isMaximization;
//	}
//	
//	@Override
//	public double getUnnormalizedFitness(double fit) {
//		return fit;
//	}
//	
//	public String toString() {
//		String objSense = (_isMaximization) ? "Maximize " : "Minimize ";
//		return objSense + " " + _metaboliteID;
//	}
//	
//	@Override
//	public String getShortString() {
//		return _metaboliteID;
//	}
//	
//	@Override
//	public String getLatexString() {
//		return "$" + getLatexFormula() + "$";
//	}
//	
//	@Override
//	public String getLatexFormula() {
//		String sense = _isMaximization ? "max" : "min";
//		return "TURN = " + sense + "\\;\\text{" + _metaboliteID + "}";
//	}
//	
//	@Override
//	public String getBuilderString() {
//		return "TURN (" + _metaboliteID + "," + _biomassID + "," + _minBiomass + "," + _isMaximization + ")";
//	}
//	
//}
