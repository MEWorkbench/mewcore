package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.exceptions;

public class CobraMatlabFormulationException extends RuntimeException {
	
	Exception exc;
	String message;
	
	@Override
	public String getMessage() {
		return message;
	}
	
	public CobraMatlabFormulationException(Exception e, String message) {
		this.exc = e;
		this.message = message;
	}
	
	public CobraMatlabFormulationException(Exception e) {
		this.exc = e;
		this.message = "Problem with COBRA Matlab formulation";
	}

}
