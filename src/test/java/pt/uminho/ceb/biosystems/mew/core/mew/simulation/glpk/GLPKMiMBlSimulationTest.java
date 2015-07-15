package pt.uminho.ceb.biosystems.mew.core.mew.simulation.glpk;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.MiMBlSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class GLPKMiMBlSimulationTest extends MiMBlSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.GLPK;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 2.226183986E-4);
			results.put(AbstractSimulationTest.KO_REACTIONS, 194.8157647); 
			results.put(AbstractSimulationTest.KO_GENETICS, 0.0);
			results.put(AbstractSimulationTest.UO_REACTIONS, 3.644776512E-4);
			results.put(AbstractSimulationTest.UO_GENETICS, 214.4727954);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 17.58876122);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0);
		}
		return results;
	}
}
