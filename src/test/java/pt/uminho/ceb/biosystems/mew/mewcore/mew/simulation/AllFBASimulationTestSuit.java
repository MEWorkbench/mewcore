package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.clp.CLPFBASimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.cplex.CPLEXFBASimulationTest;
import pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.glpk.GLPKFBASimulationTest;

@RunWith(Suite.class)
@SuiteClasses({
	GLPKFBASimulationTest.class, 
	CLPFBASimulationTest.class, 
	CPLEXFBASimulationTest.class
	})
public class AllFBASimulationTestSuit {
	
}
