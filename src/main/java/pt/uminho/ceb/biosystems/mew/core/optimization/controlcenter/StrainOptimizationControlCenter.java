package pt.uminho.ceb.biosystems.mew.core.optimization.controlcenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.InitialStateController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.IConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetMutationWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.dualset.DualSetUniformCrossoverWrapper;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.crossover.HybridSetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetNewIndividualMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeGrowMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRelativeShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.EnvironmentalSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.TournamentSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IElementsRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.dualset.DualSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.IntIntHybridSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolution;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionSet;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.IterationTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumberOfFunctionEvaluationsTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.aggregation.IAggregationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.aggregation.SimpleMultiplicativeAggregation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManagerBestSolutions;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2Configuration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.randomsearch.RandomSearch;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.randomsearch.RandomSearchConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.AnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.IAnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealing;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealingConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateMTOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.archivetrimming.SelectionValueTrimmer;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.configuration.OptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.GeneReactionKnockoutDecoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.GeneReactionUnderOverExp2Decoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.ReactionSwapDualSetRepresentationDecoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.SteadyStateKnockoutDecoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.decoder.SteadyStateUnderOverExp2Decoder;
import pt.uminho.ceb.biosystems.mew.core.optimization.evalutionfunction.StrainOptimizationEvaluationFunction;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
// import jecoli.algorithm.components.representation.set.SetRepresentation;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * The <code>StrainOptimizationControlCenter</code> <br>
 * 
 * Defines an API class for interaction with the strain optimization methods <br>
 * 
 * @author pmaia
 */
public class StrainOptimizationControlCenter {
	
	protected ISteadyStateModel							model					= null;
	
	protected boolean									geneOptimization		= false;
	
	protected boolean									overUnder				= false;
	
	protected boolean									reactionSwap			= false;
	
	protected boolean									variableSize			= false;
	
	protected int										maxSolutionSize			= 6;
	
	protected int										maxThreads				= 1;
	
	protected AlgorithmTypeEnum							optimizationMethod		= AlgorithmTypeEnum.EA;
	
	protected IAlgorithm<IElementsRepresentation<?>>	optimizationAlgorithm	= null;
	
	protected ISteadyStateDecoder						decoder					= null;
	
	protected StrainOptimizationEvaluationFunction		evaluationFunction		= null;
	
	protected IAggregationFunction						aggregationFunction		= null;
	
	protected ArchiveManager							archive					= null;
	
	protected IRandomNumberGenerator					randomGenerator			= null;
	
	protected SolverType								solver					= null;
	
	protected List<String>								notAllowedIDs			= null;
	
	protected EnvironmentalConditions					environmentalConditions	= null;
	
	protected List<String>								simulationMethods		= null;
	
	protected Map<IObjectiveFunction, String>			mapOF2Sim				= null;
	
	protected FluxValueMap								wtReference				= null;
	
	protected Pair<Integer, Integer>					ouRange					= null;
	
	protected boolean									ou2StepApproach			= false;
	
	protected Map<String, List<String>>					swapsMap				= null;
	
	protected Vector<IntegerSetRepresentationFactory>	swapsFactoryList		= null;
	
