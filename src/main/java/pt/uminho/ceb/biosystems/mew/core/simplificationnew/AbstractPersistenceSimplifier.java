package pt.uminho.ceb.biosystems.mew.core.simplificationnew;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.AbstractGeneticConditionsSimplifier;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public abstract class AbstractPersistenceSimplifier extends AbstractGeneticConditionsSimplifier{
	
	
	public AbstractPersistenceSimplifier(ISteadyStateModel model, FluxValueMap referenceFD, EnvironmentalConditions envCond, SolverType solver) {
		super(model, referenceFD, envCond, solver);
	}

	public SimulationSteadyStateControlCenter getControlCenterForMethod(String method){
		
		SimulationSteadyStateControlCenter cc = ccs.get(method);
		if (cc == null){
			cc = new SimulationSteadyStateControlCenter(envCond, null, model, method);
			cc.setMaximization(true);
			cc.setSolver(solver);
			cc.setWTReference(referenceFD);
			ccs.put(method, cc);
		}
		return cc;
	}
	
}
