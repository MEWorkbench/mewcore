package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.exceptions;

public class MatlabNotFoundException extends Exception {
	
	@Override
	public String getMessage() {
		return "Unable to find Matlab application";
	}

}
