package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;

public class ReactionsSimplifier extends AbstractPersistenceSimplifier{

	
	public ReactionsSimplifier(Map<String,Map<String,Object>> simulationConfiguration) {
		super(simulationConfiguration);
	}

	@Override
	public Set<String> getGeneticConditionsIDs(GeneticConditions gc) {
		return gc.getReactionList().getReactionIds();
	}

	@Override
	public void nextGeneticCondition(GeneticConditions solution, String reactionID, double expressionLevel) {
		solution.getReactionList().addReaction(reactionID, expressionLevel);
	}

	@Override
	public void removeGeneticCondition(GeneticConditions gc, String id) {
		gc.getReactionList().removeReaction(id);
	}

	@Override
	public double getExpressionLevel(GeneticConditions gc, String id) {
		return gc.getReactionList().getReactionFlux(id);
	}

}
