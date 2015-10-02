package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 03-03-2015.
 */
public interface IJecoliConfiguration extends IGenericConfiguration{
    String getOptimizationStrategy();
    boolean getIsOverUnderExpression();
    boolean getIsGeneOptimization();
    boolean getIsVariableSizeGenome();
}
