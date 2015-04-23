package pt.uminho.ceb.biosystems.mew.mewcore.optimization.controlcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.IConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.crossover.HybridSetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetRandomMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetShrinkMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.EnvironmentalSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.TournamentSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IElementsRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.IntIntHybridSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.SetRepresentation;
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
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2Configuration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.AnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.IAnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealing;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealingConfiguration;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateMTOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.archivetrimming.SelectionValueTrimmer;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.GeneReactionKnockoutDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.GeneReactionUnderOverExp2Decoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.SteadyStateKnockoutDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.SteadyStateUnderOverExp2Decoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.evalutionfunction.MTStrainOptimizationEvaluationFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

/**
 * The <code>StrainOptimizationControlCenter</code> <br>
 * 
 * Defines an API class for interaction with the strain optimization methods <br>
 *  
 * @author pmaia
 *
 */
public class MTStrainOptimizationControlCenter {

	protected ISteadyStateModel model;
	
	boolean geneOptimization = false; // if false = reactions
	
	boolean overUnder = false; // if false = KOs
	
	boolean variableSize = false;
	
	int maxSolutionSize = 6; //default
	
	int maxThreads = 1;
	
	protected AlgorithmTypeEnum optimizationMethod = AlgorithmTypeEnum.EA;

	protected IAlgorithm<IElementsRepresentation<?>> optimizationAlgorithm;
	
	protected ISteadyStateDecoder decoder;
	
	protected MTStrainOptimizationEvaluationFunction evaluationFunction;
	
	protected IAggregationFunction aggregationFunction;

	protected ArchiveManager<?,IElementsRepresentation<?>> archive = null;

	protected IRandomNumberGenerator randomGenerator;
	
	protected SolverType solver;
	
	protected List<String> notAllowedIDs;
	
	protected EnvironmentalConditions environmentalConditions;
	
	protected List<String> simulationMethods;
	
	protected Map<IObjectiveFunction,String> mapOF2Sim;
	
	
// NEED TO HANDLE CRITICAL
	
// NEED TO ADDRESS CONFIGURATION OF OPT ALG	- future to be ...
	
// NEED TO HANDLE INIT POP - where is this necessary ??
	
	/**
	 * <p> StrainOptimizationControlCenter main constructor </p>
	 */
	public MTStrainOptimizationControlCenter (
			ISteadyStateModel model, 
			EnvironmentalConditions envCond,
			boolean geneOptimization,
			boolean overUnder, 
			boolean variableSize,
			int maxSize,
			AlgorithmTypeEnum optMethod,
			List<String> simMethods,
			Map<IObjectiveFunction,String> mapOF2Sim,
			IConfiguration<IElementsRepresentation<?>> algorithmConfiguration,
			SolverType solver,
			String simulationMethodOverUnderReference,
			Map<String,Double> overUnderReference,
			List<String> notAllowedIDs,
			int maxThreads
			) throws Exception{
		
		this.model = model;
		this.environmentalConditions = envCond;
		this.optimizationMethod = optMethod;
		this.randomGenerator = new DefaultRandomNumberGenerator();
		this.overUnder = overUnder;
		this.geneOptimization = geneOptimization;
		this.variableSize = variableSize;
		this.maxSolutionSize = maxSize;
		this.solver = solver;
		this.notAllowedIDs = notAllowedIDs;
		this.simulationMethods = simMethods;
		this.mapOF2Sim = mapOF2Sim;
		this.maxThreads = maxThreads;
		createDecoder(geneOptimization, overUnder,notAllowedIDs);
		
		createEvaluationFunction(envCond, decoder, solver, simMethods, mapOF2Sim, maxThreads);
		
		if(algorithmConfiguration==null)
			configureAlgorithm(optMethod, maxSize, variableSize);
		
		if(overUnder){
			if(overUnderReference!=null)
				setOverUnderReferenceDistribution(overUnderReference);
			else{
				if(simulationMethodOverUnderReference==null)
					simulationMethodOverUnderReference = SimulationProperties.PARSIMONIUS;
				Map<String,Double> refMap = computeOverUnderReferenceDistribution(simulationMethodOverUnderReference,envCond,model);
				setOverUnderReferenceDistribution(refMap);
			}
		}
	}
	
