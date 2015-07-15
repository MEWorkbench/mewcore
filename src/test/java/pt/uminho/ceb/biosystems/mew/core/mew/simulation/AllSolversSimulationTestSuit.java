package pt.uminho.ceb.biosystems.mew.core.mew.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import pt.uminho.ceb.biosystems.mew.core.mew.simulation.cplex.CPLEXSimulationTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
//	GLPKSimulationTestSuite.class, 
//	CLPSimulationTestSuite.class, 
	CPLEXSimulationTestSuite.class
	})
public class AllSolversSimulationTestSuit {
	
}
