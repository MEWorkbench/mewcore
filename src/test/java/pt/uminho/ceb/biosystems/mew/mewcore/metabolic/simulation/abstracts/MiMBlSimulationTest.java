package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover.MiMBl;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover.TurnOverProperties;

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
		return TurnOverProperties.MIMBL;
	}
	
	@Override
	protected boolean isMaximization() {
		return isMax;
	}

}
