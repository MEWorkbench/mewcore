package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.AbstractObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

/**
 * 
 * @author pmaia
 * @date Sep 9, 2014
 * @version 1.0
 * @since metabolic3persistent
 */
public class MinimizeAlternativesObjectiveFunction extends AbstractObjectiveFunction {
	
	private static final long	serialVersionUID			= 1L;
	public static final String	ID							= "MIN_ALTERN";
	public static final boolean	_debug						= false;
	public static final String	CARBON						= "C";
	public static final double	THRESHOLD_VALID_ALTERNATIVE	= 0.000001;
	public static final double	BIOMASS_THRESHOLD			= 0.0001;
	
	public static final String	MIN_ALTERN_PARAM_ALTERNATIVES	= "Alternatives";
	public static final String	MIN_ALTERN_PARAM_BIOMASS		= "Biomass";
	public static final String	MIN_ALTERN_PARAM_PRODUCT		= "Product";
	public static final String	MIN_ALTERN_PARAM_SUBSTRATE		= "Substrate";
	public static final String	MIN_ALTERN_PARAM_CONTAINER		= "Container";
	public static final String	MIN_ALTERN_PARAM_SOLVER			= "Solver";
	/**
	 * By default this objective function minimizes the sum of the possible
	 * alternative fluxes normalized by carbon content, if this is set to true,
	 * it will minimize the number of active alternatives (discreve vs
	 * continuous)
	 */
	public static final String	MIN_ALTERN_PARAM_USE_COUNTS		= "UseCounts";
	
	private transient SimulationSteadyStateControlCenter	_cc							= null;
	private Map<String, Integer>							_alternativeCarbonContent	= null;
	
