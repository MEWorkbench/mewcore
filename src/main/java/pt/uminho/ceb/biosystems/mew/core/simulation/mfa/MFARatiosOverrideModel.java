package pt.uminho.ceb.biosystems.mew.core.simulation.mfa;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class MFARatiosOverrideModel extends MFAOverrideModel{
	
	/** Used to set the lower and upper bound of the fluxes in the ratio constraints, i.e., 
	 * if the direction of a flux of the ratio is positive, the flux is set to be positive,
	 * otherwise it is set to be negative */
	protected FluxRatioConstraintList fluxRatioConstraints;
	
	public MFARatiosOverrideModel(ISteadyStateModel model, 
			EnvironmentalConditions environmentalConditions, 
			GeneticConditions geneCond, 
			ExpMeasuredFluxes measuredFluxes, 
			FluxRatioConstraintList fluxRatioConstraints,
			Class<?> problemClass){
		super(model, environmentalConditions, geneCond, measuredFluxes, problemClass);
		this.fluxRatioConstraints = fluxRatioConstraints;
	}
	
	public MFARatiosOverrideModel(ISteadyStateModel model, EnvironmentalConditions environmentalConditions, Class<?> problemClass){
		this(model, environmentalConditions, null, null, null, problemClass);
	}

	
	public FluxRatioConstraintList getFluxRatioConstraints() {return fluxRatioConstraints;}
	public void setFluxRatioConstraints(FluxRatioConstraintList fluxRatioConstraints) {this.fluxRatioConstraints = fluxRatioConstraints;}

	
	@Override
	/** List of Priority for the Constraints:
	 * 1 - Knockouts
	 * 2 - Measured Fluxes
	 * 3 - Ratio Fluxes directionality
	 * 4 - Environmental Conditions
	 * 5 - Model Constraints */
	public ReactionConstraint getReactionConstraint(String reactionId)
	{	
		ReactionConstraint constraint = null;
		MFAConstraintSource constraintSource;
		boolean notFixedValue = true;
		// If the reaction is in the knockout list, set the lower and upper bounds to 0.0
		if(reactionList != null &&  reactionList.containsReaction(reactionId))
		{
			constraint = new ReactionConstraint(0.0, 0.0);
			constraintSource = MFAConstraintSource.GeneticCondition;
			notFixedValue = false;
		}
		else if(measuredFluxes!=null && measuredFluxes.containsKey(reactionId))
		{
			Pair<Double, Double> fluxValueStdev = measuredFluxes.get(reactionId);
			Double lowerBound = fluxValueStdev.getA();
			Double upperBound = fluxValueStdev.getA();
			
			Double stdev;
			if((stdev = fluxValueStdev.getB())!=null)
			{
				lowerBound -= stdev;
				upperBound += stdev;
			}
			constraint = new ReactionConstraint(lowerBound, upperBound);
			constraintSource = MFAConstraintSource.MeasuredFlux;
			notFixedValue = false;
		}
		else if(environmentalConditions != null && environmentalConditions.containsKey(reactionId))
		{
			ReactionConstraint envConstraint = environmentalConditions.getReactionConstraint(reactionId);
			constraint = new ReactionConstraint(envConstraint.getLowerLimit(), envConstraint.getUpperLimit());
			constraintSource = MFAConstraintSource.EnvironmentalCondition;
		}
		else
		{
			ReactionConstraint modelConstraint = model.getReactionConstraint(reactionId);
			constraint = new ReactionConstraint(modelConstraint.getLowerLimit(), modelConstraint.getUpperLimit());
			constraintSource = MFAConstraintSource.Model;
		}
		if(notFixedValue && overrideConstraint(reactionId, constraint))
			constraintSource = MFAConstraintSource.FluxRatio;
		
		createdFluxConstraints.put(reactionId, new Pair<ReactionConstraint, MFAConstraintSource>(constraint, constraintSource));
		return constraint;
	}
	
	/** If the flux belongs to some ratio constraint, this method changes the constraint to consider the signal value of the flux in the constraint */
	protected boolean overrideConstraint(String reactionId, ReactionConstraint constraint){	
		if(fluxRatioConstraints == null)
			return false;
	
		if(fluxRatioConstraints.isFluxNegative(reactionId) && constraint.getUpperLimit() > 0)
		{
			constraint.setUpperLimit(0);
			return true;
		}
		else if(fluxRatioConstraints.isFluxPositive(reactionId) && constraint.getLowerLimit() < 0)
		{
			constraint.setLowerLimit(0);
			return true;
		}
		return false;
	}
	
	@Override
	public Set<String> getOverriddenReactions(){
		
		return model.getReactions().keySet();
	}
	
}
