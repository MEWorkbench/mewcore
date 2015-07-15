package pt.uminho.ceb.biosystems.mew.core.mew.simulation.glpk;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class) 
@Suite.SuiteClasses({ 
 GLPKFBASimulationTest.class, 
 GLPKLMOMASimulationTest.class, 
 GLPKMOMASimulationTest.class, 
 GLPKNormLMomaSimulationTest.class, 
 GLPKPARSIMONIOUSSimulationTest.class,
 GLPKMiMBlSimulationTest.class
}) 
	public class GLPKSimulationTestSuite {
	}
