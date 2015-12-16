package pt.uminho.ceb.biosystems.mew.core.simulation.mfa;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.AbstractSimulationMethodsFactory;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.AbstractSimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.IConvexSteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ISteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.UnregistaredMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAApproaches;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.MFAMethodsFactory;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFADetermined;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFALeastSquares;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFAWeightedLeastSquares;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.linearprogramming.MFALP;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.linearprogramming.MFAParsimonious;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.linearprogramming.MFAQP;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.nullspace.MFANullSpace;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.robustness.MFARobustnessAnalysis;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability.MFAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.variability.MFATightBounds;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFASystemType;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class MFAControlCenter extends AbstractSimulationSteadyStateControlCenter implements Serializable {
	
	private static final long			serialVersionUID	= 1L;
	
	protected static MFAMethodsFactory	factory;
	
	protected boolean					forceRecreate		= false;
	
	static {
		LinkedHashMap<String, Class<?>> mapMethods = new LinkedHashMap<String, Class<?>>();
		mapMethods.put(MFAApproaches.quadraticProgramming.getPropertyDescriptor(), MFAQP.class);
		mapMethods.put(MFAApproaches.linearProgramming.getPropertyDescriptor(), MFALP.class);
		mapMethods.put(MFAApproaches.parsimonious.getPropertyDescriptor(), MFAParsimonious.class);
		
		mapMethods.put(MFAApproaches.fva.getPropertyDescriptor(), MFAFluxVariabilityAnalysis.class);
		mapMethods.put(MFAApproaches.tightBounds.getPropertyDescriptor(), MFATightBounds.class);
		
		mapMethods.put(MFAApproaches.robustnessAnalysis.getPropertyDescriptor(), MFARobustnessAnalysis.class);
		
		mapMethods.put(MFAApproaches.algebraDetermined.getPropertyDescriptor(), MFADetermined.class);
		mapMethods.put(MFAApproaches.algebraLSQ.getPropertyDescriptor(), MFALeastSquares.class);
		mapMethods.put(MFAApproaches.algebraWLSQ.getPropertyDescriptor(), MFAWeightedLeastSquares.class);
		
		mapMethods.put(MFAApproaches.nullSpace.getPropertyDescriptor(), MFANullSpace.class);
		
		factory = new MFAMethodsFactory(mapMethods);
	}
	
	/**
	 * @param environmentalConditions Environmental conditions
	 * @param geneticConditions Genetic Conditions (null)
	 * @param model Steady state model
	 * @param measuredFluxes Measured fluxes
	 * @param approach The approach to be used to perform the MFA, i.e., if it
	 *            will be used either the NullSpace, (Algebra / LP), or QP
	 *            approach
	 * @param systemType The type of the system, i.e.e, if it is either
	 *            undetermined or determined or overvetermined
	 */
	public MFAControlCenter(
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions,
			ISteadyStateModel model,
			ExpMeasuredFluxes measuredFluxes,
			FluxRatioConstraintList fluxRatioConstraints,
			MFAApproaches approach,
			MFASystemType systemType) {
		
		super(environmentalConditions, geneticConditions, model, approach.getPropertyDescriptor());
		
		addProperty(MFAProperties.MEASURED_FLUXES, measuredFluxes);
		addProperty(MFAProperties.MFA_APPROACH, approach);
		addProperty(MFAProperties.SYSTEM_TYPE, systemType);
		addProperty(MFAProperties.FLUX_RATIO_CONSTRAINTS, fluxRatioConstraints);
	}
	
	public SteadyStateSimulationResult simulate() throws Exception {		
		if (lastMethod == null || forceRecreate) {
			lastMethod = getFactory().getMethod(this.methodType, methodProperties, model);
			if(lastMethod!=null && IConvexSteadyStateSimulationMethod.class.isAssignableFrom(lastMethod.getClass())){
				((IConvexSteadyStateSimulationMethod)lastMethod).addPropertyChangeListener(this);				
			}
			forceRecreate = false;
		} else {
			lastMethod.putAllProperties(methodProperties);
		}
		
		return lastMethod.simulate();
	}
	
	public void setMeasuredFluxes(ExpMeasuredFluxes measuredFluxes) {
		if (measuredFluxes == null)
			removeProperty(MFAProperties.MEASURED_FLUXES);
		else
			addProperty(MFAProperties.MEASURED_FLUXES, measuredFluxes);
	}
	
	public void setFluxRatios(FluxRatioConstraintList fluxRatioContraints) {
		if (fluxRatioContraints == null)
			removeProperty(MFAProperties.FLUX_RATIO_CONSTRAINTS);
		else
			addProperty(MFAProperties.FLUX_RATIO_CONSTRAINTS, fluxRatioContraints);
	}
	
	@Override
	protected AbstractSimulationMethodsFactory getFactory() {
		return factory;
	}
	
	
	
	@Override
	public void addUnderOverRef() throws Exception {
	}
	
	/** This method is used if the chosen approach had been the LP one */
	public void setFBAObj(Map<String, Double> obj) {
		addProperty(SimulationProperties.OBJECTIVE_FUNCTION, obj);
	}
	
	/** This method is used if the chosen approach had been the LP one */
	public void setFBAObjSingleFlux(String fluxId, Double objValueCoef) {
		Map<String, Double> m = new HashMap<String, Double>();
		m.put(fluxId, 1.0);
		setFBAObj(m);
	}
	
	/** This method is used if the chosen approach had been the LP one */
	public void setMaximization(boolean isMaximization) {
		addProperty(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	/** This method is used if the chosen approach had been LP or QP */
	public void setSolver(SolverType solverType) {
		addProperty(SimulationProperties.SOLVER, solverType);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Null Space
	 * one
	 * 
	 * @param computeSensitivity indicates if the sensitivity must be computed
	 */
	public void setComputeSensitivity(Boolean computeSensitivity) {
		addProperty(MFAProperties.NULLSPACE_COMPUTESENTITIVITY, computeSensitivity);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Null Space
	 * one
	 * 
	 * @param alternativeFluxesSolution indicates if the alternative fluxes
	 *            solution should be computed
	 */
	public void setAlternativeFluxesSolutionn(Boolean alternativeFluxesSolution) {
		addProperty(MFAProperties.NULLSPACE_CALCULATEALTERNATIVEFLUXES, alternativeFluxesSolution);
	}
	
	/**
	 * This method can be used if the chosen approach had been the FVA one
	 * 
	 * @param minimumPercentage the minimum percentage of the minimum flux
	 */
	public void setMinimumPercentage(Double minimumPercentageFlux) {
		addProperty(MFAProperties.FVA_MIN_PERCENTAGE, minimumPercentageFlux);
	}
	
	/**
	 * This method can be used if the chosen approach had been the FVA one
	 * 
	 * @param minimumPercentageFlux the id of the minimum flux
	 */
	public void setMinimumPercentageFlux(String minimumPercentageFlux) {
		addProperty(MFAProperties.FVA_MIN_PERCENTAGE_FLUX, minimumPercentageFlux);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Classic
	 * Algebra and the system is overdetermined
	 * 
	 * @param fitting the type of fitting to be used (LSQ or Weighted LSQ)
	 */
	public void setFitting(String fitting) {
		addProperty(MFAProperties.MFA_ALGEBRA_FITTING, fitting);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Classic
	 * Algebra and the selected fitting is the weighed least squares
	 * 
	 * @param alpha if the Weighted LSQ fitting has been selected
	 */
	public void setAlpha(Double alpha) {
		addProperty(MFAProperties.MFA_ALGEBRA_WLSQ_ALPHA, alpha);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Robustness
	 * analysis
	 * 
	 * @param selectedFluxes is the set of fluxes for which the initial values
	 *            will be varied
	 */
	public void setSelectedFluxes(List<String> selectedFluxes) {
		addProperty(MFAProperties.ROBUSTNESS_SELECTED_FLUXES, selectedFluxes);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Robustness
	 * analysis
	 * 
	 * @param percentageInterval is the percentage interval with which the
	 *            initial values will be varied
	 */
	public void setPercentageInterval(int percentageInterval) {
		addProperty(MFAProperties.ROBUSTNESS_PERCENTAGE_INTERVAL, percentageInterval);
	}
	
	/**
	 * This method can be used if the chosen approach had been the Robustness
	 * analysis
	 * 
	 * @param objectiveFlux is the objective flux
	 */
	public void setRobustObjectiveFlux(String objectiveFlux) {
		addProperty(MFAProperties.ROBUSTNESS_OBJECTIVE_FLUX, objectiveFlux);
	}
	
	public ISteadyStateSimulationMethod getMethodToBeUsed() throws InstantiationException, InvocationTargetException, UnregistaredMethodException {
		return getFactory().getMethod(this.methodType, methodProperties, model);
	}
	
	public void setApproach(MFAApproaches approach) {
		addProperty(MFAProperties.MFA_APPROACH, approach);
		setMethodType(approach.getPropertyDescriptor());
	}
	
	public void setMethodType(String methodType) {
		if (!this.getMethodType().equals(methodType)) {
			forceRecreate = true;
		}
		super.setMethodType(methodType);
	}
	
}
