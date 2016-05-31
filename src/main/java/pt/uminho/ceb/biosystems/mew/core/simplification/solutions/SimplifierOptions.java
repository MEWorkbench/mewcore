package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

/**
 * Options for the solutions simplifier.
 * This class is a simple list of constants.
 * 
 * @author pmaia
 * @date May 24, 2016
 * @version 
 * @since
 */
public class SimplifierOptions {
	
	/**
	 * Maximum difference delta allowed when comparing solutions with equal fitness
	 */
	public static final String	DELTA					= "simplifier.options.delta";
														
	/**
	 * Minimum percentage allowed per objective function.
	 * When set, this will allow the simplifier to generate worse solutions down to the fitnesses,
	 * calculated as these percentages of the maximum fitness for each objective function,
	 * respectively.
	 */
	public static final String	MIN_PERCENT_PER_OBJFUNC	= "simplifier.options.minPercentPerObjectiveFunction";
														
}
