package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import cern.colt.Arrays;

public abstract class AbstractTestRunner {
	
	public String getSimulationMethod(String sm) throws Exception{
		SimulationMethodsEnum smconstant = Enum.valueOf(SimulationMethodsEnum.class, sm.toUpperCase()); 
		
		if(smconstant!=null)
			return smconstant.getSimulationProperty();
		else throw new Exception("Simulation method ["+sm+"] could not be resolved. Available ones are "+Arrays.toString(SimulationMethodsEnum.values()));
	}	

	public static void readConfig(final Properties properties,String confFile){
		try {			
			properties.load(new FileInputStream(confFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

