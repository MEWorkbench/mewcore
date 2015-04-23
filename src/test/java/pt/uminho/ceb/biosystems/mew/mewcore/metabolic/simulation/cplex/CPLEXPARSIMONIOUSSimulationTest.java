package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.cplex;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.FBASimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.PARSIMONIOUSSimulationTest;

public class CPLEXPARSIMONIOUSSimulationTest extends PARSIMONIOUSSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CPLEX;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 518.4170891056406);
			results.put(AbstractSimulationTest.KO_REACTIONS, 420.48614785283553);
			results.put(AbstractSimulationTest.KO_GENETICS, 0.78235105);
			results.put(AbstractSimulationTest.UO_REACTIONS, 656.1953145291727);
			results.put(AbstractSimulationTest.UO_GENETICS, 576.9901986943817);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 496.3986192266243);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
		}
		return results;
	}
}
