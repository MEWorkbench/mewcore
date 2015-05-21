package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IElementsRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.IAnnealingSchedule;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealing;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.simulatedannealing.SimulatedAnnealingConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.IJecoliOptimizationStrategyConverter;

/**
 * Created by ptiago on 05-03-2015.
 */
public abstract class JecoliSASCOM<E extends IJecoliOptimizationStrategyConverter> extends JecoliSCOM<JecoliSASCOMConfig,E>{
	
	private static final long	serialVersionUID	= 1L;

	public JecoliSASCOM(E converter){
        super(converter);
        algorithmConfiguration = new JecoliSASCOMConfig();
    }

    public SimulatedAnnealing createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception, InvalidConfigurationException {

        SimulatedAnnealingConfiguration configuration = new SimulatedAnnealingConfiguration();
        configuration.setEvaluationFunction(evaluationFunction);
        ISolutionFactory solutionFactory = optimizationStrategyConverter.createSolutionFactory(algorithmConfiguration,decoder,evaluationFunction);
        configuration.setSolutionFactory(solutionFactory);

        IAnnealingSchedule annealingSchedule = algorithmConfiguration.getAnnealingSchedule();
        configuration.setAnnealingSchedule(annealingSchedule);

        configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
        configuration.setProblemBaseDirectory("nullDirectory");
        configuration.setAlgorithmStateFile("nullFile");
        configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
        configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<IElementsRepresentation>>());
        StatisticsConfiguration statconf = new StatisticsConfiguration();
        statconf.setVerbose(true);
        configuration.setStatisticsConfiguration(statconf);

        ITerminationCriteria terminationCriteria = configuration.getTerminationCriteria();
        configuration.setTerminationCriteria(terminationCriteria);
        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);

        ReproductionOperatorContainer reproductionOperatorContainer = createAlgorithmReproductionOperatorContainer();
        configuration.setMutationOperatorContainer(reproductionOperatorContainer);

        return  new SimulatedAnnealing(configuration);
    }


}
