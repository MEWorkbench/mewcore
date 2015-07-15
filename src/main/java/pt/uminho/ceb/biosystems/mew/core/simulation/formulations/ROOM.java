package pt.uminho.ceb.biosystems.mew.core.simulation.formulations;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSReferenceSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public class ROOM extends AbstractSSReferenceSimulation<MILPProblem> {
	
	private static String	booleanPrefix	= "y";
	public static final double DEFAULT_ROOM_DELTA = 0.03;
	public static final double DEFAULT_ROOM_EPSILON = 0.001;
	private boolean resetROOMSpecialConstrains = false;
	
	public ROOM(ISteadyStateModel model) {
		super(model);
	}
	
	protected void initRefProperties() {
		super.initRefProperties();
		optionalProperties.add(SimulationProperties.ROOM_DELTA);
		optionalProperties.add(SimulationProperties.ROOM_EPSILON);
		optionalProperties.add(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS);
	}
	
	@Override
	public MILPProblem constructEmptyProblem() {
		return new MILPProblem();
	}
	
	@Override
	protected void createObjectiveFunction() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		
		boolean useDrains = getUseDrainsInRef();
		problem.setObjectiveFunction(new LPProblemRow(), false);		
		
		for(String rId : model.getReactions().keySet()){
			int idx = getIdxVar(booleanPrefix+rId);
			if((useDrains || !model.getReaction(rId).getType().equals(ReactionType.DRAIN))){
				objTerms.add(new VarTerm(idx));
			}
		}

	}
	
	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		super.createVariables();
		int problemVar = problem.getNumberVariables();
		
		for (String reactionId : model.getReactions().keySet()) {
			String booleanVarId = booleanPrefix + reactionId;
			((MILPProblem) problem).addIntVariable(booleanVarId, 0, 1);
			putVarMappings(booleanVarId, problemVar);
			problemVar++;
		}
	}
	
	protected void createConstraints() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		
		super.createConstraints();
		createROOMSpecialConstraints();		
	}
	
	@SuppressWarnings("unchecked")
	private void createROOMSpecialConstraints() throws PropertyCastException, MandatoryPropertyException {
		double DELTA = getDelta();
		double EPSILON = getEpsilon();
		
//		List<LPConstraint> roomConstraints = (List<LPConstraint>) getProperty(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS); 
		Map<String,LPConstraint> roomConstraints = (Map<String,LPConstraint>) getProperty(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS);
//		if(roomConstraints!=null){
//			if(debug) System.out.println("["+getClass().getSimpleName()+"] event [REMOVING OLD SPECIAL CONSTRAINTS]...");
//			problem.removeConstraintRange(roomConstraints);
//		}
		
		
		wtReference = getWTReference();
//		roomConstraints = new ArrayList<LPConstraint>();
		roomConstraints = new HashMap<String,LPConstraint>();
		
		for (String reactionID : model.getReactions().keySet()) {
			
			int reactionIdx = getIdxVar(reactionID);
			double wt_i = wtReference.get(reactionID);
			double w_u = wt_i + DELTA * Math.abs(wt_i) + EPSILON;
			double w_l = wt_i - DELTA * Math.abs(wt_i) - EPSILON;
			
			ReactionConstraint rc = model.getReactionConstraint(reactionID);
			
			int booleanVarIdx = getIdxVar(booleanPrefix + reactionID);
			
			try {
				LPProblemRow row_u = new LPProblemRow();
				row_u.addTerm(reactionIdx, 1.0);
				row_u.addTerm(booleanVarIdx, w_u - rc.getUpperLimit());
				LPConstraint constraint_u = new LPConstraint(LPConstraintType.LESS_THAN, row_u, w_u);
				problem.addConstraint(constraint_u);
				roomConstraints.put(reactionID+"_upper",constraint_u);
//				roomConstraints.add(constraint_u);
				
				LPProblemRow row_l = new LPProblemRow();
				row_l.addTerm(reactionIdx, 1.0);
				row_l.addTerm(booleanVarIdx, w_l - rc.getLowerLimit()); // var: yi
				LPConstraint constraint_l = new LPConstraint(LPConstraintType.GREATER_THAN, row_l, w_l);
				problem.addConstraint(constraint_l);
				roomConstraints.put(reactionID+"_lower",constraint_l);
//				roomConstraints.add(constraint_l);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException(e);
			}			
		}
		setProperty(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS, roomConstraints);
	}
	
	@SuppressWarnings("unchecked")
	private void replaceConstraints() throws PropertyCastException, MandatoryPropertyException{
		Map<String,LPConstraint> roomConstraints = (Map<String,LPConstraint>) getProperty(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS);
		if(roomConstraints!=null){
			double DELTA = getDelta();
			double EPSILON = getEpsilon();
			wtReference = getWTReference();			
			
			System.out.println("Reference in delta: " + wtReference.toString());
			
			for (String reactionID : model.getReactions().keySet()) {
				int reactionIdx = getIdxVar(reactionID);
				double wt_i = wtReference.get(reactionID);
				double w_u = wt_i + DELTA * Math.abs(wt_i) + EPSILON;
				double w_l = wt_i - DELTA * Math.abs(wt_i) - EPSILON;
				
				ReactionConstraint rc = model.getReactionConstraint(reactionID); 
				int booleanVarIdx = getIdxVar(booleanPrefix + reactionID);
				
				try {
					LPProblemRow row_u = new LPProblemRow();
					row_u.addTerm(reactionIdx, 1.0);
					row_u.addTerm(booleanVarIdx, w_u - rc.getUpperLimit());
					LPConstraint constraint_u_old = roomConstraints.get(reactionID+"_upper");
					LPConstraint constraint_u = new LPConstraint(LPConstraintType.LESS_THAN, row_u, w_u);
					if(!constraint_u.equals(constraint_u_old)){
//						System.out.println("OLD_UP="+constraint_u_old);						
//						System.out.println("NEW_UP="+constraint_u);
						problem.replaceConstraint(constraint_u_old,constraint_u);
						roomConstraints.put(reactionID+"_upper",constraint_u);
//						System.out.println();
					}
					
					LPProblemRow row_l = new LPProblemRow();
					row_l.addTerm(reactionIdx, 1.0);
					row_l.addTerm(booleanVarIdx, w_l - rc.getLowerLimit()); // var: yi
					LPConstraint constraint_l_old = roomConstraints.get(reactionID+"_lower");
					LPConstraint constraint_l = new LPConstraint(LPConstraintType.GREATER_THAN, row_l, w_l);
					if(!constraint_l.equals(constraint_l_old)){
//						System.out.println("OLD_LOW="+constraint_l);
//						System.out.println("OLD_LOW="+constraint_l_old);
						problem.replaceConstraint(constraint_l_old,constraint_l);
						roomConstraints.put(reactionID+"_lower",constraint_l);
//						System.out.println();
					}
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException(e);
				}			
			}
			setProperty(SimulationProperties.ROOM_SPECIAL_CONSTRAINTS, roomConstraints);
		}
	}

	private double getEpsilon() {
		double epsilon = DEFAULT_ROOM_EPSILON;
		try {
			epsilon = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.ROOM_EPSILON, false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getLocalizedMessage() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		
		return epsilon;
	}
	
	private double getDelta() {
		
		double delta = DEFAULT_ROOM_DELTA;
		try {
			delta = (Double) ManagerExceptionUtils.testCast(properties, Double.class, SimulationProperties.ROOM_DELTA, false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getLocalizedMessage() + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		
		return delta;
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		return "Σ bi ";
	}
	
	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException {
		System.out.println("Final Reference: " + getWTReference());	
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		switch (event.getPropertyName()) {		
			case ListenerHashMap.PROP_UPDATE: {
								
				if (event.getKey().equals(SimulationProperties.WT_REFERENCE)) {
					resetReference = true;
					resetROOMSpecialConstrains = true;
				}
				
				if (event.getKey().equals(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE)) {
					resetReference = true;
					resetROOMSpecialConstrains = true;
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	public void preSimulateActions(){
		super.preSimulateActions();
		if(resetROOMSpecialConstrains) try {			
			if(debug) System.out.println("["+getClass().getSimpleName()+"] event [RECOMPUTING CONSTRAINTS]...");
			replaceConstraints();
//			createROOMSpecialConstraints();
		} catch (PropertyCastException | MandatoryPropertyException e) {		
			e.printStackTrace();
		}
	};
	
	
	public void postSimulateActions(){
		super.postSimulateActions();
		resetROOMSpecialConstrains = false;
	}
	
}
