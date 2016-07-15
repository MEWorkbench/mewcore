package pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions;

public class StrategyNotDefinedException extends RuntimeException {
	
	public StrategyNotDefinedException() {
		super();
	}
	
	public StrategyNotDefinedException(String message) {
		super(message);
	}
	
	public StrategyNotDefinedException(Throwable e) {
		super(e);
	}
	
	public StrategyNotDefinedException(String message, Throwable e) {
		super(message, e);
	}

}
