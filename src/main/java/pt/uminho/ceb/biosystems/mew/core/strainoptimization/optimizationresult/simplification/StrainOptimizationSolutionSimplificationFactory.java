package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.GKSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.GOUSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.RKRSSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.RKSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.ROUSolutionSimplifier;

public class StrainOptimizationSolutionSimplificationFactory {
	
	protected Map<String, Class> simplifiersMap;
	
	public StrainOptimizationSolutionSimplificationFactory() {
		simplifiersMap = new HashMap<>();
		simplifiersMap.put("RK", 	RKSolutionSimplifier.class);
		simplifiersMap.put("GK",	GKSolutionSimplifier.class);
		simplifiersMap.put("ROU",	ROUSolutionSimplifier.class);
		simplifiersMap.put("GOU",	GOUSolutionSimplifier.class);
		simplifiersMap.put("RKRS",	RKRSSolutionSimplifier.class);
    }
	
	public void register(String strategyId, IStrainOptimizationSolutionSimplifier simplifier){
		simplifiersMap.put(strategyId, simplifier.getClass());
    }

    public void unregister(String strategyId) {
    	simplifiersMap.remove(strategyId);
    }
    
    public IStrainOptimizationSolutionSimplifier getSolutionSimplifier(String strategyId){
    	return simplifiersMap.get(strategyId).getClass();
    }
    
}
