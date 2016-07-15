package pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;

/**
 * 
 * 
 * @author hgiesteira
 *
 * @param <T>
 */
public interface IStrainOptimizationAlgorithm<T extends IGenericConfiguration> extends IAlgorithm {
	
	public T getAlgorithmConfiguration();

	public void setAlgorithmConfiguration(T algorithmConfiguration);

	public IStrainOptimizationResultSet executeAlgorithm(T configuration) throws Exception;

	public IStrainOptimizationResultSet execute() throws Exception;
}
