package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class PercentMaxTheoreticalCarbonYieldObjectiveFunction extends CarbonYieldObjectiveFunction {
	
	private static final long	serialVersionUID	= -1704252852124531600L;
	private SolverType			_solver				= null;
	private double				_maxTheorYield		= -1;
	
	public PercentMaxTheoreticalCarbonYieldObjectiveFunction(String substrateID, String targetID, String modelConfigFile, SolverType solver) throws Exception {
		super(substrateID, targetID, modelConfigFile);
		_solver = solver;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		double cYield = super.evaluate(simResult);
		if (_maxTheorYield < 0) initMTY(simResult);
		if (_maxTheorYield == 0) return 0.0;
		
		return cYield / _maxTheorYield;
	}
	
	private void initMTY(SteadyStateSimulationResult simResult) {
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), null, simResult.getModel(), simResult.getMethod());
		cc.setSolver(_solver);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(_targetID, 1.0);
		
		SteadyStateSimulationResult res = null;
		try {
			res = cc.simulate();
		} catch (Exception e) {
			e.printStackTrace();
			_maxTheorYield = 0;
			return;
		}
		
		double targ = Math.abs(res.getFluxValues().getValue(_targetID) * ((double) _carbonContentTarget));
		double subs = Math.abs(res.getFluxValues().getValue(_substrateID) * ((double) _carbonContentSubstrate));
		_maxTheorYield = (subs <= 0.0) ? getWorstFitness() : (targ / subs);
		
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.PMTCYIELD;
	}
	
	@Override
	public String getShortString() {
		return "PMTCYIELD";
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + _substrateID + "," + _targetID + "," + _configurationFile + "," + _solver + ")";
	}
}