	/**
	 * The default <code>StrainOptimizationControlCenterPersistent</code>
	 * constructor.
	 * 
	 * @param model
	 *            the <code>ISteadyStateModel</code> model.
	 * @param envCond
	 *            the <code>EnvironmentalConditions</code>.
	 * @param geneOptimization
	 *            if true uses gene optimization approach, otherwise uses
	 *            reaction optimization approach
	 * @param overUnder
	 *            if true uses over/under expression approach, otherwise uses
	 *            simple knockouts approach
	 * @param reactionSwap
	 *            if true reaction swap approach (similar to optswap) is used
	 * @param variableSize
	 *            if true the solutions can dinamically adjust their size up to
	 *            the maxSize.
	 * @param maxSize
	 *            the maximum size of a solution
	 * @param optMethod
	 *            the optimization algorithm to use
	 * @param simMethods
	 *            the simulation methods agains which solution must be evaluated
	 * @param mapOF2Sim
	 *            a map of objective functions to simulation methods, mapping
	 *            which objective function is used to evaluate the result of
	 *            each simulation method
	 * @param algorithmConfiguration
	 *            the optimization algorithm configuration
	 * @param solver
	 *            the solver to use
	 * @param simulationMethodOverUnderReference
	 *            the method used to generate the reference for the over/under
	 *            approach.
	 * @param overUnderReference
	 *            an optional reference to use when over/under expression
	 *            approach is selected.
	 * @param notAllowedIDs
	 *            a list of gene or reaction IDs that are excluded from targets
	 *            for the optimization.
	 * @param maxThreads
	 *            the maximum number of threads to be launched.
	 * @param ouRange
	 *            the over/under range of fold changes.
	 * @param ou2stepApproach
	 *            use 2-step approach for over/under expression problems
	 * @param swapsMap
	 *            map of possible swaps for selected reactions
	 * @throws Exception
	 */
	public StrainOptimizationControlCenter(
			ISteadyStateModel model,
			EnvironmentalConditions envCond,
			boolean geneOptimization,
			boolean overUnder,
			boolean reactionSwap,
			boolean variableSize,
			int maxSize,
			AlgorithmTypeEnum optMethod,
			List<String> simMethods,
			Map<IObjectiveFunction, String> mapOF2Sim,
			FluxValueMap wtReference,
			IConfiguration<IElementsRepresentation<?>> algorithmConfiguration,
			SolverType solver,
			String simulationMethodOverUnderReference,
			Map<String, Double> overUnderReference,
			List<String> notAllowedIDs,
			int maxThreads,
			Pair<Integer, Integer> ouRange,
			boolean ou2stepApproach,
			Map<String, List<String>> swapsMap,
			int maxSwaps) throws Exception {
		
		this.model = model;
		this.environmentalConditions = envCond;
		this.optimizationMethod = optMethod;
		this.randomGenerator = new DefaultRandomNumberGenerator();
		this.overUnder = overUnder;
		this.geneOptimization = geneOptimization;
		this.reactionSwap = reactionSwap;
		this.variableSize = variableSize;
		this.maxSolutionSize = maxSize;
		this.solver = solver;
		this.notAllowedIDs = notAllowedIDs;
		this.simulationMethods = simMethods;
		this.mapOF2Sim = mapOF2Sim;
		this.wtReference = wtReference;
		this.maxThreads = maxThreads;
		this.ouRange = ouRange;
		this.ou2StepApproach = ou2stepApproach;
		this.swapsMap = swapsMap;
		
		createDecoder(geneOptimization, overUnder, reactionSwap, notAllowedIDs);
		createEvaluationFunction(envCond, decoder, solver, simMethods, mapOF2Sim, wtReference, maxThreads);
		
		if (algorithmConfiguration == null) configureAlgorithm(optMethod, maxSize, maxSwaps, variableSize);
		
		if (overUnder) {
			if (overUnderReference != null)
				setOverUnderReferenceDistribution(overUnderReference);
			else {
				if (!ou2stepApproach) {
					if (simulationMethodOverUnderReference == null) simulationMethodOverUnderReference = SimulationProperties.PFBA;
					Map<String, Double> refMap = computeOverUnderReferenceDistribution(simulationMethodOverUnderReference, envCond, model);
					setOverUnderReferenceDistribution(refMap);
				}
			}
		}
	}
	
