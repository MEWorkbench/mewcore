package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;

public class SolutionFactory<T extends IStrainOptimizationResult> {

	static public <T extends IStrainOptimizationResult> T getInstance(Class<T> klazz, GeneticConditions conditions, Map<String, SteadyStateSimulationResult> results) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException, NoSuchMethodException, SecurityException {
		T instance = klazz.getDeclaredConstructor(GeneticConditions.class, Map.class).newInstance(conditions, results);
		return instance;
	}
}
