package pt.uminho.ceb.biosystems.mew.core.integrationplatform.configuration;

import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.CobraStrainOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class CobraStrainOptimizationConfiguration extends GenericConfiguration implements ISteadyStateConfiguration {
	
	public CobraStrainOptimizationConfiguration() {
		super();
	}
	
	public CobraStrainOptimizationConfiguration(Map<String,Object> propertyMapToCopy) {
		super(propertyMapToCopy);
	}
	
	public String getOptimizationAlgorithm(){
		return (String)propertyMap.get(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM);
	}
	
	public void setOptimizationAlgorithm(String algorithm){
		propertyMap.put(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, algorithm);
	}
	
	@Override
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctionsMap() {
		return (IndexedHashMap<IObjectiveFunction, String>) propertyMap.get(GenericOptimizationProperties.MAP_OF2_SIM);
	}

	@Override
	public void setObjectiveFunctionsMap(IndexedHashMap<IObjectiveFunction, String> objectiveFunctionMap) {
		propertyMap.put(GenericOptimizationProperties.MAP_OF2_SIM, objectiveFunctionMap);
	}

	@Override
	public Map<String, Map<String, Object>> getSimulationConfiguration() {
		return (Map<String, Map<String, Object>>) propertyMap.get(GenericOptimizationProperties.SIMULATION_CONFIGURATION);
	}

	@Override
	public void setSimulationConfiguration(Map<String, Map<String, Object>> simulationConfiguration) {
		propertyMap.put(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
	}

	@Override
	public ISteadyStateModel getSteadyStateModel() {
		return (ISteadyStateModel) propertyMap.get(GenericOptimizationProperties.STEADY_STATE_MODEL);
	}

	@Override
	public void setModel(ISteadyStateModel model) {
		propertyMap.put(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
	}

	public String getOptimizationStrategy() {
		return (String) propertyMap.get(GenericOptimizationProperties.OPTIMIZATION_STRATEGY);
	}
	
	public boolean getIsGeneOptimization() {
		return getDefaultValue(GenericOptimizationProperties.IS_GENE_OPTIMIZATION, false);
	}
	
	public boolean getIsOverUnderExpression() {
		return getDefaultValue(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, false);
	}
	
	public void setOptimizationStrategy(String optimizationStrategy) {
		String strategy = optimizationStrategy.toUpperCase();
		propertyMap.put(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, strategy);
	}
	
	public void setTimeLimit(int timeLimit){
		setProperty(CobraStrainOptimizationProperties.TIME_LIMIT, timeLimit);
	}
	
	public int getTimeLimit(){
		return (int) (getProperty(CobraStrainOptimizationProperties.TIME_LIMIT));
	}
	
	public void setMaxModifications(int maxModifications){
		setProperty(GenericOptimizationProperties.MAX_SET_SIZE, maxModifications);
	}
	
	public int getMaxModifications(){
		return (int) (getProperty(GenericOptimizationProperties.MAX_SET_SIZE));
	}
	
	public void setProductFlux(String productFlux){
		setProperty(CobraStrainOptimizationProperties.PRODUCT_FLUX, productFlux);
	}
	
	public String getProductFlux(){
		return (String) (getProperty(CobraStrainOptimizationProperties.PRODUCT_FLUX));
	}
	
	public void setMinGrowth(double minGrowth){
		setProperty(CobraStrainOptimizationProperties.MIN_GROWTH, minGrowth);
	}
	
	public double getMinGrowth(){
		return (double) (getProperty(CobraStrainOptimizationProperties.MIN_GROWTH));
	}
	
	public void setSelectedReactions(Set<String> selectedReactions){
		setProperty(CobraStrainOptimizationProperties.SELECTED_RXNS, selectedReactions);
	}
	
	public Set<String> getSelectedReactions(){
		return (Set<String>) (getProperty(CobraStrainOptimizationProperties.SELECTED_RXNS));
	}
	
	
	// OptKnock
	public void setConstrainedReactions(Set<ConstrainedReaction> constrainedReactions){
		setProperty(CobraStrainOptimizationProperties.CONSTRAINED_REACTIONS, constrainedReactions);
	}
	
	public Set<ConstrainedReaction> getConstrainedReactions(){
		return (Set<ConstrainedReaction>) getProperty(CobraStrainOptimizationProperties.CONSTRAINED_REACTIONS);
	}
	
	// GDLS
	public void setNeighborhoodSize(int neighborhoodSize){
		setProperty(CobraStrainOptimizationProperties.NEIGHBORHOOD_SIZE, neighborhoodSize);
	}
	
	public int getNeighborhoodSize(){
		return (int) (getProperty(CobraStrainOptimizationProperties.NEIGHBORHOOD_SIZE));
	}
	
	public void setSearchPaths(int searchPaths){
		setProperty(CobraStrainOptimizationProperties.NUM_SEARCH_PATHS, searchPaths);
	}
	
	public int getSearchPaths(){
		return (int) (getProperty(CobraStrainOptimizationProperties.NUM_SEARCH_PATHS));
	}
	
	public void setIterationLimit(int iterationLimit){
		setProperty(CobraStrainOptimizationProperties.ITERATION_LIMIT, iterationLimit);
	}
	
	public int getIterationLimit(){
		return (int) (getProperty(CobraStrainOptimizationProperties.ITERATION_LIMIT));
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() {
		return (EnvironmentalConditions)getProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
	}

}