	/**
	 * A constructor that uses an <code>OptimizationConfiguration</code>
	 * configuration as input.
	 * 
	 * @param configuration
	 *            the <code>OptimizationConfiguration</code> instance to use.
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public StrainOptimizationControlCenter(OptimizationConfiguration configuration) throws IOException, Exception {
		this(configuration.getModel(), configuration.getEnvironmentalConditions(), configuration.getOptimizationStrategy().isGeneBasedOptimization(), configuration.getOptimizationStrategy().isOverUnderExpressionOptimization(), configuration
				.getOptimizationStrategy().isReactionSwap(), configuration.isVariableSize(), configuration.getMaxSize(), configuration.getAlgorithm(), configuration.getSimulationMethod(), configuration.getObjectiveFunctions(), configuration
				.getWTReference(), null, configuration.getSimulationSolver(), SimulationProperties.PFBA, null, configuration.getOptimizationCriticalIDs(), configuration.getMaxThreads(), configuration.getOURange(), configuration
				.isOverUnder2stepApproach(), configuration.getSwapsMap(), configuration.getMaxSwaps());
	}
	
	/**
	 * <p>
	 * Creates a decoder, taking into account if it should use
	 * Gene-Protein-Reaction (GPR) information and/or over/under expression
	 * approach.
	 * </p>
	 * 
	 * @param geneOpt
	 *            uses GPR information if true, doesn't use otherwise
	 * @param overUnder
	 *            uses the over/under expression approach if true, uses the
	 *            knockout approach otherwise
	 * @param reactionSwap
	 *            uses the reaction swap approach if true, one of the others if
	 *            false
	 * @throws Exception
	 *             if adding not allowed IDs to decoder catches an Exception
	 */
	public void createDecoder(boolean geneOpt, boolean overUnder, boolean reactionSwap, List<String> notAllowedIDs) throws Exception {
		
		if (reactionSwap) {
			decoder = new ReactionSwapDualSetRepresentationDecoder(model, swapsMap);
			decoder.addNotAllowedIds(notAllowedIDs);
		} else if (geneOpt) {
			if (overUnder) {
				decoder = new GeneReactionUnderOverExp2Decoder((ISteadyStateGeneReactionModel) model);
				((GeneReactionUnderOverExp2Decoder) decoder).createNotAllowedGenesFromIds(notAllowedIDs);
			} else {
				decoder = new GeneReactionKnockoutDecoder((ISteadyStateGeneReactionModel) model);
				((GeneReactionKnockoutDecoder) decoder).createNotAllowedGenesFromIds(notAllowedIDs);
			}
		} else {
			decoder = (overUnder) ? new SteadyStateUnderOverExp2Decoder(model) : new SteadyStateKnockoutDecoder(model);
			decoder.addNotAllowedIds(notAllowedIDs);
		}
		
	}
	
	/**
	 * 
	 * @param envConds
	 * @param decoder
	 * @param ofs
	 * @throws Exception
	 */
	public void createEvaluationFunction(EnvironmentalConditions envConds, ISteadyStateDecoder decoder, SolverType solver, List<String> simulationMethods, Map<IObjectiveFunction, String> mapOF2Sim, FluxValueMap wtReference, int maxThreads)
			throws Exception {
		this.evaluationFunction = new StrainOptimizationEvaluationFunction(model, decoder, envConds, solver, simulationMethods, mapOF2Sim, wtReference, true, maxThreads, this.ou2StepApproach);
	}
	
