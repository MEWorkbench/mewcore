package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult;

import java.io.Serializable;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */

/**
 * Interface of the result set of a class
 * @param <T> the configuration extension
 * @param <E> the optimization result
 */
public interface IStrainOptimizationResultSet<T extends JecoliGenericConfiguration, E extends IStrainOptimizationResult> extends Serializable {
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
    void  readSolutionsFromFile(String file) throws Exception;

    /**
     * Creates a solution given a configuration given a set of objective values and a genetic condition
     * @param baseConfiguration
     * @param objectiveFunctionValueArray
     * @param gc
     * @return
     * @throws Exception
     */
    E createSolution(T baseConfiguration, double[] objectiveFunctionValueArray, GeneticConditions gc) throws Exception;
}
