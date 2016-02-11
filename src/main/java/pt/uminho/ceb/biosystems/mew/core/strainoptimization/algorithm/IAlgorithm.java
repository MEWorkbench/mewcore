package pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * 
 * 
 * @author hgiesteira
 *
 * @param <T>
 */
public interface IAlgorithm<T extends IGenericConfiguration> {

	public void putAllProperties(T configuration);

	public void setProperty(String id, Object object);

	public Object getProperty(String id);

}
