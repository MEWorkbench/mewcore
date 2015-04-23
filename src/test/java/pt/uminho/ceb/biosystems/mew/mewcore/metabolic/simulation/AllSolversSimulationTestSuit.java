package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.clp.CLPSimulationTestSuite;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.cplex.CPLEXSimulationTestSuite;
import pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.glpk.GLPKSimulationTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	GLPKSimulationTestSuite.class, 
	CLPSimulationTestSuite.class, 
	CPLEXSimulationTestSuite.class
	})
public class AllSolversSimulationTestSuit {
	
}
