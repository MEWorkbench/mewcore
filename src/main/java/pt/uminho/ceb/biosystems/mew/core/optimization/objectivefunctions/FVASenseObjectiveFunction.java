package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

/**
 * This objective function allows the user to maximize of minimize the FVAMax or FVAMin.
 * 
 * @author pmaia
 * @date Aug 6, 2014
 * @version 1.0
 * @since metabolic3persistent
 */
public class FVASenseObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long					serialVersionUID	= 1616476809886357786L;
	private String								_biomassID;
	private String								_targetID;
	private Boolean								_maximize;
	private Boolean								_fvaMax				= true;
	private SolverType							_solver				= null;
	private SimulationSteadyStateControlCenter	_cc					= null;
	
	public FVASenseObjectiveFunction(String biomassID, String targetID, Boolean maximize, Boolean fvaMax, SolverType solver) {
		_biomassID = biomassID;
		_targetID = targetID;
		_maximize = maximize;
		_fvaMax = fvaMax;
		_solver = solver;
	}
	
	private SimulationSteadyStateControlCenter getControlCenter(ISteadyStateModel model) {
		if (_cc == null) {
			_cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
			_cc.setSolver(_solver);
			_cc.setFBAObjSingleFlux(_targetID, 1.0);
			_cc.setMaximization(_fvaMax);
		}
		return _cc;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		double biomassFluxValue = simResult.getFluxValues().getValue(_biomassID) * 0.99999;
		
		ISteadyStateModel model = simResult.getModel();
		
		GeneticConditions gc = simResult.getGeneticConditions();
		EnvironmentalConditions ec = new EnvironmentalConditions();
		
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
		ec.addReactionConstraint(_biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
		
		getControlCenter(model).setEnvironmentalConditions(ec);
		getControlCenter(model).setGeneticConditions(gc);
		
		SteadyStateSimulationResult fvaResult = null;
		try {
			fvaResult = (SteadyStateSimulationResult) getControlCenter(model).simulate();
//			System.out.println(fvaResult.getEnvironmentalConditions().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fvaResult.getFluxValues().getValue(_targetID);
	}
	
	@Override
	public double getWorstFitness() {
		return (_maximize) ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.FVASENSE;
	}
	
	@Override
	public boolean isMaximization() {
		return _maximize;
	}
	
	@Override
	public double getUnnormalizedFitness(double fit) {
		return fit;
	}
	
	@Override
	public String getShortString() {
		return (_maximize ? "max" : "min")+"~FVA~" + (_fvaMax ? "max~" : "min~") + _targetID;
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + _biomassID + "," + _targetID + "," + _maximize + "," + _fvaMax+ ")";
	}
	
	@Override
	public String getLatexFormula() {
		return null;
	}
	
}