	/**
	 * <p> Creates a decoder, taking into account if it should use Gene-Protein-Reaction (GPR) information and/or over/under expression approach. </p>
	 * 
	 * @param geneOpt uses GPR information if true, doesn't use otherwise 
	 * @param overUnder uses the over/under expression approach if true, uses the knockout approach otherwise
	 * @throws Exception if adding not allowed IDs to decoder catches an Exception 
	 */
	public void createDecoder (boolean geneOpt, boolean overUnder, List<String> notAllowedIDs) throws Exception{
		
		if(geneOpt)
			decoder = (overUnder) ? new GeneReactionUnderOverExp2Decoder((ISteadyStateGeneReactionModel)model) 
								  :	new GeneReactionKnockoutDecoder((ISteadyStateGeneReactionModel)model);
		
		else
			decoder = (overUnder) ? new SteadyStateUnderOverExp2Decoder(model)
								  : new SteadyStateKnockoutDecoder(model);		
		
		
		decoder.addNotAllowedIds(notAllowedIDs);
	}
	
	/**
	 * 
	 * @param envConds
	 * @param decoder
	 * @param ofs
	 * @throws Exception
	 */
	public void createEvaluationFunction(EnvironmentalConditions envConds, ISteadyStateDecoder decoder, SolverType solver,List<String> simulationMethods, Map<IObjectiveFunction, String> mapOF2Sim, int maxThreads) throws Exception {
		this.evaluationFunction = new MTStrainOptimizationEvaluationFunction(model, decoder, envConds, solver, simulationMethods, mapOF2Sim, true, maxThreads);
	}
	

	/**
	 * 
	 * 
	 * @param geneOpt
	 * @param overUnder
	 * @param maxSetSize
	 * @return
	 */
	public ISolutionFactory<?> createSolutionFactory (boolean geneOpt, boolean overUnder, int maxSetSize)
	{
		ISolutionFactory<?> res = null;
		int minSetSize = 1;
		int maxSetValue = decoder.getNumberVariables();


		if (overUnder) {
			int n = 5;					
			res = new IntIntHybridSetRepresentationFactory(minSetSize, maxSetSize, maxSetValue, -(n+1), n, evaluationFunction.getNumberOfObjectives());
		}
		else res = new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives()); // check this ...	
		
