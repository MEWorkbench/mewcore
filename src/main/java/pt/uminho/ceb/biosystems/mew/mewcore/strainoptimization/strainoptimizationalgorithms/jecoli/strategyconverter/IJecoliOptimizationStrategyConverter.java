package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 19-03-2015.
 */

/**
 * Class concentrating strategy (RK,GK,ROU,GOU,RKRKS) specific methods
 * @param <T> extension of JecoliGeneric Configuration
 * @param <E> extension of an IStrainOptimizationResult
 */
public interface IJecoliOptimizationStrategyConverter<T extends JecoliGenericConfiguration,E extends IStrainOptimizationResult> {
    /**
     * Creates a solution set based on a configuration and a list of solutions
     * @param configuration
     * @param solutionList
     * @return new solution set
     */
    IStrainOptimizationResultSet<T,E> createSolutionSet(T configuration,List<E> solutionList);

    /**
     *
     * @param configuration utilized by the solution
     * @param simulations simulations regarding the simulation method and the simulation result (including flux distribution)
     * @param gc Genetic Condition of the solution
     * @return Returns a strategy solution
     */
    IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc);

    /**
     * Creates the decoder for a specific strategy
     * @param configuration
     * @return the decoder to be utilized by the evaluation function
     * @throws Exception
     */
    ISteadyStateDecoder createDecoder(T configuration) throws Exception;

    /**
     *
     * @param configuration
     * @param decoder
     * @param evaluationFunction
     * @return The specific solution factory for a given strategy. Each strategy may utilize distinct solution representations.
     */
    ISolutionFactory createSolutionFactory(T configuration,ISteadyStateDecoder decoder,AbstractMultiobjectiveEvaluationFunction evaluationFunction);
}
