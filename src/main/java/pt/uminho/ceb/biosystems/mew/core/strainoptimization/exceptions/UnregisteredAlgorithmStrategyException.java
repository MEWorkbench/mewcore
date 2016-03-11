package pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions;

public class UnregisteredAlgorithmStrategyException extends RuntimeException {

	public UnregisteredAlgorithmStrategyException() {
		super();
	}
	
	public UnregisteredAlgorithmStrategyException(String message) {
		super(message);
	}
	
	public UnregisteredAlgorithmStrategyException(Throwable e) {
		super(e);
	}
	
	public UnregisteredAlgorithmStrategyException(String message, Throwable e) {
		super(message, e);
	}
}
