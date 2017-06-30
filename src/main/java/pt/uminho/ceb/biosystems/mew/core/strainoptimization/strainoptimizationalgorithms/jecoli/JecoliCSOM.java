package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithmResult;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunctionListener;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IElementsRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManagerBestSolutions;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.plotting.IPlotter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.AbstractStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.evaluationfunction.StrainOptimizationEvaluationFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.archivetrimming.SelectionValueTrimmer;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

/**
 * Created by ptiago on 03-03-2015.
 */
public abstract class JecoliCSOM<T extends IJecoliConfiguration, E extends IJecoliOptimizationStrategyConverter> extends AbstractStrainOptimizationAlgorithm<T>
		implements IJecoliStrainOptimizationAlgorithm<T> {
		
	private static final long									serialVersionUID	= 1L;
																					
	protected Map<String, SimulationSteadyStateControlCenter>	controlCenters;
	protected E													optimizationStrategyConverter;
																
	public JecoliCSOM(E optimizationStrategyConverter) {
		this.optimizationStrategyConverter = optimizationStrategyConverter;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArchiveManager configureDefaultArchive(IAlgorithm optimizationAlgorithm, IEvaluationFunction evaluationFunction) {
		
		InsertionStrategy insertionEventType = (InsertionStrategy) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_INSERT_EVENT_TYPE);
		InsertionStrategy insertionFilter = (InsertionStrategy) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_INSERT_FILTER);
		ProcessingStrategy processingStategyFilter = (ProcessingStrategy) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_PROCESSING_STRATEGY);
		
		insertionEventType = (insertionEventType == null) ? InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT : insertionEventType;
		insertionFilter = (insertionFilter == null) ? InsertionStrategy.ADD_SMART_KEEP_BEST : insertionFilter;
		processingStategyFilter = (processingStategyFilter == null) ? ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT : processingStategyFilter;
		
		ArchiveManager archive = new ArchiveManagerBestSolutions(
				optimizationAlgorithm,
				insertionEventType,
				insertionFilter,
				processingStategyFilter,
				true);
				
		Integer maxArchiveSize = (Integer) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_MAX_SIZE);
		
		archive.setMaximumArchiveSize(maxArchiveSize != null ? maxArchiveSize : 100);
		
		ITrimmingFunction trimmer = (evaluationFunction.getNumberOfObjectives() > 1) ? new ZitzlerTruncation(archive.getMaximumArchiveSize(), evaluationFunction)
				: new SelectionValueTrimmer(archive.getMaximumArchiveSize(), 0.000002);
		archive.addTrimmingFunction(trimmer);
		
		return archive;
	}
	
	public IStrainOptimizationResultSet processAlgorithmResult(
			ArchiveManager archive,
			T configuration,
			AbstractMultiobjectiveEvaluationFunction<IRepresentation> evaluationFunction,
			IAlgorithmResult result,
			ISteadyStateDecoder decoder) throws Exception {
			
		ISolutionSet<IElementsRepresentation<?>> finalSolutionSet = archive.getArchive();
		
		List<IStrainOptimizationResult> strainOptimizationSolutionList = new ArrayList<>();
		controlCenters = new HashMap<String, SimulationSteadyStateControlCenter>();
		Map<String, Map<String, Object>> simConfiguration = configuration.getSimulationConfiguration();
		
		for (int j = 0; j < finalSolutionSet.getNumberOfSolutions(); j++) {
			ISolution solution = finalSolutionSet.getSolution(j);
			
			Double[] fitnesses = (solution.getNumberOfObjectives() > 1) ? solution.getFitnessValuesArray() : new Double[] { solution.getScalarFitnessValue() };
			
			Map<String, SteadyStateSimulationResult> simulations = new HashMap<String, SteadyStateSimulationResult>();
			
			GeneticConditions gc = decoder.decode(solution.getRepresentation());
			
			Boolean resimulateArchive = (Boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_RESIMULATE_WHEN_FINISH);
			
			if(resimulateArchive==null || resimulateArchive.equals(Boolean.TRUE)){
				for (String method : simConfiguration.keySet()) {
					Map<String, Object> methodConf = simConfiguration.get(method);
					String simMethod = (String) methodConf.get(SimulationProperties.METHOD_ID);
//					ISteadyStateModel model = (ISteadyStateModel) methodConf.get(SimulationProperties.MODEL);
					EnvironmentalConditions envConditions = (EnvironmentalConditions) methodConf.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
//					SolverType solver = (SolverType) methodConf.get(SimulationProperties.SOLVER);
					Boolean isMaximization = (Boolean) methodConf.get(SimulationProperties.IS_MAXIMIZATION);
					Boolean overUnder2StepApproach = (Boolean) methodConf.get(SimulationProperties.OVERUNDER_2STEP_APPROACH);
					FluxValueMap wtReference = (FluxValueMap) methodConf.get(SimulationProperties.WT_REFERENCE);
					FluxValueMap ouReference = (FluxValueMap) methodConf.get(SimulationProperties.OVERUNDER_REFERENCE_FLUXES);
					
					if (controlCenters.get(method) == null) {
						controlCenters.put(method, new SimulationSteadyStateControlCenter(methodConf));
						controlCenters.get(method).setGeneticConditions(gc);
					} else {
						controlCenters.get(method).setMaximization(isMaximization);
						controlCenters.get(method).setWTReference(wtReference);
						controlCenters.get(method).setOverUnder2StepApproach(overUnder2StepApproach);
						controlCenters.get(method).setUnderOverRef(ouReference);
						controlCenters.get(method).setEnvironmentalConditions(envConditions);
						controlCenters.get(method).setGeneticConditions(gc);
					}
					
					SteadyStateSimulationResult res;
					try {
						res = controlCenters.get(method).simulate();
					} catch (Exception e) {
						e.printStackTrace();
						res = null;
					}
					if(res!=null){
						simulations.put(simMethod, res);						
					}
				}				

			}
			
			IStrainOptimizationResult strainOptimizationResult = createSolutionResult(configuration, simulations, gc, fitnesses);
			strainOptimizationSolutionList.add(strainOptimizationResult);
		}
		
		
		String basename = (String) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_BASE_NAME);
		if (basename != null && finalSolutionSet.getNumberOfSolutions() > 0) {
			System.out.println("Saving bests...");
			((ArchiveManagerBestSolutions) archive).writeBestSolutionsToFile(
					basename + ".best",
					Delimiter.COMMA.toString(),
					true,
					decoder);
			System.out.println("Saved bests to " + basename + ".best");
		}
		
		return createSolutionSet(configuration, strainOptimizationSolutionList);
	}
	
	protected IStrainOptimizationResultSet createSolutionSet(T configuration, List<IStrainOptimizationResult> strainOptimizationSolutionList) {
		return optimizationStrategyConverter.createSolutionSet(configuration, strainOptimizationSolutionList);
	}
	
	protected IStrainOptimizationResult createSolutionResult(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc, Double[] fitnesses) throws Exception {
		return optimizationStrategyConverter.createSolution(configuration, simulations, gc, Arrays.asList(fitnesses));
	}
	
	protected AbstractMultiobjectiveEvaluationFunction computeStrainOptimizationEvaluationFunction(T configuration, ISteadyStateDecoder decoder) throws Exception {
		Map<String, Map<String, Object>> simulationConf = configuration.getSimulationConfiguration();
		Map<IObjectiveFunction, String> mapOF2Sim = configuration.getObjectiveFunctionsMap();
		return new StrainOptimizationEvaluationFunction(decoder, simulationConf, mapOF2Sim);
	}
	
	public abstract IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception;
	
	protected abstract ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IStrainOptimizationResultSet execute(T configuration) throws Exception {
		configuration.validate();
		IEvaluationFunctionListener<?> listener = (IEvaluationFunctionListener<?>) algorithmConfiguration.getProperty(JecoliOptimizationProperties.EVALUATION_LISTENER);
		IPlotter<?> plotter = (IPlotter<?>) algorithmConfiguration.getProperty(JecoliOptimizationProperties.ARCHIVE_PLOTTER);
		ISteadyStateDecoder decoder = optimizationStrategyConverter.createDecoder(configuration);
		AbstractMultiobjectiveEvaluationFunction evaluationFunction = computeStrainOptimizationEvaluationFunction(configuration, decoder);
		if (listener != null) {
			evaluationFunction.addEvaluationFunctionListener(listener);
			listener.setMaxValue(algorithmConfiguration.getTerminationCriteria().getNumericTerminationValue().intValue());
		}
		IAlgorithm<IRepresentation> algorithm = createAlgorithm(decoder, evaluationFunction);
		ArchiveManager archiveManager = (algorithmConfiguration.getArchiveManager() != null) ? algorithmConfiguration.getArchiveManager() : configureDefaultArchive(algorithm, evaluationFunction);
		if (plotter != null) {
			archiveManager.setPlotter(plotter);
		}
		IAlgorithmResult result = algorithm.run();
		
		return processAlgorithmResult(archiveManager, configuration, evaluationFunction, result, decoder);
	}
	
}
