package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

/**
 * Options for the solutions simplifier.
 * This class is a simple list of constants.
 * 
 * @author pmaia
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
														
	/**
	 * If this flag is set to true the simplfier keeps only the smallest solution, otherwise, it
	 * keeps all the intermediate solutions in a given solutions' simplification process
	 */
	public static final String	KEEP_ONLY_MIN_SOLUTION	= "simplifier.options.keepOnlyMinSolution";
														
}
