package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover.TurnOverProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public abstract class AbstractTurnoverFormulation<T extends LPProblem> extends AbstractSSReferenceSimulation<T> {
	
	private static final boolean	REGTURNOVERS			= false;
	static protected String			turnOverSufix			= "_turnover";
	protected double				upperBoundTurnover		= Double.MAX_VALUE;
	
	protected Map<String, Double>	turnoverReference;
	protected boolean				resetTurnoverReference	= false;

	
	public abstract T constructEmptyProblem();
	
	public AbstractTurnoverFormulation(final ISteadyStateModel model) {
		super(model);
		initProperties();
	}
	
	private void initProperties() {
		optionalProperties.add(SimulationProperties.TURNOVER_WT_REFERENCE);
	}
	
	@Override
	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		if (debug_times) System.out.println("Turnover 1 " + (System.currentTimeMillis() - initTime));
		super.createVariables();
		if (debug_times) System.out.println("Turnover 2 " + (System.currentTimeMillis() - initTime));
		
		final int numberOfMetabolites = model.getNumberOfMetabolites();
		
		if (debug_times) System.out.println("Turnover 3 " + (System.currentTimeMillis() - initTime));
		for (int i = 0; i < model.getNumberOfReactions(); i++) {
			String name = model.getReactionId(i);
			String idPositive = generateSplitVariableName(name, i, true);
			String idNegative = generateSplitVariableName(name, i, false);
			
			ReactionConstraint rc = model.getReactionConstraint(name);
			
			Map<String, Integer> newVars = L1VarTerm.splitNegAndPosVariable(problem, i, idPositive, idNegative, rc.getLowerLimit(), rc.getUpperLimit());
			putNewVariables(newVars);
			//			System.out.println(newVars);
		}
		if (debug_times) System.out.println("Turnover 4 " + (System.currentTimeMillis() - initTime));
		
		final int startNumVars = problem.getNumberVariables();
		for (int i = 0; i < numberOfMetabolites; i++) {
			final String varID = model.getMetaboliteId(i) + turnOverSufix;
			final int varIdx = i + startNumVars;
			putVarMappings(varID, varIdx);
			
			final LPVariable var = new LPVariable(varID, 0.0, upperBoundTurnover);
			problem.addVariable(var);
		}
		if (debug_times) System.out.println("Turnover 5 " + (System.currentTimeMillis() - initTime));
	}
	
	protected String generateSplitVariableName(String name, Integer index, boolean isPositive) {
		if (index == null) index = model.getReactionIndex(name);
		return (isPositive) ? "TORV_" + name + "(" + index + ")_PST" : "TORV_" + name + "(" + index + ")_NGT";
	}
	
	protected void setVariables(IOverrideReactionBounds override) throws PropertyCastException, MandatoryPropertyException {
		
		for (String r : override.getOverriddenReactions()) {
			//original variables
//			System.out.println(r);
			Integer index = idToIndexVarMapings.get(r);
			if(index!=null){
				ReactionConstraint rc = override.getReactionConstraint(r);
				double lower = rc.getLowerLimit();
				double upper = rc.getUpperLimit();
				problem.changeVariableBounds(index, lower, upper);
				//turnovers
				changeTurnoverBounds(r, lower, upper);
			}
		}
	}
	
	protected void unsetVariables(IOverrideReactionBounds override) throws PropertyCastException, MandatoryPropertyException {
		
		for (String r : override.getOverriddenReactions()) {
			//original variables
			int index = idToIndexVarMapings.get(r);
			ReactionConstraint rc = model.getReactionConstraint(r);
			double lower = rc.getLowerLimit();
			double upper = rc.getUpperLimit();
			problem.changeVariableBounds(index, lower, upper);
			//turnovers
			changeTurnoverBounds(r, lower, upper);
		}
	}
	
	protected void changeTurnoverBounds(String id, Double lower, Double upper) {
		//		System.out.println(">>>>>>>>>>>>>>>>>> CHANGING TURNOVER BOUND FOR ["+id+"]");
		String positive = generateSplitVariableName(id, null, true);
		String negative = generateSplitVariableName(id, null, false);
		ReactionConstraint rcPos = L1VarTerm.getConvertedLimits(lower, upper, true);
		ReactionConstraint rcNeg = L1VarTerm.getConvertedLimits(lower, upper, false);
		int idxPos = idToIndexVarMapings.get(positive);
		int idxNeg = idToIndexVarMapings.get(negative);
		problem.changeVariableBounds(idxPos, rcPos.getLowerLimit(), rcPos.getUpperLimit());
		problem.changeVariableBounds(idxNeg, rcNeg.getLowerLimit(), rcNeg.getUpperLimit());
	}
	
	protected void createConstraints() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		//		System.out.println("createConstrains 1 " + (System.currentTimeMillis()-initType));
		super.createConstraints();
		
		//		System.out.println("createConstrains 2 " + (System.currentTimeMillis()-initType));
		final int numberOfMetabolites = model.getNumberOfMetabolites();
		final int numberOfReactions = model.getNumberOfReactions();
		
		for (int m = 0; m < numberOfMetabolites; m++) {
			
			final LPProblemRow row = new LPProblemRow();
			for (int r = 0; r < numberOfReactions; r++) {
				
				double coef = model.getStoichiometricValue(m, r);
				int idx = getVariableIdxForConsumingMetabolite(r, m, coef);
				
				if (idx != -1) try {
					row.addTerm(idx, Math.abs(coef));
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException(e);
				}
				
			}
			
			final int trnIdx = getTurnoverVarIndex(m);
			try {
				row.addTerm(trnIdx, -1);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				e.printStackTrace();
				throw new WrongFormulationException(e);
			}
			problem.addConstraint(row, LPConstraintType.EQUALITY, 0.0);
		}
	}
	
	protected int getVariableIdxForConsumingMetabolite(final int reactionIdx, final int metaboliteIdx, final double coef) {
		
		int varIdx = -1;
		
		if (coef > 0) {
			varIdx = getPositiveFluxValue(reactionIdx);
		} else if (coef < 0) {
			varIdx = getNegativeFluxValue(reactionIdx);
		}
		
		return varIdx;
	}
	
	protected void addDirectionReversibilityInProblem(T problem, int r) {

		String reactionId = model.getReactionId(r);
		
		int vp = getPositiveFluxValue(reactionId);
		int vn = getNegativeFluxValue(reactionId);
		
		int bp = getBooleanPositiveValue(reactionId);
		int bn = getBooleanNegativeValue(reactionId);
		
		try {
			addBooleanConstractProblem(vp, bp);
			addBooleanConstractProblem(vn, bn);
			
			LPProblemRow row = new LPProblemRow();
			row.addTerm(bp, 1);
			row.addTerm(bn, 1);
			
			LPConstraint lpc = new LPConstraint(LPConstraintType.LESS_THAN, row, 1.0);
			problem.addConstraint(lpc);
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			throw new WrongFormulationException(e);
		}
	}
	
	private void addBooleanConstractProblem(int variable, int bvariable) throws LinearProgrammingTermAlreadyPresentException{
		LPProblemRow row = new LPProblemRow();
		
		row.addTerm(variable, 1.0);
		row.addTerm(bvariable, -100000);
				
		LPConstraint lpc = new LPConstraint(LPConstraintType.LESS_THAN, row, 0.0);
		problem.addConstraint(lpc);
	}

	public int getBooleanPositiveValue(String reactionId){
		String name = getNameBooleanDirection(reactionId, true);
		return getIdxVar(name);
	}
	
	public int getBooleanNegativeValue(String reactionId){
		String name = getNameBooleanDirection(reactionId, false);
		return getIdxVar(name);
	}
	
	public String getNameBooleanDirection(String reactionName, boolean positive){
		String name = "b_" + reactionName + ((positive)?"_pos":"_neg");
		return name;
	}
	
	public int getPositiveFluxValue(int reactionIdx) {
		String id = generateSplitVariableName(model.getReactionId(reactionIdx), reactionIdx, true);
		return getIdxVar(id);
	}
	
	public int getNegativeFluxValue(int reactionIdx) {
		String id = generateSplitVariableName(model.getReactionId(reactionIdx), reactionIdx, false);
		return getIdxVar(id);
	}
	
	protected int getTurnoverVarIndex(final int metIdx) {
		return getTurnoverVarIndex(model.getMetaboliteId(metIdx));
	}
	
	public int getPositiveFluxValue(String reactionId){
		String id = generateSplitVariableName(reactionId, null, true);
		return getIdxVar(id);
	}
	
	public int getNegativeFluxValue(String reactionId){
		String id = generateSplitVariableName(reactionId, null, false);
		return getIdxVar(id);
	}
	
	protected int getTurnoverVarIndex(final String metId) {
		return idToIndexVarMapings.get(metId + turnOverSufix);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getTurnOverReference() throws PropertyCastException, MandatoryPropertyException {
		
		if (turnoverReference == null) {
			if (debug_times) initTime = System.currentTimeMillis();
			turnoverReference = ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.TURNOVER_WT_REFERENCE, true);
			if (debug_times) System.out.println("cast " + (System.currentTimeMillis() - initTime));
			
			if (turnoverReference == null) {
				if (debug_times) initTime = System.currentTimeMillis();
				Map<String, Double> wtReference = getWTReference();
				if (debug_times) System.out.println("getWTReference" + (System.currentTimeMillis() - initTime));
				
				if (debug_times) initTime = System.currentTimeMillis();
				turnoverReference = SimulationProperties.getTurnOverCalculation(model, wtReference);
				if (debug_times) System.out.println("getTurnoverCalculation" + (System.currentTimeMillis() - initTime));
			}
			
			if (debug_times) initTime = System.currentTimeMillis();
			setTurnoverReference(turnoverReference);
			if (debug_times) System.out.println("setTurnoverReference" + (System.currentTimeMillis() - initTime));
		}
		return turnoverReference;
	}
	
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		
		MapStringNum possitive = new MapStringNum();
		MapStringNum negative = new MapStringNum();
		for (int i = 0; i < model.getNumberOfReactions(); i++) {
			
			int neg = getNegativeFluxValue(i);
			int pos = getPositiveFluxValue(i);
			String reactionId = model.getReactionId(i);
			
			possitive.put(reactionId, solution.getValues().get(pos));
			negative.put(reactionId, solution.getValues().get(neg));
		}
		
		SteadyStateSimulationResult res = super.convertLPSolutionToSimulationSolution(solution);
		//		System.out.println("convertLPSolutionToSimulationSolution   ==> 2");
		
		MapStringNum toSolution = getCalculatedTurnOvers(solution);
		Map<String, Double> wtFluxes = getWTReference();
		Map<String, Double> turor = getTurnOverReference();
		
		res.addComplementaryInfoMetabolites(SimulationProperties.TURNOVER_WT_REFERENCE, MapStringNum.convertMapToStringNum(turor));
		res.addComplementaryInfoReactions(SimulationProperties.WT_REFERENCE, MapStringNum.convertMapToStringNum(wtFluxes));
		
		if (REGTURNOVERS) {
			res.addComplementaryInfoMetabolites(TurnOverProperties.TURNOVER_MAP_SOLUTION, toSolution);
			res.addComplementaryInfoReactions("V_POSITIVE", possitive);
			res.addComplementaryInfoReactions("V_NEGATIVE", negative);
		}
		
		return res;
	}
	
	public MapStringNum getCalculatedTurnOvers(LPSolution solution) {
		
		MapStringNum trno = new MapStringNum();
		for (String id : model.getMetabolites().keySet()) {
			int varIdx = getTurnoverVarIndex(id);
			double value = solution.getValues().get(varIdx);
			
			trno.put(id, value);
		}
		return trno;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//		super.propertyChange(evt);
		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		switch (event.getPropertyName()) {
			case ListenerHashMap.PROP_UPDATE: {
				
				if (event.getKey().equals(SimulationProperties.WT_REFERENCE)) {
					resetReference = true;
					resetTurnoverReference = true;
					setRecreateOF(true);
				}
				
				if (event.getKey().equals(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE)) {
					resetReference = true;
					resetTurnoverReference = true;
					setRecreateOF(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	public void setTurnoverReference(Map<String, Double> turnoverReference) {
		if (turnoverReference == null) this.turnoverReference = null;
		setProperty(SimulationProperties.TURNOVER_WT_REFERENCE, turnoverReference);
	}
	
	public void clearAllProperties() {
		super.clearAllProperties();
		setTurnoverReference(null);
	}
	
	public void preSimulateActions() {
		super.preSimulateActions();
		if (resetTurnoverReference) setTurnoverReference(null);
	};
	
	public void postSimulateActions() {
		super.postSimulateActions();
		resetTurnoverReference = false;
	};
	
}
