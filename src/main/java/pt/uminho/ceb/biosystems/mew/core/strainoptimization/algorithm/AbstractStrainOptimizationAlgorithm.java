package pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm;

import java.io.Serializable;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;

/**
 * Created by ptiago on 03-03-2015.
 */

/**
 * Base class To Create Algorithms
 * @param <T> extends IGenericConfiguration
 */
public abstract class AbstractStrainOptimizationAlgorithm<T extends IGenericConfiguration> implements IStrainOptimizationAlgorithm<T>, Serializable{
    
	private static final long	serialVersionUID	= 1L;
	/**
     * Base configuration to be utilized by the method
     */
    protected T algorithmConfiguration;

    /**
     *
     * @param configuration execute this specific configuration
     * @return A specific solution set
     * @throws Exception if the method configuration is not well specified
     */
    public IStrainOptimizationResultSet executeAlgorithm(T configuration) throws Exception {
        configuration.validate();
        return execute(configuration);
    }

    /**
     * Change the algorithm configuration
     * @param algorithmConfiguration
     */
    public void setAlgorithmConfiguration(T algorithmConfiguration) {
        this.algorithmConfiguration = algorithmConfiguration;
    }

    /**
     * Execute the method with a specific configuration
     * @param configuration - Base configuration
     * @return a specific solution set
     * @throws Exception if the method configuration is not well specified
     */
    
    protected abstract IStrainOptimizationResultSet execute(T configuration) throws Exception;

    /**
     * Executes the algorithm with the local configuration
     * @return  specific solution set
     * @throws Exception if the method configuration is not well specified
     */
    public IStrainOptimizationResultSet execute() throws Exception {
        return execute(algorithmConfiguration);
    }

    /**
     * Puts all the properties in the configuration parameter in the local algorithm configuration
     * @param configuration
     */

    public void putAllProperties(IGenericConfiguration configuration){
        for(Map.Entry<String,Object> configurationEntry:configuration.getPropertyMap().entrySet())
            algorithmConfiguration.setProperty(configurationEntry.getKey(),configurationEntry.getValue());
    }

    /**
     * Sets the method property in the local configuration
     * @param id property identification
     * @param object value of the property
     */
    public void setProperty(String id,Object object){
        algorithmConfiguration.setProperty(id,object);
    }

    /**
     * Returns the value of the local property with a specific id
     * @param id property id
     * @return property values
     */
    public Object getProperty(String id){
        return algorithmConfiguration.getProperty(id);
    }

	public T getAlgorithmConfiguration() {
		return algorithmConfiguration;
	}
}
