package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions;

import java.util.Map;

public class ManagerExceptionUtils {
	
	static public <T> T testCast(Map<String, Object> props, Class<T> klass, String prop, boolean canPropBeNull) throws PropertyCastException, MandatoryPropertyException{
		T ret =null;
		Object inprop = props.get(prop);
		
//		System.out.println(props);
		if(!canPropBeNull && inprop == null)
			throw new MandatoryPropertyException(prop, klass);
		
		try{
			ret = klass.cast(inprop);
		}catch (ClassCastException e) {
			throw new PropertyCastException(prop, klass, inprop.getClass());
		}
		return ret; 
	}

}
