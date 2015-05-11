package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;

/**
 * 
 * 
 * 
 * @author pmaia
 * @date Sep 9, 2014
 * @version 1.0
 * @since metabolic3persistent
 */
public class MinimizeAlternativesObjectiveFunction implements IObjectiveFunction {
	
	private static final long					serialVersionUID			= -1704252852124531600L;
	public static final boolean					_debug						= false;
	public static final String					CARBON						= "C";
	public static final double					THRESHOLD_VALID_ALTERNATIVE	= 0.000001;
	public static final double					BIOMASS_THRESHOLD			= 0.0001;
	
	/**
	 * By default this objective function minimizes the sum of the possible
	 * alternative fluxes normalized by carbon content, if this is set to true,
	 * it will minimize the number of active alternatives (discreve vs
	 * continuous)
	 */
	private boolean								_useCountsInsteadOfCarbons	= false;
	
	private SolverType							_solver						= null;
	private Map<String, Integer>				_alternativeCarbonContent	= null;
	private Set<String>							_alternatives				= null;
	private Container							_container					= null;
	private SimulationSteadyStateControlCenter	_cc							= null;
	private ModelConfiguration					_modelConfig				= null;
	private String								_biomassID					= null;
	private String								_target						= null;
	private String								_substrate					= null;
	
	/**
	 * Default constructor
	 * 
	 * @param substrateID
	 * @param targetID
	 * @param alternatives
	 * @param modelConfigFile
	 * @param solver
	 * @param useCountsInsteadOfCarbons
	 * @throws Exception
	 */
	public MinimizeAlternativesObjectiveFunction(Set<String> alternatives, String target, String substrate, String modelConfigFile, SolverType solver, Boolean useCountsInsteadOfCarbons) throws Exception {
		_alternatives = alternatives;
		_target = target;
		_substrate = substrate;
		_modelConfig = new ModelConfiguration(modelConfigFile);
		_solver = solver;
		_useCountsInsteadOfCarbons = useCountsInsteadOfCarbons;
		_container = _modelConfig.getContainer();
		_biomassID = _modelConfig.getModelBiomass();
		if (!_container.metabolitesHasFormula())
			throw new Exception(getClass().getCanonicalName() + ": model [" + _modelConfig.getModelFile() + "] does not contain metabolite formulas. Formulas are required to calculate the carbon content of the metabolites.");
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		double res = getWorstFitness();
		
		
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: evaluating "+simResult.getGeneticConditions().toStringOptions(",", true));
		
		if (simResult != null && (simResult.getSolutionType().equals(LPSolutionType.OPTIMAL) || simResult.getSolutionType().equals(LPSolutionType.FEASIBLE))) {

			
			if (_cc == null) {
				initControlCenter(simResult);
			}
			if (_alternatives == null || _alternatives.isEmpty()) {
				computePossibleAlternatives(simResult);
			}
			if (_alternativeCarbonContent == null) {
				try {
					initCarbonContents(simResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			double biomassFluxValue = simResult.getFluxValues().getValue(_biomassID) * 0.99999;
			
			if (biomassFluxValue > BIOMASS_THRESHOLD) {
				if (_debug) System.out.println("\t Biomass level = "+biomassFluxValue);
				res = 0.0;
				EnvironmentalConditions ec = new EnvironmentalConditions();
				if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
				ec.addReactionConstraint(_biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
				_cc.setEnvironmentalConditions(ec);
				_cc.setGeneticConditions(simResult.getGeneticConditions());
				
				for (String alternative : _alternatives) {
				
					_cc.setFBAObjSingleFlux(alternative, 1.0);
					SteadyStateSimulationResult result = null;
					
					try {
						result = _cc.simulate();
					} catch (Exception e) {
						e.printStackTrace();
//						continue;
					}
					
					if (_debug) System.out.print("\t Alternative ["+alternative+"]... ");
					if (result != null && !result.getFluxValues().get(alternative).isNaN()) {
						double value = result.getFluxValues().get(alternative);
						if (_debug) System.out.println(value);
						if (value > 0 && Math.abs(value) > THRESHOLD_VALID_ALTERNATIVE) {
							if (_useCountsInsteadOfCarbons)
								res += 1.0;
							else
								res += (getAlternativesCarbonContent().get(alternative) * value);
						}
					}else
						if (_debug) System.out.println(" NULL!!");
					
				}
			}
		}
		
		double finalFitness = (1.0 / (new Double(res) + 1));
		
		if (_debug) System.out.println("\t Final fitness = "+finalFitness);
		
		return finalFitness;
		
	}
	
	private void computePossibleAlternatives(SteadyStateSimulationResult simResult) {
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: computing possible alternatives to [" + _target + "]");
		
		Set<String> alternatives = new HashSet<String>();
		for (String r : simResult.getModel().getReactions().keySet())
			if (simResult.getModel().getReaction(r).getType().equals(ReactionType.DRAIN)) alternatives.add(r);
		
		alternatives.remove(_target);
		alternatives.remove(_substrate);
		
		EnvironmentalConditions ec = new EnvironmentalConditions();
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
		_cc.setEnvironmentalConditions(ec);
		_cc.setGeneticConditions(null);
		
		for (String alt : alternatives) {
			if (_debug) System.out.print("[" + getClass().getSimpleName() + "]:\t testing alterative [" + alt + "]");
			_cc.setFBAObjSingleFlux(alt, 1.0);
			try {
				SteadyStateSimulationResult res = _cc.simulate();
				double value = res.getFluxValues().get(alt);
				if ((Math.abs(value) > THRESHOLD_VALID_ALTERNATIVE)) {
					_alternatives.add(alt);
					if (_debug) System.out.println("...YES!");
				} else if (_debug) System.out.println("...NO!");
			} catch (Exception e) {
				if (_debug) System.out.println("...NO (INFEASIBLE!");
				e.printStackTrace();
			}
		}
	}
	
	public void initCarbonContents(SteadyStateSimulationResult simResult) throws Exception {
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: computing carbon contents...");
		ISteadyStateModel model = simResult.getModel();
		
		BalanceValidator validator = new BalanceValidator(_container);
		validator.setFormulasFromContainer();
		Set<String> notCarbonBased = new HashSet<String>();
		for (String alternative : _alternatives) {
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
		
		_alternatives.removeAll(notCarbonBased);
		
		_container = null;
	}
	
	private void initControlCenter(SteadyStateSimulationResult simResult) {
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: initializing control center");
		_cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), simResult.getGeneticConditions(), simResult.getModel(), SimulationProperties.FBA);
//		_cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), null, simResult.getModel(), SimulationProperties.FBA);
		_cc.setSolver(_solver);
		_cc.setMaximization(true);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
		
		try {
			System.out.print("[" + getClass().getSimpleName() + "]:... warming up using [" + _biomassID + "]... ");
			SteadyStateSimulationResult res = _cc.simulate(); // warm-up
			System.out.println(res.getOFString() + "=" + res.getOFvalue() + " ...done!");
		} catch (Exception e) {
//			e.printStackTrace();
			
		}
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.MIN_ALTERN;
	}
	
	@Override
	public String getShortString() {
		return getType().name();
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + _alternatives.toString() + "," + _solver + ")";
	}
	
	public Map<String, Integer> getAlternativesCarbonContent() {
		if (_alternativeCarbonContent == null) _alternativeCarbonContent = new HashMap<String, Integer>();
		
		return _alternativeCarbonContent;
	}
	
	@Override
	public double getWorstFitness() {
//		return (_maximization) ? -Double.MAX_VALUE : Double.MAX_VALUE;
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
}
