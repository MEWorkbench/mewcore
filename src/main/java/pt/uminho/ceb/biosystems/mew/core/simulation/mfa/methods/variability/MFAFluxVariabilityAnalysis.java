package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAWithSolvers;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.linearprogramming.MFALP;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public class MFAFluxVariabilityAnalysis<T extends MFAWithSolvers<?>> extends MFAWithSolvers<LPProblem> {
	
	/**
	 * The formulation maximize/minimize the flux to be fixed, for instance the
	 * minimum value of Biomass
	 */
	protected T			internalProblem				= null;
	protected boolean	recomputeObjectiveValue	= true;
	
	public MFAFluxVariabilityAnalysis(ISteadyStateModel model) {
		super(model);
		
		optionalProperties.add(MFAProperties.FVA_FIXED_FLUX_PROBLEM);
		optionalProperties.add(MFAProperties.FVA_FIXED_FLUX_VALUE);
		mandatoryProperties.add(MFAProperties.FVA_MIN_PERCENTAGE);
		mandatoryProperties.add(MFAProperties.FVA_MIN_PERCENTAGE_FLUX);
		mandatoryProperties.add(MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE);
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
		double ofValue = result.getOFvalue();
		setProperty(MFAProperties.FVA_FIXED_FLUX_VALUE, ofValue);
		
		try {
			if (getMinimumPercentage() != null) {
				String minimumFluxId = (String) ManagerExceptionUtils.testCast(properties, String.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX, true);
				double minimumFluxValue = result.getFluxValues().getValue(minimumFluxId);
				setProperty(MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE, minimumFluxValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		recomputeObjectiveValue = false;
	}
	
	public double getObjectiveValue() throws Exception {
		
		if (recomputeObjectiveValue) runObjectiveProblem();
		
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(properties, Double.class, MFAProperties.FVA_FIXED_FLUX_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		return value;
	}
	
	public Double getMinimumPercentage() {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(properties, Double.class, MFAProperties.FVA_MIN_PERCENTAGE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
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
	
	public String getMinimumPercentageFlux() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, IOException, SolverException {
		if (recomputeObjectiveValue) runObjectiveProblem();
		
		String flux = null;
		try {
			flux = (String) ManagerExceptionUtils.testCast(properties, String.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		return flux;
	}
	
	public Double getMinimumPercentageFluxValue() {
		Double value = null;
		try {
			value = (Double) ManagerExceptionUtils.testCast(properties, Double.class, MFAProperties.FVA_MIN_PERCENTAGE_FLUX_VALUE, true);
		} catch (PropertyCastException e) {
			System.err.println("Property ignored reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
		}
		
		return value;
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
	public IOverrideReactionBounds createModelOverride() throws PropertyCastException, MandatoryPropertyException {
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
		
		MFAFvaResult result = new MFAFvaResult(model, getMethod());
		
		boolean proceed = true;
		try {
			createProblemIfEmpty();
		} catch (Exception e1) {
			e1.printStackTrace();
			proceed = false;
		}
		
		EnvironmentalConditions origEnv = addObjectiveFluxLowerBound();
		
		if (proceed) {
			String objectiveFlux = getObjectiveFlux();
			for (String rId : model.getReactions().keySet())
				if (!rId.equals(objectiveFlux) && (vms == null || !vms.contains(rId)) && (knockouts == null || !knockouts.containsKey(rId))) {
					double[] bounds = new double[] { Double.NaN, Double.NaN };
					try {
						bounds = simulateFluxBounds(rId);
					} catch (WrongFormulationException | SolverException | IOException e) {
						e.printStackTrace();
					}
					
					result.addFluxBounds(rId, bounds[0], bounds[1]);
				}
		}
		
		setEnvironmentalConditions(origEnv);
		
		result.setEnvironmentalConditions(getEnvironmentalConditions());
		result.setGeneticConditions(getGeneticConditions());
		result.setOFString(getObjectiveFunctionToString());
		
		try {
			Integer objFluxIndex = (Integer) internalProblem.getProblem().getObjectiveFunction().getRow().getVarIdxs().iterator().next();
			String objFlux = internalProblem.getIdVar(objFluxIndex);
			result.setObjectiveFlux(objFlux);
			result.setObjectiveFluxValue(getObjectiveValue());
			result.setMinPercentage(getMinimumPercentage());
			result.setMinPercentageFlux(getMinimumPercentageFlux());
			result.setMinPercentageFluxValue(getMinimumPercentageFluxValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		TreeMap tm = new TreeMap();
//		tm.putAll(indexToIdVarMapings);
//		MapUtils.prettyPrint(tm);
		
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
			//			addObjectiveFluxLowerBound();
		}
	}
	
	public EnvironmentalConditions addObjectiveFluxLowerBound() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		Double percentage = null;
		String minPercentageFlux = null;
		EnvironmentalConditions origEnv = getEnvironmentalConditions();
		try {
			percentage = getMinimumPercentage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (percentage != null) {
			int index = -1;
			try {
				minPercentageFlux = getMinimumPercentageFlux();
				index = getIdxVar(minPercentageFlux);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new WrongFormulationException(e);
			} catch (SolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new WrongFormulationException(e);
			}
			EnvironmentalConditions newEnv = origEnv.copy();
			double upperbound = 100000;
			double min = getMinimumPercentageFluxValue() * percentage;
			ReactionConstraint rc = new ReactionConstraint(min, upperbound);
			try {
				boolean replace = newEnv.addReplaceReactionContraint(minPercentageFlux, rc);
				setEnvironmentalConditions(newEnv);
			} catch (SolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return origEnv;
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
					if(problem!=null)
						setRecomputeObjectiveValue(true);
				}
				
				if (key.equals(SimulationProperties.GENETIC_CONDITIONS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if(problem!=null){
						setRecomputeObjectiveValue(true);
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
					if(problem!=null){
						setRecomputeObjectiveValue(true);
						setRecreateOF(true);
					}
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					if(problem!=null){
						setUpdateFRConstraints(true);
						setRecomputeObjectiveValue(true);
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
					setRecomputeObjectiveValue(true);
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
					setRecomputeObjectiveValue(true);
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
					setRecomputeObjectiveValue(true);
					setRecreateOF(true);
				}
				
				if (key.equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setUpdateFRConstraints(true);
					setRecomputeObjectiveValue(true);
					setRecreateOF(true);
				}
				
				if (key.equals(MFAProperties.FVA_MIN_PERCENTAGE)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveValue(true);
				}
				
				if (key.equals(MFAProperties.FVA_MIN_PERCENTAGE_FLUX)) {
					getInternalProblem().setProperty((String) event.getKey(), evt.getNewValue());
					setRecomputeObjectiveValue(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	public void setRecomputeObjectiveValue(boolean recompute) {
		recomputeObjectiveValue = recompute;
	}
	
	public void preSimulateActions() {
		super.preSimulateActions();		
				
		if (recomputeObjectiveValue) {
			try {
				runObjectiveProblem();
			} catch (WrongFormulationException | SolverException | PropertyCastException | MandatoryPropertyException | IOException e) {
				e.printStackTrace();
			}
		}
//		if (updateFRConstraints) {
//			try {
//				updateConstraints();
//			} catch (PropertyCastException | MandatoryPropertyException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	public void postSimulateActions() {
		super.postSimulateActions();
		setRecomputeObjectiveValue(false);
	}
	
	@Override
	public void clearAllProperties() {
		super.clearAllProperties();
		getInternalProblem().clearAllProperties();
	}
	
}
