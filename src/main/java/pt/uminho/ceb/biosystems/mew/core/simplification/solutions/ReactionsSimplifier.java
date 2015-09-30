package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class ReactionsSimplifier extends AbstractPersistenceSimplifier{

	public ReactionsSimplifier(ISteadyStateModel model, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		super(model, referenceFD, envCond, solver);
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
