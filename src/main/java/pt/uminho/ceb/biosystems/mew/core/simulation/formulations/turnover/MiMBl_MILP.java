package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractTurnoverFormulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class MiMBl_MILP extends AbstractTurnoverFormulation<MILPProblem>{

	public Boolean use2Opt;
	
	public MiMBl_MILP(ISteadyStateModel model) {
		super(model);
	}

	@Override
	public MILPProblem constructEmptyProblem() {
		MILPProblem newProblem = new MILPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		Map<String, Double> tor = getTurnOverReference();
		for(String metId: model.getMetabolites().keySet()){
			
			double ref = tor.get(metId); 
			int turnOverIndex = getTurnoverVarIndex(metId);
			objTerms.add(new L1VarTerm(turnOverIndex, -ref));
		}	
	}
	
	@Override
	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		if (debug_times) System.out.println("Turnover 1 " + (System.currentTimeMillis() - initTime));
		int numberVariables = model.getNumberOfReactions();
		for (int i = 0; i < numberVariables; i++) {
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i);
			
			ReactionConstraint rc = model.getReactionConstraint(i);
			LPVariable var = new LPVariable(r.getId(), rc.getLowerLimit(), rc.getUpperLimit());
			problem.addVariable(var);
		}
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
			
			int numVariables = problem.getNumberVariables();
			problem.addIntVariable(getNameBooleanDirection(name, true) , 0, 1);
			problem.addIntVariable(getNameBooleanDirection(name, false) , 0, 1);
			
			putVarMappings(getNameBooleanDirection(name, true), numVariables);
			putVarMappings(getNameBooleanDirection(name, false), numVariables+1);
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
	

	protected void createConstraints() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		int numberVariables = model.getNumberOfReactions();
		int numberConstraints = model.getNumberOfMetabolites();
		
		for (int i = 0; i < numberConstraints; i++) {
			LPProblemRow row = new LPProblemRow();
			for (int j = 0; j < numberVariables; j++) {
				double value = model.getStoichiometricValue(i, j);
				if (value != 0) try {
					row.addTerm(j, value);
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException("Cannot add term " + j + "to row with value: " + value);
				}
			}
			
			LPConstraint constraint = new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
		}

		final int numberOfMetabolites = model.getNumberOfMetabolites();
		final int numberOfReactions = model.getNumberOfReactions();

		for (int m = 0; m < numberOfMetabolites; m++) {

			final LPProblemRow row = new LPProblemRow();
			for (int r = 0; r < numberOfReactions; r++) {

				double coef = model.getStoichiometricValue(m,r);
				int idx = getVariableIdxForConsumingMetabolite(r, m, coef);
				
				if(idx!=-1)
					try {
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
		
		for (int r = 0; r < numberOfReactions; r++) {
			addDirectionReversibilityInProblem(problem, r);
		}
	}

	public boolean use2Opt() throws PropertyCastException, MandatoryPropertyException{
		if(use2Opt==null){
			use2Opt = ManagerExceptionUtils.testCast(properties, Boolean.class, TurnOverProperties.USE_2OPT, true);
			
			if(use2Opt == null){
				use2Opt = false;
			}
		}
		return use2Opt;
	}
	
	//FIXME: Think if this method should be in Abstract...
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException  {
		SteadyStateSimulationResult res = super.convertLPSolutionToSimulationSolution(solution);

		if(use2Opt())
		normalizeResult(res);
		
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
	
	@Override
	public String getObjectiveFunctionToString() {
		return "min Σ|T - Twt|";
	}

	//TODO
	private void normalizeResult(SteadyStateSimulationResult res) throws PropertyCastException, MandatoryPropertyException{		
		
//		MiMBl_NormalizeSolution normalise = new MiMBl_NormalizeSolution(model);
//		normalise.setSolverType(getSolverType());
//		normalise.setInitProblem(this);
//		normalise.setReference(wtReference);
//		normalise.setObjectiveValue(res.getOFvalue());
//
//		res.addComplementaryInfoReactions(TurnOverProperties.MIMBL_FIRST_OPTIMIZARION_FLUXVALUE,
//				res.getFluxValues());
//		
////		if(debug){
////			System.out.println(res.getOFString() + " = " + res.getOFvalue());
////			System.out.println(new TreeMap(res.getComplementaryInfoMetabolites().get(TurnOverProperties.TURNOVER_WT_REFERENCE)));
////			System.out.println(new TreeMap(res.getComplementaryInfoMetabolites().get(TurnOverProperties.TURNOVER_MAP_SOLUTION)));
////		}
//		SteadyStateSimulationResult normResult;
//		
//		try {
//			normResult = normalise.simulate();
//			res.setFluxValues(normResult.getFluxValues());
//			res.setOFString(res.getOFString() + " (" + normalise.getObjectiveFunctionToString() + ")");
////			System.out.println(new TreeMap(normResult.getComplementaryInfoMetabolites().get(TurnOverProperties.TURNOVER_MAP_SOLUTION)));
////			System.out.println(new TreeMap(normResult.getComplementaryInfoMetabolites().get(TurnOverProperties.TURNOVER_MAP_SOLUTION)));
//
//			if(debug)
//			System.out.println("Result: " + normResult.getOFString() + " = " + normResult.getOFvalue());
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new Error(e);
//		}
		
	}

	public static Double recalculateOF(SteadyStateSimulationResult wt,
			SteadyStateSimulationResult sim) {
		
		Map<String, Double> pfbaTurn = TurnOverProperties.getTurnOverCalculation(wt.getModel(), wt.getFluxValues());
		Map<String, Double> simTurn = TurnOverProperties.getTurnOverCalculation(sim.getModel(), sim.getFluxValues());
		
		return recalculateOF(pfbaTurn, simTurn);
	}
	
	public static Double recalculateOF(Map<String, Double> wtTurnovers,
			Map<String, Double> simTurnovers) {
		
		Double turno = 0.0;
		for(String id : simTurnovers.keySet()){
			
			turno +=Math.abs((simTurnovers.get(id) - wtTurnovers.get(id)));
		}
		return turno;
	}
}
