package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

/**
 * Created by ptiago on 19-03-2015.
 */

/**
 * The interface type of a result
 */
public interface IStrainOptimizationResult extends IStrainOptimizationWriter {
	/**
	 * It is important to bear in mind that if the solution does not contain a
	 * result for a given method it will be computed
	 * an memoized (its memoized and not memorized in this context)
	 * 
	 * @param method the simulation method
	 * @return a steady state simulation result
	 * @throws Exception if the method is not defined or is not possible to
	 *             compute the result
	 */
	SteadyStateSimulationResult getSimulationResultForMethod(String method) throws Exception;
	
	/**
	 * 
	 * @param method
	 * @param result
	 */
	void addSimulationResultForMethod(String method, SteadyStateSimulationResult result);
	
	/**
	 * 
	 * @return The solution genetic conditions
	 */
	GeneticConditions getGeneticConditions();
	
	/**
	 * Writes a solution to a supplied stream
	 * 
	 * @param outputStream output stream
	 * @throws Exception Error related with the stream
	 */
	@Override
	void write(OutputStreamWriter outputStream) throws Exception;

	/**
	 * 
	 * @return the fitnesses
	 */
	List<Double> getFitnesses();
	
}
