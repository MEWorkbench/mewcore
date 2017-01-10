package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jfree.util.ArrayUtilities;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.IntIntHybridSetRepresentationFactory;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.ISolutionFactory;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ROUDecoder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.list.ListUtilities;
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
        return createSolution(configuration, simulations, gc,null);
    }
    
    @Override
    public IStrainOptimizationResult createSolution(T configuration, Map<String, SteadyStateSimulationResult> simulations, GeneticConditions gc, List<Double> fitnesses) {
        return new ROUSolution(gc, simulations, fitnesses);
    }

    @Override
    public ISteadyStateDecoder createDecoder(T configuration) throws Exception {
        ISteadyStateModel model = configuration.getSteadyStateModel();
        List<String>  notAllowedIDs = configuration.getNonAllowedIds();
        ISteadyStateDecoder decoder = new ROUDecoder(model);
        if(notAllowedIDs != null) {
        	decoder.addNotAllowedIds(notAllowedIDs);
        }

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
        Boolean negativeAllowed = configuration.getOUnegativeAllowed();
        Integer exponentBase = configuration.getOUexponentBase();
        
        if(exponentBase!=null){
        	((ROUDecoder) decoder).setExponentBase(exponentBase);
        }
        
        if (ouRange != null) {
            nmin = ouRange.getA();
            nmax = ouRange.getB();
            ((ROUDecoder) decoder).setSpecialIndex(nmin);
        }
        
        if(negativeAllowed){
        	int maxIndex = ((nmax-nmin)*2);
        	double[] range = new double[maxIndex+1];
        	
        	int count = 0;
        	for(int i=nmin; i<=nmax; i++){
        		double v = ((ROUDecoder) decoder).convertExpressionValue(i);
        		range[count] = v;
        		if(v!=0.0){
        			range[count+1] = -v;
        			count++;
        		}
        		count++;
        	}
        	((ROUDecoder) decoder).setFixedRange(range);
        	nmin = 0;        	
        	nmax = maxIndex;
        }
        
        return new IntIntHybridSetRepresentationFactory(minSetSize, maxSetSize, maxSetValue, nmin, nmax, evaluationFunction.getNumberOfObjectives());
    }        
}

