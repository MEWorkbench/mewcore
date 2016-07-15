package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

/**
 * 
 * 
 * 
 * @author pvilaca
 * @date Mar 3, 2016
 * @version 0.1
 * @since mewcore - staging
 * @param <T> - An <code>LPProblem</code> to be used as the inner problem.
 */
public class LPVariabilityProblem <T extends AbstractSSBasicSimulation<?>> extends AbstractSSBasicSimulation<LPProblem>{

	public static final double DEFAULT_RELAX = 0.99999999;
//	public static final double DEFAULT_RELAX = 0.99;
	protected T initProblem;
		
	public LPVariabilityProblem(ISteadyStateModel model) {
		super(model);
		initPFBAPros();
	}
	
	private void initPFBAPros() {
		
		mandatoryProperties.add(SimulationProperties.IS_MAXIMIZATION);
		mandatoryProperties.add(SimulationProperties.PARSIMONIOUS_PROBLEM);
		mandatoryProperties.add(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE);
		mandatoryProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
		optionalProperties.add(SimulationProperties.RELAX_COEF);
	}

	public double getObjectiveValue() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		Double value = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, false);
		return value;
	}
	
	public void setBaseMethod(AbstractSSBasicSimulation<?> method){
		setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, method);
	}
	
	public void setObjValueBaseMethod(Double value){
		setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, value);
	}
	
	@SuppressWarnings("unchecked")
	public T getInitProblem() {
		if(initProblem == null){
			initProblem = (T) ManagerExceptionUtils.testCast(properties, AbstractSSBasicSimulation.class, SimulationProperties.PARSIMONIOUS_PROBLEM, false);
		}
		
		return initProblem;
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		objTerms = new ArrayList<AbstractObjTerm>();
		return getInitProblem().constructEmptyProblem();
	}

	
	private Double relaxFactor(Double x, Double mantainingRelax){
		
		return Math.abs(x * (1-mantainingRelax));
	}
	
	@Override
	protected void createConstraints() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException  {
		
		ArrayList<LPConstraint> constraits = new ArrayList<LPConstraint>(this.getInitProblem().getProblem().getConstraints());
		
		problem.setConstraints(constraits);
				
		try {
			
			double oldObjective = getObjectiveValue();
			double relaxFactor = relaxFactor(oldObjective, getRelaxCoef());
			
			LPConstraintType type = LPConstraintType.LESS_THAN; 
			double value =  oldObjective + relaxFactor;
			
			if(initProblem.getProblem().getObjectiveFunction().isMaximization()){
				type = LPConstraintType.GREATER_THAN; 
				value = oldObjective - relaxFactor;
			}
			
			problem.addConstraint(initProblem.getProblem().getObjectiveFunction().getRow(),type, value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WrongFormulationException(e);
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WrongFormulationException(e);
		}
		
	}
	
	protected double getRelaxCoef() {
		
		Double coef = null;
		try {
			coef = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.RELAX_COEF, false);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			coef = DEFAULT_RELAX;
		} catch (MandatoryPropertyException e) {
			coef = DEFAULT_RELAX;
		}
		
		setProperty(SimulationProperties.RELAX_COEF, coef);
		
		return coef;
	}

	@Override
	protected void createVariables() throws SolverException, MandatoryPropertyException, PropertyCastException, WrongFormulationException {
		
		problem.setVariables(new ArrayList<>(this.getInitProblem().getProblem().getVariables()));
		
		setIdToIndexVarMapings(initProblem.getIdToIndexVarMapings());
		setIndexToIdVarMapings(initProblem.getIndexToIdVarMapings());
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getObjectiveFunction(){
		Map<String, Double> obj_coef = ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.OBJECTIVE_FUNCTION, false);
		return obj_coef;
	}
	
	public void setObjectiveFunction(Map<String, Double> of){
		setProperty(SimulationProperties.OBJECTIVE_FUNCTION, of);
	}
	
	public void setIsMaximization(boolean isMaximization){
		setProperty(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
	
	@Override
	protected void createObjectiveFunction() {
		
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());
		
		
		Map<String, Double> obj_coef = getObjectiveFunction();
		for(String r : obj_coef.keySet()){
			double coef = obj_coef.get(r);
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get(r), coef, 0.0));
		}
	}


	public void setInitProblem(T problem){
		this.initProblem= problem;
		setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, initProblem);
	}
	
	public void setObjectiveValue(Double value){
		setProperty(SimulationProperties.PARSIMONIOUS_OBJECTIVE_VALUE, value);
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
		
		if(max)
			ret = "max:";
		else
			ret = "min:";
		Map<String, Double> obj_coef = getObjectiveFunction();
		for(String id : obj_coef.keySet()){
			double v = obj_coef.get(id);
			if(v!=1)
				ret += " " + v;
			ret +=  " " + id;
		}
		
		return ret;
	}
	
	@Override
	public SteadyStateSimulationResult simulate(){
		
		SteadyStateSimulationResult ret = super.simulate();
		problem = null;
		return ret;
	}
}