		return res;
	}
		
	/**
	 * 
	 * @param algorithmType
	 * @param maxSetSize
	 * @param isVariableSizeGenome
	 * @throws Exception
	 */
	public void configureAlgorithm(AlgorithmTypeEnum algorithmType, int maxSetSize, boolean isVariableSizeGenome) throws Exception
	{
		this.optimizationMethod = algorithmType;
		int minSize = 1;

		int numberOfObjectives = evaluationFunction.getNumberOfObjectives();
		
		ISolutionFactory solutionFactory = createSolutionFactory(this.geneOptimization, this.overUnder, maxSetSize);
			
		if(optimizationMethod.equals(AlgorithmTypeEnum.EA)){
			configureEA(solutionFactory,isVariableSizeGenome);
		}
		else if(optimizationMethod.equals(AlgorithmTypeEnum.SA)){
			configureSA(solutionFactory,isVariableSizeGenome);
		}
		else if(optimizationMethod.equals(AlgorithmTypeEnum.SPEA2)){
			configureSPEA2(solutionFactory,isVariableSizeGenome);
		}
		else throw new Exception("Unsupported optimization algorithm");
	}
	

	public void configureEA(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException
	{
		EvolutionaryConfiguration configuration = new EvolutionaryConfiguration();	
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory); 
		
		int populationSize = 100;
		configuration.setPopulationSize(populationSize);

		//TODO: re-implement this trench of code, can be useful for warm starts/ local optimizations 
/*
		if(initPopFile != null){
			configuration.setInitialPopulation(createInitPopFromFile(populationSize));
			configuration.setPopulationInitialization(false);
		}
*/		
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<SetRepresentation>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		int numberIterations = 500;
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(numberIterations);
		configuration.setTerminationCriteria(terminationCriteria);
		
		RecombinationParameters recombinationParameters = new RecombinationParameters(50,49,1,true);
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperator(new TournamentSelection<SetRepresentation>(1,2));
		configuration.setSurvivorSelectionOperator(new TournamentSelection<SetRepresentation>(1,2));
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		reproductionOperatorContainer = createEAReproductionOperatorContainer(solutionFactory, isVariableSizeGenome);
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		
		optimizationAlgorithm = new EvolutionaryAlgorithm(configuration);	
	}
		
	
	protected ReproductionOperatorContainer createEAReproductionOperatorContainer(ISolutionFactory<SetRepresentation> solutionFactory,boolean isVariableSizeGenome) throws Exception{
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		
		if (this.overUnder)  {
			if(isVariableSizeGenome){
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());				
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());					
			}else{
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
			}
		}
		else {
			if(isVariableSizeGenome){
				reproductionOperatorContainer.addOperator(0.25, new SetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());	
				reproductionOperatorContainer.addOperator(0.125, new SetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.125, new SetShrinkMutation());
			}
			else {
				reproductionOperatorContainer.addOperator(0.5, new SetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());
			}
		}
		return reproductionOperatorContainer;	
	}
	
	
	public void configureSA(ISolutionFactory solutionFactory, boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException
	{
		SimulatedAnnealingConfiguration configuration = new SimulatedAnnealingConfiguration();
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory); 
		
		IAnnealingSchedule annealingSchedule = new AnnealingSchedule(0.007,0.000006,50,50000);
		configuration.setAnnealingSchedule(annealingSchedule);
		
//		if(initPopFile!=null){
//			configuration.setInitialPopulation(createInitPopFromFile(1));
//			throw new Exception("Stop");
//		}
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<SetRepresentation>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		int numberOfFunctionEvaluations = 50000;
		ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(numberOfFunctionEvaluations);
		configuration.setTerminationCriteria(terminationCriteria);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		ReproductionOperatorContainer reproductionOperatorContainer = createSAReproductionOperatorContainer(solutionFactory, isVariableSizeGenome);
		configuration.setMutationOperatorContainer(reproductionOperatorContainer);
		
		optimizationAlgorithm = new SimulatedAnnealing(configuration);
	}
	
	
	protected ReproductionOperatorContainer createSAReproductionOperatorContainer(ISolutionFactory<SetRepresentation> solutionFactory,boolean isVariableSizeGenome) throws Exception{
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();


		if (this.overUnder)  {
			if(isVariableSizeGenome)
			{			
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetGaussianPertubationMutation(2));	
				reproductionOperatorContainer.addOperator(0.2, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.2, new HybridSetShrinkMutation());
			}
			else
			{
				reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.334, new HybridSetGaussianPertubationMutation(2));	
			}
		}
		else {
			if(isVariableSizeGenome){
				reproductionOperatorContainer.addOperator(0.50,new SetRandomMutation(2));	
				reproductionOperatorContainer.addOperator(0.25,new SetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.25,new SetShrinkMutation());
			}
			else
			{
				reproductionOperatorContainer.addOperator(1,new SetRandomMutation(2));
			}
		}	

		return reproductionOperatorContainer;	
	}
	
	public void configureSPEA2(ISolutionFactory solutionFactory,boolean isVariableSizeGenome) throws Exception, InvalidConfigurationException{
		
		SPEA2Configuration configuration = new SPEA2Configuration();	
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory); 
		configuration.setNumberOfObjectives(evaluationFunction.getNumberOfObjectives());
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<SetRepresentation>>());
	
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		int populationSize = 100;
		int archiveSize = 100;
		
		configuration.setPopulationSize(populationSize);
		configuration.setMaximumArchiveSize(archiveSize);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(archiveSize);

		int numberIterations = 1000;
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(numberIterations);
		configuration.setTerminationCriteria(terminationCriteria);
		
		RecombinationParameters recombinationParameters = new RecombinationParameters(0,100,0,true);
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setEnvironmentalSelectionOperator(new EnvironmentalSelection());
		configuration.setSelectionOperator(new TournamentSelection(1,2));
		
		
		ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
		if (this.overUnder)  {
			if(isVariableSizeGenome){
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());				
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
				reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
				reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());					
			}else{
				reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
				reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
				reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
			}
		}else{
			reproductionOperatorContainer.addOperator(0.25, new SetUniformCrossover());
			reproductionOperatorContainer.addOperator(0.5, new SetRandomMutation());	
			reproductionOperatorContainer.addOperator(0.125, new SetGrowthMutation(2));
			reproductionOperatorContainer.addOperator(0.125, new SetShrinkMutation(1));
		}
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		optimizationAlgorithm = new SPEA2(configuration);	
	
	}
	
	// rever isto ...
	public void setNumberFunctionEvaluations(int nfe) throws Exception
	{		
		IConfiguration configuration = optimizationAlgorithm.getConfiguration();
		ITerminationCriteria terminationCriteria = new NumFunctionEvaluationsListenerHybridTerminationCriteria(nfe);
		configuration.setTerminationCriteria(terminationCriteria);
	}
	
	public void setTerminationCriteria(ITerminationCriteria terminationCriteria){
		IConfiguration configuration = optimizationAlgorithm.getConfiguration();
		configuration.setTerminationCriteria(terminationCriteria);
	}
	
	
	public void configureDefaultArchive(){

		archive = new ArchiveManager(
				optimizationAlgorithm,
				InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
				InsertionStrategy.ADD_SMART,
				ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT
				);

		archive.setMaximumArchiveSize(100);

		ITrimmingFunction trimmer = (evaluationFunction.getNumberOfObjectives()>1) 	? new ZitzlerTruncation(archive.getMaximumArchiveSize(), evaluationFunction)
		: new SelectionValueTrimmer(archive.getMaximumArchiveSize(), 0.000002);

		archive.addTrimmingFunction(trimmer);		
	}

	/**
	 * Runnable. Must be invoked after all the necessary parameters have been set. <br>
	 * Returns a solution set that contains the solutions kept by the archive during the execution of the algorithm. <br>
	 * 
	 * @return result the resulting <code>SteadyStateOptimizationResult</code>.
	 * 
	 * @throws Exception
	 */
	public SteadyStateMTOptimizationResult run () throws Exception{		

		//archive
		if(archive==null)
			configureDefaultArchive();
		
		// evaluation function multi -> single objective aggregation policy
		if(aggregationFunction==null && !optimizationMethod.isMultiObjective()){ 
			aggregationFunction = new SimpleMultiplicativeAggregation();
			evaluationFunction.setFitnessAggregation(aggregationFunction);
		}
		
		
		// execute algorithm
		optimizationAlgorithm.run();
		
		// build the results container
		SteadyStateMTOptimizationResult result = new SteadyStateMTOptimizationResult(model,evaluationFunction.getMapOF2Sim());
		
		ISolutionSet<IElementsRepresentation<?>> finalSolutionSet = archive.getArchive();
		
		// rebuild simulations from solutions
//		SimulationSteadyStateControlCenter[] simulationCCS = evaluationFunction.getControlCenters();
		
		int k=0;
		for(ISolution<IElementsRepresentation<?>> solution : finalSolutionSet.getListOfSolutions()){
			String id = "Solution_"+k;
			// decode
			
			Map<String, SteadyStateSimulationResult> simulations = new HashMap<String, SteadyStateSimulationResult>();
			
			GeneticConditions gc = decoder.decode(solution.getRepresentation());
			
			for(String simMethod : evaluationFunction.getSimMethods()){		
				SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null,gc,model,simMethod);
				cc.setSolver(solver);
				cc.setMaximization(evaluationFunction.isMaximization());
				SteadyStateSimulationResult res = cc.simulate();
				simulations.put(simMethod, res);
			}
						
			SteadyStateMultiSimulationResult mres = new SteadyStateMultiSimulationResult(id, gc, environmentalConditions, simulations);
			
			// retrieve fitnesses
			ArrayList<Double> fitnesses = new ArrayList<Double>();
			for(int i=0; i<solution.getNumberOfObjectives(); i++)
				fitnesses.add(i,solution.getFitnessValue(i));
			
			// add new optimization result
			result.addOptimizationResult(mres, fitnesses);
			k++;
		}

		return result;
	}
	
	/**
	 * Computes a reference flux distribution for over/under expression problems. <br>
	 * 
	 * @param simulationMethod the method to be used in the computation of the flux distribution
	 * @param envCond the set of environmental conditions to be taken into account
	 * @param model the base model
	 * 
	 * @return <code>Map<String,Double></code> a map containing a <code>String</code> for the flux id and a <code>Double</code> for the respective flux value 
	 * 
	 * @throws OverUnderReferenceComputationException if any problem occurs when calculating the flux distribution
	 */
	public Map<String,Double> computeOverUnderReferenceDistribution(String simulationMethod,EnvironmentalConditions envCond, ISteadyStateModel model) throws OverUnderReferenceComputationException{
		SimulationSteadyStateControlCenter simCC = new SimulationSteadyStateControlCenter(envCond, null, model, simulationMethod);
		simCC.setSolver(solver);
		simCC.setSimulationProperty(SimulationProperties.IS_MAXIMIZATION, true);
		
		Map<String,Double> result = null;
		try {
			result = simCC.simulate().getFluxValues();
		} catch (Exception e) {
			e.printStackTrace();
			throw new OverUnderReferenceComputationException("Problem computing over/under expression reference distribution while using method ["+simulationMethod+"]");
		}
		
		return result;
	}
	
	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}

	public ISteadyStateModel getModel() {
		return model;
	}
	
	
	public Object getSimulationProperty(String propertyKey)
	{
		return evaluationFunction.getSimulationProperty(propertyKey);
	}
	
	public void setSimulationProperty(String key, Object value)
	{
		evaluationFunction.setSimulationProperty(key, value);
	}
	
	public void setSimulationMethod (List<String> simulationMethod)
	{
		this.evaluationFunction.setSimMethods(simulationMethod);
	}
	
	public List<String> getSimulationMethod()
	{
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
	public ArchiveManager<?, IElementsRepresentation<?>> getArchive() {
		return archive;
	}

	/**
	 * @param archive the archive to set
	 */
	public void setArchive(ArchiveManager<?,IElementsRepresentation<?>> archive) {
		this.archive = archive;
	}

	/**
	 * @return the solver
	 */
	public SolverType getSolver() {
		return solver;
	}

	/**
	 * @param solver the solver to set
	 */
	public void setSolver(SolverType solver) {
		this.solver = solver;
	}
	
	
	public void setOverUnderReferenceDistribution(Map<String,Double> reference){
		evaluationFunction.setOverUnderReferenceDistribution(reference);
	}
	
	public IAlgorithm getOptimizationAlgorithm(){
		return optimizationAlgorithm;
	}
	
	public MTStrainOptimizationEvaluationFunction getEvaluationFunction(){
		return evaluationFunction;
	}

	/**
	 * @return the aggregationFunction
	 */
	public IAggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	/**
	 * @param aggregationFunction the aggregationFunction to set
	 */
	public void setAggregationFunction(IAggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	

	
}
