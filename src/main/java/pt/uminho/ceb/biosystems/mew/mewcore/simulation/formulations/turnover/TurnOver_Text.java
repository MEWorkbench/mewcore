package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractTurnoverFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public class TurnOver_Text extends AbstractTurnoverFormulation<LPProblem>{

	public TurnOver_Text(ISteadyStateModel model) {
		super(model);
	}

//	private void initProperties() {
//		mandatoryProps.add(SimulationProperties.WT_REFERENCE);
//	}

	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		for(String metId:model.getMetabolites().keySet()){
			objTerms.add(new VarTerm(getTurnoverVarIndex(metId)));
		}	
	}

	@Override
	public String getObjectiveFunctionToString() {
		
		return "";
	}
	
	protected void createVariables() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException{
		super.createVariables();
		
		int biomassIdx = getIdxVar(model.getBiomassFlux());
		LPVariable variable = problem.getVariable(biomassIdx);
		
		double biomassFlux = 0;
		try {
			biomassFlux = (Double) getWTReference().get(model.getBiomassFlux());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		variable.setLowerBound(biomassFlux*0.9999999);
	}

}
