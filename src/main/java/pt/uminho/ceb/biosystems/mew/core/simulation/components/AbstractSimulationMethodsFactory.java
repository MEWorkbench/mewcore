package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.NoConstructorMethodException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.UnregistaredMethodException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class AbstractSimulationMethodsFactory {

	protected Map<String,Class<?>> mapMethods;
	
	public void registerMethod(String id, Class<?> method){
		mapMethods.put(id,method);
	}
	
	
	public Set<String> getRegisteredMethods(){
		LinkedHashSet<String> setMethods = new LinkedHashSet<String>();
		for(String methodId : mapMethods.keySet())
			setMethods.add(methodId);
		
		return setMethods;
	}
	
	public AbstractSimulationMethodsFactory(Map<String,Class<?>> mapMethods)
	{
		this.mapMethods = mapMethods;
//		methodProperties = new HashMap<String, Object>();
	}
	
	public Class<?> getClassProblem( String methodId) throws UnregistaredMethodException{
		if(!mapMethods.containsKey(methodId)) throw new UnregistaredMethodException(methodId);
		return mapMethods.get(methodId);
	}
	
	public ISteadyStateSimulationMethod getMethod (String methodId, Map<String,Object> methodProperties, ISteadyStateModel model) throws InstantiationException, InvocationTargetException, UnregistaredMethodException 
	{
		
		ISteadyStateSimulationMethod method = null;
		
		Class<?> klass = getClassProblem(methodId);
		
		try {
			method = (ISteadyStateSimulationMethod) klass.getDeclaredConstructor(ISteadyStateModel.class).newInstance(model);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SecurityException e) {
			System.err.println("Nunca devia ter passado por aqui...");
			e.printStackTrace();
		} catch (InstantiationException e) {
			throw e;
		} catch (IllegalAccessException e) {
			System.err.println("Nunca devia ter passado por aqui...");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			System.err.println("Nunca devia ter passado por aqui...");
			e.printStackTrace();
		}
		
		method.setProperty(SimulationProperties.METHOD_NAME, methodId);
		method.putAllProperties(methodProperties);
		return method;
	}
	
	
//	public void addProperty(String key, Object value){
//		methodProperties.put(key, value);
//	}
//	
//	public void removeProperty(String key){
//		
//		methodProperties.remove(key);
//	}
//	
//	public Object getProperty(String key){
//		return methodProperties.get(key);
//	}
	
	public void addSimulationMethod(String methodId, Class<?> method) throws RegistMethodException, NoConstructorMethodException{
		
		if(mapMethods.containsKey(methodId)) throw new RegistMethodException("The simulation method " + methodId + " is already registed");
		if(method==null) throw new RegistMethodException("The simulation method is already a null class");
		try{
			method.getConstructor(ISteadyStateModel.class);
		}catch (Exception e) {
			throw new NoConstructorMethodException(method);
		}
		
		mapMethods.put(methodId, method);
		
	}


//	public ISteadyStateModel getModel() {
//		return model;
//	}
//
//
//	public void setModel(ISteadyStateModel model) {
//		this.model = model;
//	}

}