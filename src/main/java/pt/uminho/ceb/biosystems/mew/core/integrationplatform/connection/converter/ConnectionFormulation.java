package pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.converter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ISteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.OverrideSteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.UnderOverSingleReference;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;

public abstract class ConnectionFormulation implements ISteadyStateSimulationMethod {

	protected ISteadyStateModel model;
	
	protected OverrideSteadyStateModel overrideRC;
	protected Map<String, Object> properties;
	protected IConverter converter;
	
	protected Set<String> possibleProperties;
	protected Set<String> mandatoryProps;
	
	abstract protected Map<String, Object> createConverterParameteres();
	
	public ConnectionFormulation(ISteadyStateModel model, IConverter converter){
		this.model = model;
		this.converter = converter;
		initPropsKeys();
	}
	
	protected void initPropsKeys() {
		properties = new HashMap<String, Object>();
		
		mandatoryProps = new HashSet<String>();
		mandatoryProps.add(SimulationProperties.SOLVER);
		
		possibleProperties = new HashSet<String>();
		possibleProperties.add(SimulationProperties.IS_OVERUNDER_SIMULATION);
		possibleProperties.add(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
		possibleProperties.add(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
		possibleProperties.add(SimulationProperties.GENETIC_CONDITIONS);		
	}
	
	public SteadyStateSimulationResult simulate () throws Exception
	{
			converter.init();
			converter.sendModel(getModel(), getModifiedModelId());
//			System.out.println(createConverterParameteres());
//			System.out.println(properties);
			createModelOverride();
			if(overrideRC.getOverriddenReactions() != null)
				converter.sendModifiedModel(overrideRC);
			
			prepareMatlabEnvironment();
			executeSimulationCommand();
			return parseMatlabOutput();			
	}
	
	public String getModifiedModelId(){
		if(getModel().getId() == null || getModel().getId().equals(""))
			return "model";
		
		if(getModel().getId().startsWith("."))
			return "DOT" + getModel().getId();
		return getModel().getId();
	}
	
	public abstract void executeSimulationCommand();
	
	public abstract SteadyStateSimulationResult parseMatlabOutput();
	
	public abstract void prepareMatlabEnvironment/*ALTERAR NOME*/();
		// Todas os metodos e inicializacoes que sao necessarias para
		// que a funcao de simulacao funcione
		// Ex: no MOMA fazer a alteracao no modelo para comparar com o WT
	
	
	public ISteadyStateModel getModel(){
		return model;
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException{
		EnvironmentalConditions ec = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class, SimulationProperties.ENVIRONMENTAL_CONDITIONS, true); 
		return ec;
	}

	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions){
		setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
	}

	public GeneticConditions getGeneticConditions() throws PropertyCastException, MandatoryPropertyException{
		return (GeneticConditions) ManagerExceptionUtils.testCast(properties, GeneticConditions.class, SimulationProperties.GENETIC_CONDITIONS, true);
	}

	public void setGeneticConditions(GeneticConditions geneticConditions){
		setProperty(SimulationProperties.GENETIC_CONDITIONS, geneticConditions);
		setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, !geneticConditions.getReactionList().allKnockouts());
		
	} 
	
	protected void createModelOverride() throws Exception{
		
		boolean isOverUnder = false;
		
		try {
			isOverUnder = (Boolean) ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_OVERUNDER_SIMULATION,
					false);
		} catch (PropertyCastException e) {
			System.err.println("The property " + SimulationProperties.IS_OVERUNDER_SIMULATION + " was ignored!!\n Reason: " + e.getMessage());
		} catch (MandatoryPropertyException e) {
			isOverUnder = false;
		}
		
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
	
		if (isOverUnder){
			FluxValueMap reference = (FluxValueMap) ManagerExceptionUtils.testCast(properties
					, FluxValueMap.class, SimulationProperties.OVERUNDER_REFERENCE_FLUXES, false);
			
			if(geneticConditions == null) throw new Exception();
			overrideRC = new UnderOverSingleReference(model, environmentalConditions, geneticConditions, reference);
		}
		else{
			overrideRC = new OverrideSteadyStateModel(model,environmentalConditions, geneticConditions);
		}
				
	}

	public Set<String> getPossibleProperties(){
		return possibleProperties;
	}
	
	public Set<String> getMandatoryProperties(){
		return mandatoryProps;
	}

	public void setProperty(String m, Object o){
		properties.put(m, o);
	}
//	
	public void putAllProperties(Map<String, Object> p){
		this.properties.putAll(p);	
	}
//	
	public <T> T getProperty (String k){
		return (T) properties.get(k);
	}
//	
	public Class<?> getFormulationClass(){
		return null;
	}
	
	public IConverter getConverter() {
		return converter;
	}
	
	@Override
	public void forceSolverCleanup() {}
}
