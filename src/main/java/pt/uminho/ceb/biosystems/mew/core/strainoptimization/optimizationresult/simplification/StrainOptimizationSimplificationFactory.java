package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGeneSteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.ISteadyStateConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.GKSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.GOUSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.RKRSSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.RKSolutionSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.simplifiers.ROUSolutionSimplifier;

public class StrainOptimizationSimplificationFactory {

protected static Map<String, Class<? extends IStrainOptimizationResultsSimplifier>> simplifiers;
	
	static {
		Map<String, Class<? extends IStrainOptimizationResultsSimplifier>> mysimplifiers = new HashMap<>();
		mysimplifiers.put("RK", RKSolutionSimplifier.class);
		mysimplifiers.put("GK", GKSolutionSimplifier.class);
		mysimplifiers.put("ROU", ROUSolutionSimplifier.class);
		mysimplifiers.put("GOU", GOUSolutionSimplifier.class);
		mysimplifiers.put("RKRS", RKRSSolutionSimplifier.class);
		simplifiers = Collections.unmodifiableMap(mysimplifiers);
	}
	
	public StrainOptimizationSimplificationFactory() {
		super();
	}
	
	public void registerSimplier(String id, Class<? extends IStrainOptimizationResultsSimplifier> simplifierClass) {
		registerSimplifier(id, simplifierClass, false);
	}
	
	public void registerSimplifier(String id, Class<? extends IStrainOptimizationResultsSimplifier> simplifierClass, boolean override) {
		if (simplifiers.containsKey(id) && !override) {
			throw new IllegalStateException("Simplifier [" + simplifiers.get(id).getName() + "] is already registered for strategy [" + id + "].");
		} else {
			simplifiers.put(id, simplifierClass);
		}
	}
	
	public void unregisterSimplifier(String id) {
		simplifiers.remove(id);
	}
	
	public Set<String> getRegisteredSimplifiers() {
		LinkedHashSet<String> setSimplifiers = new LinkedHashSet<String>();
		for (String simplifierID : simplifiers.keySet())
			setSimplifiers.add(simplifierID);
			
		return setSimplifiers;
	}
	
	public <T extends ISteadyStateConfiguration> IStrainOptimizationResultsSimplifier getSimplifierInstance(String id, T configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationResultsSimplifier> ofKlazz = simplifiers.get(id);
		IStrainOptimizationResultsSimplifier instance = ofKlazz.getConstructor(ISteadyStateConfiguration.class).newInstance(configuration);
		return instance;
	}	
	
}
