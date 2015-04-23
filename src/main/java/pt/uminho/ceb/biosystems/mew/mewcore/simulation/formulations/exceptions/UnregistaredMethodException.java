package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions;

public class UnregistaredMethodException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String methodId;
	
	public UnregistaredMethodException(String methodId){
		this.methodId = methodId;
	}

	public String getMessage(){
		return "The method " + methodId + " is not well registered!!";
	}
}
