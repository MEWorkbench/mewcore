package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public abstract class AbstractSimplifier extends AbstractGeneticConditionsSimplifier {
	
	protected Map<String, SimulationSteadyStateControlCenter> ccs;
	
	public AbstractSimplifier(Map<String, Map<String, Object>> simulationConfiguration) {
		super(simulationConfiguration);
	}
	
	public SimulationSteadyStateControlCenter getControlCenterForMethod(String method) throws Exception {
		
		SimulationSteadyStateControlCenter cc = ccs.get(method);
		if (cc == null) {
			cc = new SimulationSteadyStateControlCenter(simulationConfiguration.get(method));
			ccs.put(method, cc);			
		}
		
		return cc;
	}
}
