package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions;

public class MandatoryPropertyException extends RuntimeException{

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
