package pt.uminho.ceb.biosystems.mew.core.optimization.controlcenter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.optimization.CobraGDLSFormulation;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.optimization.CobraOptKnockFormulation;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.OptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.AbstractSimulationMethodsFactory;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.AbstractSimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.RegistMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationMethodsFactory;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;

public class OptimizationSteadyStateControlCenter extends AbstractSimulationSteadyStateControlCenter implements Serializable{

	protected static SimulationMethodsFactory factory;
	
	static{
		
		LinkedHashMap<String, Class<?>> mapMethods = new LinkedHashMap<String,Class<?>>();
		
		mapMethods.put(OptimizationProperties.COBRA_GDLS, CobraGDLSFormulation.class);
		mapMethods.put(OptimizationProperties.COBRA_OPTKNOCK,  CobraOptKnockFormulation.class);
		
		factory = new SimulationMethodsFactory(mapMethods);
	}
	
	public OptimizationSteadyStateControlCenter(
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions, ISteadyStateModel model,
			String methodType) {
		super(environmentalConditions, geneticConditions, model, methodType);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractSimulationMethodsFactory getFactory() {
		return factory;
	}
	
	public SteadyStateSimulationResult optimize() throws Exception{
		return simulate();
	}

	@Override
	public void addUnderOverRef() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setTimeLimit(int timeLimit){
		addProperty(OptimizationProperties.TIME_LIMIT, timeLimit);
	}
	
	public int getTimeLimit(){
		return (int) (getProperty(OptimizationProperties.TIME_LIMIT));
	}
	
	public void setMaxKnockouts(int maxKnockouts){
		addProperty(OptimizationProperties.MAX_KOS, maxKnockouts);
	}
	
	public int getMaxKnockouts(){
		return (int) (getProperty(OptimizationProperties.MAX_KOS));
	}
	
	public void setProductFlux(String productFlux){
		addProperty(OptimizationProperties.PRODUCT_FLUX, productFlux);
	}
	
	public String getProductFlux(){
		return (String) (getProperty(OptimizationProperties.PRODUCT_FLUX));
	}
	
	public void setMinGrowth(double minGrowth){
		addProperty(OptimizationProperties.MIN_GROWTH, minGrowth);
	}
	
	public double getMinGrowth(){
		return (double) (getProperty(OptimizationProperties.MIN_GROWTH));
	}
	
	public void setSelectedReactions(Set<String> selectedReactions){
		addProperty(OptimizationProperties.SELECTED_RXNS, selectedReactions);
	}
	
	public Set<String> getSelectedReactions(){
		return (Set<String>) (getProperty(OptimizationProperties.SELECTED_RXNS));
	}
	
	
	// OptKnock
	public void setConstrainedReactions(Set<ConstrainedReaction> constrainedReactions){
		addProperty(OptimizationProperties.CONSTRAINED_REACTIONS, constrainedReactions);
	}
	
	public Set<ConstrainedReaction> getConstrainedReactions(){
		return (Set<ConstrainedReaction>) getProperty(OptimizationProperties.CONSTRAINED_REACTIONS);
	}
	
	
	// GDLS
	public void setNeighborhoodSize(int neighborhoodSize){
		addProperty(OptimizationProperties.NEIGHBORHOOD_SIZE, neighborhoodSize);
	}
	
	public int getNeighborhoodSize(){
		return (int) (getProperty(OptimizationProperties.NEIGHBORHOOD_SIZE));
	}
	
	public void setSearchPaths(int searchPaths){
		addProperty(OptimizationProperties.NUM_SEARCH_PATHS, searchPaths);
	}
	
	public int getSearchPaths(){
		return (int) (getProperty(OptimizationProperties.NUM_SEARCH_PATHS));
	}
	
	public void setIterationLimit(int iterationLimit){
		addProperty(OptimizationProperties.ITERATION_LIMIT, iterationLimit);
	}
	
	public int getIterationLimit(){
		return (int) (getProperty(OptimizationProperties.ITERATION_LIMIT));
	}
	
	
	static public void registMethod(String methodId, Class<?> klass) throws RegistMethodException, NoConstructorMethodException {
		factory.addSimulationMethod(methodId, klass);
	}
	
	public static void registerMethod(String id, Class<?> method) {
		factory.registerMethod(id, method);
	}

}
