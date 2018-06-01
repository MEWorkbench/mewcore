package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.RegistMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.UnregistaredMethodException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.AbstractStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.IStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Class for spawning concrete method algorithms. Note: The algorithm classes
 * have to contain at least an empty constructor
 */
public class AbstractOptimizationMethodsFactory<T extends IStrainOptimizationAlgorithm<IGenericConfiguration>> extends AbstractMethodsFactory<T> implements Serializable {

	private static final long		serialVersionUID	= 1L;

	public AbstractOptimizationMethodsFactory(Map<String, Class<?>> mapMethods) {
		super(mapMethods);
	}

	/**
	 * Returns an instance of a method without initializing a configuration 
	 * 
	 * @param methodId
	 * @return
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws UnregistaredMethodException
	 */
	public AbstractStrainOptimizationAlgorithm getMethod(String methodId) throws InstantiationException, InvocationTargetException, UnregistaredMethodException {
		
		AbstractStrainOptimizationAlgorithm method = null;
		
		Class<?> klass = getClassProblem(methodId);
		
		try {
			method = (AbstractStrainOptimizationAlgorithm) klass.getDeclaredConstructor().newInstance();
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
		
		return method;
	}
	
	/**
	 *
	 * @param methodId The name of the method/Problem
	 * @param method The problem/method class
	 * @throws RegistMethodException When the method is already registered in
	 *             the factory
	 * @throws NoConstructorMethodException
	 */
	public void addStrainOptimizationMethod(String methodId, Class<?> method) throws RegistMethodException, NoConstructorMethodException {

		if (mapMethods.containsKey(methodId)) throw new RegistMethodException("The optimization method " + methodId + " is already registed");
		if (method == null) throw new RegistMethodException("The optimization method is already a null class");
		try {
			method.getConstructor(ISteadyStateModel.class);
		} catch (Exception e) {
			throw new NoConstructorMethodException(method);
		}

		mapMethods.put(methodId, method);
	}
	
	public boolean validate(String algorithm, String strategy){
		String concat = algorithm.trim()+strategy.trim();
		
		return mapMethods.containsKey(concat);
	}
}

