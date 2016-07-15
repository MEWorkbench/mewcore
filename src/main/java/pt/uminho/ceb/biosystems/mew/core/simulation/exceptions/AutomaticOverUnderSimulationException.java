package pt.uminho.ceb.biosystems.mew.core.simulation.exceptions;

public class AutomaticOverUnderSimulationException extends AutomaticSimulationException {
	
	public AutomaticOverUnderSimulationException() {
		super();
	}
	
	public AutomaticOverUnderSimulationException(String errorMsg, Throwable t) {
		super(errorMsg, t);
	}
	
	public AutomaticOverUnderSimulationException(Throwable t) {
		super(t);
	}
	
	public AutomaticOverUnderSimulationException(String errorMsg, Exception e) {
		super(errorMsg, e);
	}
	
	public AutomaticOverUnderSimulationException(Exception e) {
		super(e);
	}
	
	public AutomaticOverUnderSimulationException(String errorMsg) {
		super(errorMsg);
	}

	@Override
	public String getMessage() {
		String messageToRet = "Problem while performing automatic over/under simulation\n";
		messageToRet += super.getMessage();
		return messageToRet;
	}

}
