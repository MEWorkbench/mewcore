package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions;

public class InvalidExpressionException extends Exception{

	private static final long serialVersionUID = 3875233912027983727L;

	public InvalidExpressionException(){
		super();
	}
		
	public InvalidExpressionException(String e){
		super(e);
	}
	
}
