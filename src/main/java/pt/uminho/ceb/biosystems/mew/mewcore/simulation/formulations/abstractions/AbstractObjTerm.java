package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;

public abstract class AbstractObjTerm{
	
	protected int varIndex;
	protected double multiplier;
	protected double additional;
		
	public AbstractObjTerm(int varIndex, double multiplier, double additional) {

		this.varIndex = varIndex;
		this.multiplier = multiplier;
		this.additional = additional;
	}

	
	abstract public Map<String, Integer> addObjectiveTermToProblem(LPProblem problem) throws WrongFormulationException;
		
	public int getProblemVarIdx(){
		return varIndex;
	}
	
	public double getMutiplierFactor(){
		return multiplier;
	}
	
	public double getAdditionalFactor(){
		return additional;
	}

}
