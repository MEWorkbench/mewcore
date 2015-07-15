package pt.uminho.ceb.biosystems.mew.core.mew.simulation.abstracts;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.turnover.MiMBl;

public abstract class MiMBlSimulationTest extends AbstractSimulationTest{
		
	boolean isMax = true;
	String objFunc = "R_Biomass_Ecoli_core_w_GAM";
	
	protected Map<String, Double> results = null;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	
	@Override
	protected void setParameters()
	{			
		HashMap<String, Double> obj_coef = new HashMap<String, Double>();
		obj_coef.put(objFunc, 1.0);
		
		method = new MiMBl(super.model);

		// Mandatory properties:
		// Solver
		// IsMaximization
		method.setProperty(SimulationProperties.IS_MAXIMIZATION, isMax);
		//method.setProperty(SimulationProperties.OBJECTIVE_FUNCTION, obj_coef);
		
		super.setParameters();
	}
	
	@Override
	protected String getMethodString() {
		return SimulationProperties.MIMBL;
	}
	
	@Override
	protected boolean isMaximization() {
		return isMax;
	}

}
