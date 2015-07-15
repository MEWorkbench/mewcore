package pt.uminho.ceb.biosystems.mew.core.mew.simulation.glpk;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.NormLMomaSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class GLPKNormLMomaSimulationTest extends NormLMomaSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.GLPK;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 3.739532985E-5);
			results.put(AbstractSimulationTest.KO_REACTIONS, 18.58100574);
			results.put(AbstractSimulationTest.KO_GENETICS, 0.0);
			results.put(AbstractSimulationTest.UO_REACTIONS, 3.633883385E-5);
			results.put(AbstractSimulationTest.UO_GENETICS, 42.02444129);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 7.815506839);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
		}
		return results;
	}
}
