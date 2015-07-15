package pt.uminho.ceb.biosystems.mew.core.mew.simulation.clp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class) 
@Suite.SuiteClasses({ 
 CLPFBASimulationTest.class, 
 CLPLMOMASimulationTest.class,  
 CLPNormLMomaSimulationTest.class, 
 CLPPARSIMONIOUSSimulationTest.class,
 CLPROOMSimulationTest.class,
 CLPMiMBlSimulationTest.class
}) 
	public class CLPSimulationTestSuite {
	}