	/**
	 * 
	 * 
	 * @param geneOpt
	 * @param overUnder
	 * @param maxSetSize
	 * @return
	 */
	public ISolutionFactory<?> createSolutionFactory(boolean geneOpt, boolean overUnder, boolean reactionSwap, int maxSetSize, int maxAllowedSwaps) {
		ISolutionFactory<?> res = null;
		int minSetSize = 1;
		int maxSetValue = decoder.getNumberVariables();
		
		if (reactionSwap) {
			int maxPossibleSwaps = swapsMap.size();
			IntegerSetRepresentationFactory koSolutionFactory = new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives());
			IntegerSetRepresentationFactory swapSolutionFactory = new IntegerSetRepresentationFactory(maxPossibleSwaps, maxAllowedSwaps, evaluationFunction.getNumberOfObjectives());
			Vector<IntegerSetRepresentationFactory> setSolutionFactoryList = new Vector<>();
			setSolutionFactoryList.add(koSolutionFactory);
			setSolutionFactoryList.add(swapSolutionFactory);
			swapsFactoryList = setSolutionFactoryList;
			
			res = new DualSetRepresentationFactory(maxSetValue, maxPossibleSwaps, maxSetSize, maxAllowedSwaps,evaluationFunction.getNumberOfObjectives());
		} else if (overUnder) {
			int n = 5;
			int nmin = -(n + 1);
			int nmax = n;
			if (ouRange != null) {
				nmin = ouRange.getA();
				nmax = ouRange.getB();
				if (!geneOpt) ((SteadyStateUnderOverExp2Decoder) decoder).setSpecialIndex(nmin);
			}
			res = new IntIntHybridSetRepresentationFactory(minSetSize, maxSetSize, maxSetValue, nmin, nmax, evaluationFunction.getNumberOfObjectives());
		} else
			res = new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives());
		
		return res;
	}
	
	/**
	 * 
	 * @param algorithmType
	 * @param maxSetSize
	 * @param isVariableSizeGenome
	 * @throws Exception
	 */
	public void configureAlgorithm(AlgorithmTypeEnum algorithmType, int maxSetSize, int maxSwaps, boolean isVariableSizeGenome) throws Exception {
		
		this.optimizationMethod = algorithmType;
		
		ISolutionFactory<?> solutionFactory = createSolutionFactory(this.geneOptimization, this.overUnder, this.reactionSwap, maxSetSize, maxSwaps);
		
		switch (optimizationMethod) {
			case EA:
				configureEA(solutionFactory, isVariableSizeGenome);
				break;
			case SA:
				configureSA(solutionFactory, isVariableSizeGenome);
				break;
			case SPEA2:
				configureSPEA2(solutionFactory, isVariableSizeGenome);
				break;
			case RANDOM:
				configureRS(solutionFactory, isVariableSizeGenome);
				break;
			default:
				throw new Exception("Unsupported optimization algorithm");
		}
		
	}
	
	public void configureEA(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException {
		EvolutionaryConfiguration configuration = new EvolutionaryConfiguration();
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory);
		
		int populationSize = 500;
		configuration.setPopulationSize(populationSize);
		
		// TODO: re-implement this trench of code, can be useful for warm
		// starts/ local optimizations
		/*
		 * if(initPopFile != null){
		 * configuration.setInitialPopulation(createInitPopFromFile
		 * (populationSize)); configuration.setPopulationInitialization(false);
		 * }
		 */
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<IElementsRepresentation>>());
		StatisticsConfiguration statconf = new StatisticsConfiguration();
		statconf.setVerbose(true);
		configuration.setStatisticsConfiguration(statconf);
		
		int numberIterations = 500;
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(numberIterations);
		configuration.setTerminationCriteria(terminationCriteria);
		
		RecombinationParameters recombinationParameters = new RecombinationParameters(50, 49, 1, true);
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperator(new TournamentSelection<IElementsRepresentation>(1, 2));
		configuration.setSurvivorSelectionOperator(new TournamentSelection<IElementsRepresentation>(1, 2, true));
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		reproductionOperatorContainer = createEAReproductionOperatorContainer(solutionFactory, isVariableSizeGenome);
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		
		ReproductionOperatorContainer controllerContainer = new ReproductionOperatorContainer<>();
		//				if(overUnder)
		//					controllerContainer.addOperator(1.0, new HybridSetNewIndividualMutation<>());
		//				else
		controllerContainer.addOperator(1.0, new SetNewIndividualMutation<>());
		
		InitialStateController controller = new InitialStateController(controllerContainer, reproductionOperatorContainer);
		optimizationAlgorithm = new EvolutionaryAlgorithm(configuration, controller);
		//		configuration.setSteadyStateReplacement(false);
		//		optimizationAlgorithm = new EvolutionaryAlgorithm(configuration);
	}
	
	protected ReproductionOperatorContainer createEAReproductionOperatorContainer(ISolutionFactory<IElementsRepresentation> solutionFactory, boolean isVariableSizeGenome) throws Exception {
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		
		if (this.reactionSwap) {
			/** FIXME: HARDCODED - THIS NEEDS TO BE SET AS A PARAMETER */
			List<Integer> genomeApplicationList = new ArrayList<>();
			genomeApplicationList.add(0);
			genomeApplicationList.add(1);
			
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.25, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.50, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetRelativeGrowMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetRelativeShrinkMutation(), genomeApplicationList, swapsFactoryList));
			} else {
				reproductionOperatorContainer.addOperator(0.5, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));				
			}
		} else if (this.overUnder) {
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());
			} else {
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
			}
		} else {
			if (isVariableSizeGenome) {
				/*
				 * reproductionOperatorContainer.addOperator(0.25, new
				 * SetUniformCrossover());
				 * reproductionOperatorContainer.addOperator(0.5, new
				 * SetRandomMutation());
				 * reproductionOperatorContainer.addOperator(0.125, new
				 * SetGrowthMutation());
				 * reproductionOperatorContainer.addOperator(0.125, new
				 * SetShrinkMutation());
				 */
				
				reproductionOperatorContainer.addOperator(0.25, new SetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.5, new SetRelativeRandomMutation<>());
				reproductionOperatorContainer.addOperator(0.125, new SetRelativeGrowMutation<>());
				reproductionOperatorContainer.addOperator(0.125, new SetRelativeShrinkMutation<>());
			} else {
				reproductionOperatorContainer.addOperator(0.5, new SetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());
			}
		}
		return reproductionOperatorContainer;
	}
	
	public void configureSA(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException {
		SimulatedAnnealingConfiguration configuration = new SimulatedAnnealingConfiguration();
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory);
		
		IAnnealingSchedule annealingSchedule = new AnnealingSchedule(0.007, 0.000006, 50, 50000);
		configuration.setAnnealingSchedule(annealingSchedule);
		
		// if(initPopFile!=null){
		// configuration.setInitialPopulation(createInitPopFromFile(1));
		// throw new Exception("Stop");
		// }
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<IElementsRepresentation>>());
		StatisticsConfiguration statconf = new StatisticsConfiguration();
		statconf.setVerbose(true);
		configuration.setStatisticsConfiguration(statconf);
		
		int numberOfFunctionEvaluations = 50000;
		ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(numberOfFunctionEvaluations);
		configuration.setTerminationCriteria(terminationCriteria);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		ReproductionOperatorContainer reproductionOperatorContainer = createSAReproductionOperatorContainer(solutionFactory, isVariableSizeGenome);
		configuration.setMutationOperatorContainer(reproductionOperatorContainer);
		
		optimizationAlgorithm = new SimulatedAnnealing(configuration);
	}
	
	protected ReproductionOperatorContainer createSAReproductionOperatorContainer(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception {
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		
		if (this.reactionSwap) {
			/** FIXME: HARDCODED - THIS NEEDS TO BE SET AS A PARAMETER */
			List<Integer> genomeApplicationList = new ArrayList<>();
			genomeApplicationList.add(0);
			genomeApplicationList.add(1);
			
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.50, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.25, new DualSetMutationWrapper(new SetRelativeGrowMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.25, new DualSetMutationWrapper(new SetRelativeShrinkMutation(), genomeApplicationList, swapsFactoryList));
			} else {
				reproductionOperatorContainer.addOperator(1.0, new DualSetMutationWrapper(new SetRandomMutation(), genomeApplicationList, swapsFactoryList));				
			}
		} else if (this.overUnder) {
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetGaussianPertubationMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.2, new HybridSetShrinkMutation());
			} else {
				reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.334, new HybridSetGaussianPertubationMutation(2));
			}
		} else {
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.50, new SetRandomMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new SetGrowthMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new SetShrinkMutation());
			} else {
				reproductionOperatorContainer.addOperator(1, new SetRandomMutation(2));
			}
		}
		
		return reproductionOperatorContainer;
	}
	
	public void configureSPEA2(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException {
		
		SPEA2Configuration configuration = new SPEA2Configuration();
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(evaluationFunction.getNumberOfObjectives());
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter>());
		
		StatisticsConfiguration statconf = new StatisticsConfiguration();
		statconf.setVerbose(true);
		configuration.setStatisticsConfiguration(statconf);
		
		int populationSize = 100;
		int archiveSize = 100;
		
		configuration.setPopulationSize(populationSize);
		configuration.setMaximumArchiveSize(archiveSize);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(archiveSize);
		
		int numberIterations = 1000;
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(numberIterations);
		configuration.setTerminationCriteria(terminationCriteria);
		
		RecombinationParameters recombinationParameters = new RecombinationParameters(0, 100, 0, true);
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setEnvironmentalSelectionOperator(new EnvironmentalSelection());
		configuration.setSelectionOperator(new TournamentSelection(1, 2));
		
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		if (this.reactionSwap) {
			/** FIXME: HARDCODED - THIS NEEDS TO BE SET AS A PARAMETER */
			List<Integer> genomeApplicationList = new ArrayList<>();
			genomeApplicationList.add(0);
			genomeApplicationList.add(1);
			
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.25, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRelativeRandomMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetGrowthMutation(), genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.125, new DualSetMutationWrapper(new SetShrinkMutation(), genomeApplicationList, swapsFactoryList));
			} else {
				reproductionOperatorContainer.addOperator(0.5, new DualSetUniformCrossoverWrapper(genomeApplicationList, swapsFactoryList));
				reproductionOperatorContainer.addOperator(0.5, new DualSetMutationWrapper(new SetRandomMutation(), genomeApplicationList, swapsFactoryList));				
			}
		} else if (this.overUnder) {
			if (isVariableSizeGenome) {
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());
			} else {
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
			}
		} else {
			
			reproductionOperatorContainer.addOperator(0.25, new SetUniformCrossover());
			reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());
			reproductionOperatorContainer.addOperator(0.125, new SetGrowthMutation());
			reproductionOperatorContainer.addOperator(0.125, new SetShrinkMutation());
			
		}
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		optimizationAlgorithm = new SPEA2(configuration);
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void configureRS(ISolutionFactory<?> solutionFactory, boolean isVariableSizeGenome) throws Exception {
		RandomSearchConfiguration configuration = new RandomSearchConfiguration();
		//		configuration.setNumberOfObjectives(evaluationFunction.getNumberOfObjectives());
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<IElementsRepresentation>>());
		StatisticsConfiguration statconf = new StatisticsConfiguration();
		statconf.setVerbose(true);
		configuration.setStatisticsConfiguration(statconf);
		int numberOfFunctionEvaluations = 50000;
		ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(numberOfFunctionEvaluations);
		configuration.setTerminationCriteria(terminationCriteria);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		optimizationAlgorithm = new RandomSearch(configuration);
	}
	
	// rever isto ...
	public void setNumberFunctionEvaluations(int nfe) throws Exception {
		IConfiguration configuration = optimizationAlgorithm.getConfiguration();
		ITerminationCriteria terminationCriteria = new NumFunctionEvaluationsListenerHybridTerminationCriteria(nfe);
		configuration.setTerminationCriteria(terminationCriteria);
	}
	
	public void setTerminationCriteria(ITerminationCriteria terminationCriteria) {
		IConfiguration configuration = optimizationAlgorithm.getConfiguration();
		configuration.setTerminationCriteria(terminationCriteria);
	}
	
	public void configureDefaultArchive() {
		
		archive = new ArchiveManagerBestSolutions(optimizationAlgorithm, InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT, InsertionStrategy.ADD_SMART, ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT, true);
		
		archive.setMaximumArchiveSize(100);
		
		ITrimmingFunction trimmer = (evaluationFunction.getNumberOfObjectives() > 1) ? new ZitzlerTruncation(archive.getMaximumArchiveSize(), evaluationFunction) : new SelectionValueTrimmer(archive.getMaximumArchiveSize(), 0.000002);
		
		archive.addTrimmingFunction(trimmer);
	}
	
	/**
	 * Runnable. Must be invoked after all the necessary parameters have been
	 * set. <br>
	 * Returns a solution set that contains the solutions kept by the archive
	 * during the execution of the algorithm. <br>
	 * 
	 * @return result the resulting <code>SteadyStateOptimizationResult</code>.
	 * 
	 * @throws Exception
	 */
	public SteadyStateMTOptimizationResult run() throws Exception {
		
		// archive
		if (archive == null) configureDefaultArchive();
		
		// evaluation function multi -> single objective aggregation policy
		if (aggregationFunction == null && !optimizationMethod.isMultiObjective()) {
			aggregationFunction = new SimpleMultiplicativeAggregation();
			evaluationFunction.setFitnessAggregation(aggregationFunction);
		}
		
		// execute algorithm
		optimizationAlgorithm.run();
		
		// build the results container
		SteadyStateMTOptimizationResult result = new SteadyStateMTOptimizationResult(model, evaluationFunction.getMapOF2Sim());
		
		ISolutionSet<IElementsRepresentation<?>> finalSolutionSet = archive.getArchive();
		
		// rebuild simulations from solutions
		SimulationSteadyStateControlCenter[] controlCenters = new SimulationSteadyStateControlCenter[evaluationFunction.getSimMethods().size()];
		for (int i = 0; i < evaluationFunction.getSimMethods().size(); i++) {
			String simMethod = evaluationFunction.getSimMethods().get(i);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions, null, model, simMethod);
			cc.setSolver(solver);
			cc.setMaximization(evaluationFunction.isMaximization());
			cc.setOverUnder2StepApproach(this.ou2StepApproach);
			controlCenters[i] = cc;
		}
		
		int k = 0;
		for (ISolution solution : finalSolutionSet.getListOfSolutions()) {
			String id = "Solution_" + k;
			// decode
			
			Map<String, SteadyStateSimulationResult> simulations = new HashMap<String, SteadyStateSimulationResult>();
			
			GeneticConditions gc = decoder.decode(solution.getRepresentation());
			
			for (int i = 0; i < evaluationFunction.getSimMethods().size(); i++) {
				String simMethod = evaluationFunction.getSimMethods().get(i);
				controlCenters[i].setGeneticConditions(gc);
				SteadyStateSimulationResult res = controlCenters[i].simulate();
				simulations.put(simMethod, res);
			}
			
			SteadyStateMultiSimulationResult mres = new SteadyStateMultiSimulationResult(id, gc, environmentalConditions, simulations);
			
			// retrieve fitnesses
			ArrayList<Double> fitnesses = new ArrayList<Double>();
			for (int i = 0; i < solution.getNumberOfObjectives(); i++)
				fitnesses.add(i, solution.getFitnessValue(i));
			
			// add new optimization result
			result.addOptimizationResult(mres, fitnesses);
			k++;
		}
		
		return result;
	}
	
	/**
	 * Computes a reference flux distribution for over/under expression
	 * problems. <br>
	 * 
	 * @param simulationMethod
	 *            the method to be used in the computation of the flux
	 *            distribution
	 * @param envCond
	 *            the set of environmental conditions to be taken into account
	 * @param model
	 *            the base model
	 * 
	 * @return <code>Map<String,Double></code> a map containing a
	 *         <code>String</code> for the flux id and a <code>Double</code> for
	 *         the respective flux value
	 * 
	 * @throws OverUnderReferenceComputationException
	 *             if any problem occurs when calculating the flux distribution
	 */
	public Map<String, Double> computeOverUnderReferenceDistribution(String simulationMethod, EnvironmentalConditions envCond, ISteadyStateModel model) throws OverUnderReferenceComputationException {
		SimulationSteadyStateControlCenter simCC = new SimulationSteadyStateControlCenter(envCond, null, model, simulationMethod);
		simCC.setSolver(solver);
		simCC.setSimulationProperty(SimulationProperties.IS_MAXIMIZATION, true);
		
		Map<String, Double> result = null;
		try {
			result = simCC.simulate().getFluxValues();
		} catch (Exception e) {
			e.printStackTrace();
			throw new OverUnderReferenceComputationException("Problem computing over/under expression reference distribution while using method [" + simulationMethod + "]");
		}
		
		return result;
	}
	
	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}
	
	public ISteadyStateModel getModel() {
		return model;
	}
	
	public Object getSimulationProperty(String propertyKey) {
		return evaluationFunction.getSimulationProperty(propertyKey);
	}
	
	public void setSimulationProperty(String key, Object value) {
		evaluationFunction.setSimulationProperty(key, value);
	}
	
	public void setSimulationMethod(List<String> simulationMethod) {
		this.evaluationFunction.setSimMethods(simulationMethod);
	}
	
	public List<String> getSimulationMethod() {
		return this.evaluationFunction.getSimMethods();
	}
	
	public AlgorithmTypeEnum getOptimizationMethod() {
		return optimizationMethod;
	}
	
	public void setOptimizationMethod(AlgorithmTypeEnum optimizationMethod) {
		this.optimizationMethod = optimizationMethod;
	}
	
	/**
	 * @return the archive
	 */
	public ArchiveManager getArchive() {
		return archive;
	}
	
	/**
	 * @param archive
	 *            the archive to set
	 */
	public void setArchive(ArchiveManager archive) {
		this.archive = archive;
	}
	
	/**
	 * @return the solver
	 */
	public SolverType getSolver() {
		return solver;
	}
	
	/**
	 * @param solver
	 *            the solver to set
	 */
	public void setSolver(SolverType solver) {
		this.solver = solver;
	}
	
	public void setOverUnderReferenceDistribution(Map<String, Double> reference) {
		evaluationFunction.setOverUnderReferenceDistribution(reference);
	}
	
	public IAlgorithm getOptimizationAlgorithm() {
		return optimizationAlgorithm;
	}
	
	public StrainOptimizationEvaluationFunction getEvaluationFunction() {
		return evaluationFunction;
	}
	
	/**
	 * @return the aggregationFunction
	 */
	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}
	
	/**
	 * @param aggregationFunction
	 *            the aggregationFunction to set
	 */
	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	/**
	 * 
	 * @return
	 */
	public ISteadyStateDecoder getDecoder() {
		return this.decoder;
	}
	
}
