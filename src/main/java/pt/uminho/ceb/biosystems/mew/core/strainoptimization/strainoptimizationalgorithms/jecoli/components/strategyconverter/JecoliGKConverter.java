package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.integer.IntegerSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GKSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.GKDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;

/**
 * Created by ptiago on 19-03-2015.
 */
public class JecoliGKConverter <T extends JecoliGenericConfiguration> implements IJecoliOptimizationStrategyConverter<T,GKSolution> {
	
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
    public IStrainOptimizationResultSet<T,GKSolution> createSolutionSet(T configuration,List<GKSolution> solutionList) {
        return new GKSolutionSet<T>(configuration,solutionList);
    }

    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc) {
        return new GKSolution(gc, simulations);
    }

    @Override
    public ISteadyStateDecoder createDecoder(T configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        List<String>  notAllowedIDs = configuration.getNonAllowedIds();
        ISteadyStateDecoder decoder =  new GKDecoder((ISteadyStateGeneReactionModel) model);
        if(notAllowedIDs != null) decoder.addNotAllowedIds(notAllowedIDs);
        return decoder;
    }

    @Override
    public ISolutionFactory<?> createSolutionFactory(T configuration, ISteadyStateDecoder decoder, AbstractMultiobjectiveEvaluationFunction<?> evaluationFunction) {
        int maxSetValue = decoder.getNumberVariables();
        int maxSetSize = configuration.getMaxSetSize();
        return new IntegerSetRepresentationFactory(maxSetValue, maxSetSize, evaluationFunction.getNumberOfObjectives());
    }
}