package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public abstract class AbstractSSReferenceSimulation<T extends LPProblem> extends AbstractSSBasicSimulation<T>{
	
	
	protected MapStringNum wtReference;
	protected boolean resetReference = false;
	
	public AbstractSSReferenceSimulation(ISteadyStateModel model) {
		super(model);
		initRefProperties();
	}
	
	protected void initRefProperties() {
		optionalProperties.add(SimulationProperties.WT_REFERENCE);
		optionalProperties.add(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE);
	}
	
	public MapStringNum getWTReference() throws PropertyCastException, MandatoryPropertyException{
		
		if(wtReference == null){
			
			try {
				wtReference = ManagerExceptionUtils.testCast(properties, MapStringNum.class, SimulationProperties.WT_REFERENCE, true);
			} catch (PropertyCastException e) {
				System.err.println("The property " + e.getProperty() + " was ignored!!\n Reason: " + e.getMessage());
			} catch (MandatoryPropertyException e) {
				
			}
			
			if(wtReference == null){
				SolverType solver = getSolverType();
				EnvironmentalConditions envCond = getEnvironmentalConditions();
				try {
					if(debug) System.out.print("["+getClass().getSimpleName()+"] event [COMPUTING WT REFERENCE]...");
					if(debug_times) initTime = System.currentTimeMillis();
					wtReference = SimulationProperties.simulateWT(model, envCond, solver);
					if(debug) System.out.println("done!");
					if(debug_times) times.put("AbstractSSReferenceSimulation.getWTReference", System.currentTimeMillis() - initTime);
				} catch (Exception e) {
					throw new Error(e);
				}
				setProperty(SimulationProperties.WT_REFERENCE, wtReference);
			}
		}
		return wtReference;
	}
	
	public void setReference(Map<String, Double> wtReference){
		if(wtReference==null)
			this.wtReference = null;
//		else setProperty(SimulationProperties.WT_REFERENCE, wtReference);
		setProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
	public void setUseDrainsInRef(boolean b){
		if(wtReference!=null)
			setReference(null);
		setProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, b);
	}
	
	public boolean getUseDrainsInRef(){
		Boolean useDrains;
		
		try {
			useDrains = (Boolean) ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.USE_DRAINS_IN_WT_REFERENCE,
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
//		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		
		switch (event.getPropertyName()) {		
			case ListenerHashMap.PROP_UPDATE: {
								
				if (event.getKey().equals(SimulationProperties.WT_REFERENCE)) {
					resetReference = true;
					setRecreateOF(true);
				}
				
				if (event.getKey().equals(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE)) {
					resetReference = true;
					setRecreateOF(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	public void clearAllProperties(){
		super.clearAllProperties();
		setReference(null);
	}
	
	public void preSimulateActions(){
		if(resetReference)
			setReference(null);
	};
	
	public void postSimulateActions(){
		resetReference = false;
	};
		
}
