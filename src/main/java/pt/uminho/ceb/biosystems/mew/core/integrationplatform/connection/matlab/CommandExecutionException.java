package pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab;

public class CommandExecutionException extends RuntimeException{
	
	public CommandExecutionException(Exception e) {
		super(e);
	}
	
	public CommandExecutionException(String message){
		super(message);
	}
	
	public CommandExecutionException(Throwable throwable){
		super(throwable);
	}
	
	public CommandExecutionException(String message, Throwable throwable){
		super(message, throwable);
	}

}
