package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions;

public class NoConstructorMethodException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Class<?> klass;
	
	public NoConstructorMethodException(Class<?> klass) {
		this.klass = klass;
	}

	public String getMessage(){
		
		String message = "The Class " + klass + " cannot be registered\n   * The class must have a constructor" + klass.getName() + "(ISteadyStateModel )";
		return message;
	}
}
