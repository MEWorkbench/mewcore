package pt.uminho.ceb.biosystems.mew.core.mew.simulation.cplex;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.ROOMSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;

public class CPLEXROOMSimulationTest extends ROOMSimulationTest{

	@Override
	public String getSolver() {
		return CPLEX3SolverBuilder.ID;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, 0.0);
			results.put(AbstractSimulationTest.KO_REACTIONS, 37.0);
			results.put(AbstractSimulationTest.KO_GENETICS, 0.0); //Infeasible Problem
			results.put(AbstractSimulationTest.UO_REACTIONS, 0.0);
			results.put(AbstractSimulationTest.UO_GENETICS, 35.0);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 32.0);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 0.0); //Infeasible Problem
		}
		return results;
	}	
}
