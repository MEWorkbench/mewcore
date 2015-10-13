package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.InvalidTerminationCriteriaParameter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 03-03-2015.
 */
public class JecoliGenericConfiguration extends GenericConfiguration implements IJecoliConfiguration {
	
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
		mandatoryPropertyMap.put(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM, String.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY, String.class);
		
		mandatoryPropertyMap.put(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, Boolean.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.STEADY_STATE_MODEL, ISteadyStateModel.class);
		
		mandatoryPropertyMap.put(JecoliOptimizationProperties.SOLVER, SolverType.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.SIMULATION_METHOD_LIST, List.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.IS_MAXIMIZATION, Boolean.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.MAP_OF2_SIM, IndexedHashMap.class);
		mandatoryPropertyMap.put(JecoliOptimizationProperties.TERMINATION_CRITERIA, ITerminationCriteria.class);
		
		// Recent change - It was mandatory
		optionalPropertyMap.put(JecoliOptimizationProperties.NOT_ALLOWED_IDS, List.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.ENVIRONMENTAL_CONDITIONS, EnvironmentalConditions.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.REFERENCE_FLUX_DISTRIBUITION, FluxValueMap.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.OU_2_STEP_APPROACH, Boolean.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.STATISTICS_CONFIGURATION, StatisticsConfiguration.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.REACTION_SWAP_MAP, Map.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.OU_RANGE, Pair.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS, Integer.class);
		optionalPropertyMap.put(JecoliOptimizationProperties.MAX_SET_SIZE, Integer.class);
	}
	
	public ITerminationCriteria getTerminationCriteria() throws InvalidTerminationCriteriaParameter {
		return getDefaultValue(JecoliOptimizationProperties.TERMINATION_CRITERIA, new NumFunctionEvaluationsListenerHybridTerminationCriteria(50000));
	}
	
	public String getOptimizationStrategy() {
		return (String) propertyMap.get(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY);
	}
	
	public boolean getIsGeneOptimization() {
		return getDefaultValue(JecoliOptimizationProperties.IS_GENE_OPTIMIZATION, false);
	}
	
	public boolean getIsOverUnderExpression() {
		return getDefaultValue(JecoliOptimizationProperties.IS_OVER_UNDER_EXPRESSION, false);
	}
	
	public boolean getIsVariableSizeGenome() {
		return getDefaultValue(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
	}
	
	public ISteadyStateModel getSteadyStateModel() {
		return (ISteadyStateModel) propertyMap.get(JecoliOptimizationProperties.STEADY_STATE_MODEL);
	}
	
	public ISteadyStateGeneReactionModel getGeneReactionSteadyStateModel() {
		return (ISteadyStateGeneReactionModel) propertyMap.get(JecoliOptimizationProperties.STEADY_STATE_GENE_REACTION_MODEL);
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() {
		return (EnvironmentalConditions) propertyMap.get(JecoliOptimizationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	public SolverType getSolver() {
		return (SolverType) propertyMap.get(JecoliOptimizationProperties.SOLVER);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getSimulationMethodList() {
		Collection<String> collection = (Collection<String>) propertyMap.get(JecoliOptimizationProperties.SIMULATION_METHOD_LIST);
		return new ArrayList<String>(collection);
	}
	
	public FluxValueMap getReferenceFluxDistribution() {
		return (FluxValueMap) propertyMap.get(JecoliOptimizationProperties.REFERENCE_FLUX_DISTRIBUITION);
	}
	
	public Boolean getIsMaximization() {
		return (Boolean) propertyMap.get(JecoliOptimizationProperties.IS_MAXIMIZATION);
	}
	
	public Boolean getOu2StepApproach() {
		return (Boolean) propertyMap.get(JecoliOptimizationProperties.OU_2_STEP_APPROACH);
	}
	
	@SuppressWarnings("unchecked")
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctionsMap() {
		return (IndexedHashMap<IObjectiveFunction, String>) propertyMap.get(JecoliOptimizationProperties.MAP_OF2_SIM);
	}
	
	public int getNumberOfObjectives() {
		return getObjectiveFunctionsMap().size();
	}
	
	public int getMaxSetSize() {
		return getDefaultValue(JecoliOptimizationProperties.MAX_SET_SIZE, 1);
	}
	
	public void setOptimizationStrategy(String optimizationStrategy) throws Exception {
		String strategy = optimizationStrategy.toUpperCase();
		propertyMap.put(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY, strategy);
	}
	
	public void setIsVariableSizeGenome(boolean isVariableSizeGenome) {
		propertyMap.put(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, isVariableSizeGenome);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		propertyMap.put(JecoliOptimizationProperties.ENVIRONMENTAL_CONDITIONS, environmentalConditions);
	}
	
	public void setSolver(SolverType solver) {
		propertyMap.put(JecoliOptimizationProperties.SOLVER, solver);
	}
	
	public void setSimulationMethod(List<String> simulationMethodList) {
		propertyMap.put(JecoliOptimizationProperties.SIMULATION_METHOD_LIST, simulationMethodList);
	}
	
	public void setIsMaximization(boolean isMaximization) {
		propertyMap.put(JecoliOptimizationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	public void setOu2StepApproach(boolean ou2StepApproach) {
		propertyMap.put(JecoliOptimizationProperties.OU_2_STEP_APPROACH, ou2StepApproach);
	}
	
	public void setModel(ISteadyStateModel model) {
		propertyMap.put(JecoliOptimizationProperties.STEADY_STATE_MODEL, model);
	}
	
	public void setDecoder(ISteadyStateDecoder decoder) {
		propertyMap.put(JecoliOptimizationProperties.SOLUTION_DECODER, decoder);
	}
	
	public void setMapOF2Sim(Map<IObjectiveFunction, String> mapOF2Sim) {
		propertyMap.put(JecoliOptimizationProperties.MAP_OF2_SIM, mapOF2Sim);
	}
	
	public void setNotAllowedIds(List<String> notAllowedIds) {
		propertyMap.put(JecoliOptimizationProperties.NOT_ALLOWED_IDS, notAllowedIds);
	}
	
	public List<String> getNonAllowedIds() {
		return (List<String>) propertyMap.get(JecoliOptimizationProperties.NOT_ALLOWED_IDS);
	}
	
	public void setMaxSetSize(int maxSetSize) {
		propertyMap.put(JecoliOptimizationProperties.MAX_SET_SIZE, maxSetSize);
	}
	
	public StatisticsConfiguration getStatisticsConfiguration() {
		return getDefaultValue(JecoliOptimizationProperties.STATISTICS_CONFIGURATION, new StatisticsConfiguration());
	}
	
	public void setOptimizationStrategyConverter(IJecoliOptimizationStrategyConverter<JecoliGenericConfiguration, IStrainOptimizationResult> optimizationStrategyConverter) {
		propertyMap.put(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY_CONVERTER, optimizationStrategyConverter);
	}
	
	public Map<String, List<String>> getReactionSwapMap() {
		return (Map<String, List<String>>) propertyMap.get(JecoliOptimizationProperties.REACTION_SWAP_MAP);
	}
	
	public void setReactionSwapMap(Map<String, List<String>> reactionSwapMap) {
		propertyMap.put(JecoliOptimizationProperties.REACTION_SWAP_MAP, reactionSwapMap);
	}
	
	public Pair<Integer, Integer> getOURange() {
		return (Pair<Integer, Integer>) propertyMap.get(JecoliOptimizationProperties.OU_RANGE);
	}
	
	public void setOURange(Pair<Integer, Integer> ouRange) {
		propertyMap.put(JecoliOptimizationProperties.OU_RANGE, ouRange);
	}
	
	public int getMaxAllowedSwaps() {
		return (int) propertyMap.get(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS);
	}
	
	public void setMaxAllowedSwaps(int maxAllowedSwaps) {
		propertyMap.put(JecoliOptimizationProperties.MAX_ALLOWED_SWAPS, maxAllowedSwaps);
	}
	
}
