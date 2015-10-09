package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.IntIntHybridSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GOUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GOUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.GOUDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 19-03-2015.
 */
public class JecoliGOUConverter <T extends JecoliGenericConfiguration> implements IJecoliOptimizationStrategyConverter<T,GOUSolution> {
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
    public IStrainOptimizationResultSet<T,GOUSolution> createSolutionSet(T configuration,List<GOUSolution> solutionList) {
        return new GOUSolutionSet<T>(configuration,solutionList);
    }

    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) {
        return createSolution(configuration, simulations, gc,null);
    }
    
    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc,List<Double> fitnesses) {
        return new GOUSolution(gc, simulations,fitnesses);
    }

    @Override
    public ISteadyStateDecoder createDecoder(T configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        List<String>  notAllowedIDs = configuration.getNonAllowedIds();
        ISteadyStateDecoder decoder =  new GOUDecoder((ISteadyStateGeneReactionModel) model);
        if(notAllowedIDs != null) decoder.addNotAllowedIds(notAllowedIDs);
        return decoder;
    }

    @Override
    public ISolutionFactory<?> createSolutionFactory(T configuration, ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction<?> evaluationFunction) {
        int minSetSize = 1;
        int maxSetValue = decoder.getNumberVariables();
        int maxSetSize = configuration.getMaxSetSize();
        int n = 5;
        int nmin = -(n + 1);
        int nmax = n;
        Pair<Integer,Integer> ouRange = configuration.getOURange();
        if (ouRange != null) {
            nmin = ouRange.getA();
            nmax = ouRange.getB();
            ((GOUDecoder) decoder).setSpecialIndex(nmin);
        }
        return new IntIntHybridSetRepresentationFactory(minSetSize, maxSetSize, maxSetValue, nmin, nmax, evaluationFunction.getNumberOfObjectives());
    }
}
