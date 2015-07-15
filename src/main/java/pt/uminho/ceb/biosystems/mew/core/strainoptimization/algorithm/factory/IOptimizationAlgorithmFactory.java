package pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.factory;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 23-02-2015.
 */
public interface IOptimizationAlgorithmFactory {
    public void constructAlgorithm(String algorithm,IGenericConfiguration genericConfiguration) throws Exception;
    public Set<String> getAvailableOptimizationAlgorithmSet();
    public boolean canConstructAlgorithm(String algorithm);
}
