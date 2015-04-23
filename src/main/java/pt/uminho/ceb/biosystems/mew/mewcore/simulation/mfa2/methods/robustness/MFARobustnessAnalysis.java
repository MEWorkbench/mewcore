package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.robustness;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions.ErrorLog;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions.MFAObjectiveSimulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.linearprogramming.MFALP;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;

public class MFARobustnessAnalysis<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem>{

	/** The formulation for the objective problem. Afterwards, the percentage for the selected fluxes
	 * will be applied to the value of those fluxes in the solution of the objective problem */
	protected T objectiveProblem;
	protected boolean isObjectiveProblemRan;

	public MFARobustnessAnalysis(ISteadyStateModel model) {
		super(model);
		
		possibleProperties.add(MFAProperties.ROBUSTNESS_SELECTED_FLUXES);
		possibleProperties.add(MFAProperties.ROBUSTNESS_PERCENTAGE_INTERVAL);
		possibleProperties.add(MFAProperties.ROBUSTNESS_OBJECTIVE_PROBLEM);
		possibleProperties.add(MFAProperties.ROBUSTNESS_OBJECTIVE_FLUX);
		possibleProperties.add(MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE);
		possibleProperties.add(MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MFAWithSolvers getObjectiveProblem(){
		if(objectiveProblem == null){
			try {
				objectiveProblem = (T) ManagerExceptionUtils.testCast(propreties, MFAWithSolvers.class, MFAProperties.ROBUSTNESS_OBJECTIVE_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
			}
			
			if(objectiveProblem == null){
				objectiveProblem = (T) new MFALP(model);
				objectiveProblem.putAllProperties(propreties);
				setProperty(MFAProperties.ROBUSTNESS_OBJECTIVE_PROBLEM, objectiveProblem);
			}
		}
		return objectiveProblem;
	}
	
	/** Run the simulation of the objective problem */
	private void runObjectiveProblem() throws MFAObjectiveSimulationException {
		SteadyStateSimulationResult result;
		try {
			result = getObjectiveProblem().simulate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MFAObjectiveSimulationException(e.toString());
		}

		List<String> selectedFluxes = getSelectedFluxes();
		try {
			if(selectedFluxes!=null)
			{
				Map<String, Double> origValues = new HashMap<String, Double>();
				for(String fluxId : selectedFluxes)
				{
					double fluxValue = result.getFluxValues().getValue(fluxId);
					origValues.put(fluxId, fluxValue);
				}
				setProperty(MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES, origValues);
				setProperty(MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE, result.getOFvalue());
			}
		} catch (Exception e) {e.printStackTrace();}
		
		isObjectiveProblemRan = true;
	}
	
	/** @return the values of the selected fluxes in the solution of the initial objective problem 
	 * @throws MFAObjectiveSimulationException */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Double> getInicProblemFluxValues() throws MFAObjectiveSimulationException {
		
		if(!isObjectiveProblemRan)
			runObjectiveProblem();
		
		Map<String, Double> value = null;
		try {
			value = (Map) ManagerExceptionUtils.testCast(propreties, Map.class, MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		return value;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getSelectedFluxes() {
		List<String> value = null;
		try {
			value = (List) ManagerExceptionUtils.testCast(propreties, List.class, MFAProperties.ROBUSTNESS_SELECTED_FLUXES, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
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

	/** @return The interval for the percentage of the selected fluxes that will be used vary the its values and to perform the analysis */
	public Integer getPercentageInterval(){
		Integer value = null;
		try {
			value = (Integer) ManagerExceptionUtils.testCast(propreties, Integer.class, MFAProperties.ROBUSTNESS_PERCENTAGE_INTERVAL, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {}
		
		return value;
	}
	
	
	protected Double simulateForFluxPercentage(int fluxIndex, double fluxInitValue, int fluxPercentage) throws PropertyCastException, MandatoryPropertyException, IOException, SolverException{
		double fluxValue = fluxInitValue * fluxPercentage / 100;
		problem.getVariable(fluxIndex).setLowerBound(fluxValue);
		problem.getVariable(fluxIndex).setUpperBound(fluxValue);
		
		SolverType solverType = getSolverType();
		ILPSolver solver = solverType.lpSolver(problem);
		
		Double res = null;
		try {
			System.out.println("Simulationg for '" + problem.getVariable(fluxIndex).variableName + "' the percentage " + fluxPercentage + " of its initial value: " + fluxInitValue);
			LPSolution solution = solver.solve();
			res = solution.getOfValue();
		} catch (Exception e) {
			e.printStackTrace();}
		return res;
	}
	
	protected double[] simulateForFluxVariation(String fluxId, double fluxInitValue, int numberOfIntervals) throws Exception {
		
		int fluxIndex = getIdxVar(fluxId);
		int percentageInterval = getPercentageInterval();
		int p = 100 - percentageInterval;
		int i = 0;
		double[] objValues = new double[numberOfIntervals];
		
		double originalLB = problem.getVariable(fluxIndex).getLowerBound();
		double originalUB = problem.getVariable(fluxIndex).getUpperBound();
		
		objValues[i] = fluxInitValue; // 100% of the flux value 
		while(p>0)
		{
			Double v = simulateForFluxPercentage(fluxIndex, fluxInitValue, p);
			objValues[++i] = (v==null) ? 0 : v;
			p -= percentageInterval;
		}
		
		// For the first position. By decreasing the percentage, if the last percentage value is less than zero, the percentage is converted to zero.
		// If it is equal to zero, the zero value is used. This happens because depending on the flux percentage interval choosen, it might happen that
		// the first interval is not equal to the choosen interval.
		Double v = simulateForFluxPercentage(fluxIndex, fluxInitValue, 0);
		objValues[i++] = (v==null) ? 0 : v;
		
		
		// Reset the variable bounds to its original values
		problem.getVariable(fluxIndex).setLowerBound(originalLB);
		problem.getVariable(fluxIndex).setUpperBound(originalUB);
		return objValues;
	}
	
		
	@Override
	protected void createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();
		
		overrideRC = new MFARatiosOverrideModel(model, environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
	}
	
		
	@Override
	public MFARobustnessResult simulate() throws PropertyCastException, MandatoryPropertyException  {

		int percentageInterval = getPercentageInterval();
		MFARobustnessResult result = new MFARobustnessResult(model, getMethod(), percentageInterval);
		ErrorLog errLog = new ErrorLog();
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
			errLog.appendError("The problem could not be created. Reason:\n" + e1.getMessage());
		}
		
		if(proceed)
		{
			Map<String, Double> fluxInicValues = null;
			try {
				fluxInicValues = getInicProblemFluxValues();
			} catch (MFAObjectiveSimulationException e1) {
				e1.printStackTrace();
				errLog.appendError("The objective problem could not be ran. Reason:\n" + e1.toString());
				errLog.addException(e1);
			}
			
			if(fluxInicValues!=null)
			{
				int numberOfIntervals = result.calculateNumberOfIntervals();
				
				for(String fId : fluxInicValues.keySet())
				{
					double[] objValues = null;
					try {
						objValues = simulateForFluxVariation(fId, fluxInicValues.get(fId), numberOfIntervals);
					} catch (Exception e) {
						e.printStackTrace();
						errLog.addException(e);
					}
					
					result.setFluxPercentageSolutions(fId, objValues);
					
				}
				result.setEnvironmentalConditions(getEnvironmentalConditions());
				result.setGeneticConditions(getGeneticConditions());
				result.setOFString(getObjectiveFunctionToString());
				result.setOFvalue(getOfValue());
			}
		}
		result.setErrorLog(errLog);
		return result;
	}
	
	public Map<String, Double> getObjectiveFunction(){
		Map<String, Double> obj_coef = null;
		try {
			obj_coef = ManagerExceptionUtils.testCast(propreties, Map.class, SimulationProperties.OBJECTIVE_FUNCTION, false);
		} catch (Exception e) {
			obj_coef = new HashMap<String, Double>();
			obj_coef.put(model.getBiomassFlux(), 1.0);
		}
		return obj_coef;
	}
	
	@Override
	protected void createConstrains() throws MandatoryPropertyException, PropertyCastException, WrongFormulationException, SolverException  {
		getObjectiveProblem();
		List<LPConstraint> constraits = objectiveProblem.getProblem().getConstraints();
		problem.setConstraints(constraits);
	}
	
	@Override
	public LPProblem constructEmptyProblem() {
		try {
			getObjectiveProblem();
		} catch (Exception e) {e.printStackTrace();}
		
		return (objectiveProblem==null) ? null : objectiveProblem.constructEmptyProblem();
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException  {
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());
		
		Map<String, Double> obj_coef = getObjectiveFunction();
		
		for(String r : obj_coef.keySet()){
			double coef = obj_coef.get(r);
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get(r), coef, 0.0));
		}
	}
	
	public Double getOfValue(){
		Double ofValue = null;
		
		try {
			ofValue = (Double) ManagerExceptionUtils.testCast(propreties, Double.class, MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE, true);

		} catch (PropertyCastException e) {e.printStackTrace();
		} catch (MandatoryPropertyException e) {e.printStackTrace();}
		
		return ofValue;
	}
	

	@Override
	public String getObjectiveFunctionToString() {
		return objectiveProblem.getObjectiveFunctionToString();
	}

}
