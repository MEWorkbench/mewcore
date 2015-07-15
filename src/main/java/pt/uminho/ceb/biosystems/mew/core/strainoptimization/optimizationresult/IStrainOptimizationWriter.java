package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;
import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 17-03-2015.
 */
public interface IStrainOptimizationWriter<T extends JecoliGenericConfiguration,E extends AbstractStrainOptimizationResult<T>> extends Serializable {
    void write(OutputStreamWriter outputStream) throws Exception;
}
