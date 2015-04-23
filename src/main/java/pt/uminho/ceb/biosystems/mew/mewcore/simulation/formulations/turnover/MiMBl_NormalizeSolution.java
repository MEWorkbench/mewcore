package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.PARSIMONIOUS;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractTurnoverFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public class MiMBl_NormalizeSolution extends PARSIMONIOUS<AbstractTurnoverFormulation<LPProblem>>{

	public MiMBl_NormalizeSolution(ISteadyStateModel model) {
		super(model);
		possibleProperties.add(SimulationProperties.WT_REFERENCE);
	}
	
	
	@SuppressWarnings("unchecked")
	public AbstractSSBasicSimulation<LPProblem> getInitProblem(){
		
		if(initProblem!=null){
			try {
				initProblem = ManagerExceptionUtils.testCast(propreties, AbstractTurnoverFormulation.class, SimulationProperties.PARSIMONIUS_PROBLEM, false);
			} catch (MandatoryPropertyException e) {
				initProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				initProblem = null;
			}
		}
		return initProblem;
	}
	
	
	public double getObjectiveValue() throws PropertyCastException, MandatoryPropertyException {
		Double value = null;
		
		
		value = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.PARSIMONIUS_OBJECTIVE_VALUE, false);
//		if(Math.abs(value) < 0.00001)
//			value = 0.1;
		
		return Math.abs(value);
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		
		return "min ∑ (1/Vwt)*|Vwt - v|";
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getWTReference(){
		
		Map<String, Double> wtReference = null;
			
		try {
			wtReference = ManagerExceptionUtils.testCast(propreties, Map.class, SimulationProperties.WT_REFERENCE, true);
		} catch (Exception e) {
			throw new Error();
		}
	
		return wtReference;
	}
	
	
	@Override
	protected void createObjectiveFunction() {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		Map<String, Double> wtRef = getWTReference();
		
		for(String id: wtRef.keySet()){
			int idxVar = idToIndexVarMapings.get(id);
			double value = wtRef.get(id);
			
			if(value !=0.0)
				objTerms.add(new L1VarTerm(idxVar,-1/value,1));
		}
		
	}
	
	@Override
	protected void createConstrains() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		getInitProblem();
		List<LPConstraint> constraits = initProblem.getProblem().getConstraints();
		
		problem.setConstraints(constraits);
		
//		System.out.println(constraits);
//		System.out.println(getObjectiveValue());
		
		problem.addConstraint(initProblem.getProblem().getObjectiveFunction().getRow(),
				LPConstraintType.LESS_THAN,getObjectiveValue() * (1+(1-getRelaxCoef())));
	
//		System.out.println("Variables:    " + problem.getVariables().size());
//		System.out.println("Restrictions: " + problem.getConstraints().size());
	}
	
	@Override
	protected void putMetaboliteExtraInfo(LPSolution solution,
			SteadyStateSimulationResult res) {
		super.putMetaboliteExtraInfo(solution, res);
		
//		System.out.println(new TreeMap<String, Double>(((MiMBl) initProblem).getCalculatedTurnOvers(solution)));
	}
	
	@Override
	public void setInitProblem(AbstractTurnoverFormulation<LPProblem> problem){
		this.initProblem= problem;
		setProperty(SimulationProperties.PARSIMONIUS_PROBLEM, initProblem);
	}
	
	public void setReference(Map<String, Double> wtReference){
		setProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
}
