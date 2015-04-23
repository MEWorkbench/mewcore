package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions;

public class MandatoryPropertyException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String property;
	protected Class<?> klass;
	
	public MandatoryPropertyException(String property, Class<?> klass) {
		this.property = property;
		this.klass = klass;
	}
	
	public String getMessage(){
		return property + "\t" + klass;
	}
	
	public String getProperty(){
		return property;
	}
}
