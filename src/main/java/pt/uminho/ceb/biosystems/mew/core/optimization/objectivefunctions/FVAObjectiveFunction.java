package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

/**
 * 
 * @author pmaia Feb 12, 2014
 */
public class FVAObjectiveFunction implements IObjectiveFunction, Serializable {
	
	private static final long					serialVersionUID	= 1616476809886357786L;
	private static final boolean				_debug				= false;
	private String								_biomassID;
	private String								_targetID;
	private Boolean								_maximize;
	private SimulationSteadyStateControlCenter	_cc					= null;
	
	public FVAObjectiveFunction(String biomassID, String targetID, Boolean maximize) {
		_biomassID = biomassID;
		_targetID = targetID;
		_maximize = maximize;
	}
	
	@Override
	public double evaluate(SteadyStateSimulationResult simResult) {
		
		double biomassFluxValue = simResult.getFluxValues().getValue(_biomassID) * 0.99999;
		
		if (_cc == null) initControlCenter(simResult);
				
		GeneticConditions gc = simResult.getGeneticConditions();
		
		EnvironmentalConditions ec = new EnvironmentalConditions();
		if (simResult.getEnvironmentalConditions() != null) ec.putAll(simResult.getEnvironmentalConditions());
		ec.addReactionConstraint(_biomassID, new ReactionConstraint(biomassFluxValue, 100000.0));
		
		_cc.setEnvironmentalConditions(ec);
		_cc.setGeneticConditions(gc);
		_cc.setFBAObjSingleFlux(_targetID, 1.0);
		
		SteadyStateSimulationResult fvaResult = null;
		try {
			fvaResult = (SteadyStateSimulationResult) _cc.simulate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double value = fvaResult.getFluxValues().getValue(_targetID); 
		if(Double.isNaN(value))
			value = 0;
//		System.out.println("[FVA] testing ["+gc.toStringOptions(",", true)+"]: fva="+value);
		return value;
	}
	
	private void initControlCenter(SteadyStateSimulationResult simResult) {
		if (_debug) System.out.println("[" + getClass().getSimpleName() + "]: initializing control center");
		_cc = new SimulationSteadyStateControlCenter(simResult.getEnvironmentalConditions(), simResult.getGeneticConditions(), simResult.getModel(), SimulationProperties.FBA);
		_cc.setSolver(SolverType.CPLEX3);
		_cc.setMaximization(_maximize);
		_cc.setFBAObjSingleFlux(_biomassID, 1.0);
		
		try {
			if(_debug) System.out.print("[" + getClass().getSimpleName() + "]:... warming up using [" + _biomassID + "]... ");
			SteadyStateSimulationResult res = _cc.simulate(); // warm-up
			if(_debug) System.out.println(res.getOFString() + "=" + res.getOFvalue() + " ...done!");
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public double getWorstFitness() {
		return (_maximize) ? -Double.MAX_VALUE : Double.MAX_VALUE;
	}
	
	@Override
	public ObjectiveFunctionType getType() {
		return ObjectiveFunctionType.FVA;
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
		return "FVA~" + (_maximize ? "max~" : "min~") + _targetID;
	}
	
	@Override
	public String getLatexString() {
		return getShortString();
	}
	
	@Override
	public String getBuilderString() {
		return getType() + "(" + _biomassID + "," + _targetID + "," + _maximize + ")";
	}
	
	@Override
	public String getLatexFormula() {
		return null;
	}
	
}
