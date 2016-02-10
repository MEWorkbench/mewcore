package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.randomsearch;

import java.util.ArrayList;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.SetRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.statistics.StatisticsConfiguration;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.randomsearch.RandomSearch;
import pt.uminho.ceb.biosystems.jecoli.algorithm.singleobjective.randomsearch.RandomSearchConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;

/**
 * Created by ptiago on 03-03-2015.
 */
public abstract class JecoliRANDOMCSOM<E extends IJecoliOptimizationStrategyConverter> extends JecoliCSOM<JecoliRANDOMCSOMConfig, E> {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliRANDOMCSOM(E converter) {
		super(converter);
		algorithmConfiguration = new JecoliRANDOMCSOMConfig();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) throws Exception {
		RandomSearchConfiguration configuration = new RandomSearchConfiguration();
		ISolutionFactory solutionFactory = optimizationStrategyConverter.createSolutionFactory(algorithmConfiguration, decoder, evaluationFunction);
		configuration.setEvaluationFunction(evaluationFunction);
		configuration.setSolutionFactory(solutionFactory);
		
		IRandomNumberGenerator randomGenerator = new DefaultRandomNumberGenerator();
		configuration.setRandomNumberGenerator(randomGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<SetRepresentation>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration(true));
		
		ITerminationCriteria terminationCriteria = algorithmConfiguration.getTerminationCriteria();
		configuration.setTerminationCriteria(terminationCriteria);
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(15);
		
		return new RandomSearch(configuration);
	}
	
}
