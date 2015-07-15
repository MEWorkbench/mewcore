package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

public class MFATightBounds extends MFAWithSolvers<LPProblem> {
	
	
	public MFATightBounds(ISteadyStateModel model) {
		super(model);		
	}		
	
	protected double[] simulateFluxBounds(String fluxId) throws WrongFormulationException, SolverException, PropertyCastException, MandatoryPropertyException, IOException {
		
		Map<String, Double> ofMap = new HashMap<String, Double>();
		ofMap.put(fluxId, 1.0);
		setProperty(MFAProperties.OBJECTIVE_FUNCTION, ofMap);
		setProperty(MFAProperties.IS_MAXIMIZATION, false);
		
		SteadyStateSimulationResult res = super.simulate();
		double lower = res.getOFvalue();
		
		setProperty(MFAProperties.IS_MAXIMIZATION, true);
		
		res = super.simulate();
		double upper = res.getOFvalue();
		
		return new double[] { lower, upper };
	}
	
	@Override
	protected IOverrideReactionBounds createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();
		
		MFARatiosOverrideModel overrideRC = new MFARatiosOverrideModel(model, environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
		return overrideRC;
	}
	
	@Override
	public MFATightBoundsResult simulate() throws PropertyCastException, MandatoryPropertyException {
		
		Set<String> vms = null;
		{
			ExpMeasuredFluxes expVm = getMeasuredFluxes();
			if (expVm != null) vms = expVm.keySet();
		}
		
		ReactionChangesList knockouts = null;
		{
			GeneticConditions geneticConditions = getGeneticConditions();
			if (geneticConditions != null) knockouts = geneticConditions.getReactionList();
		}
		
		MFATightBoundsResult result = new MFATightBoundsResult(model, getMethod());
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
		}		
		
		if (proceed) {
			for (String rId : model.getReactions().keySet())
				if ( (vms == null || !vms.contains(rId)) && (knockouts == null || !knockouts.containsKey(rId))) {
					double[] bounds = new double[] { Double.NaN, Double.NaN };
					try {
						bounds = simulateFluxBounds(rId);
					} catch (WrongFormulationException | SolverException | IOException e) {
						e.printStackTrace();
					}
					
					result.addFluxBounds(rId, bounds[0], bounds[1]);
				}
		}		

		return result;
	}
	
	protected void createProblemIfEmpty() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException {
		if (problem == null) {
			problem = constructEmptyProblem();
			createVariables();
			createConstraints();
			if (problem.getObjectiveFunction() == null) {
				problem.setObjectiveFunction(new LPProblemRow(), false);
				putObjectiveFunctionIntoProblem();
			}
		}
	}
		
	
	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());
		
		Map<String, Double> obj_coef = getObjectiveFunction();
		for (String r : obj_coef.keySet()) {
			double coef = obj_coef.get(r);
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get(r), coef, 0.0));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getObjectiveFunction() {
		Map<String, Double> obj_coef = null;
		try {
			obj_coef = ManagerExceptionUtils.testCast(properties, Map.class, MFAProperties.OBJECTIVE_FUNCTION, false);
		} catch (Exception e) {
			obj_coef = new HashMap<String, Double>();
			obj_coef.put(model.getBiomassFlux(), 1.0);
		}
		return obj_coef;
	}
	
	@Override
	public String getObjectiveFunctionToString() {
		return null;
	}
	
}
