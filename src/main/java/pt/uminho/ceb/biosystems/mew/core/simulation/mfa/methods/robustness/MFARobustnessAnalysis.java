package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.robustness;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.exceptions.ErrorLog;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.linearprogramming.MFALP;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public class MFARobustnessAnalysis<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem> {
	
	/**
	 * The formulation maximize/minimize the flux to be fixed, for instance the
	 * minimum value of Biomass
	 */
	protected T					internalProblem				= null;
	protected boolean			recomputeObjectiveProblem	= true;
	private static final double	RELAX						= 0.00001;
	
	public MFARobustnessAnalysis(ISteadyStateModel model) {
		super(model);
		
		mandatoryProperties.add(MFAProperties.ROBUSTNESS_PERCENTAGE_INTERVAL);
		mandatoryProperties.add(MFAProperties.ROBUSTNESS_SELECTED_FLUXES);
		optionalProperties.add(MFAProperties.ROBUSTNESS_OBJECTIVE_PROBLEM);
		optionalProperties.add(MFAProperties.ROBUSTNESS_OBJECTIVE_FLUX);
		optionalProperties.add(MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE);
		optionalProperties.add(MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MFAWithSolvers getInternalProblem() {
		if (internalProblem == null) {
			try {
				internalProblem = (T) ManagerExceptionUtils.testCast(properties, MFAWithSolvers.class, MFAProperties.FVA_FIXED_FLUX_PROBLEM, true);
			} catch (MandatoryPropertyException e) {
				internalProblem = null;
			} catch (PropertyCastException e) {
				System.err.println("Property ignored reason: " + e.getMessage());
				internalProblem = null;
			}
			
			if (internalProblem == null) {
				internalProblem = (T) new MFALP(model);
				internalProblem.putAllProperties(properties);
				setProperty(MFAProperties.FVA_FIXED_FLUX_PROBLEM, internalProblem);
			}
		}
		return internalProblem;
	}
	
	// Run the simulation for the objective problem
	private void runObjectiveProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		SteadyStateSimulationResult result = getInternalProblem().simulate();
		
		List<String> selectedFluxes = getSelectedFluxes();
		try {
			if (selectedFluxes != null) {
				Map<String, Double> origValues = new HashMap<String, Double>();
				for (String fluxId : selectedFluxes) {
					double fluxValue = result.getFluxValues().getValue(fluxId);
					origValues.put(fluxId, fluxValue);
				}
				setProperty(MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES, origValues);
				setProperty(MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE, result.getOFvalue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		recomputeObjectiveProblem = false;
	}
	
	/**
	 * @return the values of the selected fluxes in the solution of the initial
	 *         objective problem
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Double> getInicProblemFluxValues() throws Exception {
		
		if (recomputeObjectiveProblem) runObjectiveProblem();
		
		Map<String, Double> value = null;
		try {
			value = (Map) ManagerExceptionUtils.testCast(properties, Map.class, MFAProperties.ROBUSTNESS_SELECTED_FLUXES_INITVALUES, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		return value;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getSelectedFluxes() {
		List<String> value = null;
		try {
			value = (List) ManagerExceptionUtils.testCast(properties, List.class, MFAProperties.ROBUSTNESS_SELECTED_FLUXES, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		
		return value;
	}
	
	public String getObjectiveFlux() {
		try {
			Integer objFluxIndex = (Integer) getInternalProblem().getProblem().getObjectiveFunction().getRow().getVarIdxs().iterator().next();
			return internalProblem.getIdVar(objFluxIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return The interval for the percentage of the selected fluxes that will
	 *         be used vary the its values and to perform the analysis
	 */
	public Integer getPercentageInterval() {
		Integer value = null;
		try {
			value = (Integer) ManagerExceptionUtils.testCast(properties, Integer.class, MFAProperties.ROBUSTNESS_PERCENTAGE_INTERVAL, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored! Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		
		return value;
	}
	
	protected double simulateForFluxPercentage(String fluxID, double fluxInitValue, double fluxPercentage) throws PropertyCastException, MandatoryPropertyException {
		double fluxValue = fluxInitValue * fluxPercentage / 100.0d;
		
		EnvironmentalConditions origEnv = getEnvironmentalConditions();
		EnvironmentalConditions newEnv = origEnv.copy();
		
		ReactionConstraint rc = new ReactionConstraint(fluxValue - RELAX, fluxValue + RELAX);
		try {
			newEnv.addReplaceReactionContraint(fluxID, rc);
			setEnvironmentalConditions(newEnv);
		} catch (SolverException e) {
			e.printStackTrace();
		}
		
		SteadyStateSimulationResult res;
		double value = 0.0;
		try {
			res = super.simulate();
			value = res.getOFvalue();
		} catch (WrongFormulationException | SolverException e) {
			e.printStackTrace();
		}
		
		setEnvironmentalConditions(origEnv);
		return value;
	}
	
	protected double[] simulateForFluxVariation(String fluxId, double fluxInitValue, int numberOfIntervals) throws Exception {
		
		int percentageInterval = getPercentageInterval();
		int p = 100;
		int i = 0;
		double[] objValues = new double[numberOfIntervals];
		
		//		objValues[i] = fluxInitValue; // 100% of the flux value 
		while (p > 0) {
			//			if(p==100)
			//				objValues[i] = simulateForFluxPercentage(fluxId, fluxInitValue, 99.999);
			//			else
			objValues[i] = simulateForFluxPercentage(fluxId, fluxInitValue, p);
			p -= percentageInterval;
			if (p > 0) i++;
		}
		
		// For the first position. By decreasing the percentage, if the last percentage value is less than zero, the percentage is converted to zero.
		// If it is equal to zero, the zero value is used. This happens because depending on the flux percentage interval choosen, it might happen that
		// the first interval is not equal to the choosen interval.
		objValues[i + 1] = simulateForFluxPercentage(fluxId, fluxInitValue, 0);
		
		return objValues;
	}
	
	@Override
	public IOverrideReactionBounds createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();
		
		MFARatiosOverrideModel overrideRC = new MFARatiosOverrideModel(model, environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
		return overrideRC;
	}
	
	@Override
	public MFARobustnessResult simulate() throws PropertyCastException, MandatoryPropertyException {
		
		int percentageInterval = getPercentageInterval();
		MFARobustnessResult result = new MFARobustnessResult(model, getMethod(), percentageInterval);
		ErrorLog errorLog = new ErrorLog();
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
			errorLog.appendError("The problem could not be created. Reason:\n" + e1.getMessage());
		}
		
		if (proceed) {
			int numberOfIntervals = result.calculateNumberOfIntervals();
			Map<String, Double> fluxInicValues;
			
			try {
				fluxInicValues = getInicProblemFluxValues();
				
				for (String fId : fluxInicValues.keySet()) {
					double[] objValues = null;
					try {
						objValues = simulateForFluxVariation(fId, fluxInicValues.get(fId), numberOfIntervals);
					} catch (Exception e) {
						errorLog.appendError("The objective problem could not be ran. Reason:\n" + e.toString());
						errorLog.addException(e);
						e.printStackTrace();
					}
					
					result.setFluxPercentageSolutions(fId, objValues);
				}
			} catch (Exception e1) {
				errorLog.appendError("The objective problem could not be ran. Reason:\n" + e1.toString());
				errorLog.addException(e1);
				e1.printStackTrace();
			}
		}
		result.setErrorLog(errorLog);
		result.setEnvironmentalConditions(getEnvironmentalConditions());
		result.setGeneticConditions(getGeneticConditions());
		result.setOFString(getObjectiveFunctionToString());
		result.setOFvalue(getOfValue());
		
		return result;
	}
	
	public Double getOfValue() {
		Double ofValue = null;
		
		try {
			ofValue = (Double) ManagerExceptionUtils.testCast(properties, Double.class, MFAProperties.ROBUSTNESS_WT_OBJECTIVE_VALUE, true);
			
		} catch (PropertyCastException e) {
			e.printStackTrace();
		} catch (MandatoryPropertyException e) {
			e.printStackTrace();
		}
		
		return ofValue;
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
		return internalProblem.getObjectiveFunctionToString();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		String key = (String) event.getKey();
		
		//		System.out.println("[" + getClass().getSimpleName() + "]: got event ["+evt.getPropertyName()+"]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
		
		switch (event.getPropertyName()) {
			case ListenerHashMap.PROP_PUT: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [PUT]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (key.equals(SimulationProperties.SOLVER)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (event.getKey().equals(SimulationProperties.IS_MAXIMIZATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.ENVIRONMENTAL_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					//					if(problem!=null)
					//						setRecomputeObjectiveProblem(true);
				}
				
				if (key.equals(SimulationProperties.GENETIC_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if (problem != null) {
						setRecomputeObjectiveProblem(true);
						setRecreateOF(true);
					}
				}
				
				if (key.equals(SimulationProperties.IS_OVERUNDER_SIMULATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_REFERENCE_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_2STEP_APPROACH)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(MFAProperties.MEASURED_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if (problem != null) {
						setRecomputeObjectiveProblem(true);
						setRecreateOF(true);
					}
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if (problem != null) {
						setUpdateFRConstraints(true);
						setRecomputeObjectiveProblem(true);
						setRecreateOF(true);
					}
					
				}
				
				break;
			}
			case ListenerHashMap.PROP_UPDATE: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [UPDATE]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (key.equals(SimulationProperties.SOLVER)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.ENVIRONMENTAL_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveProblem(true);
				}
				
				if (event.getKey().equals(MFAProperties.IS_MAXIMIZATION)) {
					problem.changeObjectiveSense((boolean) evt.getNewValue());
					setRecreateOF(true);
				}
				
				if (event.getKey().equals(MFAProperties.OBJECTIVE_FUNCTION)) {
					setRecreateOF(true);
				}
				
				if (key.equals(SimulationProperties.GENETIC_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveProblem(true);
					setRecreateOF(true);
				}
				
				if (key.equals(SimulationProperties.IS_OVERUNDER_SIMULATION)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_REFERENCE_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(SimulationProperties.OVERUNDER_2STEP_APPROACH)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
				}
				
				if (key.equals(MFAProperties.MEASURED_FLUXES)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveProblem(true);
					setRecreateOF(true);
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateFRConstraints(true);
					setRecomputeObjectiveProblem(true);
					setRecreateOF(true);
				}
				
				if (key.equals(MFAProperties.FVA_MIN_PERCENTAGE)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveProblem(true);
				}
				
				if (key.equals(MFAProperties.FVA_MIN_PERCENTAGE_FLUX)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveProblem(true);
				}
				
				break;
			}
			default:
				break;
				
		}
	}
	
	public void setRecomputeObjectiveProblem(boolean recompute) {
		recomputeObjectiveProblem = recompute;
	}
	
	public void preSimulateActions() {
		super.preSimulateActions();
		
		if (recomputeObjectiveProblem) {
			try {
				runObjectiveProblem();
			} catch (WrongFormulationException | SolverException | PropertyCastException | MandatoryPropertyException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void postSimulateActions() {
		super.postSimulateActions();
		setRecomputeObjectiveProblem(false);
	}
	
	@Override
	public void clearAllProperties() {
		super.clearAllProperties();
		getInternalProblem().clearAllProperties();
	}
	
}
