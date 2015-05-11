package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.abstracts.LMOMASimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CLPLMOMASimulationTest extends LMOMASimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CLP;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 285.78438);
			results.put(AbstractSimulationTest.KO_GENETICS, 395.90621);
			results.put(AbstractSimulationTest.UO_REACTIONS, -8.7712632E-7);
			results.put(AbstractSimulationTest.UO_GENETICS, 299.09969);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 109.97111);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 373.3359);
		}
		return results;
	}
	
	

}
