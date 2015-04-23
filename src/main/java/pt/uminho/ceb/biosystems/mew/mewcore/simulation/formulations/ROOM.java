package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public class ROOM extends AbstractSSReferenceSimulation<MILPProblem>{

	private static String booleanPrefix = "y";
	
	public ROOM(ISteadyStateModel model) {
		super(model);
		initRefProperties();
	}

	private void initRefProperties() {
		possibleProperties.add(SimulationProperties.ROOM_DELTA);
		possibleProperties.add(SimulationProperties.ROOM_EPSILON);
	}
	
	@Override
	public MILPProblem constructEmptyProblem() {
		return new MILPProblem();
		
	}

	@Override
	protected void createObjectiveFunction() {
		
		boolean useDrains = getUseDrainsInRef();
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		for(String rId : model.getReactions().keySet()){
			int idx = getIdxVar(booleanPrefix+rId);
			if((useDrains || !model.getReaction(rId).getType().equals(ReactionType.DRAIN))){
				objTerms.add(new VarTerm(idx));
			}
		}
		
	}

	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		super.createVariables();		
		int problemVar = problem.getNumberVariables();
		
		for(String reactionId: model.getReactions().keySet()){
			String booleanVarId = booleanPrefix+reactionId;
			((MILPProblem)problem).addIntVariable(booleanVarId, 0, 1);
			putVarMappings(booleanVarId, problemVar);
			problemVar++;
		}
	}
	
	protected void createConstrains() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		double DELTA = getDelta();
		double EPSILON = getEpsilon();
		
		wtReference = getWTReference();
		super.createConstrains();
		
		for(String reactionID : wtReference.keySet()){
			
			int reactionIdx = getIdxVar(reactionID);
			double wt_i = wtReference.get(reactionID);
			double w_u = wt_i + DELTA *Math.abs(wt_i) + EPSILON;
			double w_l = wt_i - DELTA *Math.abs(wt_i) - EPSILON;
			
			ReactionConstraint rc = model.getReactionConstraint(reactionID);
			
			int booleanVarIdx = getIdxVar(booleanPrefix+reactionID);
			
			LPProblemRow row_u = new LPProblemRow();
			try {
				row_u.addTerm(reactionIdx, 1.0);
				row_u.addTerm(booleanVarIdx, w_u - rc.getUpperLimit());
				LPConstraint constraint_u = new LPConstraint(LPConstraintType.LESS_THAN, row_u, w_u);
				problem.addConstraint(constraint_u);
				
				LPProblemRow row_l = new LPProblemRow();
				row_l.addTerm(reactionIdx, 1.0);
				row_l.addTerm(booleanVarIdx, w_l - rc.getLowerLimit()); // var: yi
				LPConstraint constraint_l = new LPConstraint(LPConstraintType.GREATER_THAN, row_l, w_l);
				problem.addConstraint(constraint_l);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException(e);
			}
			
		}	
	}
	
	private double getEpsilon() {
		double epsilon = 0.001;
		try {
			epsilon = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.ROOM_EPSILON,
					false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getLocalizedMessage() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {} 
		
		return epsilon;
	}

	private double getDelta() {
		
		double delta = 0.03;
		try {
			delta = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, SimulationProperties.ROOM_DELTA,
					false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getLocalizedMessage() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {} 
		
		return delta;
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "Σ bi ";
	}
	
	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
			
		SteadyStateSimulationResult result = super.convertLPSolutionToSimulationSolution(solution);
		
		if(solution!=null && (solution.getSolutionType().equals(LPSolutionType.BOUND) || solution.getSolutionType().equals(LPSolutionType.FEASIBLE) || solution.getSolutionType().equals(LPSolutionType.OPTIMAL))){
//			System.out.println("Solution: "+solution.getOfValue()+"/ type="+solution.getSolutionType().toString());
			result.addComplementaryInfoReactions(SimulationProperties.ROOM_BOOLEAN_VAR_VALUES, getBooleanVariable(solution));
		}
		return result;
	}
	
	public MapStringNum getBooleanVariable(LPSolution solution){
		MapStringNum ret = new MapStringNum();
		
		for(String reactionId : model.getReactions().keySet()){
			String varProblemName = booleanPrefix+reactionId;
			int varIdx = getIdxVar(varProblemName);
			double value = solution.getValues().get(varIdx);
			ret.put(reactionId, value);
		}
		
		return ret;
	}
}
