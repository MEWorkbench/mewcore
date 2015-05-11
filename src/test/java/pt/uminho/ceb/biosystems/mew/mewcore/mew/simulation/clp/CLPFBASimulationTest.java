package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.abstracts.FBASimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CLPFBASimulationTest extends FBASimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CLP;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.87392151);
			results.put(AbstractSimulationTest.KO_REACTIONS, 0.33887138);
			results.put(AbstractSimulationTest.KO_GENETICS, -0.0);
			results.put(AbstractSimulationTest.UO_REACTIONS, 0.5162538);
			results.put(AbstractSimulationTest.UO_GENETICS, 0.40940924);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 0.36439464);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
		}
		return results;
	}
	
	

}
