package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.InvalidTerminationCriteriaParameter;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGeneSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;

/**
 * Created by ptiago on 03-03-2015.
 */
public interface IJecoliConfiguration extends IGenericConfiguration, ISteadyStateConfiguration, IGeneSteadyStateConfiguration {
    String getOptimizationStrategy();
    boolean getIsOverUnderExpression();
    boolean getIsGeneOptimization();
    boolean getIsVariableSizeGenome();
    ArchiveManager getArchiveManager();
    ITerminationCriteria getTerminationCriteria() throws InvalidTerminationCriteriaParameter ;
}
