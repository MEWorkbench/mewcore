package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;

import java.io.IOException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public abstract class AbstractSSReferenceSimulation<T extends LPProblem> extends AbstractSSBasicSimulation<T>{
	
	
	protected MapStringNum wtReference;
	public AbstractSSReferenceSimulation(ISteadyStateModel model){
		super(model);
		initRefProperties();
	}
	
	private void initRefProperties() {
		possibleProperties.add(SimulationProperties.WT_REFERENCE);
		possibleProperties.add(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE);
	}
	
	@SuppressWarnings("unchecked")
	public MapStringNum getWTReference() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException{
		
		if(wtReference == null){
			
			try {
				wtReference = ManagerExceptionUtils.testCast(propreties, MapStringNum.class, SimulationProperties.WT_REFERENCE, true);
			} catch (PropertyCastException e) {
				System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
			} catch (MandatoryPropertyException e) {
				
			}
			
			if(wtReference == null){
				SolverType solver = getSolverType();
				EnvironmentalConditions envCond = getEnvironmentalConditions();
				
				wtReference = SimulationProperties.simulateWT(model, envCond, solver);
				
				setProperty(SimulationProperties.WT_REFERENCE, wtReference);
			}
		}
		return wtReference;
	}
	
	public void setReference(Map<String, Double> wtReference){
		setProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
	public void setUseDrainsInRef(boolean b){
		setProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, b);
	}
	
	public boolean getUseDrainsInRef(){
		Boolean useDrains;
		
		try {
			useDrains = (Boolean) ManagerExceptionUtils.testCast(propreties, Boolean.class, SimulationProperties.USE_DRAINS_IN_WT_REFERENCE,
					false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + e.getLocalizedMessage() + " was ignored!!\n Reason: " + e.getMessage());
			useDrains = false;
		} catch (MandatoryPropertyException e) {
			useDrains = false;
		}
		
		return useDrains;
	}
	
	@Override
	protected void putReactionExtraInfo(LPSolution solution,
			SteadyStateSimulationResult res) {

		super.putReactionExtraInfo(solution, res);
		res.getComplementaryInfoReactions().put(SimulationProperties.WT_REFERENCE, wtReference);
	}
		
}
