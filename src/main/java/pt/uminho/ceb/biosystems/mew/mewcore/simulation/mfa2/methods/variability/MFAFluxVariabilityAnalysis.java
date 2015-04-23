package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.variability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.linearprogramming.MFALP;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;

public class MFAFluxVariabilityAnalysis<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem>{

	/** The formulation maximize/minimize the flux to be fixed, for instance the minimum value of Biomass */
	protected T objectiveProblem;
	protected boolean isObjectiveProblemRan;

	public MFAFluxVariabilityAnalysis(ISteadyStateModel model) {
		super(model);
		
		possibleProperties.add(MFAProperties.FVA_FIXED_FLUX_PROBLEM);
		possibleProperties.add(MFAProperties.FVA_FIXED_FLUX_VALUE);
		possibleProperties.add(MFAProperties.FVA_MIN_PERCENTAGE);
		possibleProperties.add(MFAProperties.FVA_MIN_PERCENTAGE_FLUX);
		possibleProperties.add(MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MFAWithSolvers getObjectiveProblem(){
		if(objectiveProblem == null){
			try {
				objectiveProblem = (T) ManagerExceptionUtils.testCast(propreties, MFAWithSolvers.class, MFAProperties.FVA_FIXED_FLUX_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
			}
			
			if(objectiveProblem == null){
				objectiveProblem = (T) new MFALP(model);
				objectiveProblem.putAllProperties(propreties);
				setProperty(MFAProperties.FVA_FIXED_FLUX_PROBLEM, objectiveProblem);
			}
		}
		return objectiveProblem;
	}
	
	// Run the simulation for the objective problem
	private void runObjectiveProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		SteadyStateSimulationResult result = getObjectiveProblem().simulate();
		double ofValue = result.getOFvalue();
		setProperty(MFAProperties.FVA_FIXED_FLUX_VALUE, ofValue);
		try {
			if(getMinimumPercentage()!=null)
			{
				String minimumFluxId = (String) ManagerExceptionUtils.testCast(propreties, String.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX, true);
				double minimumFluxValue = result.getFluxValues().getValue(minimumFluxId);
				setProperty(MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE, minimumFluxValue);
			}
		} catch (Exception e) {e.printStackTrace();}
		isObjectiveProblemRan = true;
	}
	
	public double getObjectiveValue() throws Exception{
		
		if(!isObjectiveProblemRan)
			runObjectiveProblem();
		
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, MFAProperties.FVA_FIXED_FLUX_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		return value;
	}
	
	public Double getMinimumPercentage() {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, MFAProperties.FVA_MIN_PERCENTAGE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		
		return value;
	}
	
	public String getObjectiveFlux(){
		try {
			Integer objFluxIndex = (Integer) getObjectiveProblem().getProblem().getObjectiveFunction().getRow().getVarIdxs().iterator().next();
			return objectiveProblem.getIdVar(objFluxIndex);
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getMinimumPercentageFlux() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		if(!isObjectiveProblemRan)
			runObjectiveProblem();
		
		String flux = null;
		try {
			flux = (String) ManagerExceptionUtils.testCast(propreties, String.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		return flux;
	}
	
	public Double getMinimumPercentageFluxValue() {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		
		return value;
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
		
		MFAFvaResult result = new MFAFvaResult(model, getMethod());
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
		}

		if(proceed)
		{
			String objectiveFlux = getObjectiveFlux();
			for(String rId : model.getReactions().keySet())
				if(!rId.equals(objectiveFlux) && (vms==null || !vms.contains(rId)) && (knockouts==null || !knockouts.containsKey(rId)))
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
		try {
//			Integer objFluxIndex = (Integer) objectiveProblem.getProblem().getObjectiveFunction().getRow().getVarIdxs().iterator().next();
			String objFlux = ((Map<String, Double>) getProperty(SimulationProperties.OBJECTIVE_FUNCTION)).keySet().iterator().next();
			result.setObjectiveFlux(objFlux);
			result.setObjectiveFluxValue(getObjectiveValue());
			result.setMinPercentage(getMinimumPercentage());
			result.setMinPercentageFlux(getMinimumPercentageFlux());
			result.setMinPercentageFluxValue(getMinimumPercentageFluxValue());
		} catch (Exception e) {e.printStackTrace();}

		return result;
	}
	
	@Override
	protected void createConstrains() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException  {
		
		getObjectiveProblem();
		
		List<LPConstraint> constraits = objectiveProblem.getProblem().getConstraints();
		problem.setConstraints(constraits);
		
		Double percentage=null;
		try {
			percentage = getMinimumPercentage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(percentage!=null)
		{
			// Setting the constraint for the minimum flux to be greater than the fixed value times the minimum percentage of the value
			
			int index;
			try {
				index = getIdxVar(getMinimumPercentageFlux());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new WrongFormulationException(e);
			} catch (SolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new WrongFormulationException(e);
			}
			problem.getVariable(index).setLowerBound(getMinimumPercentageFluxValue() * percentage);
			
		}
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		try {
			getObjectiveProblem();
		} catch (Exception e) {e.printStackTrace();}
		
		return (objectiveProblem==null) ? null : objectiveProblem.constructEmptyProblem();
	}

	@Override
	protected void createObjectiveFunction()  {}

	@Override
	public String getObjectiveFunctionToString() {
		return objectiveProblem.getObjectiveFunctionToString();
	}

}
