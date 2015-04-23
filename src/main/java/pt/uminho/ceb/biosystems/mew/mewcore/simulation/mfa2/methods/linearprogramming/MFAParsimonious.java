package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.linearprogramming;

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
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties.MFAProperties;

public class MFAParsimonious<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem>{

	/** The formulation for the initial objective function, for instance Maximize the Biomass */
	protected T initialProblem;
	
	
	public MFAParsimonious(ISteadyStateModel model) {
		super(model);
		possibleProperties.add(MFAProperties.PARSIMONIUS_PROBLEM);
		possibleProperties.add(MFAProperties.PARSIMONIUS_OBJECTIVE_VALUE);
		possibleProperties.add(MFAProperties.RELAX_COEF);
	}
		
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MFAWithSolvers getInitialProblem() {
		if(initialProblem == null){
			try {
				initialProblem = (T) ManagerExceptionUtils.testCast(propreties, MFAWithSolvers.class, MFAProperties.PARSIMONIUS_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
				initialProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				initialProblem = null;
			}
			
			if(initialProblem == null){
				initialProblem = (T) new MFALP(model);
				initialProblem.putAllProperties(propreties);
				setProperty(MFAProperties.PARSIMONIUS_PROBLEM, initialProblem);
			}
		}
		return initialProblem;
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
			SteadyStateSimulationResult result =  initialProblem.simulate();
			value = result.getOFvalue();
			setProperty(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, value);

		}
		return value;
	}

	public double getRelaxCoef() {
		Double coef = null;
		try {
			coef = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.RELAX_COEF, false);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
			coef = 0.99999;
		} catch (MandatoryPropertyException e) {
			coef = 0.99999;
		}
		setProperty(SimulationProperties.RELAX_COEF, coef);
		return coef;
	}	
	
	public void setInitProblem(T problem){
		this.initialProblem = problem;
		setProperty(SimulationProperties.PARSIMONIUS_PROBLEM, initialProblem);
	}
	
	public void setObjectiveValue(Double value){
		setProperty(SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, value);
	}

	
	@Override
	public LPProblem constructEmptyProblem() {
		try {
			getInitialProblem();
		} catch (Exception e) {e.printStackTrace();}
		
		return (initialProblem==null) ? null : initialProblem.constructEmptyProblem();
	}

	@Override
	protected void createConstrains() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		getInitialProblem();
		
		List<LPConstraint> constraits = initialProblem.getProblem().getConstraints();
		
		problem.setConstraints(constraits);
		
		double b;
		try {
			b = getObjectiveValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new WrongFormulationException(e);
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			throw new WrongFormulationException(e);
		}
		
		
		// Setting the constraint for the initial Objective Function, for instance setting the value of the biomass to be the one maximized by the initial problem formulation
		problem.addConstraint(initialProblem.getProblem().getObjectiveFunction().getRow(),
				LPConstraintType.EQUALITY, b * getRelaxCoef());
	}
	

	
	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		problem.setVariables(initialProblem.getProblem().getVariables());
		setIdToIndexVarMapings(initialProblem.getIdToIndexVarMapings());
		setIndexToIdVarMapings(initialProblem.getIndexToIdVarMapings());
	}
	
	
	@Override
	/** Minimization of the sum of all fluxes */
	protected void createObjectiveFunction() {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		Set<String> reactionIds = model.getReactions().keySet();
		for(String rId : reactionIds){
			int varIdx= idToIndexVarMapings.get(rId);
			
			// L1 Term because of the norm of the fluxes in the objective function
			objTerms.add(new L1VarTerm(varIdx));
		}	
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "min Σ|V|";
	}
}
