package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions;


public class WrongFormulationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public WrongFormulationException(String error) {
		super(error);
	}


	public WrongFormulationException(
			Throwable e) {
		super(e);
	}

	

}
