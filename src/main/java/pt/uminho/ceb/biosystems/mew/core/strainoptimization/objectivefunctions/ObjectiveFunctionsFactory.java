package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.BPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.WeightedBPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.WeightedYIELDObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class ObjectiveFunctionsFactory {
	
	protected static Map<String, Class<? extends IObjectiveFunctionNew>> mapObjectiveFunctions = new HashMap<>();
	
	static {
		mapObjectiveFunctions.put(BPCYObjectiveFunction.ID, BPCYObjectiveFunction.class);
		mapObjectiveFunctions.put(WeightedBPCYObjectiveFunction.ID, WeightedBPCYObjectiveFunction.class);
		mapObjectiveFunctions.put(WeightedYIELDObjectiveFunction.ID, WeightedYIELDObjectiveFunction.class);
	}
	
	public ObjectiveFunctionsFactory() {
	}
	
	public Set<String> getRegisteredOFs() {
		LinkedHashSet<String> setOFs = new LinkedHashSet<String>();
		for (String ofID : mapObjectiveFunctions.keySet())
			setOFs.add(ofID);
			
		return setOFs;
	}
	
	public void registerOF(String id, Class<? extends IObjectiveFunctionNew> objectiveFunction) {
		mapObjectiveFunctions.put(id, objectiveFunction);
	}
	
	public void unregisterOF(String id) {
		mapObjectiveFunctions.remove(id);
	}
	
	public IObjectiveFunctionNew getObjectiveFunction(String ofID, Map<String, Object> configuration)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IObjectiveFunctionNew> ofKlazz = mapObjectiveFunctions.get(ofID);
		configuration.put(AbstractObjectiveFunction.OBJECTIVE_FUNCTION_ID, ofID);
		IObjectiveFunctionNew instance = ofKlazz.getConstructor(Map.class).newInstance(configuration);
		return instance;
	}
	
	public IObjectiveFunctionNew getObjectiveFunction(Map<String, Object> configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String ofID = (String) configuration.get(AbstractObjectiveFunction.OBJECTIVE_FUNCTION_ID);
		Class<? extends IObjectiveFunctionNew> ofKlazz = mapObjectiveFunctions.get(ofID);
		IObjectiveFunctionNew instance = ofKlazz.getConstructor(Map.class).newInstance(configuration);
		return instance;
	}
	
	public IObjectiveFunctionNew getObjectiveFunction(String ofID, Object... initArgs) throws InvalidObjectiveFunctionConfiguration {
		
		Class<? extends IObjectiveFunctionNew> klazz = mapObjectiveFunctions.get(ofID);
		Class<?>[] argsClasses = getArgumentsClasses(initArgs);
		IObjectiveFunctionNew of;
		try {
			
			Constructor<?> constructor = klazz.getConstructor(argsClasses);
			Object unTypedOF = constructor.newInstance(initArgs);
			of = IObjectiveFunctionNew.class.cast(unTypedOF);
		} catch (Exception e) {
			throw new InvalidObjectiveFunctionConfiguration(initArgs, argsClasses, klazz, e);
		}
		
		return of;
	}
	
	/**
	 * Reflective method to return the parameter types of a given objective
	 * function
	 * 
	 * @param ofID the key (identifier) of a registered objective function
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException 
	 */
	public Map<String, ObjectiveFunctionParameterType> getObjectiveFunctionParameterTypes(String ofID) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		Class<? extends IObjectiveFunctionNew> ofKlazz = mapObjectiveFunctions.get(ofID);
		Object untypedObj = ofKlazz.newInstance();
		IObjectiveFunctionNew of = IObjectiveFunctionNew.class.cast(untypedObj);
		Map<String, ObjectiveFunctionParameterType> types = (Map<String, ObjectiveFunctionParameterType>) of.mandatoryParameters();
		return types;
	}
	
	private Class<?>[] getArgumentsClasses(Object[] initArgs) {
		Class<?>[] klazzes = new Class<?>[initArgs.length];
		for (int i = 0; i < initArgs.length; i++) {
			klazzes[i] = initArgs[i].getClass();
		}
		return klazzes;
	}
	
//	@Test
	public void testOF(String[] args) throws InvalidObjectiveFunctionConfiguration {
		ObjectiveFunctionsFactory fact = new ObjectiveFunctionsFactory();
		BPCYObjectiveFunction of = (BPCYObjectiveFunction) fact.getObjectiveFunction("BPCY", "R_bio","R_prod","R_subst");
		
		MapUtils.prettyPrint(of.mandatoryParameters());
		MapUtils.prettyPrint(of.getValues());
	}
	
	@Test
	public void testParams() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
		ObjectiveFunctionsFactory fact = new ObjectiveFunctionsFactory();
		Map<String,ObjectiveFunctionParameterType> types = fact.getObjectiveFunctionParameterTypes("BPCY");
		MapUtils.prettyPrint(types);
	}
	
}
