package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.InvalidTerminationCriteriaParameter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGeneSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISwapsSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 03-03-2015.
 */
public class JecoliGenericConfiguration extends GenericConfiguration implements IJecoliConfiguration, ISteadyStateConfiguration, IGeneSteadyStateConfiguration, ISwapsSteadyStateConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliGenericConfiguration() {
		super();
		loadMandatoryOptionalProperties();
	}
	
	public JecoliGenericConfiguration(Map<String,Object> propertyMapToCopy) {
		super(propertyMapToCopy);
		loadMandatoryOptionalProperties();
	}
	
	private void loadMandatoryOptionalProperties() {
		mandatoryPropertyMap.put(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, String.class);
		mandatoryPropertyMap.put(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, String.class);
		
		mandatoryPropertyMap.put(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, Boolean.class);
		mandatoryPropertyMap.put(GenericOptimizationProperties.STEADY_STATE_MODEL, ISteadyStateModel.class);
		
		mandatoryPropertyMap.put(GenericOptimizationProperties.SIMULATION_CONFIGURATION, Map.class);
		mandatoryPropertyMap.put(GenericOptimizationProperties.MAP_OF2_SIM, IndexedHashMap.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.TERMINATION_CRITERIA, ITerminationCriteria.class);
		
		// Recent change - It was mandatory
		optionalPropertyMap.put(GenericOptimizationProperties.NOT_ALLOWED_IDS, List.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.STATISTICS_CONFIGURATION, StatisticsConfiguration.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.REACTION_SWAP_MAP, Map.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.OU_RANGE, Pair.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS, Integer.class);
		optionalPropertyMap.put(GenericOptimizationProperties.MAX_SET_SIZE, Integer.class);
	}
	
	public int getNumberOfObjectives() {
		return getObjectiveFunctionsMap().size();
	}
	
	public int getMaxSetSize() {
		return getDefaultValue(GenericOptimizationProperties.MAX_SET_SIZE, 1);
	}
	
	public void setIsVariableSizeGenome(boolean isVariableSizeGenome) {
		propertyMap.put(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, isVariableSizeGenome);
	}
	
	
	public void setDecoder(ISteadyStateDecoder decoder) {
		propertyMap.put(JecoliOptimizationProperties.SOLUTION_DECODER, decoder);
	}
	
	public void setNotAllowedIds(List<String> notAllowedIds) {
		propertyMap.put(GenericOptimizationProperties.NOT_ALLOWED_IDS, notAllowedIds);
	}
	
	public List<String> getNonAllowedIds() {
		return (List<String>) propertyMap.get(GenericOptimizationProperties.NOT_ALLOWED_IDS);
	}
	
	public void setMaxSetSize(int maxSetSize) {
		propertyMap.put(GenericOptimizationProperties.MAX_SET_SIZE, maxSetSize);
	}
	
	public StatisticsConfiguration getStatisticsConfiguration() {
		return getDefaultValue(JecoliOptimizationProperties.STATISTICS_CONFIGURATION, new StatisticsConfiguration());
	}
	
	public <T extends JecoliGenericConfiguration, E extends IStrainOptimizationResult> void setOptimizationStrategyConverter(IJecoliOptimizationStrategyConverter<T, E> optimizationStrategyConverter) {
		propertyMap.put(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY_CONVERTER, optimizationStrategyConverter);
	}
	
	public Pair<Integer, Integer> getOURange() {
		return (Pair<Integer, Integer>) propertyMap.get(JecoliOptimizationProperties.OU_RANGE);
	}
	
	public void setOURange(Pair<Integer, Integer> ouRange) {
		propertyMap.put(JecoliOptimizationProperties.OU_RANGE, ouRange);
	}
	
	public void setArchiveManager(ArchiveManager archiveManager){
		propertyMap.put(JecoliOptimizationProperties.ARCHIVE_MANAGER, archiveManager);
	}
	
	
	
	// From IJecoliConfiguration
	
	public boolean getIsVariableSizeGenome() {
		return getDefaultValue(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
	}
	
	public ArchiveManager getArchiveManager(){
		return (ArchiveManager) propertyMap.get(JecoliOptimizationProperties.ARCHIVE_MANAGER);
	}
	
	public ITerminationCriteria getTerminationCriteria() throws InvalidTerminationCriteriaParameter {
		return getDefaultValue(JecoliOptimizationProperties.TERMINATION_CRITERIA, new NumFunctionEvaluationsListenerHybridTerminationCriteria(50000));
	}
	
	
	
	// From ISteadyStateConfiguration
	
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
	
	@SuppressWarnings("unchecked")
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctionsMap() {
		return (IndexedHashMap<IObjectiveFunction, String>) propertyMap.get(GenericOptimizationProperties.MAP_OF2_SIM);
	}
	
	public void setObjectiveFunctionsMap(IndexedHashMap<IObjectiveFunction, String> objectiveFunctionMap) {
		propertyMap.put(GenericOptimizationProperties.MAP_OF2_SIM, objectiveFunctionMap);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Map<String,Object>> getSimulationConfiguration(){
		return (Map<String, Map<String, Object>>) propertyMap.get(GenericOptimizationProperties.SIMULATION_CONFIGURATION);
	}

	public void setSimulationConfiguration(Map<String, Map<String, Object>> simulationConfiguration) {
		propertyMap.put(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
	}
	
	public ISteadyStateModel getSteadyStateModel() {
		return (ISteadyStateModel) propertyMap.get(GenericOptimizationProperties.STEADY_STATE_MODEL);
	}
	
	public void setModel(ISteadyStateModel model) {
		propertyMap.put(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
	}
	
	
	
	// From IGeneSteadyStateConfiguration
	
	public ISteadyStateGeneReactionModel getGeneReactionSteadyStateModel() throws Exception {
		Object o = propertyMap.get(GenericOptimizationProperties.STEADY_STATE_MODEL);
		if(ISteadyStateGeneReactionModel.class.isAssignableFrom(o.getClass())){
			return (ISteadyStateGeneReactionModel) o;
		}else{
			throw new Exception("Model is not assignable from ISteadyStateGeneReactionModel");
		}
	}
	
	
	
	// From ISwapsSteadyStateConfiguration

	public int getMaxAllowedSwaps() {
		return (int) propertyMap.get(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS);
	}
	
	public void setMaxAllowedSwaps(int maxAllowedSwaps) {
		propertyMap.put(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS, maxAllowedSwaps);
	}
	
	public Map<String, List<String>> getReactionSwapMap() {
		return (Map<String, List<String>>) propertyMap.get(JecoliOptimizationProperties.REACTION_SWAP_MAP);
	}
	
	public void setReactionSwapMap(Map<String, List<String>> reactionSwapMap) {
		propertyMap.put(JecoliOptimizationProperties.REACTION_SWAP_MAP, reactionSwapMap);
	}
	
	@Override
	public void validate() throws InvalidConfigurationException, ClassCastException {
		super.validate();
		
		List<String> nonDefinedPropertyList = new ArrayList<>();
		
		if(getIsOverUnderExpression()){
			for (String methodID : getSimulationConfiguration().keySet()) {
				Map<String, Object> mapByMethodID = getSimulationConfiguration().get(methodID);
				
				boolean isSimOverUnder = false;
				if(mapByMethodID.containsKey(SimulationProperties.IS_OVERUNDER_SIMULATION))
					isSimOverUnder = (boolean) mapByMethodID.get(SimulationProperties.IS_OVERUNDER_SIMULATION);
				
				if(!isSimOverUnder)
					nonDefinedPropertyList.add(SimulationProperties.IS_OVERUNDER_SIMULATION + " in simulation configuration ID: " +methodID);
				
			}
		}else{
			for (String methodID : getSimulationConfiguration().keySet()) {
				Map<String, Object> mapByMethodID = getSimulationConfiguration().get(methodID);
				
				boolean isSimOverUnder = false;
				if(mapByMethodID.containsKey(SimulationProperties.IS_OVERUNDER_SIMULATION))
					isSimOverUnder = (boolean) mapByMethodID.get(SimulationProperties.IS_OVERUNDER_SIMULATION);
				
				if(isSimOverUnder)
					nonDefinedPropertyList.add(SimulationProperties.IS_OVERUNDER_SIMULATION + " in simulation configuration ID: " +methodID);
				
			}
		}
		
		if (nonDefinedPropertyList.size() > 0) throw new InvalidConfigurationException(nonDefinedPropertyList);
	}
}
