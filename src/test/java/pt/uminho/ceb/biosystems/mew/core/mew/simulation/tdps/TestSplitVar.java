package pt.uminho.ceb.biosystems.mew.core.mew.simulation.tdps;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class TestSplitVar {
	
	@Test
	public void test(){
		
		LPProblem problemRUI = new LPProblem();
		LPProblem problemMEW = new LPProblem();
		
		String posVarNameRui = "pos_var_rui";
		String negVarNameRui = "neg_var_rui";
		String posVarNameMew = "pos_var_mew";
		String negVarNameMew = "neg_var_mew";
		
		int idx = 1000;

		Double lower = 0.0;
		Double upper =  0.0;
		
		Map<String,Integer> newVarsRui = splitNegAndPosVariableRUI(problemRUI, idx, posVarNameRui, negVarNameRui, lower, upper);
		Map<String,Integer> newVarsMew = splitNegAndPosVariableMEW(problemMEW, idx, posVarNameMew, negVarNameMew, lower, upper);
		
		System.out.println("===[ RUI ]===");
		MapUtils.prettyPrint(newVarsRui);
		for(LPVariable var : problemRUI.getVariables())
			System.out.println(var.toString());
		for(LPConstraint cons : problemRUI.getConstraints())
			System.out.println(cons.toString());
		
		System.out.println("\n===[ MEW ]===");		
		MapUtils.prettyPrint(newVarsMew);		
		for(LPVariable var : problemMEW.getVariables())
			System.out.println(var.toString());
		for(LPConstraint cons : problemMEW.getConstraints())
			System.out.println(cons.toString());
		
		
	}
	
	static public ReactionConstraint getConvertedLimits(Double lower, Double upper, boolean isPositive) {
		double lowerLimit = 0.0;
		double upperLimit = Double.MAX_VALUE;
		
		if (isPositive) {
			lowerLimit = (lower != null && lower > 0) ? lower : 0.0;
			upperLimit = (upper != null && upper > 0) ? upper : 0.0;
		} else {
			upperLimit = (lower != null && lower < 0) ? Math.abs(lower) : 0.0;
			lowerLimit = (upper != null && upper < 0) ? Math.abs(upper) : 0.0;
		}
		
		return new ReactionConstraint(lowerLimit, upperLimit);
	}
	
	public static Map<String, Integer> splitNegAndPosVariableRUI(LPProblem problem, int idx, String posVarName, String negVarName, Double lower, Double upper) throws WrongFormulationException {
		Map<String, Integer> variablePositions = new HashMap<String, Integer>();
		
	
		int numVariables = problem.getNumberVariables();
		LPVariable normFoVarNeg = new LPVariable(negVarName, lower, 0);
		LPVariable normFoVarPos = new LPVariable(posVarName, 0, upper);
		
		int varPos = numVariables;
		int varNeg = numVariables + 1;
		problem.addVariable(normFoVarPos);
		variablePositions.put(posVarName, varPos);
		problem.addVariable(normFoVarNeg);
		variablePositions.put(negVarName, varNeg);
		
		LPProblemRow rowFoPosNeg = new LPProblemRow();
		try {
			rowFoPosNeg.addTerm(varPos, -1);
			rowFoPosNeg.addTerm(varNeg, -1);
			rowFoPosNeg.addTerm(idx, 1);
			
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			e.printStackTrace();
			throw new WrongFormulationException("Linear Programming Term Already Present");
		}
		
		LPConstraint constPosNeg = new LPConstraint(LPConstraintType.EQUALITY, rowFoPosNeg, 0.0);
		problem.addConstraint(constPosNeg);
		
		return variablePositions;
	}
	
	static public Map<String, Integer> splitNegAndPosVariableMEW(LPProblem problem, int idx, String posVarName, String negVarName, Double lower, Double upper) throws WrongFormulationException {
		Map<String, Integer> variablePositions = new HashMap<String, Integer>();

		ReactionConstraint negative = getConvertedLimits(lower, upper, false);
		ReactionConstraint positive = getConvertedLimits(lower, upper, true);
		
		double lowerLimitNegative = negative.getLowerLimit();
		double upperLimitNegative = negative.getUpperLimit();
		
		double lowerLimitPositive = positive.getLowerLimit();
		double upperLimitPositive = positive.getUpperLimit();
				
		
		//		System.out.println(idx + "\t[l]" + lower + "\t[u]" + upper+ "\t[ln]" + lowerLimitNegative + "\t[un]" + upperLimitNegative + "\t[lp]" + lowerLimitPossitive+ "\t[up]" + upperLimitPossitive );
		int numVariables = problem.getNumberVariables();
		LPVariable normFoVarNeg = new LPVariable(negVarName, lowerLimitNegative, upperLimitNegative);
		LPVariable normFoVarPos = new LPVariable(posVarName, lowerLimitPositive, upperLimitPositive);
		
		int varPos = numVariables;
		int varNeg = numVariables + 1;
		problem.addVariable(normFoVarPos);
		variablePositions.put(posVarName, varPos);
		problem.addVariable(normFoVarNeg);
		variablePositions.put(negVarName, varNeg);
		
		LPProblemRow rowFoPosNeg = new LPProblemRow();
		try {
			rowFoPosNeg.addTerm(varPos, -1);
			rowFoPosNeg.addTerm(varNeg, 1);
			rowFoPosNeg.addTerm(idx, 1);
			
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			e.printStackTrace();
			throw new WrongFormulationException("Linear Programming Term Already Present");
		}
		
		LPConstraint constPosNeg = new LPConstraint(LPConstraintType.EQUALITY, rowFoPosNeg, 0.0);
		problem.addConstraint(constPosNeg);
		
		return variablePositions;
	}

}
