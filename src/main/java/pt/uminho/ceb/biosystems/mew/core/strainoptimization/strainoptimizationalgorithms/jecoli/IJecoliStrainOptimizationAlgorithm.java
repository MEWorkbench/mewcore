package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.algorithm.IAlgorithmResult;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.IStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;

/**
 * 
 * 
 * @author hgiesteira
 *
 * @param <T>
 */
public interface IJecoliStrainOptimizationAlgorithm<T extends IJecoliConfiguration> extends IStrainOptimizationAlgorithm<T> {

	IStrainOptimizationResultSet processAlgorithmResult(ArchiveManager archive, T configuration,
			AbstractMultiobjectiveEvaluationFunction<IRepresentation> evaluationFunction,
			IAlgorithmResult result, ISteadyStateDecoder decoder) throws Exception;

	public IAlgorithm<IRepresentation> createAlgorithm(ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction<?> evaluationFunction)
			throws Exception;
}
