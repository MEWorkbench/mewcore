package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.InputStream;
import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;

/**
 * Created by ptiago on 17-03-2015.
 */

/**
 * Interface for the specific readers
 */
public interface IStrainOptimizationReader extends Serializable{
    /**
     *
     * @param inputStream to read data from (file,db,...)
     * @param configuration base algorithm configuration
     * @return a solution genetic condition based on a specific jecoli configuration and an input stream
     * @throws Exception
     */
    GeneticConditions readSolutionFromStream(InputStream inputStream) throws Exception;
}
