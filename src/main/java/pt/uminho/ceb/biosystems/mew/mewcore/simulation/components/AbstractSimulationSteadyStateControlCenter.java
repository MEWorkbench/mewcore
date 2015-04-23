package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.UnregistaredMethodException;

public abstract class AbstractSimulationSteadyStateControlCenter {
	
	protected ISteadyStateModel model;
		
	protected String methodType = SimulationProperties.FBA; 
	
	protected ISteadyStateSimulationMethod lastMethod;
	
	
	protected abstract AbstractSimulationMethodsFactory getFactory();
	
	public abstract void addUnderOverRef() throws Exception;

	protected Map<String,Object> methodProperties;
	
	
	public AbstractSimulationSteadyStateControlCenter(
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions, 
			ISteadyStateModel model,
			String methodType
			) {
		
		super();	
		this.model = model;
		methodProperties = new HashMap<String, Object>();
		addProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
		if(geneticConditions!=null)
			addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, geneticConditions.isOverUnder());
		addProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
		
		setMethodType(methodType);
	}
	
	
	public Object getProperty (String key)
	{
		return methodProperties.get(key);
	}

	public void setSimulationProperty (String key, Object value)
	{
		addProperty(key, value);
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() {
		return (EnvironmentalConditions)getProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}

	
	public void setEnvironmentalConditions(
			EnvironmentalConditions environmentalConditions) {
		addProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
		removeProperty(SimulationProperties.WT_REFERENCE);
	}

	private Object removeProperty(String key) {
		return methodProperties.remove(key);
		
	}

	public GeneticConditions getGeneticConditions()
	{
		return (GeneticConditions)getProperty(SimulationProperties.GENETIC_CONDITIONS);		
	}
	
	public void setGeneticConditions(GeneticConditions geneticConditions) {
		if(geneticConditions.getReactionList().allKnockouts())
			addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
		else
			addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
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
	
	
	public SteadyStateSimulationResult simulate() throws Exception{
		if(isUnderOverSimulation() && getUnderOverRef() == null){
			addUnderOverRef();
		}
		this.lastMethod = getFactory().getMethod(this.methodType, methodProperties, model);
		return lastMethod.simulate(); 
	}
	
	public void setUnderOverRef(FluxValueMap ref) throws Exception  {
		addProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
	}

	public Object getUnderOverRef() {
		return getProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
	}

	public boolean isUnderOverSimulation() {
		if(getProperty(SimulationProperties.IS_OVERUNDER_SIMULATION)==null)
			return false;
		return (Boolean) getProperty(SimulationProperties.IS_OVERUNDER_SIMULATION);
	}


	public ISteadyStateSimulationMethod getSimulatedMethod(){
		return lastMethod;
	}
	
	public void setWTReference(Map<String, Double> wtReference){
		addProperty(SimulationProperties.WT_REFERENCE, wtReference);
	}
	
	public void setReactionsKnockoutConditions(Set<String> knockoutIds){
		
//		FIXME: ReactionChangesList should has a constructor with Set<String>
		ReactionChangesList reactionList = new ReactionChangesList(new ArrayList<String>(knockoutIds));
		GeneticConditions genConditions = new GeneticConditions(reactionList, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void setGenesKnockoutConditions(Set<String> knockoutIds) throws Exception{
		GeneChangesList geneList = new GeneChangesList(new ArrayList<String>(knockoutIds));
		GeneticConditions genConditions = new GeneticConditions(geneList, (ISteadyStateGeneReactionModel)model, false);
		
		addProperty(SimulationProperties.GENETIC_CONDITIONS, genConditions);
		addProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
	}
	
	public void addProperty(String id, Object obj){
		methodProperties.put(id, obj);
	}
	
//	public static Set<String> getRegisteredMethods(){
//		return SimulationMethodsFactory.getRegisteredMethods();
//	}
//	
//	public static Class<? extends LPProblem> getProblemTypeFromMethod(String method){
//		return SimulationMethodsFactory.getProblemTypeFromMethod(method);
//	}
	
	

}
