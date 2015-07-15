package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.PFBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractTurnoverFormulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

public class MiMBl_NormalizeSolution extends PFBA<AbstractTurnoverFormulation<LPProblem>> {
	
	public MiMBl_NormalizeSolution(ISteadyStateModel model) {
		super(model);
		optionalProperties.add(SimulationProperties.WT_REFERENCE);
	}
	
	@SuppressWarnings("unchecked")
	public AbstractSSBasicSimulation<LPProblem> getInternalProblem() {
		
		if (internalProblem != null) {
			try {
				internalProblem = ManagerExceptionUtils.testCast(properties, AbstractTurnoverFormulation.class, SimulationProperties.PARSIMONIOUS_PROBLEM, false);
			} catch (MandatoryPropertyException e) {
				internalProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				internalProblem = null;
			}
		}
		return internalProblem;
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		
		return "min ∑ (1/Vwt)*|Vwt - v|";
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getWTReference() {
		
		Map<String, Double> wtReference = null;
		
		try {
			wtReference = ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.WT_REFERENCE, true);
		} catch (Exception e) {
			throw new Error();
		}
		
		return wtReference;
	}
	
	@Override
	protected void createObjectiveFunction() {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		Map<String, Double> wtRef = getWTReference();
		
		for (String id : wtRef.keySet()) {
			int idxVar = idToIndexVarMapings.get(id);
			double value = wtRef.get(id);
			
			if (value != 0.0) objTerms.add(new L1VarTerm(idxVar, -1 / value, 1));
		}		
	}
	
	@Override
	protected void createConstraints() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		getInternalProblem();
		List<LPConstraint> constraits = internalProblem.getProblem().getConstraints();
		
		problem.setConstraints(constraits);
		
		try {
			double objectiveValue = getObjectiveValue();
			parsimoniousConstraint = createParsimoniousConstraint(objectiveValue);
			problem.addConstraint(parsimoniousConstraint);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrongFormulationException(e);
		} catch (SolverException e) {
			e.printStackTrace();
			throw new WrongFormulationException(e);
		}
		
	}
	
	protected LPConstraint createParsimoniousConstraint(double objectiveValue) throws WrongFormulationException, MandatoryPropertyException, PropertyCastException {
		LPProblemRow fbaRow = getInternalProblem().getProblem().getObjectiveFunction().getRow();
		return new LPConstraint(LPConstraintType.LESS_THAN, fbaRow, objectiveValue);
	}

	
	public double computeObjectiveValue() throws WrongFormulationException, SolverException, PropertyCastException, MandatoryPropertyException, IOException {
		double value = Math.abs(super.computeObjectiveValue()) * (1 + (1 - getRelaxCoef()));
		return value;
	}
	
	@Override
	protected void putMetaboliteExtraInfo(LPSolution solution, SteadyStateSimulationResult res) {
		super.putMetaboliteExtraInfo(solution, res);
		
		//		System.out.println(new TreeMap<String, Double>(((MiMBl) initProblem).getCalculatedTurnOvers(solution)));
	}
	
	@Override
	public void setInitProblem(AbstractTurnoverFormulation<LPProblem> problem) {
		this.internalProblem = problem;
		setProperty(SimulationProperties.PARSIMONIOUS_PROBLEM, internalProblem);
	}
	
	public void setReference(Map<String, Double> wtReference) {
		setProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
}
