package pt.uminho.ceb.biosystems.mew.core.mew.simulation.clp;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.AbstractSimulationTest;
import pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts.MiMBlSimulationTest;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CLPSolverBuilder;

public class CLPMiMBlSimulationTest extends MiMBlSimulationTest{

	@Override
	public String getSolver() {
		return CLPSolverBuilder.ID;
	}

	@Override
	protected Map<String, Double> getResults() {
		if(results == null){
			results= new HashMap<String, Double>();
			results.put(AbstractSimulationTest.WILDTYPE, -1.067123E-6);
			results.put(AbstractSimulationTest.KO_REACTIONS, 194.81571); 
			results.put(AbstractSimulationTest.KO_GENETICS, 201.45883); //NaN
			results.put(AbstractSimulationTest.UO_REACTIONS, -5.6267746E-8);
			results.put(AbstractSimulationTest.UO_GENETICS, 214.47306);
			results.put(AbstractSimulationTest.UO_REACTIONSGENETICS, 17.609333);
			results.put(AbstractSimulationTest.KO_REACTIONSGENETICS, 233.77655); //NaN
		}
		return results;
	}
	
	

}
