package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover.TurnOverProperties;

public abstract class AbstractTurnoverFormulationFIXED<T extends MILPProblem> extends
		AbstractSSReferenceSimulation<T> {

	private static final boolean REGTURNOVERS = true;
	static protected String turnOverSufix = "_turnover";
	protected double upperBoundTurnover = Double.MAX_VALUE;
//	protected Map<String, Double> turnoverRef;
	
	public abstract T constructEmptyProblem();
	
	
	public AbstractTurnoverFormulationFIXED(final ISteadyStateModel model) {
		super(model);
		initProperties();
	}
	
	private void initProperties() {
		possibleProperties.add(TurnOverProperties.TURNOVER_WT_REFERENCE);
	}

	@Override
	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException{
		if(debug)System.out.println("Turnover 1 " + (System.currentTimeMillis()-initType));
		super.createVariables();
		if(debug)System.out.println("Turnover 2 " + (System.currentTimeMillis()-initType));
		
		final int numberOfMetabolites = model.getNumberOfMetabolites();
		
		if(debug)System.out.println("Turnover 3 " + (System.currentTimeMillis()-initType));
		for(int i =0; i < model.getNumberOfReactions(); i++){
			String name = model.getReactionId(i);
			String idPositive = "TORV_"+name+"("+i+")_PST";
			String idNegative = "TORV_"+name+"("+i+")_NGT";
			ReactionConstraint rc = overrideRC.getReactionConstraint(i);
			rc=(rc!=null)?rc:model.getReactionConstraint(i);
			
			Map<String, Integer> newVars = L1VarTerm.splitNegAndPosVariable(problem, i, idPositive, idNegative, rc.getLowerLimit(), rc.getUpperLimit());
			putNewVariables(newVars);
//			System.out.println(newVars);
			
			int numVariables = problem.getNumberVariables();
			problem.addIntVariable(getNameBooleanDirection(name, true) , 0, 1);
			problem.addIntVariable(getNameBooleanDirection(name, false) , 0, 1);
			
			putVarMappings(getNameBooleanDirection(name, true), numVariables);
			putVarMappings(getNameBooleanDirection(name, false), numVariables+1);
			
			
		}
		if(debug)System.out.println("Turnover 4 " + (System.currentTimeMillis()-initType));
		
		final int startNumVars = problem.getNumberVariables();
		for (int i = 0; i < numberOfMetabolites; i++) {
			final String varID = model.getMetaboliteId(i) + turnOverSufix;
			final int varIdx = i + startNumVars;
			putVarMappings(varID, varIdx);

			final LPVariable var = new LPVariable(varID, 0.0, upperBoundTurnover);
			problem.addVariable(var);
		}
		if(debug)System.out.println("Turnover 5 " + (System.currentTimeMillis()-initType));
	}

	public String getNameBooleanDirection(String reactionName, boolean possitive){
		String name = "b_" + reactionName + ((possitive)?"_pos":"_neg");
		return name;
	}
	
	protected void createConstrains() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
//		System.out.println("createConstrains 1 " + (System.currentTimeMillis()-initType));
		super.createConstrains();

//		System.out.println("createConstrains 2 " + (System.currentTimeMillis()-initType));
		final int numberOfMetabolites = model.getNumberOfMetabolites();
		final int numberOfReactions = model.getNumberOfReactions();

		for (int m = 0; m < numberOfMetabolites; m++) {

			final LPProblemRow row = new LPProblemRow();
			for (int r = 0; r < numberOfReactions; r++) {

//				double coef = model.getStoichiometricMatrix().getValue(m, r);
				double coef = model.getStoichiometricValue(m,r);
				int idx = getVariableIdxForConsumingMetabolite(r, m, coef);
				
//				if(idx!=null)
//					try {
//						row.addTerm(idx[0], Math.abs(coef));
//						row.addTerm(idx[1], Math.abs(coef));
//					} catch (LinearProgrammingTermAlreadyPresentException e) {
//						e.printStackTrace();
//						throw new WrongFormulationException(e);
//					}
				
				if(idx!=-1)
					try {
						row.addTerm(idx, Math.abs(coef));
					} catch (LinearProgrammingTermAlreadyPresentException e) {
						throw new WrongFormulationException(e);
					}
//				addDrirectionReversibilityInProblem(problem, r);
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
		
		for (int r = 0; r < numberOfReactions; r++) {
			addDrirectionReversibilityInProblem(problem, r);
		}
//		System.out.println("createConstrains 3 " + (System.currentTimeMillis()-initType));
	}
	
	private void addDrirectionReversibilityInProblem(T problem, int r) {

		String reactionId = model.getReactionId(r);
		
		int vp = getPossitiveFluxValue(reactionId);
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

	private int getVariableIdxForConsumingMetabolite(final int reactionIdx, final int metaboliteIdx, final double coef){
		
		int varIdx = -1;
//		int[] ret = null;
//		
//		if(metaboliteIdx !=0){
//			ret=new int[2];
//			ret[0] = getNegativeFluxValue(reactionIdx);
//			ret[1] = getPossitiveFluxValue(reactionIdx);
//		}
		if(coef > 0){
			varIdx = getPossitiveFluxValue(reactionIdx);
		}else if(coef < 0){
			varIdx=getNegativeFluxValue(reactionIdx);
		}

//		return ret;
		return varIdx;
	}
	
	public int getBooleanPositiveValue(String reactionId){
		String name = getNameBooleanDirection(reactionId, true);
		return getIdxVar(name);
	}
	
	public int getBooleanNegativeValue(String reactionId){
		String name = getNameBooleanDirection(reactionId, false);
		return getIdxVar(name);
	}
	
	public int getPossitiveFluxValue(String reactionId){
		String id = "TORV_"+reactionId+"("+model.getReactionIndex(reactionId)+")_PST";
		return getIdxVar(id);
	}
	
	public int getPossitiveFluxValue(int reactionIdx){
		String id = "TORV_"+model.getReactionId(reactionIdx)+"("+reactionIdx+")_PST";
		return getIdxVar(id);
	}
	
	public int getNegativeFluxValue(int reactionIdx){
		String id = "TORV_"+model.getReactionId(reactionIdx)+"("+reactionIdx+")_NGT";
		return getIdxVar(id);
	}
	
	public int getNegativeFluxValue(String reactionId){
		String id = "TORV_"+reactionId+"("+model.getReactionIndex(reactionId)+")_NGT";
		return getIdxVar(id);
	}

	protected int getTurnoverVarIndex(final int metIdx) {
		return getTurnoverVarIndex(model.getMetaboliteId(metIdx));
	}

	protected int getTurnoverVarIndex(final String metId) {
		return idToIndexVarMapings.get(metId + turnOverSufix);
	}
	
	public void setTurnOverReference(final Map<String, Double> tr){
		propreties.put(TurnOverProperties.TURNOVER_WT_REFERENCE, tr);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getTurnOverReference() throws PropertyCastException, MandatoryPropertyException {
		if(debug)System.out.println("inside getTurnOverReference " + (System.currentTimeMillis()-initType));
		Map<String, Double> tor = ManagerExceptionUtils.testCast(propreties, Map.class, TurnOverProperties.TURNOVER_WT_REFERENCE, true);
		
		if(debug)System.out.println("cast " + (System.currentTimeMillis()-initType));
		if(tor == null){
			Map<String, Double> wtReference = getWTReference();
			if(debug)System.out.println("getWTReference " + (System.currentTimeMillis()-initType));
			tor = TurnOverProperties.getTurnOverCalculation(model, wtReference);
			if(debug)System.out.println("getTurnOverCalculation " + (System.currentTimeMillis()-initType));
		}
		
		setTurnOverReference(tor);
		if(debug)System.out.println("setTurnOverReference " + (System.currentTimeMillis()-initType));
		return tor;
	}
	
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException  {
		
		
		MapStringNum possitive = new MapStringNum();
		MapStringNum negative = new MapStringNum();
		for(int i=0; i < model.getNumberOfReactions(); i++){
			
			int neg = getNegativeFluxValue(i);
			int pos = getPossitiveFluxValue(i);
			String reactionId = model.getReactionId(i);
			
			possitive.put(reactionId, solution.getValues().get(pos));
			negative.put(reactionId, solution.getValues().get(neg));
		}
		
		
		SteadyStateSimulationResult res = super.convertLPSolutionToSimulationSolution(solution);
//		System.out.println("convertLPSolutionToSimulationSolution   ==> 2");
		
		MapStringNum toSolution = getCalculatedTurnOvers(solution);
		Map<String, Double> wtFluxes = getWTReference();
		Map<String, Double> turor = getTurnOverReference();
		
		res.addComplementaryInfoMetabolites(TurnOverProperties.TURNOVER_WT_REFERENCE, MapStringNum.convertMapToStringNum(turor));
		res.addComplementaryInfoReactions(SimulationProperties.WT_REFERENCE, MapStringNum.convertMapToStringNum(wtFluxes));
		
		if(REGTURNOVERS){
			res.addComplementaryInfoMetabolites(TurnOverProperties.TURNOVER_MAP_SOLUTION, toSolution);
			res.addComplementaryInfoReactions("V_POSITIVE", possitive);
			res.addComplementaryInfoReactions("V_NEGATIVE", negative);
		}
		
		return res;
	}
	
	public MapStringNum getCalculatedTurnOvers(LPSolution solution){
		
		MapStringNum trno = new MapStringNum();
		for(String id : model.getMetabolites().keySet()){
			int varIdx = getTurnoverVarIndex(id);
			double value = solution.getValues().get(varIdx);
			
			trno.put(id, value);
		}
		return trno;
	}
	
//	@SuppressWarnings("unchecked")
//	public Map<String, Double> getWTReference() throws PropertyCastException, MandatoryPropertyException  {
//		
//		Map<String, Double> wtReference = null;
//			
//		try {
//			wtReference = ManagerExceptionUtils.testCast(propreties, Map.class, SimulationProperties.WT_REFERENCE, true);
//		} catch (PropertyCastException e) {
//			System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
//		} catch (MandatoryPropertyException e) {
//			
//		}
//		
//		if(wtReference == null){
//			SolverType solver = getSolverType();
//			EnvironmentalConditions envCond = getEnvironmentalConditions();
//			try{
//				wtReference = SimulationProperties.simulateWT(model, envCond, solver);
//				setProperty(SimulationProperties.WT_REFERENCE, wtReference);
//			}catch(Exception e){
//				throw new MandatoryPropertyException(SimulationProperties.WT_REFERENCE, Map.class);
//			}
//			setProperty(SimulationProperties.WT_REFERENCE, wtReference);
//			System.out.println("Calculating WT...");
//		}
//		
//		
//		return wtReference;
//	}
	
	
}
