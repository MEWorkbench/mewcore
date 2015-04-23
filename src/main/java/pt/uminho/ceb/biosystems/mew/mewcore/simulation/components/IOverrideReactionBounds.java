package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;

public interface IOverrideReactionBounds {

	public ReactionConstraint getReactionConstraint(String reactionId);
	
	public ReactionConstraint getReactionConstraint(int reactionIndex);

	
}
