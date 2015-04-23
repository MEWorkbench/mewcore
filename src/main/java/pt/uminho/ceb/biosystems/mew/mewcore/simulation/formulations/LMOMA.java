package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public class LMOMA extends AbstractSSReferenceSimulation<LPProblem>{

	
	public LMOMA(ISteadyStateModel model) {
		super(model);
	}

	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
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
