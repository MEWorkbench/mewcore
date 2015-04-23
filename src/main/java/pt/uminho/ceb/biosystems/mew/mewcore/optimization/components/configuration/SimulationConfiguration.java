package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;

public class SimulationConfiguration extends ModelConfiguration {
	
	private static final long	serialVersionUID	= 1L;
	
	public static final String	ENV_COND_DELIMITER	= Delimiter.COMMA.toString();
	
	public static final String	SIM_PREFIX			= "simulation";
	
	public static final String	SIM_SOLVER			= "simulation.solver";
	
	public static final String	SIM_METHOD			= "simulation.method";
	
	public static final String	SIM_ENV_COND		= "simulation.environmentalconditions";	
	
	public SimulationConfiguration(String properties) throws Exception {
		super(properties);
		analyzeSimulationProperties();
	}
	
	private void analyzeSimulationProperties() throws Exception {
		
		if (!containsKey(SIM_METHOD))
			throw new Exception("Illegal SimulationProperties definition. Must define a [" + SIM_METHOD + "] property.");
		
	}
	
	public SolverType getSimulationSolver() {
		String tag = getProperty(SIM_SOLVER,currentState,true);
		if (tag != null)
			return SolverType.valueOf(tag.toUpperCase());
		else
			return null;
	}
	
	public List<String> getSimulationMethod() {
		String tag = getProperty(SIM_METHOD,currentState,true);
		String[] tags = tag.split(Delimiter.SEMICOLON.toString());
		ArrayList<String> sim = new ArrayList<String>(tags.length);
		for (String t : tags) {
			t = t.trim();
			sim.add(SimulationMethodsEnum.getFromString(t));
		}
		
		return sim;
	}
	
	public EnvironmentalConditions getEnvironmentalConditions(){
		String file = getProperty(SIM_ENV_COND,currentState,true);
		if(file!=null && !file.isEmpty()){
			try{
				return EnvironmentalConditions.readFromFile(file, ENV_COND_DELIMITER);
			}catch(IOException e){
				e.printStackTrace();
				return null;
			}
		}else return null;
	}
	
}
