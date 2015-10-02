package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithmResult;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IElementsRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManagerBestSolutions;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
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
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

/**
 * Created by ptiago on 03-03-2015.
 */
public abstract class JecoliCSOM<T extends JecoliGenericConfiguration, E extends IJecoliOptimizationStrategyConverter> extends AbstractStrainOptimizationAlgorithm<T> {
	
	private static final long	serialVersionUID	= 1L;
	
	protected SimulationSteadyStateControlCenter	simulationSteadyStateControlCenter;
	protected E										optimizationStrategyConverter;
	
	public JecoliCSOM(E optimizationStrategyConverter) {
		this.optimizationStrategyConverter = optimizationStrategyConverter;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArchiveManager configureDefaultArchive(IAlgorithm optimizationAlgorithm, IEvaluationFunction evaluationFunction) {
		
		ArchiveManager archive = new ArchiveManagerBestSolutions(
				optimizationAlgorithm, 
				InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
				InsertionStrategy.ADD_SMART, 
				ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
				true);
		
		archive.setMaximumArchiveSize(100);
		
		ITrimmingFunction trimmer = (evaluationFunction.getNumberOfObjectives() > 1) ? new ZitzlerTruncation(archive.getMaximumArchiveSize(), evaluationFunction) : new SelectionValueTrimmer(archive.getMaximumArchiveSize(), 0.000002);		
		archive.addTrimmingFunction(trimmer);
		
		return archive;
	}
	
	public IStrainOptimizationResultSet processAlgorithmResult(ArchiveManager archive, T configuration, AbstractMultiobjectiveEvaluationFunction<IRepresentation> evaluationFunction, IAlgorithmResult result, ISteadyStateDecoder decoder)
			throws Exception {
		
		ISolutionSet<IElementsRepresentation<?>> finalSolutionSet = archive.getArchive();
		ISteadyStateModel model = configuration.getSteadyStateModel();
		
//		int k = 0;
		List<IStrainOptimizationResult> strainOptimizationSolutionList = new ArrayList<>();
		for (int j = 0; j < finalSolutionSet.getNumberOfSolutions(); j++) {
			ISolution solution = finalSolutionSet.getSolution(j);
			
//			String id = "Solution_" + k;
			
			Map<String, SteadyStateSimulationResult> simulations = new HashMap<String, SteadyStateSimulationResult>();
			
			GeneticConditions gc = decoder.decode(solution.getRepresentation());
			List<String> simulationMethodList = configuration.getSimulationMethodList();
			for (String simMethod : simulationMethodList) {
				EnvironmentalConditions environmentalConditions = configuration.getEnvironmentalConditions();
				
				if (simulationSteadyStateControlCenter == null)
					simulationSteadyStateControlCenter = new SimulationSteadyStateControlCenter(environmentalConditions, gc, model, simMethod);
				else {
					simulationSteadyStateControlCenter.setEnvironmentalConditions(environmentalConditions);
					simulationSteadyStateControlCenter.setGeneticConditions(gc);
					simulationSteadyStateControlCenter.setMethodType(simMethod);
				}
				SolverType solver = configuration.getSolver();
				simulationSteadyStateControlCenter.setSolver(solver);
				simulationSteadyStateControlCenter.setMaximization(evaluationFunction.isMaximization());
				SteadyStateSimulationResult res = simulationSteadyStateControlCenter.simulate();
				simulations.put(simMethod, res);
			}
			
			IStrainOptimizationResult strainOptimizationResult = createSolutionResult(configuration, simulations, gc);
			strainOptimizationSolutionList.add(strainOptimizationResult);
//			k++;
		}
		return createSolutionSet(configuration, strainOptimizationSolutionList);
	}
	
	protected IStrainOptimizationResultSet createSolutionSet(T configuration, List<IStrainOptimizationResult> strainOptimizationSolutionList) {
		return optimizationStrategyConverter.createSolutionSet(configuration, strainOptimizationSolutionList);
	}
	
	protected IStrainOptimizationResult createSolutionResult(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) throws Exception {
		return optimizationStrategyConverter.createSolution(configuration, simulations, gc);
	}
	
	protected AbstractMultiobjectiveEvaluationFunction computeStrainOptimizationEvaluationFunction(T configuration, ISteadyStateDecoder decoder) throws Exception {
		ISteadyStateModel model = configuration.getSteadyStateModel();
		
		EnvironmentalConditions environmentalConditions = configuration.getEnvironmentalConditions();
		SolverType solver = configuration.getSolver();
		List<String> simulationMethodList = configuration.getSimulationMethodList();
		FluxValueMap referenceFD = configuration.getReferenceFluxDistribution();
		Boolean isMaximization = configuration.getIsMaximization();
		Boolean ou2stepApproach = (configuration.getOu2StepApproach() != null) ? configuration.getOu2StepApproach() : false;
		Map<IObjectiveFunction, String> mapOF2Sim = configuration.getObjectiveFunctionsMap();
		return new StrainOptimizationEvaluationFunction(model, decoder, environmentalConditions, solver, simulationMethodList, mapOF2Sim, referenceFD, isMaximization, 1, ou2stepApproach);
	}
	
	public abstract IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception;
	
	protected abstract ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception;
	
	public IStrainOptimizationResultSet execute(T configuration) throws Exception {
		configuration.validate();
		ISteadyStateDecoder decoder = optimizationStrategyConverter.createDecoder(configuration);
		AbstractMultiobjectiveEvaluationFunction evaluationFunction = computeStrainOptimizationEvaluationFunction(configuration, decoder);
		IAlgorithm<IRepresentation> algorithm = createAlgorithm(decoder, evaluationFunction);
		ArchiveManager archiveManager = configureDefaultArchive(algorithm, evaluationFunction);
		IAlgorithmResult result = algorithm.run();
		
		return processAlgorithmResult(archiveManager, configuration, evaluationFunction, result, decoder);
	}
	
}
