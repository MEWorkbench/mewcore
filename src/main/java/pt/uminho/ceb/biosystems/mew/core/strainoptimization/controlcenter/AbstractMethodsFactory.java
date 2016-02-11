package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.UnregistaredMethodException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.IAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

public abstract class AbstractMethodsFactory<T extends IAlgorithm>  implements Serializable {

	private static final long		serialVersionUID	= 1L;
	/**
	 * Map of methods
	 */
	protected Map<String, Class<?>>	mapMethods;


	/**
	 *
	 * @param id method identification
	 * @param method the class to construct
	 */
	public void registerMethod(String id, Class<?> method) {
		mapMethods.put(id, method);
	}

	/**
	 *
	 * @return The set of registered method identifications
	 */
	public Set<String> getRegisteredMethods() {
		LinkedHashSet<String> setMethods = new LinkedHashSet<String>();
		for (String methodId : mapMethods.keySet())
			setMethods.add(methodId);

		return setMethods;
	}

	/**
	 * Initializes the instance variable mapMethods to the value in the
	 * parameter
	 *
	 * @param mapMethods Map of methods
	 */
	public AbstractMethodsFactory(Map<String, Class<?>> mapMethods) {
		this.mapMethods = mapMethods;
		//		methodProperties = new HashMap<String, Object>();
	}

	/**
	 *
	 * @param methodId previously registered method/problem in the factory
	 * @return The specific problem/method
	 * @throws UnregistaredMethodException - (Note typo name of the class) The
	 *             method is not registered in the factory
	 */
	public Class<?> getClassProblem(String methodId) throws UnregistaredMethodException {
		if (!mapMethods.containsKey(methodId)) throw new UnregistaredMethodException(methodId);
		return mapMethods.get(methodId);
	}

	/**
	 *
	 * @param methodId The name of the method/problem to create
	 * @param genericConfiguration the base configuration
	 * @return A specific strain optimization algorithm
	 * @throws InstantiationException - Unable to create the method
	 * @throws InvocationTargetException - Unable to invoke the method
	 * @throws UnregistaredMethodException - The method is not registered in the
	 *             factory
	 */
	public T getMethod(String methodId, IGenericConfiguration genericConfiguration) throws InstantiationException, InvocationTargetException, UnregistaredMethodException {

		T method = null;

		Class<?> klass = getClassProblem(methodId);

		try {
			method = (T) klass.getDeclaredConstructor().newInstance();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		method.putAllProperties(genericConfiguration);
		return method;
	}
	
	/**
	 * Removes the method/problem with id from the favtory
	 *
	 * @param id
	 */
	public void unregisterMethod(String id) {
		mapMethods.remove(id);
	}

}
