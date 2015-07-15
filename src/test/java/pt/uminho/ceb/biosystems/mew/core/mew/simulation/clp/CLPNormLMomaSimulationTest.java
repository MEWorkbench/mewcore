package pt.uminho.ceb.biosystems.mew.core.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.NormLMomaSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CLPNormLMomaSimulationTest extends NormLMomaSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CLP;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 6.2880225E-8);
			results.put(AbstractSimulationTest.KO_REACTIONS, 18.580999);
			results.put(AbstractSimulationTest.KO_GENETICS, 8553.4658);
			results.put(AbstractSimulationTest.UO_REACTIONS, 1.5429991E-7);
			results.put(AbstractSimulationTest.UO_GENETICS, 42.024494);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 16.885322);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 38.020611);
		}
		return results;
	}

}
