package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ErrorLog {
	
	protected StringBuffer log;
	protected List<Exception> exceptions;
	
	
	public ErrorLog(){
		log = null;
	}
	
	public ErrorLog(String error){
		log = new StringBuffer(error);
	}

	public void appendError(String error){
		if(log==null)
			log = new StringBuffer();
		log.append("\n" + error);
	}
	
	public void addException(Exception e){
		if(exceptions==null)
			exceptions = new ArrayList<>();
		exceptions.add(e);
	}
	
	public void clearLog(){
		log = null;
		exceptions = null;
	}
	
	public boolean isEmpty(){
		return ((log==null || log.length()==0) && (exceptions==null || exceptions.size()==0));
	}
	
	
	public String getLog(){return log==null ? "" : log.toString();}
	public void setLog(String log){this.log = new StringBuffer(log);}
	public List<Exception> getExceptions() {return exceptions;}
	public void setExceptions(List<Exception> exceptions) {this.exceptions = exceptions;}
	
	
	@Override
	public String toString() {
		String err = (log==null) ? "" : log.toString();
		if(exceptions!=null)
			for(Exception e : exceptions)
				err += "\n" + e.toString() + ":\n" + e.getMessage();
		return err;
	}
}
