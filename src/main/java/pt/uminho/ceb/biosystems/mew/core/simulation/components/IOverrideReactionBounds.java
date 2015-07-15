package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;

public interface IOverrideReactionBounds {

	public ReactionConstraint getReactionConstraint(String reactionId);
	
	public ReactionConstraint getReactionConstraint(int reactionIndex);
	
	public Set<String> getOverriddenReactions();

	
}
