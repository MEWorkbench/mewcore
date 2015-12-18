package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;

public class ResultSetFactory {

protected static Map<String, Class<? extends IStrainOptimizationResultSet>> solutionSets;
	
	static {
		Map<String, Class<? extends IStrainOptimizationResultSet>> mySolutionSets = new HashMap<>();
		mySolutionSets.put("RK", RKSolutionSet.class);
		mySolutionSets.put("GK", GKSolutionSet.class);
		mySolutionSets.put("ROU", ROUSolutionSet.class);
		mySolutionSets.put("GOU", GOUSolutionSet.class);
		mySolutionSets.put("RKRS", RKRSSolutionSet.class);
		solutionSets = Collections.unmodifiableMap(mySolutionSets);
	}
	
	public ResultSetFactory() {
		super();
	}
	
	public void registerSolutionSet(String id, Class<? extends IStrainOptimizationResultSet> solutionSetClass) {
		registerSolutionSet(id, solutionSetClass, false);
	}

	public void registerSolutionSet(String id, Class<? extends IStrainOptimizationResultSet> solutionSetClass, boolean override) {
		if (solutionSets.containsKey(id) && !override) {
			throw new IllegalStateException("SolutionSet [" + solutionSets.get(id).getName() + "] is already registered for strategy [" + id + "].");
		} else {
			solutionSets.put(id, solutionSetClass);
		}
	}
	
	public void unregisterSolutionSet(String id) {
		solutionSets.remove(id);
	}
	
	public Set<String> getRegisteredSolutionSets() {
		LinkedHashSet<String> setSolutionSets = new LinkedHashSet<String>();
		for (String simplifierID : solutionSets.keySet())
			setSolutionSets.add(simplifierID);
			
		return setSolutionSets;
	}
	
	public <C extends IGenericConfiguration> IStrainOptimizationResultSet getResultSetInstance(String id, C configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationResultSet> ofKlazz = solutionSets.get(id);
		IStrainOptimizationResultSet instance = ofKlazz.getConstructor(IGenericConfiguration.class).newInstance(configuration);
		return instance;
	}
	
}
