package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.AbstractGeneticConditionsSimplifier;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public abstract class AbstractSimplifier extends AbstractGeneticConditionsSimplifier{
	
	protected SimulationSteadyStateControlCenter cc;
	
	public AbstractSimplifier(ISteadyStateModel model, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		super(model, referenceFD, envCond, solver);
		cc = new SimulationSteadyStateControlCenter(envCond, null, model, null);
		cc.setMaximization(true);
		cc.setSolver(solver);
		cc.setWTReference(referenceFD);
	}
	

	public SimulationSteadyStateControlCenter getControlCenterForMethod(String method){
		cc.setMethodType(method);
		return cc;
	}
	
}
