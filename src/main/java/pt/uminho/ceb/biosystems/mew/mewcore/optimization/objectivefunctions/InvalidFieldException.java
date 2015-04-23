package pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions;

public class InvalidFieldException extends Exception {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFieldException(String field, String reason, Exception e) {
		super(field + " is not invalid!\n" + reason, e);
	}
}