	static {
		Map<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(MIN_ALTERN_PARAM_ALTERNATIVES, ObjectiveFunctionParameterType.SET.of(ObjectiveFunctionParameterType.REACTION));
		myparams.put(MIN_ALTERN_PARAM_BIOMASS, ObjectiveFunctionParameterType.REACTION_BIOMASS);
		myparams.put(MIN_ALTERN_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(MIN_ALTERN_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		myparams.put(MIN_ALTERN_PARAM_CONTAINER, ObjectiveFunctionParameterType.CONTAINER);
		myparams.put(MIN_ALTERN_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		myparams.put(MIN_ALTERN_PARAM_USE_COUNTS, ObjectiveFunctionParameterType.BOOLEAN);
	}
	
	public MinimizeAlternativesObjectiveFunction() {
		super();
	}
	
	public MinimizeAlternativesObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public MinimizeAlternativesObjectiveFunction(Set<String> alternatives, String biomass, String target, String substrate, Container container, SolverType solver, Boolean useCountsInsteadOfCarbons) throws Exception {
		super(alternatives, biomass, target, substrate, container, solver, useCountsInsteadOfCarbons);
		if (!((Container) getParameterValue(MIN_ALTERN_PARAM_CONTAINER)).metabolitesHasFormula()) throw new Exception(getClass().getCanonicalName() + ": provided container ["
				+ ((Container) getParameterValue(MIN_ALTERN_PARAM_CONTAINER)).getModelName() + "] does not contain metabolite formulas. Metabolite formulas are required to calculate the carbon content of the metabolites.");
	}
	
	@Override
	protected void processParams(Object... params) {
		setParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES, params[0]);
		setParameterValue(MIN_ALTERN_PARAM_BIOMASS, params[1]);
		setParameterValue(MIN_ALTERN_PARAM_PRODUCT, params[2]);
		setParameterValue(MIN_ALTERN_PARAM_SUBSTRATE, params[3]);
		setParameterValue(MIN_ALTERN_PARAM_CONTAINER, params[4]);
		setParameterValue(MIN_ALTERN_PARAM_SOLVER, params[5]);
		setParameterValue(MIN_ALTERN_PARAM_USE_COUNTS, params[6]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		Set<String> alternatives = (Set<String>) getParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES);
		String biomassID = (String) getParameterValue(MIN_ALTERN_PARAM_BIOMASS);
		Boolean useCounts = (Boolean) getParameterValue(MIN_ALTERN_PARAM_USE_COUNTS);
		
		double res = getWorstFitness();
		
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: evaluating " + simResult.getGeneticConditions().toStringOptions(",", true));
		
		if (simResult != null && (simResult.getSolutionType().equals(LPSolutionType.OPTIMAL) || simResult.getSolutionType().equals(LPSolutionType.FEASIBLE))) {
			
			if (_cc == null) {
				initControlCenter(simResult);
			}
			if (alternatives == null || alternatives.isEmpty()) {
				computePossibleAlternatives(simResult);
			}
			if (_alternativeCarbonContent == null) {
				try {
					initCarbonContents(simResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			double biomassFluxValue = simResult.getFluxValues().getValue(biomassID) * 0.99999;
			
			if (biomassFluxValue > BIOMASS_THRESHOLD) {
				if (_debug) System.out.println("\t Biomass level = " + biomassFluxValue);
				res = 0.0;
				EnvironmentalConditions ec = new EnvironmentalConditions();
				if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
				ec.addReactionConstraint(biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
				_cc.setEnvironmentalConditions(ec);
				_cc.setGeneticConditions(simResult.getGeneticConditions());
				
				for (String alternative : alternatives) {
					
					_cc.setFBAObjSingleFlux(alternative, 1.0);
					SteadyStateSimulationResult result = null;
					
					try {
						result = _cc.simulate();
					} catch (Exception e) {
						e.printStackTrace();
						//						continue;
					}
					
					if (_debug) System.out.print("\t Alternative [" + alternative + "]... ");
					if (result != null && !result.getFluxValues().get(alternative).isNaN()) {
						double value = result.getFluxValues().get(alternative);
						if (_debug) System.out.println(value);
						if (value > 0 && Math.abs(value) > THRESHOLD_VALID_ALTERNATIVE) {
							if (useCounts)
								res += 1.0;
							else
								res += (getAlternativesCarbonContent().get(alternative) * value);
						}
					} else if (_debug) System.out.println(" NULL!!");
					
				}
			}
		}
		
		double finalFitness = (1.0 / (new Double(res) + 1));
		
		if (_debug) System.out.println("\t Final fitness = " + finalFitness);
		
		return finalFitness;
		
	}
	
	private void computePossibleAlternatives(SteadyStateSimulationResult simResult) {
		String productID = (String) getParameterValue(MIN_ALTERN_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(MIN_ALTERN_PARAM_SUBSTRATE);
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: computing possible alternatives to [" + productID + "]");
		
		Set<String> alternatives = new HashSet<String>();
		for (String r : simResult.getModel().getReactions().keySet())
			if (simResult.getModel().getReaction(r).getType().equals(ReactionType.DRAIN)) alternatives.add(r);
			
		alternatives.remove(productID);
		alternatives.remove(substrateID);
		
		EnvironmentalConditions ec = new EnvironmentalConditions();
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
		_cc.setEnvironmentalConditions(ec);
		_cc.setGeneticConditions(null);
		
		Set<String> finalAlternatives = new HashSet<>();
		
		for (String alt : alternatives) {
			if (_debug) System.out.print("[" + getClass().getSimpleName() + "]:\t testing alterative [" + alt + "]");
			_cc.setFBAObjSingleFlux(alt, 1.0);
			try {
				SteadyStateSimulationResult res = _cc.simulate();
				double value = res.getFluxValues().get(alt);
				if ((Math.abs(value) > THRESHOLD_VALID_ALTERNATIVE)) {
					finalAlternatives.add(alt);
					if (_debug) System.out.println("...YES!");
				} else if (_debug) System.out.println("...NO!");
			} catch (Exception e) {
				if (_debug) System.out.println("...NO (INFEASIBLE!");
				e.printStackTrace();
			}
		}
		
		setParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES, finalAlternatives);
	}
	
	@SuppressWarnings("unchecked")
	public void initCarbonContents(SteadyStateSimulationResult simResult) throws Exception {
		
		Container container = (Container) getParameterValue(MIN_ALTERN_PARAM_CONTAINER);
		
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: computing carbon contents...");
		ISteadyStateModel model = simResult.getModel();
		
		BalanceValidator validator = new BalanceValidator(container);
		validator.setFormulasFromContainer();
		Set<String> notCarbonBased = new HashSet<String>();
		for (String alternative : (Set<String>) getParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES)) {
			int drainIndex = model.getReactionIndex(alternative);
			int index = model.getMetaboliteFromDrainIndex(drainIndex);
			MetaboliteFormula formula = validator.getformula(model.getMetaboliteId(index));
			if (formula != null) {
				Integer carbonContent = formula.getValue(CARBON);
				if (carbonContent != null && carbonContent > 0) {
					getAlternativesCarbonContent().put(alternative, carbonContent);
					if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: carbon content for [" + alternative + "] = " + carbonContent);
				} else {
					notCarbonBased.add(alternative);
				}
			} else {
				if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: No formula present for [" + alternative + "]. Please validate this metabolite in your model.");
				notCarbonBased.add(alternative);
			}
		}
		
		((Set<String>) getParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES)).removeAll(notCarbonBased);
	}
	
	private void initControlCenter(SteadyStateSimulationResult simResult) {
		SolverType solver = (SolverType) getParameterValue(MIN_ALTERN_PARAM_SOLVER);
		String biomassID = (String) getParameterValue(MIN_ALTERN_PARAM_BIOMASS);
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: initializing control center");
		_cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), simResult.getGeneticConditions(), simResult.getModel(), SimulationProperties.FBA);
		_cc.setSolver(solver);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(biomassID, 1.0);
		
		try {
			System.out.print("[" + getClass().getSimpleName() + "]:... warming up using [" + biomassID + "]... ");
			SteadyStateSimulationResult res = _cc.simulate(); // warm-up
			System.out.println(res.getOFString() + "=" + res.getOFvalue() + " ...done!");
		} catch (Exception e) {
			//			e.printStackTrace();
			
		}
	}
	
	@Override
	public String getShortString() {
		return getID();
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(MIN_ALTERN_PARAM_ALTERNATIVES).toString() + "," + getParameterValue(MIN_ALTERN_PARAM_SOLVER) + ")";
	}
	
	public Map<String, Integer> getAlternativesCarbonContent() {
		if (_alternativeCarbonContent == null) {
			_alternativeCarbonContent = new HashMap<String, Integer>();
		}
		return _alternativeCarbonContent;
	}
	
	@Override
	public double getWorstFitness() {
		return -Double.MAX_VALUE;
	}
	
	@Override
	public boolean isMaximization() {
		return true;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		double ret = (1 / fit) - 1;
		return ret;
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getLatexFormula() {
		return null;
	}
	
	@Override
	public String getID() {
		return ID;
	}
}
