package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionParameterType;

public class PercentMaxTheoreticalCarbonYieldObjectiveFunction extends CYIELDObjectiveFunction {
	
	private static final long	serialVersionUID	= 1L;
	public static final String	ID					= "PMTCYIELD";
	
	private double _maxTheorYield = -1;
	
	public static final String PMTCYIELD_PARAM_SOLVER = "Solver";
	
	public Map<String, ObjectiveFunctionParameterType> loadParameters(){
		HashMap<String, ObjectiveFunctionParameterType> myparams = new HashMap<>();
		myparams.put(CYIELD_PARAM_SUBSTRATE, ObjectiveFunctionParameterType.REACTION_SUBSTRATE);
		myparams.put(CYIELD_PARAM_PRODUCT, ObjectiveFunctionParameterType.REACTION_PRODUCT);
		myparams.put(CYIELD_PARAM_CONTAINER, ObjectiveFunctionParameterType.CONTAINER);
		myparams.put(PMTCYIELD_PARAM_SOLVER, ObjectiveFunctionParameterType.SOLVER);
		return Collections.unmodifiableMap(myparams);
	}
	
	public PercentMaxTheoreticalCarbonYieldObjectiveFunction() {
		super();
	}
	
	public PercentMaxTheoreticalCarbonYieldObjectiveFunction(Map<String, Object> configuration) throws InvalidObjectiveFunctionConfiguration {
		super(configuration);
	}
	
	public PercentMaxTheoreticalCarbonYieldObjectiveFunction(String substrateID, String targetID, Container container, String solver) throws Exception {
		processParams(substrateID, targetID, container, solver);
	}
	
	@Override
	protected void processParams(Object... params) {
		super.processParams(params);
		setParameterValue(PMTCYIELD_PARAM_SOLVER, params[3]);
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		double cYield = super.evaluate(simResult);
		if (_maxTheorYield < 0) initMTY(simResult);
		if (_maxTheorYield == 0) return 0.0;
		
		return cYield / _maxTheorYield;
	}
	
	private void initMTY(SteadyStateSimulationResult simResult) {
		String solver = (String) getParameterValue(PMTCYIELD_PARAM_SOLVER);
		String productID = (String) getParameterValue(CYIELD_PARAM_PRODUCT);
		String substrateID = (String) getParameterValue(CYIELD_PARAM_SUBSTRATE);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), null, simResult.getModel(), simResult.getMethod());
		cc.setSolver(solver);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(productID, 1.0);
		
		SteadyStateSimulationResult res = null;
		try {
			res = cc.simulate();
		} catch (Exception e) {
			e.printStackTrace();
			_maxTheorYield = 0;
			return;
		}
		
		double targ = Math.abs(res.getFluxValues().getValue(productID) * ((double) _carbonContentTarget));
		double subs = Math.abs(res.getFluxValues().getValue(substrateID) * ((double) _carbonContentSubstrate));
		_maxTheorYield = (subs <= 0.0) ? getWorstFitness() : (targ / subs);
		
	}
	
	@Override
	public String getShortString() {
		return "PMTCYIELD";
	}
	
	public String getID() {
		return ID;
	}
	
	@Override
	public String getBuilderString() {
		return getID() + "(" + getParameterValue(CYIELD_PARAM_SUBSTRATE) + "," + getParameterValue(CYIELD_PARAM_PRODUCT) + "," + ((Container) getParameterValue(CYIELD_PARAM_CONTAINER)).getModelName() + "," + getParameterValue(PMTCYIELD_PARAM_SOLVER)
				+ ")";
	}
}
