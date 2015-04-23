package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;

public class QuadraticTerm extends AbstractObjTerm{

	//this method only support the function (mx + b)²
	public QuadraticTerm(int varIndex, double multiplier, double additional) {
		super(varIndex, multiplier, additional);
	}

	@Override
	public Map<String, Integer> addObjectiveTermToProblem(LPProblem problem) throws WrongFormulationException {
		//(ax +b)² = a²x² + 2abx + b^2
		
		//In this case the part for the objective function is ax² + abx
		//in the solver implementation we need check if it use this form.
		
			 
		QPProblem qpProblem = (QPProblem) problem;
		QPObjectiveFunction quadratic = qpProblem.getQPObjectiveFunction();
		LPObjectiveFunction lpObjective = qpProblem.getObjectiveFunction();
		
		quadratic.addQPTerm(varIndex, multiplier);
		try {
			lpObjective.addRow(varIndex, multiplier*additional);
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			throw new WrongFormulationException(e);
		}
		
		return null;
	}
}
