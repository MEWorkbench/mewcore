package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions;

public class PropertyCastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String property;
	public Class<?> corretClass;
	public Class<?> incorrectClass;
	
	
	public PropertyCastException(String property, Class<?> corretClass, Class<?> incorrectClass){
		this.property = property;
		this.corretClass = corretClass;
		this.incorrectClass = incorrectClass;
	}
	
	public String getMessage(){
		return getMessage(property, corretClass, incorrectClass);
	}

	
	static public String getMessage(String property, Class<?> corretClass, Class<?> incorrectClass){
		String erro = "Type Mismach in Property " + property +"\n" +
			"Expected Class: " + corretClass + "\n" +
			"Introduced Class: " + incorrectClass;
		return erro;
	}
	
	public String getProperty(){
		return property;
	}
}
