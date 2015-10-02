package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

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
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;

/**
 * Created by ptiago on 05-03-2015.
 */
public abstract class JecoliSACSOM<E extends IJecoliOptimizationStrategyConverter> extends JecoliCSOM<JecoliSACSOMConfig,E>{
	
	private static final long	serialVersionUID	= 1L;

	public JecoliSACSOM(E converter){
        super(converter);
        algorithmConfiguration = new JecoliSACSOMConfig();
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

        ITerminationCriteria terminationCriteria = algorithmConfiguration.getTerminationCriteria();
        configuration.setTerminationCriteria(terminationCriteria);
        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);

        ReproductionOperatorContainer reproductionOperatorContainer = createAlgorithmReproductionOperatorContainer();
        configuration.setMutationOperatorContainer(reproductionOperatorContainer);

        return  new SimulatedAnnealing(configuration);
    }


}
