package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractTurnoverFormulationFIXED;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.L1VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class MiMBlFIXED extends AbstractTurnoverFormulationFIXED<MILPProblem>{

	public Boolean use2Opt;
	
	public MiMBlFIXED(ISteadyStateModel model) {
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
		// Try add to MIMBl for SolverDefinitionException
		for(String metId: model.getMetabolites().keySet()){
			
			double ref = tor.get(metId); 
			int turnOverIndex = getTurnoverVarIndex(metId);
			objTerms.add(new L1VarTerm(turnOverIndex, -ref));
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

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
