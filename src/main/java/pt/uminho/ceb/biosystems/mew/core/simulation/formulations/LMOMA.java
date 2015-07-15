package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;

public class LMOMA extends AbstractSSReferenceSimulation<LPProblem>{

	
	public LMOMA(ISteadyStateModel model) {
		super(model);
	}

	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), false);

		getWTReference();
		boolean useDrains = getUseDrainsInRef();
		for(String id: wtReference.keySet()){
			int idxVar = idToIndexVarMapings.get(id);
			double value = wtReference.get(id);
			
			if((useDrains || !model.getReaction(id).getType().equals(ReactionType.DRAIN))){
//				System.out.println(id + "\t" + value);
				objTerms.add(new L1VarTerm(idxVar,-value));
			}
		}
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "Σ |v-wt|";
	}
}
