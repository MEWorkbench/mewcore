package pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class InvalidObjectiveFunctionConfiguration extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidObjectiveFunctionConfiguration(String e){
		super(e);
	}

	public InvalidObjectiveFunctionConfiguration(String[] args, Class<?>[] argsClasses, Class<?> klazz, Exception e) {
		super(buildMessage(args, argsClasses, klazz, e), e);
	}
	
	public InvalidObjectiveFunctionConfiguration(Object[] args, Class<?>[] argsClasses, Class<?> klazz, Exception e) {
		super(buildMessage(args, argsClasses, klazz, e), e);
	}

	protected static String buildMessage(Object[] args, Class<?>[] argsClasses, Class<?> klazz, Exception e) {
		String out = "" + klazz.toString() + " requires [" +
			CollectionUtils.join(argsClasses, ",") +
			"] arguments but received [" +  CollectionUtils.join(args, ",") + "] caused by " + e;
		return out;
	}

}
