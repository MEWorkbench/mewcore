package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;
import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 17-03-2015.
 */
public interface IStrainOptimizationWriter<T extends IGenericConfiguration,E extends AbstractSolution> extends Serializable {
    void write(OutputStreamWriter outputStream) throws Exception;
}
