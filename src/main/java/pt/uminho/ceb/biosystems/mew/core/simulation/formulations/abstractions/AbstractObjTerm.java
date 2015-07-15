package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;

public abstract class AbstractObjTerm{

	protected static double minValue = -Double.MAX_VALUE;
	protected static double maxValue = Double.MAX_VALUE;
	
	protected int varIndex;
	protected double multiplier;
	protected double additional;
		
	public AbstractObjTerm(int varIndex, double multiplier, double additional) {

		this.varIndex = varIndex;
		this.multiplier = multiplier;
		this.additional = additional;
	}
	
	abstract public Map<String, Integer> addObjectiveTermToProblem(LPProblem problem) throws WrongFormulationException;
	abstract public Map<String, Integer> addObjectiveTermToProblem(LPProblem problem,List<LPVariable> ofAssociatedVars, List<LPConstraint> ofAssociatedConstraints) throws WrongFormulationException;
		
	public int getProblemVarIndex(){
		return varIndex;
	}
	
	public double getMultiplierFactor(){
		return multiplier;
	}
	
	public double getAdditionalFactor(){
		return additional;
	}
	
	public static void setMinValue(double minVal){
		minValue = minVal;
	}
	
	public static void setMaxValue(double maxVal){
		maxValue = maxVal;
	}
	
	
}
