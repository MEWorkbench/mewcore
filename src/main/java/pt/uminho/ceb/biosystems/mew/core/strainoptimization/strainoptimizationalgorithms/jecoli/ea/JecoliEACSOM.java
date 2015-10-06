package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import java.util.ArrayList;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.controller.InitialStateController;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.set.SetNewIndividualMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.TournamentSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.SetRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;

/**
 * Created by ptiago on 03-03-2015.
 */
public abstract class JecoliEACSOM<E extends IJecoliOptimizationStrategyConverter> extends JecoliCSOM<JecoliEACSOMConfig,E> {

	private static final long	serialVersionUID	= 1L;



	public JecoliEACSOM(E converter){
        super(converter);
        algorithmConfiguration = new JecoliEACSOMConfig();
    }



    @SuppressWarnings({ "rawtypes", "unchecked" })
	public IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception{
        EvolutionaryConfiguration configuration = new EvolutionaryConfiguration();
        ISolutionFactory solutionFactory = optimizationStrategyConverter.createSolutionFactory(algorithmConfiguration,decoder,evaluationFunction);
        configuration.setEvaluationFunction(evaluationFunction);
        configuration.setSolutionFactory(solutionFactory);

        int populationSize = algorithmConfiguration.getPopulationSize();
        configuration.setPopulationSize(populationSize);
        IRandomNumberGenerator randomGenerator = new DefaultRandomNumberGenerator();
        configuration.setRandomNumberGenerator(randomGenerator);
        configuration.setProblemBaseDirectory("nullDirectory");
        configuration.setAlgorithmStateFile("nullFile");
        configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
        configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<SetRepresentation>>());
        configuration.setStatisticsConfiguration(new StatisticsConfiguration(true));

        ITerminationCriteria terminationCriteria = algorithmConfiguration.getTerminationCriteria();
        configuration.setTerminationCriteria(terminationCriteria);


        int elitism = algorithmConfiguration.getNumberOfElitistIndividuals();
        int numberOfSurvivors = algorithmConfiguration.getNumberOfSurvivors();
        int offSpringSize = algorithmConfiguration.getOffSpringSize();
        RecombinationParameters recombinationParameters = new RecombinationParameters(numberOfSurvivors,offSpringSize, elitism, true);
        configuration.setRecombinationParameters(recombinationParameters);

        configuration.setSelectionOperator(new TournamentSelection<SetRepresentation>(1, 2));
        configuration.setSurvivorSelectionOperator(new TournamentSelection<SetRepresentation>(1, 2,true));
        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);

        ReproductionOperatorContainer reproductionOperatorContainer = createAlgorithmReproductionOperatorContainer();
        configuration.setReproductionOperatorContainer(reproductionOperatorContainer);

        ReproductionOperatorContainer controllerContainer = new ReproductionOperatorContainer<>();
        controllerContainer.addOperator(1.0, new SetNewIndividualMutation<>());


        InitialStateController controller = new InitialStateController(controllerContainer, reproductionOperatorContainer);
        return new EvolutionaryAlgorithm(configuration,controller);
    }






}
