package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.RegistMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Abstract class for the creation of a controlCenter. This class has a factory
 * and a local configuration
 * that is passed to the algorithm in the creation process.
 * 
 * @param <T> The type of the result to return
 */
public abstract class AbstractStrainOptimizationControlCenter<T> implements Serializable {
	
	private static final long	serialVersionUID	= 1L;

	/**
	 * Factory containing the methods that can be instantiated. The factory can
	 * be updated in runtime
	 */
	protected static OptimizationMethodsFactory	factory;
	
	/**
	 * Local generic configuration
	 */
	protected IGenericConfiguration				genericConfiguration;
	
	/**
	 * Empty constructor that creates an empty generic configuration
	 */
	public AbstractStrainOptimizationControlCenter() {
		this.genericConfiguration = new GenericConfiguration();
	}
	
	static {
		LinkedHashMap<String, Class<?>> mapMethods = new LinkedHashMap<>();
		factory = new OptimizationMethodsFactory(mapMethods);
	}
	
	/**
	 * Sets the value of the local property
	 * 
	 * @param propertyId property identification
	 * @param property value of the property
	 */
	public void setProperty(String propertyId, Object property) {
		genericConfiguration.setProperty(propertyId, property);
	}
	
	/**
	 * 
	 * @param propertyId Name of the property to get
	 * @return Returns the value of the registered property
	 */
	public Object getProperty(String propertyId) {
		return genericConfiguration.getProperty(propertyId);
	}
	
	/**
	 * Executes the specified algorithm based on the generic configuration
	 * 
	 * @return The pre-specified value type of the class
	 * @throws Exception
	 */
	public T execute() throws Exception {
		return execute(genericConfiguration);
	}
	
	/**
	 * Executes as specific configuration
	 * 
	 * @param genericConfiguration
	 * @return The pre-specified value type of the class
	 * @throws Exception
	 */
	public abstract T execute(IGenericConfiguration genericConfiguration) throws Exception;
	
	/**
	 * Static method to regist new methods in the factory. The control center
	 * may not be instantiated
	 * 
	 * @param methodId method/problem identification
	 * @param klass method/problem class
	 * @throws RegistMethodException
	 * @throws NoConstructorMethodException
	 */
	static public void registMethod(String methodId, Class<?> klass) throws RegistMethodException, NoConstructorMethodException {
		factory.addStrainOptimizationMethod(methodId, klass);
	}
	
	/**
	 * Similar method to register new problem instances when the control center
	 * is instantiated
	 * 
	 * @param id
	 * @param method
	 */
	public static void registerMethod(String id, Class<?> method) {
		factory.registerMethod(id, method);
	}
	
	/**
	 * Unregisters a specific method
	 * 
	 * @param id
	 * @param method
	 */
	public static void unregisterMethod(String id, Class<?> method) {
		factory.unregisterMethod(id);
	}
	
	/**
	 * 
	 * @return The set of methods specified in the factory
	 */
	public static Set<String> getRegisteredMethods() {
		return factory.getRegisteredMethods();
	}
}
