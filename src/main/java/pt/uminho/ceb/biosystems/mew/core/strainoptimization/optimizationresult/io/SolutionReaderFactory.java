package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gk.GKStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gou.GOUStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rk.RKStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rkrs.RKRSStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rou.ROUStrategyReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

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
	
	public IStrainOptimizationReader getReaderInstance(String id, ISteadyStateGeneReactionModel model) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationReader> ofKlazz = readers.get(id);		
		IStrainOptimizationReader instance = ofKlazz.getConstructor(ISteadyStateGeneReactionModel.class).newInstance(model);
		return instance;
	}
	
	public IStrainOptimizationReader getReaderInstance(String id) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends IStrainOptimizationReader> ofKlazz = readers.get(id);
		IStrainOptimizationReader instance = ofKlazz.getConstructor().newInstance();
		return instance;
	}
	
	public IStrainOptimizationReader getReaderInstance(JecoliGenericConfiguration configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String strategy = configuration.getOptimizationStrategy();
		Class<? extends IStrainOptimizationReader> ofKlazz = readers.get(strategy);
		
		IStrainOptimizationReader instance = null;
		if(configuration.getIsGeneOptimization()){
			ISteadyStateGeneReactionModel model = configuration.getGeneReactionSteadyStateModel();
			instance = ofKlazz.getConstructor(ISteadyStateGeneReactionModel.class).newInstance(model);
		}else{			
			instance = ofKlazz.getConstructor().newInstance();
		}
		
		return instance;
	}
	
}
