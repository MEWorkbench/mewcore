package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;


public class PARSIMONIOUS<T extends AbstractSSBasicSimulation<?>> extends AbstractSSBasicSimulation<LPProblem>{

	public static double DEFAULT_RELAX = 0.99999;
	protected T initProblem;
		
	public PARSIMONIOUS(ISteadyStateModel model) {
		super(model);
		initPFBAPros();
	}
	
	private void initPFBAPros() {
		possibleProperties.add(SimulationProperties.PARSIMONIUS_PROBLEM);
		possibleProperties.add(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE);
		possibleProperties.add(SimulationProperties.RELAX_COEF);
	}

	public double getObjectiveValue() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			value = null;
		} catch (MandatoryPropertyException e) {
			value = null;
		}
		
		if(value == null){
			SteadyStateSimulationResult result;
				result = initProblem.simulate();

			value = result.getOFvalue();
			setProperty(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, value);
		}
		
		
		return value;
	}
	
	
	public AbstractSSBasicSimulation getInitProblem(){
		
		if(initProblem == null){
			try {
				initProblem = (T) ManagerExceptionUtils.testCast(propreties, AbstractSSBasicSimulation.class, SimulationProperties.PARSIMONIUS_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
				initProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				initProblem = null;
			}
			
			if(initProblem == null){
				initProblem = (T) new FBA(model);
				initProblem.putAllProperties(propreties);
				setProperty(SimulationProperties.PARSIMONIUS_PROBLEM, initProblem);
			}
		}
		
		return initProblem;
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		getInitProblem();
		return initProblem.constructEmptyProblem();
	}

	@Override
	protected void createConstrains() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		getInitProblem();
		List<LPConstraint> constraits = initProblem.getProblem().getConstraints();
		
		problem.setConstraints(constraits);
				
		try {
			problem.addConstraint(initProblem.getProblem().getObjectiveFunction().getRow(),
					LPConstraintType.EQUALITY,getObjectiveValue() * getRelaxCoef());
		} catch (IOException e) {
			throw new WrongFormulationException(e);
		}		
		
	}
	
	protected double getRelaxCoef() {
		
		Double coef = null;
		try {
			coef = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.RELAX_COEF, false);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			coef = DEFAULT_RELAX;
		} catch (MandatoryPropertyException e) {
			coef = DEFAULT_RELAX;
		}
		
		setProperty(SimulationProperties.RELAX_COEF, coef);
		
		return coef;
	}

	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		problem.setVariables(initProblem.getProblem().getVariables());
		setIdToIndexVarMapings(initProblem.getIdToIndexVarMapings());
		setIndexToIdVarMapings(initProblem.getIndexToIdVarMapings());
	}
	
	@Override
	protected void createObjectiveFunction() {
		
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		Set<String> reactionIds = model.getReactions().keySet();
		for(String rId : reactionIds){
			int varIdx= idToIndexVarMapings.get(rId);
			objTerms.add(new L1VarTerm(varIdx));
		}
		
	}


	public void setInitProblem(T problem){
		this.initProblem= problem;
		setProperty(SimulationProperties.PARSIMONIUS_PROBLEM, initProblem);
	}
	
	public void setObjectiveValue(Double value){
		setProperty(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, value);
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		
		return "min Σ|V|";
	}
}
