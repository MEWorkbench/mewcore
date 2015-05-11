package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 17-03-2015.
 */
public interface IStrainOptimizationWriter<T extends JecoliGenericConfiguration,E extends AbstractStrainOptimizationResult<T>>  {
    void write(OutputStreamWriter outputStream) throws Exception;
}
