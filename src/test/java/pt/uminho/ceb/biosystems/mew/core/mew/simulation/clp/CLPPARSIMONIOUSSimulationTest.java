package pt.uminho.ceb.biosystems.mew.core.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.PARSIMONIOUSSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CLPPARSIMONIOUSSimulationTest extends PARSIMONIOUSSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CLP;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 518.41709);
			results.put(AbstractSimulationTest.KO_REACTIONS, 420.48612);
			results.put(AbstractSimulationTest.KO_GENETICS, 53.48625);
			results.put(AbstractSimulationTest.UO_REACTIONS, 656.19532);
			results.put(AbstractSimulationTest.UO_GENETICS, 576.99019);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 496.29771);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 68.003158);
		}
		return results;
	}
	
	

}
