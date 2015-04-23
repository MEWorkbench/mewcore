package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.linearprogramming;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblemRow;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.QuadraticTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;

/** This class is used to solve MFA problems formulated as a Quadratic Programming problem, 
 * either the system is undetermined, determined or overdevermined */
public class MFAQP extends MFAWithSolvers<QPProblem>{

	public MFAQP(ISteadyStateModel model){
		super(model);
	}

	
	@Override
	public QPProblem constructEmptyProblem() {
		return new QPProblem();
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException  {
		
		((QPProblem) problem).setQPObjectiveFunction(new QPProblemRow());
		problem.setObjectiveFunction(new LPProblemRow(), false);
		
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		
		for(String rId : model.getReactions().keySet())
		{
			int idx = getIdxVar(rId);
//			double b = (measuredFluxes.containsKey(rId)) ? -1 * measuredFluxes.getValue(rId) : 0.0;
//			objTerms.add(new QuadraticTerm(idx, 1, b));
			
			double a, b;
			
			if(measuredFluxes.containsKey(rId))
			{
				b = -1 * measuredFluxes.getFluxValue(rId);
				a = 1;
			}
			else
			{
				a = 0;
				b = 0;
			}
			objTerms.add(new QuadraticTerm(idx, a, b));
		}
			
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "MFA QP Objective Function";
	}
	
	@Override
	protected void createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();

		overrideRC = new MFARatiosOverrideModel(model,environmentalConditions, geneticConditions, null, ratioConstraints, getProblemClass());
	}
	

}
