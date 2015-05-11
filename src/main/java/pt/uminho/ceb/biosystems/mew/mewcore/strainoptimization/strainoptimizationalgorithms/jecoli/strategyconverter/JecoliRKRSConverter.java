package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.dualset.DualSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ReactionSwapDualSetRepresentationDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solution.RKRSSolution;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solutionset.RKRSSolutionSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * Created by ptiago on 19-03-2015.
 */
public class JecoliRKRSConverter<T extends JecoliGenericConfiguration> implements IJecoliOptimizationStrategyConverter<T,RKRSSolution> {
    protected List<IntegerSetRepresentationFactory> swapFactoryList;

    @Override
    public IStrainOptimizationResultSet<T,RKRSSolution> createSolutionSet(T configuration,List<RKRSSolution> solutionList) {
        return new RKRSSolutionSet<T>(configuration,solutionList);
    }

    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) {
        return new RKRSSolution(configuration,simulations,gc);
    }

    @Override
    public ISolutionFactory createSolutionFactory(T configuration, ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) {
        swapFactoryList = new ArrayList<>();
        int maxSetValue = decoder.getNumberVariables();
        int maxSetSize = configuration.getMaxSetSize();
        Map swapsMap = configuration.getReactionSwapMap();
        int maxAllowedSwaps = configuration.getMaxAllowedSwaps();
        int maxPossibleSwaps = swapsMap.size();
        IntegerSetRepresentationFactory koSolutionFactory = new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives());
        IntegerSetRepresentationFactory swapSolutionFactory = new IntegerSetRepresentationFactory(maxPossibleSwaps, maxAllowedSwaps, evaluationFunction.getNumberOfObjectives());
        swapFactoryList.add(koSolutionFactory);
        swapFactoryList.add(swapSolutionFactory);
        return new DualSetRepresentationFactory(maxSetValue, maxPossibleSwaps, maxSetSize, maxAllowedSwaps,evaluationFunction.getNumberOfObjectives());
    }

    @Override
    public ISteadyStateDecoder createDecoder(JecoliGenericConfiguration configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        Map<String,List<String>> reactionSwapMap  = configuration.getReactionSwapMap();
        ISteadyStateDecoder decoder = new ReactionSwapDualSetRepresentationDecoder(model,reactionSwapMap);
        return decoder;
    }

    public List<IntegerSetRepresentationFactory> getSwapsFactoryList(){
        return swapFactoryList;
    }
}
