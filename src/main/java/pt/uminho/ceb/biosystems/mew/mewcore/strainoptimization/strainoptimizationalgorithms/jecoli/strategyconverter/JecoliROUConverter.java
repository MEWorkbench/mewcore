package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.IntIntHybridSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.SteadyStateKnockoutDecoder;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder.SteadyStateUnderOverExp2Decoder;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 19-03-2015.
 */
public class JecoliROUConverter <T extends JecoliGenericConfiguration> implements IJecoliOptimizationStrategyConverter<T,ROUSolution> {
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
    public IStrainOptimizationResultSet<T,ROUSolution> createSolutionSet(T configuration,List<ROUSolution> solutionList) {
        return new ROUSolutionSet<T>(configuration,solutionList);
    }

    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) {
        return new ROUSolution(configuration,simulations,gc);
    }

    @Override
    public ISteadyStateDecoder createDecoder(T configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        List<String>  notAllowedIDs = configuration.getNonAllowedIds();
        ISteadyStateDecoder decoder = new SteadyStateKnockoutDecoder(model);;
        decoder.addNotAllowedIds(notAllowedIDs);
        return decoder;
    }

    @Override
    public ISolutionFactory createSolutionFactory(T configuration, ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction evaluationFunction) {
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
            ((SteadyStateUnderOverExp2Decoder) decoder).setSpecialIndex(nmin);
        }
        return new IntIntHybridSetRepresentationFactory(minSetSize, maxSetSize, maxSetValue, nmin, nmax, evaluationFunction.getNumberOfObjectives());
    }
}

