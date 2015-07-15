package pt.uminho.ceb.biosystems.mew.core.simulation.mfa;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.OverrideSteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class MFAOverrideModel extends OverrideSteadyStateModel{
	
	protected Class<?> problemClass;
	protected ExpMeasuredFluxes measuredFluxes;
	
	protected Map<String, Pair<ReactionConstraint, MFAConstraintSource>> createdFluxConstraints;
	
	
	public MFAOverrideModel(ISteadyStateModel model, 
			EnvironmentalConditions environmentalConditions, 
			GeneticConditions geneCond, 
			ExpMeasuredFluxes measuredFluxes, 
			Class<?> problemClass){
		super(model, environmentalConditions, geneCond);
		this.measuredFluxes = measuredFluxes;
		this.problemClass = problemClass;
		this.createdFluxConstraints = new LinkedHashMap<String, Pair<ReactionConstraint,MFAConstraintSource>>();
	}
	
	public MFAOverrideModel(ISteadyStateModel model, EnvironmentalConditions environmentalConditions, Class<?> problemClass){
		this(model, environmentalConditions, null, null, problemClass);
	}
		
	
	public ExpMeasuredFluxes getMeasuredFluxes() {return measuredFluxes;}
	public void setMeasuredFluxes(ExpMeasuredFluxes measuredFluxes) {this.measuredFluxes = measuredFluxes;}
	
	public Map<String, Pair<ReactionConstraint, MFAConstraintSource>> getCreatedConstraints(){return this.createdFluxConstraints;}

	
	@Override
	/** List of Priority for the Constraints:
	 * 1 - Knockouts
	 * 2 - Measured Fluxes
	 * 3 - Environmental Conditions
	 * 4 - Model Constraints */
	public ReactionConstraint getReactionConstraint(String reactionId)
	{	
		ReactionConstraint constraint;
		MFAConstraintSource constraintSource;
		
		// If the reaction is in the knockout list, set the lower and upper bounds to 0.0
		if(reactionList != null &&  reactionList.containsReaction(reactionId))
		{
			constraint = new ReactionConstraint(0.0, 0.0);
			constraintSource = MFAConstraintSource.GeneticCondition;
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
		}
		else if(environmentalConditions != null && environmentalConditions.containsKey(reactionId))
		{
			constraint = environmentalConditions.getReactionConstraint(reactionId);
			constraintSource = MFAConstraintSource.EnvironmentalCondition;
		}
		else
		{
			constraint = model.getReactionConstraint(reactionId);
			constraintSource = MFAConstraintSource.Model;
		}
		createdFluxConstraints.put(reactionId, new Pair<ReactionConstraint, MFAConstraintSource>(constraint, constraintSource));
		return constraint;
	}

	@Override
	public ReactionConstraint getReactionConstraint(int reactionIndex)
	{
		String reactionId = model.getReactionId(reactionIndex);
		return getReactionConstraint(reactionId);
	}
	
	@Override
	public Set<String> getOverriddenReactions(){
		Set<String> toret = super.getOverriddenReactions();
		
		if(createdFluxConstraints!=null) toret.addAll(createdFluxConstraints.keySet());
		
		return toret;
	}
}
