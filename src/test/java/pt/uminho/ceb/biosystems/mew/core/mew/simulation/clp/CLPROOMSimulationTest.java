package pt.uminho.ceb.biosystems.mew.core.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.ROOMSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CLPROOMSimulationTest extends ROOMSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CLP;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 3.7302177);
			results.put(AbstractSimulationTest.KO_GENETICS, 12.023402);
			results.put(AbstractSimulationTest.UO_REACTIONS, -4.3020343E-5);
			results.put(AbstractSimulationTest.UO_GENETICS, 1.5511579);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 1.1384144);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 11.425106);
		}
		return results;
	}
	
	

}
