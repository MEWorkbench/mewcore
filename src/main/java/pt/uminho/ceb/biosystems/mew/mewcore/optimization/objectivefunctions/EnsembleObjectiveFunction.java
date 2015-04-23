package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class EnsembleObjectiveFunction implements IObjectiveFunction, Serializable{

	private static final long serialVersionUID = 1L;
	private String simMethod = null;
	private IObjectiveFunction objFunc = null;
	
	public EnsembleObjectiveFunction(String simMethod, IObjectiveFunction objFunc){
		this.simMethod = simMethod;
		this.objFunc = objFunc;
	}

	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		return objFunc.evaluate(simResult);
	}

	@Override
	public double getWorstFitness() {
		return objFunc.getWorstFitness();
	}

	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.EN;
	}

	@Override
	public boolean isMaximization() {
		return objFunc.isMaximization();
	}

	@Override
	public double getUnnormalizedFitness(double fit) {
		return objFunc.getUnnormalizedFitness(fit);
	}

	@Override
	public String getShortString() {
		return "Ensemble: "+objFunc.getShortString()+" with "+simMethod;
	}

	/**
	 * @return the simMethod
	 */
	public String getSimMethod() {
		return simMethod;
	}

	/**
	 * @param simMethod the simMethod to set
	 */
	public void setSimMethod(String simMethod) {
		this.simMethod = simMethod;
	}

	/**
	 * @return the objFunc
	 */
	public IObjectiveFunction getObjFunc() {
		return objFunc;
	}

	/**
	 * @param objFunc the objFunc to set
	 */
	public void setObjFunc(IObjectiveFunction objFunc) {
		this.objFunc = objFunc;
	}

	@Override
	public String getLatexString() {
		return "$"+getLatexFormula()+"$";
	}
	
	@Override
	public String getLatexFormula() {
		return simMethod + "\\mapsto" + objFunc.getLatexFormula();
	}

	@Override
	public String getBuilderString() {
		// TODO Auto-generated method stub
		return null;
	}

}
