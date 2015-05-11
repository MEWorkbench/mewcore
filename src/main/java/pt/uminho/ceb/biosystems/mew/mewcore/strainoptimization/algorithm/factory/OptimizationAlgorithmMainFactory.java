package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.algorithm.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.configuration.IGenericConfiguration;

/**
 * Created by ptiago on 23-02-2015.
 */
public class OptimizationAlgorithmMainFactory implements  IOptimizationAlgorithmFactory{
    protected List<IOptimizationAlgorithmFactory> optimizationAlgorithmFactoryList;

    //PRE 1 algoritmo apenas pode estar numa factory
    public OptimizationAlgorithmMainFactory() {
        optimizationAlgorithmFactoryList = new ArrayList<>();
        //optimizationAlgorithmFactoryList.add(new JecoliAlgorithmOptimizationFactory());
        //Adicionar factories
    }

    public void constructAlgorithm(String algorithm,IGenericConfiguration genericConfiguration) throws Exception {
        IOptimizationAlgorithmFactory factory = getAlgorithmFactory(algorithm);
        //return factory.constructAlgorithm(algorithm,genericConfiguration);
        factory.constructAlgorithm(algorithm,genericConfiguration);
    }

    private IOptimizationAlgorithmFactory getAlgorithmFactory(String algorithm) throws AlgorithmNotInFactoryException {
        for(IOptimizationAlgorithmFactory optimizationFactory:optimizationAlgorithmFactoryList)
            if(optimizationFactory.canConstructAlgorithm(algorithm))
                return optimizationFactory;
        throw new AlgorithmNotInFactoryException();
    }

    @Override
    public Set<String> getAvailableOptimizationAlgorithmSet() {
        Set<String> mainAlgorithmSet = new HashSet<>();
        for(IOptimizationAlgorithmFactory optimizationFactory:optimizationAlgorithmFactoryList)
            mainAlgorithmSet.addAll(optimizationFactory.getAvailableOptimizationAlgorithmSet());
        return mainAlgorithmSet;
    }

    @Override
    public boolean canConstructAlgorithm(String algorithm) {
        for(IOptimizationAlgorithmFactory optimizationFactory:optimizationAlgorithmFactoryList)
            if(optimizationFactory.canConstructAlgorithm(algorithm))
                return true;
        return false;
    }
}
