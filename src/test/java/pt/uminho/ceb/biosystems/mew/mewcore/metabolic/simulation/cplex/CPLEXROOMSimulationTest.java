package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.cplex;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.FBASimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts.ROOMSimulationTest;

public class CPLEXROOMSimulationTest extends ROOMSimulationTest{

	@Override
	public SolverType getSolver() {
		return SolverType.CPLEX;
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
