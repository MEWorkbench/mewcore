package pt.uminho.ceb.biosystems.mew.core.simulation.exceptions;

public class AutomaticSimulationException extends RuntimeException {

	public AutomaticSimulationException(){
		super();
	}
	
	public AutomaticSimulationException(String errorMsg, Throwable t) {
		super(errorMsg, t);
	}
	
	public AutomaticSimulationException(Throwable t) {
		super(t);
	}
	
	public AutomaticSimulationException(String errorMsg, Exception e) {
		super(errorMsg, e);
	}
	
	public AutomaticSimulationException(Exception e) {
		super(e);
	}
	
	public AutomaticSimulationException(String errorMsg) {
		super(errorMsg);
	}
}
