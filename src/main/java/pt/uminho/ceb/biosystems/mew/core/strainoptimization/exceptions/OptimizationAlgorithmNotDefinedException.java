package pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions;

public class OptimizationAlgorithmNotDefinedException extends RuntimeException {
	
	public OptimizationAlgorithmNotDefinedException() {
		super();
	}
	
	public OptimizationAlgorithmNotDefinedException(String message) {
		super(message);
	}
	
	public OptimizationAlgorithmNotDefinedException(Throwable e) {
		super(e);
	}
	
	public OptimizationAlgorithmNotDefinedException(String message, Throwable e) {
		super(message, e);
	}
}
