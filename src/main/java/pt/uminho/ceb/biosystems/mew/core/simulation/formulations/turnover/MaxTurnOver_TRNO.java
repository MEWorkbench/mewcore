package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractTurnoverFormulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;

public class MaxTurnOver_TRNO extends AbstractTurnoverFormulation<LPProblem>{

	public MaxTurnOver_TRNO(ISteadyStateModel model) {
		super(model);
		initProperties();
	}

	private void initProperties() {
		optionalProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
		optionalProperties.add(SimulationProperties.IS_MAXIMIZATION);
	}

	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());
		String obj = getObjectiveFunction();
		int objIdx = -1;
		
		if(obj==null) objIdx = getIdxVar(model.getBiomassFlux());
		else objIdx=getTurnoverVarIndex(obj);
		
		System.out.println(objIdx);
		objTerms.add(new VarTerm(objIdx));
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "";
	}

	public void setIsMaximization(boolean isMaximization){
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	public void setObjectiveFunction(String metaboliteTurnOver){
		properties.put(SimulationProperties.MAX_TURN_OBJECTIVE_FUNCTION, metaboliteTurnOver);
	}
	
	public String getObjectiveFunction() throws PropertyCastException, MandatoryPropertyException{
		String obj= null;
			obj = ManagerExceptionUtils.testCast(properties, String.class, SimulationProperties.MAX_TURN_OBJECTIVE_FUNCTION, false);
		return obj;
	}
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
}
