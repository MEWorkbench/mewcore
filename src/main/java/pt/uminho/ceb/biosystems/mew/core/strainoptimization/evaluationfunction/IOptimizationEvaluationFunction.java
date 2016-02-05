package pt.uminho.ceb.biosystems.mew.core.strainoptimization.evaluationfunction;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;

public interface IOptimizationEvaluationFunction {
	
	public int getNumberOfObjectives();
	
	public List<IObjectiveFunction> getObjectiveFunctions();

}
