package pt.uminho.ceb.biosystems.mew.core.mew.simulation.cplex;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class) 
@Suite.SuiteClasses({ 
 CPLEXFBASimulationTest.class, 
 CPLEXLMOMASimulationTest.class, 
 CPLEXMOMASimulationTest.class, 
 CPLEXNormLMomaSimulationTest.class, 
 CPLEXPARSIMONIOUSSimulationTest.class,
 CPLEXROOMSimulationTest.class,
 CPLEXMiMBlSimulationTest.class
}) 
	public class CPLEXSimulationTestSuite {
	}
