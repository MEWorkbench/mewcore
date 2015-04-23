package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.variability;

import java.util.ArrayList;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;

public class MFATightBounds extends MFAWithSolvers<LPProblem>{
	
	public MFATightBounds(ISteadyStateModel model) {
		super(model);
	}
		
		
	protected double simulateFluxBound(String fluxId, boolean isMaximization) throws Exception {
		// Create the objective function
		problem.setObjectiveFunction(new LPProblemRow(), isMaximization);
		objTerms = new ArrayList<AbstractObjTerm>();
		objTerms.add(new VarTerm(getIdToIndexVarMapings().get(fluxId), 1.0, 0.0));
		putObjectiveFunctionIntoProblem();
		
		// Simulate the lower / upper bound of the given flux
		SolverType solverType = getSolverType();
		ILPSolver solver = solverType.lpSolver(problem);
		
		LPSolution solution = solver.solve();
		return solution.getOfValue();
	}
	
	@Override
	protected void createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();
		
		overrideRC = new MFARatiosOverrideModel(model,environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
	}
	
	@Override
	public MFATightBoundsResult simulate() throws PropertyCastException, MandatoryPropertyException  {
		
		Set<String> vms = null;
		{
			ExpMeasuredFluxes expVm = getMeasuredFluxes();
			if(expVm!=null)
				vms = expVm.keySet();
		}
		
		ReactionChangesList knockouts = null;
		{
			GeneticConditions geneticConditions = getGeneticConditions();
			if(geneticConditions!=null)
				knockouts = geneticConditions.getReactionList();
		}
		
		MFATightBoundsResult result = new MFATightBoundsResult(model, getMethod());
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
		}

		if(proceed)
		{
			for(String rId : model.getReactions().keySet())
				if((vms==null || !vms.contains(rId)) && (knockouts==null || !knockouts.containsKey(rId)))
				{
					Double lowerBound = null;
					try {
						lowerBound = simulateFluxBound(rId, false);
					} catch (Exception e) {e.printStackTrace();}
					
					Double upperBound = null;
					try {
						upperBound = simulateFluxBound(rId, true);
					} catch (Exception e) {e.printStackTrace();}
					
					result.addFluxBounds(rId, lowerBound, upperBound);
				}
		}
		
		result.setEnvironmentalConditions(getEnvironmentalConditions());
		result.setGeneticConditions(getGeneticConditions());
		result.setOFString(getObjectiveFunctionToString());

		return result;
	}

	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}

	@Override
	protected void createObjectiveFunction()  {}


	@Override
	public String getObjectiveFunctionToString() {
		return null;
	}
}
