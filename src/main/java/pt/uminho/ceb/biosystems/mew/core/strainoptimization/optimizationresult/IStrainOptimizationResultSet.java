package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.Serializable;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */

/**
 * Interface of the result set of a class
 * @param <T> the configuration extension
 * @param <E> the optimization result
 */
public interface IStrainOptimizationResultSet<T extends IGenericConfiguration, E extends IStrainOptimizationResult> extends Serializable {
    /**
     *
     * @return the configuration utilized in this solution set
     */
    T getBaseConfiguration();

    /**
     *
     * @return List of results/solutions of this solution set
     */
    List<E> getResultList();

    /**
     *
     * @param file the name of the file to write the solution set
     * @throws Exception IOErrors
     */
    void writeToFile(String file) throws Exception;

    /**
     * Reads a specific type of solutions from a file
      * @param file input file
     * @throws Exception
     */
    void readSolutionsFromFile(String file) throws Exception;

    /**
     * Creates a solution given a genetic condition
     * 
     * @param gc
     * @param result
     * @return
     * @throws Exception
     */
    E createSolution(GeneticConditions gc);
    
    /**
     * Creates a solution given a genetic condition and a list of attribute values 
     * 
     * @param gc
     * @param attributes
     * @return
     */
    E createSolution(GeneticConditions gc, List<Double> attributes);
    
    
    void addSolution(E solution);
    
    IStrainOptimizationResultSet<T,E> merge(IStrainOptimizationResultSet<T, E> resultSetToMerge);

}
