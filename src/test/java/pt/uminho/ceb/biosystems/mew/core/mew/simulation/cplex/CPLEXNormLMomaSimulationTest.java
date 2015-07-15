package pt.uminho.ceb.biosystems.mew.core.mew.simulation.cplex;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.NormLMomaSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CPLEXNormLMomaSimulationTest extends NormLMomaSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CPLEX3;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 18.58099843228634);
			results.put(AbstractSimulationTest.KO_GENETICS, 29369.584660945722); //NaN
			results.put(AbstractSimulationTest.UO_REACTIONS, 0.0);
			results.put(AbstractSimulationTest.UO_GENETICS, 42.02449401561671);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 7.815502561088792);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 79.5459544053748); //NaN
		}
		return results;
	}
}
