package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.RKDecoder;

/**
 * Created by ptiago on 19-03-2015.
 */
public class JecoliRKConverter<T extends JecoliGenericConfiguration> implements IJecoliOptimizationStrategyConverter<T,RKSolution> {
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
    public IStrainOptimizationResultSet<T,RKSolution> createSolutionSet(T configuration,List<RKSolution> solutionList) {
        return new RKSolutionSet<T>(configuration,solutionList);
    }

    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) {
        return new RKSolution(gc, simulations);
    }

    @Override
    public ISteadyStateDecoder createDecoder(T configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        List<String>  notAllowedIDs = configuration.getNonAllowedIds();
        ISteadyStateDecoder decoder = new RKDecoder(model);
        if(notAllowedIDs != null) decoder.addNotAllowedIds(notAllowedIDs);
        return decoder;
    }

    @Override
    public ISolutionFactory<?> createSolutionFactory(T configuration,ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction<?> evaluationFunction) {
        int maxSetValue = decoder.getNumberVariables();
        int maxSetSize = configuration.getMaxSetSize();
        return new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives());
    }
}
