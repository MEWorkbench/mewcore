package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations;

import java.io.IOException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.QuadraticTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblemRow;

public class MOMA extends AbstractSSReferenceSimulation<QPProblem> {
	
	public MOMA(ISteadyStateModel model) {
		super(model);
	}
	
	@Override
	public QPProblem constructEmptyProblem() {
		return new QPProblem();
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		getWTReference();
		boolean useDrains = getUseDrainsInRef();
		
		problem.setQPObjectiveFunction(new QPProblemRow());
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		for (String rId : wtReference.keySet()) {
			int idx = getIdxVar(rId);
			double value = wtReference.get(rId);
			
			if ((useDrains || !model.getReaction(rId).getType().equals(ReactionType.DRAIN))) {
				objTerms.add(new QuadraticTerm(idx, 1, -1 * value));
			}
		}
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		return "Σ(v-wt)²";
	}
	
	
	
	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException {
		SteadyStateSimulationResult res = super.convertLPSolutionToSimulationSolution(solution);
		
		Map<String, Double> wt = getWTReference();
		Map<String, Double> sol = res.getFluxValues();
		
		double value = momaFOResult(sol, wt);
		res.setOFvalue(value);
		
		return res;
	}
	
	@Override
	protected LPSolution simulateProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		return super.simulateProblem();
	}
	
	static public double momaFOResult(Map<String, Double> ref, Map<String, Double> momaFD) {
		double ret = 0.0;
		for (String id : ref.keySet()) {
			ret += Math.pow((ref.get(id) - momaFD.get(id)), 2);
		}
		return ret;
	}
	
}
