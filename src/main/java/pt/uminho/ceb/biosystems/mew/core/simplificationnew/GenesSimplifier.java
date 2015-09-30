package pt.uminho.ceb.biosystems.mew.core.simplificationnew;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class GenesSimplifier extends AbstractPersistenceSimplifier{
	
	public GenesSimplifier(ISteadyStateModel model, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		super(model, referenceFD, envCond, solver);
	}

	@Override
	public Set<String> getGeneticConditionsIDs(GeneticConditions gc) {
		return gc.getGeneList().getGeneIds();
	}

	@Override
	public void nextGeneticCondition(GeneticConditions solution, String geneID, double expressionLevel) throws Exception {
		solution.getGeneList().addGene(geneID, expressionLevel);
		solution.updateReactionsList((ISteadyStateGeneReactionModel) model);
	}

	@Override
	public void removeGeneticCondition(GeneticConditions gc, String id) throws Exception {
		gc.getGeneList().removeGene(id);
		gc.updateReactionsList((ISteadyStateGeneReactionModel) model);
	}

	@Override
	public double getExpressionLevel(GeneticConditions gc, String id) {
		return gc.getGeneList().getGeneExpression(id);
	}

}
