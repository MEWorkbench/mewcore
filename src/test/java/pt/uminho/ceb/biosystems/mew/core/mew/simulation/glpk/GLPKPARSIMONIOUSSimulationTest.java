package pt.uminho.ceb.biosystems.mew.core.mew.simulation.glpk;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.PARSIMONIOUSSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class GLPKPARSIMONIOUSSimulationTest extends PARSIMONIOUSSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.GLPK;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 518.4170891);
			results.put(AbstractSimulationTest.KO_REACTIONS, 420.4861483);
			results.put(AbstractSimulationTest.KO_GENETICS, 0.78235105);
			results.put(AbstractSimulationTest.UO_REACTIONS, 656.1953146);
			results.put(AbstractSimulationTest.UO_GENETICS, 576.9897619);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 496.3986191);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
		}
		return results;
	}
}
