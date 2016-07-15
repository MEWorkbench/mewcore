package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public abstract class AbstractSimulationSteadyStateControlCenter implements PropertyChangeListener, Serializable {
	
	private static final long					serialVersionUID	= 1L;
																	
	public static final boolean					_debug				= false;
																	
	protected ISteadyStateModel					model;
												
	protected String							methodType			= SimulationProperties.FBA;
																	
	protected ISteadyStateSimulationMethod		lastMethod;
												
	protected ListenerHashMap<String, Object>	methodProperties;
												
	protected abstract AbstractSimulationMethodsFactory getFactory();
	
	public abstract void addUnderOverRef() throws Exception;
	
	public AbstractSimulationSteadyStateControlCenter(Map<String, Object> simulationConfiguration) {
		this((EnvironmentalConditions) simulationConfiguration.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS),
				(GeneticConditions) simulationConfiguration.get(SimulationProperties.GENETIC_CONDITIONS),
				(ISteadyStateModel) simulationConfiguration.get(SimulationProperties.MODEL),
				(String) simulationConfiguration.get(SimulationProperties.METHOD_ID));
	}
	
	public AbstractSimulationSteadyStateControlCenter(EnvironmentalConditions environmentalConditions, GeneticConditions geneticConditions, ISteadyStateModel model, String methodType) {
		
		this.model = model;
		methodProperties = new ListenerHashMap<String, Object>();
		methodProperties.addPropertyChangeListener(this);
		if (environmentalConditions != null)
			addProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);

		if (geneticConditions != null) {
			addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, geneticConditions.isOverUnder());
			addProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
		}
		
		setMethodType(methodType);
	}
	
	public Object getProperty(String key) {
		return methodProperties.get(key);
	}
	
	public void setSimulationProperty(String key, Object value) {
		addProperty(key, value);
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() {
		return (EnvironmentalConditions) getProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		if (environmentalConditions == null)
			removeProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
		else
			addProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
			
		setWTReference(null);
	}
	
	protected Object removeProperty(String key) {
		return methodProperties.remove(key);
		
	}
	
	public GeneticConditions getGeneticConditions() {
		return (GeneticConditions) getProperty(SimulationProperties.GENETIC_CONDITIONS);
	}
	
	public void setGeneticConditions(GeneticConditions geneticConditions) {
		if (geneticConditions == null) {
			removeProperty(SimulationProperties.GENETIC_CONDITIONS);
			removeProperty(SimulationProperties.IS_OVERUNDER_SIMULATION);
			removeProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
		} else {
			if (geneticConditions.getReactionList().allKnockouts())
				addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
			else
				addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
				
			addProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
		}
		
	}
	
	public String getMethodType() {
		return methodType;
	}
	
	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}
	
	public ISteadyStateModel getModel() {
		return model;
	}
	
	public SteadyStateSimulationResult simulate() throws Exception {
		if (isUnderOverSimulation() && getUnderOverRef() == null) {
			addUnderOverRef();
		}
		this.lastMethod = getFactory().getMethod(this.methodType, methodProperties, model);
		return lastMethod.simulate();
	}
	
	public void setUnderOverRef(FluxValueMap ref) {
		if (ref == null) {
			removeProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
		} else
			addProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
	}
	
	public Object getUnderOverRef() {
		return getProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
	}
	
	public boolean isUnderOverSimulation() {
		if (getProperty(SimulationProperties.IS_OVERUNDER_SIMULATION) == null)
			return false;
		return (Boolean) getProperty(SimulationProperties.IS_OVERUNDER_SIMULATION);
	}
	
	public boolean isUnderOver2StepApproach() {
		if (getProperty(SimulationProperties.OVERUNDER_2STEP_APPROACH) == null)
			return false;
		else
			return (Boolean) getProperty(SimulationProperties.OVERUNDER_2STEP_APPROACH);
	}
	
	public ISteadyStateSimulationMethod getSimulatedMethod() {
		return lastMethod;
	}
	
	public FluxValueMap getWTReference() {
		FluxValueMap wtref = (FluxValueMap) getProperty(SimulationProperties.WT_REFERENCE);
		return wtref;
	}
	
	public void setWTReference(Map<String, Double> wtReference) {
		addProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
	public void setReactionsKnockoutConditions(Set<String> knockoutIds) {
		
		//FIXME: ReactionChangesList should have a constructor with Set<String>
		ReactionChangesList reactionList = new ReactionChangesList(new ArrayList<String>(knockoutIds));
		GeneticConditions genConditions = new GeneticConditions(reactionList, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void setGenesKnockoutConditions(Set<String> knockoutIds) throws Exception {
		GeneChangesList geneList = new GeneChangesList(new ArrayList<String>(knockoutIds));
		GeneticConditions genConditions = new GeneticConditions(geneList, (ISteadyStateGeneReactionModel) model, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void addProperty(String id, Object obj) {
//		if (id == null) System.out.println("NULL ID");
//		if (obj == null) System.out.println("OBJ NULL");
//		if (methodProperties == null) System.out.println("method props null");
		methodProperties.put(id, obj);
	}
	
	public Map<String, Object> getMethodPropertiesMap() {
		return methodProperties;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		Object source = event.getSource();
		
		//		System.out.println("SOURCE EQUALS ?= "+source.equals(methodProperties));
		//		System.out.println("LAST METHOD ?= "+lastMethod);
		
		if (source.equals(methodProperties) && lastMethod != null) {
			if (_debug)
				System.out.println("[AbstractSimulationSteadyStateControlCenter]: sending event [" + event.getPropertyName() + "] to [" + lastMethod.getClass().getSimpleName() + "] = "
						+ event.getKey() + " from " + event.getOldValue()
						+ " to " + event.getNewValue());
			lastMethod.setProperty((String) event.getKey(), event.getNewValue());
		} else {
			if (event.getKey().equals(SimulationProperties.WT_REFERENCE)) {
				if (lastMethod != null && _debug)
					System.out.println("[AbstractSimulationSteadyStateControlCenter]: receiving event [" + event.getPropertyName() + "] from [" + lastMethod.getClass().getSimpleName() + "] = "
							+ event.getKey() + " to " + event.getNewValue());
				methodProperties.putQuietly((String) event.getKey(), event.getNewValue());
			}
		}
	}
	
	public ISteadyStateSimulationMethod getLastMethod() {
		return lastMethod;
	};
	
	public void saveModelToMPSFile(String file, Boolean includeTime) {
		if (lastMethod != null && IConvexSteadyStateSimulationMethod.class.isAssignableFrom(lastMethod.getClass())) {
			((IConvexSteadyStateSimulationMethod) lastMethod).saveModelToMPS(file, includeTime);
		}
	}
	
}
