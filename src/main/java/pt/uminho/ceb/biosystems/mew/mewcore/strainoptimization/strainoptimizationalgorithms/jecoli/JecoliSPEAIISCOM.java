package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.EnvironmentalSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.selection.TournamentSelection;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumberOfFunctionEvaluationsTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.spea2.SPEA2Configuration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.IJecoliOptimizationStrategyConverter;

/**
 * Created by ptiago on 09-03-2015.
 */
public abstract class JecoliSPEAIISCOM<E extends IJecoliOptimizationStrategyConverter>  extends JecoliSCOM<JecoliSPEAIISCOMConfig,E>{


    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliSPEAIISCOM(E optimizationStrategyConverter) {
        super(optimizationStrategyConverter);
        algorithmConfiguration = new JecoliSPEAIISCOMConfig();
    }

    @Override
    public IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception  {
        SPEA2Configuration configuration = new SPEA2Configuration();
        configuration.setEvaluationFunction(evaluationFunction);
        ISolutionFactory solutionFactory = optimizationStrategyConverter.createSolutionFactory(algorithmConfiguration,decoder,evaluationFunction);
        configuration.setSolutionFactory(solutionFactory);
        configuration.setNumberOfObjectives(evaluationFunction.getNumberOfObjectives());
        IRandomNumberGenerator randomGenerator = new DefaultRandomNumberGenerator();
        configuration.setRandomNumberGenerator(randomGenerator);

        configuration.setProblemBaseDirectory("nullDirectory");
        configuration.setAlgorithmStateFile("nullFile");
        configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
        configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter>());

        StatisticsConfiguration statconf = algorithmConfiguration.getStatisticsConfiguration();
        statconf.setVerbose(true);
        configuration.setStatisticsConfiguration(statconf);

        int populationSize = algorithmConfiguration.getPopulationSize();
        int archiveSize = algorithmConfiguration.getArchiveSize();

        configuration.setPopulationSize(populationSize);
        configuration.setMaximumArchiveSize(archiveSize);
        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(archiveSize);

        int numberOfFunctionEvaluations = algorithmConfiguration.getMaximumNumberOfFunctionEvaluations();
        ITerminationCriteria terminationCriteria = new NumberOfFunctionEvaluationsTerminationCriteria(numberOfFunctionEvaluations);
        configuration.setTerminationCriteria(terminationCriteria);

        int elitism = algorithmConfiguration.getNumberOfElitistIndividuals();
        int numberOfSurvivors = algorithmConfiguration.getNumberOfSurvivors();
        int offSpringSize = algorithmConfiguration.getOffSpringSize();
        RecombinationParameters recombinationParameters = new RecombinationParameters(numberOfSurvivors,offSpringSize, elitism, true);
        configuration.setRecombinationParameters(recombinationParameters);

        configuration.setEnvironmentalSelectionOperator(new EnvironmentalSelection());
        configuration.setSelectionOperator(new TournamentSelection(1, 2));


        ReproductionOperatorContainer reproductionOperatorContainer = createAlgorithmReproductionOperatorContainer();
        configuration.setReproductionOperatorContainer(reproductionOperatorContainer);

        return new SPEA2(configuration);
    }


}
