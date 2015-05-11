package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.variability;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class MFATightBoundsResult extends SteadyStateSimulationResult {

	private static final long serialVersionUID = -4930281784439070905L;
	
	public static final double MINUS_INFINITY = Double.MIN_VALUE;
	public static final double PLUS_INFINITY = Double.MAX_VALUE;
	
	private Map<String, ReactionConstraint> fluxBounds;
	
	
	public MFATightBoundsResult(ISteadyStateModel model, String method){
		super(model, method, null);
		this.fluxBounds = new HashMap<String, ReactionConstraint>();
	}

	public void addFluxBounds(String flux, Double lowerBound, Double upperBound){
		double lB = (lowerBound==null) ? MINUS_INFINITY : lowerBound;
		double uB = (upperBound==null) ? PLUS_INFINITY : upperBound;
		fluxBounds.put(flux, new ReactionConstraint(lB, uB));
	}
	
	
	public Map<String, ReactionConstraint> getFluxBounds() {return fluxBounds;}
	public void setFluxBounds(Map<String, ReactionConstraint> fluxBounds) {this.fluxBounds = fluxBounds;}
}
