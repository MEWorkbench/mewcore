package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gk.GKStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gou.GOUStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rk.RKStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rkrs.RKRSStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rou.ROUStrategyReader;

public class SolutionReaderFactory {
	
	protected static Map<String, Class<? extends IStrainOptimizationReader>> readers;
	
	static {
		Map<String, Class<? extends IStrainOptimizationReader>> myreaders = new HashMap<>();
		myreaders.put("RK", RKStrategyReader.class);
		myreaders.put("GK", GKStrategyReader.class);
		myreaders.put("ROU", ROUStrategyReader.class);
		myreaders.put("GOU", GOUStrategyReader.class);
		myreaders.put("RKRS", RKRSStrategyReader.class);
		readers = Collections.unmodifiableMap(myreaders);
	}
	
	public SolutionReaderFactory() {
		super();
	}
	
	public void registerStrategyReader(String id, Class<? extends IStrainOptimizationReader> readerClass) {
		registerStrategyReader(id, readerClass, false);
	}
	
	public void registerStrategyReader(String id, Class<? extends IStrainOptimizationReader> readerClass, boolean override) {
		if (readers.containsKey(id) && !override) {
			throw new IllegalStateException("Reader [" + readers.get(id).getName() + "] is already registered for strategy [" + id + "].");
		} else {
			readers.put(id, readerClass);
		}
	}
	
	public void unregisterStrategyReader(String id) {
		readers.remove(id);
	}
	
	public Set<String> getRegisteredReaders() {
		LinkedHashSet<String> setReaders = new LinkedHashSet<String>();
		for (String readerID : readers.keySet())
			setReaders.add(readerID);
			
		return setReaders;
	}
	
	public IStrainOptimizationReader getReaderInstance(String id, ISteadyStateModel model) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationReader> ofKlazz = readers.get(id);		
		IStrainOptimizationReader instance = ofKlazz.getConstructor(ISteadyStateModel.class).newInstance(model);
		return instance;
	}
	
	public IStrainOptimizationReader getReaderInstance(String id) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationReader> ofKlazz = readers.get(id);
		IStrainOptimizationReader instance = ofKlazz.getConstructor().newInstance();
		return instance;
	}
	
}
