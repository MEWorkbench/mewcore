package pt.uminho.ceb.biosystems.mew.core.mew.simulation.cplex;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.LMOMASimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CPLEXLMOMASimulationTest extends LMOMASimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CPLEX3;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 285.7843825865247);
			results.put(AbstractSimulationTest.KO_GENETICS, 425.89928658441875);
			results.put(AbstractSimulationTest.UO_REACTIONS, 0.0);
			results.put(AbstractSimulationTest.UO_GENETICS, 299.09969020172133);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 109.88175406559344);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 425.90157471775206);
		}
		return results;
	}
}
