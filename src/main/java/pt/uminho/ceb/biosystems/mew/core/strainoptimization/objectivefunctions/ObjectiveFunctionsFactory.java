package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.BPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.CYIELDObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FVAObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FVASenseMinPSYieldObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FVASenseObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.NumKnockoutsObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.ProductYieldObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.TurnoverObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.WeightedBPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.WeightedBiomassYIELDObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.WeightedYIELDObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class ObjectiveFunctionsFactory {
	
	protected static Pattern BOOLEAN_STRING_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
	
	protected static Map<String, Class<? extends IObjectiveFunction>> mapObjectiveFunctions = new HashMap<>();
	
	static {
		mapObjectiveFunctions.put(BPCYObjectiveFunction.ID, BPCYObjectiveFunction.class);
		mapObjectiveFunctions.put(WeightedBPCYObjectiveFunction.ID, WeightedBPCYObjectiveFunction.class);
		mapObjectiveFunctions.put(WeightedYIELDObjectiveFunction.ID, WeightedYIELDObjectiveFunction.class);
		mapObjectiveFunctions.put(WeightedBiomassYIELDObjectiveFunction.ID, WeightedBiomassYIELDObjectiveFunction.class);
		mapObjectiveFunctions.put(CYIELDObjectiveFunction.ID, CYIELDObjectiveFunction.class);
		mapObjectiveFunctions.put(NumKnockoutsObjectiveFunction.ID, NumKnockoutsObjectiveFunction.class);
		mapObjectiveFunctions.put(FluxValueObjectiveFunction.ID, FluxValueObjectiveFunction.class);
		mapObjectiveFunctions.put(ProductYieldObjectiveFunction.ID, ProductYieldObjectiveFunction.class);
		mapObjectiveFunctions.put(FVAObjectiveFunction.ID, FVAObjectiveFunction.class);
		mapObjectiveFunctions.put(FVASenseObjectiveFunction.ID, FVASenseObjectiveFunction.class);
		mapObjectiveFunctions.put(TurnoverObjectiveFunction.ID, TurnoverObjectiveFunction.class);
		mapObjectiveFunctions.put(FVASenseMinPSYieldObjectiveFunction.ID, FVASenseMinPSYieldObjectiveFunction.class);
	}
	
	public ObjectiveFunctionsFactory() {
	}
	
	public Set<String> getRegisteredOFs() {
		LinkedHashSet<String> setOFs = new LinkedHashSet<String>();
		for (String ofID : mapObjectiveFunctions.keySet())
			setOFs.add(ofID);
			
		return setOFs;
	}
	
	public void registerOF(String id, Class<? extends IObjectiveFunction> objectiveFunction) {
		mapObjectiveFunctions.put(id, objectiveFunction);
	}
	
	public void unregisterOF(String id) {
		mapObjectiveFunctions.remove(id);
	}
	
	public IObjectiveFunction getObjectiveFunction(String ofID, Map<String, Object> configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IObjectiveFunction> ofKlazz = mapObjectiveFunctions.get(ofID);
		configuration.put(AbstractObjectiveFunction.OBJECTIVE_FUNCTION_ID, ofID);
		IObjectiveFunction instance = ofKlazz.getConstructor(Map.class).newInstance(configuration);
		return instance;
	}
	
	public IObjectiveFunction getObjectiveFunction(Map<String, Object> configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String ofID = (String) configuration.get(AbstractObjectiveFunction.OBJECTIVE_FUNCTION_ID);
		Class<? extends IObjectiveFunction> ofKlazz = mapObjectiveFunctions.get(ofID);
		IObjectiveFunction instance = ofKlazz.getConstructor(Map.class).newInstance(configuration);
		return instance;
	}
	
	public IObjectiveFunction getObjectiveFunction(String ofID, Object... initArgs) throws InvalidObjectiveFunctionConfiguration {
		
		Class<? extends IObjectiveFunction> klazz = mapObjectiveFunctions.get(ofID);
		if (klazz == null) {
			throw new InvalidObjectiveFunctionConfiguration("Unknown objective function [" + ofID + "]. Please make sure it has been registered in the respective factory or contact the admin.");
		}
		Object[] convertedArgs = convertArgs(initArgs);
		Class<?>[] argsClasses = getArgumentsClasses(convertedArgs);
		IObjectiveFunction of;
		try {
			
			Constructor<?> constructor = klazz.getConstructor(argsClasses);
			Object unTypedOF = constructor.newInstance(convertedArgs);
			of = IObjectiveFunction.class.cast(unTypedOF);
		} catch (Exception e) {
			throw new InvalidObjectiveFunctionConfiguration(initArgs, argsClasses, klazz, e);
		}
		
		return of;
	}
	
	private Object[] convertArgs(Object[] initArgs) {
		Object[] converted = new Object[initArgs.length];
		
		for (int i = 0; i < initArgs.length; i++) {
			Object oi = initArgs[i];
			Object conv = null;
			if (String.class.isAssignableFrom(oi.getClass())) {
				String ois = (String) oi;
				Matcher matcher = BOOLEAN_STRING_PATTERN.matcher(ois.trim());
				if (matcher.matches()) {
					conv = Boolean.parseBoolean(ois.trim());
				} else {
					Double val = null;
					try {
						val = Double.parseDouble(ois.trim());
					} catch (NumberFormatException e) {
					}
					if(val!=null)
						conv = val;
				}				
			}
			conv = (conv==null) ? oi : conv;		
			converted[i] = conv;
		}
		
		return converted;
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
	public Map<String, ObjectiveFunctionParameterType> getObjectiveFunctionParameterTypes(String ofID)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		Class<? extends IObjectiveFunction> ofKlazz = mapObjectiveFunctions.get(ofID);
		Object untypedObj = ofKlazz.newInstance();
		IObjectiveFunction of = IObjectiveFunction.class.cast(untypedObj);
		Map<String, ObjectiveFunctionParameterType> types = (Map<String, ObjectiveFunctionParameterType>) of.mandatoryParameters();
		return types;
	}
	
	private Class<?>[] getArgumentsClasses(Object[] initArgs) {
		Class<?>[] klazzes = new Class<?>[initArgs.length];
		for (int i = 0; i < initArgs.length; i++) {
			if(initArgs[i] instanceof Enum<?>){
				klazzes[i] = ((Enum<?>)initArgs[i]).getDeclaringClass();
			}else{
				klazzes[i] = initArgs[i].getClass();				
			}
		}
		return klazzes;
	}
	
	//	@Test
	public void testOF(String[] args) throws InvalidObjectiveFunctionConfiguration {
		ObjectiveFunctionsFactory fact = new ObjectiveFunctionsFactory();
		BPCYObjectiveFunction of = (BPCYObjectiveFunction) fact.getObjectiveFunction("BPCY", "R_bio", "R_prod", "R_subst");
		
		MapUtils.prettyPrint(of.mandatoryParameters());
		MapUtils.prettyPrint(of.getValues());
	}
	
//	@Test
	public void testParams() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		ObjectiveFunctionsFactory fact = new ObjectiveFunctionsFactory();
		Map<String, ObjectiveFunctionParameterType> types = fact.getObjectiveFunctionParameterTypes("FVA_SENSE_MIN_PSYIELD");
		MapUtils.prettyPrint(types);
	}
	
//	@Test
	public void testEnum(){
		SolverType clp = SolverType.CLP;
		
		SolverType cplex3 = SolverType.CPLEX3;
		
		Object[] initArgs = new Object[]{clp, cplex3};
		Class<?>[] argumentClasses = getArgumentsClasses(initArgs);
		
		System.out.println(Arrays.toString(argumentClasses));
	}
}
