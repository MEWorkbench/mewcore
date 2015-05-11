package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.exceptions;

public class MatlabNotFoundException extends Exception {
	
	private static String errorMessage = "Unable to find MATLAB application.\nMake sure you chose the right settings.";
	
	public MatlabNotFoundException(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public MatlabNotFoundException() {
		this(errorMessage);
	}
	
	@Override
	public String getMessage() {
		return errorMessage;
	}

}
