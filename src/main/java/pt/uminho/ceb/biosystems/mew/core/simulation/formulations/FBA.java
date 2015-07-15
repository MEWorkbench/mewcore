package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;

public class FBA extends AbstractSSBasicSimulation<LPProblem> {
	
	public FBA(
			ISteadyStateModel model) {
		super(model);
		initFBAPros();
	}
	
	private void initFBAPros() {
		
		mandatoryProperties.add(SimulationProperties.IS_MAXIMIZATION);
		optionalProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
	}
	
	public void setObjectiveFunction(Map<String, Double> obj_coef) {
		properties.put(SimulationProperties.OBJECTIVE_FUNCTION, obj_coef);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getObjectiveFunction() {
		Map<String, Double> obj_coef = null;
		try {
			obj_coef = ManagerExceptionUtils.testCast(
					properties,
					Map.class,
					SimulationProperties.OBJECTIVE_FUNCTION,
					false);
		} catch (Exception e) {
			obj_coef = new HashMap<String, Double>();
			obj_coef.put(model.getBiomassFlux(), 1.0);
		}
		return obj_coef;
	}
	
	public void setIsMaximization(boolean isMaximization) {
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);

	}
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException {
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());
		objTerms.clear();
		
		Map<String, Double> obj_coef = getObjectiveFunction();
		for (String r : obj_coef.keySet()) {
			double coef = obj_coef.get(r);
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get(r), coef, 0.0));
		}
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		String ret = "";
		boolean max = true;
		try {
			max = getIsMaximization();
		} catch (PropertyCastException e) {
			e.printStackTrace();
		} catch (MandatoryPropertyException e) {
			e.printStackTrace();
		}
		
		if (max)
			ret = "max:";
		else
			ret = "min:";
		Map<String, Double> obj_coef = getObjectiveFunction();
		for (String id : obj_coef.keySet()) {
			double v = obj_coef.get(id);
			if (v != 1)
				ret += " " + v;
			ret += " " + id;
		}
		
		return ret;
	}

}
