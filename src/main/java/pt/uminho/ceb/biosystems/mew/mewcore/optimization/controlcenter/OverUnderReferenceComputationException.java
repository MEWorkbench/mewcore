/**
 * 
 */
package pt.uminho.ceb.biosystems.mew.mewcore.optimization.controlcenter;

/**
 * @author pmaia
 *
 */
public class OverUnderReferenceComputationException extends Exception {

	/**
	 * @param string
	 */
	public OverUnderReferenceComputationException(String string) {
		super(string);
	}
	
	public OverUnderReferenceComputationException(Exception e){
		super(e);
	}
	
	public OverUnderReferenceComputationException(Throwable t){
		super(t);
	}

	private static final long serialVersionUID = -675037171321746092L;

}
