package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult;

import java.io.DataInputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.gk.GKStrategyReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.gou.GOUStrategyReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rk.RKStrategyReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rkrs.RKRSStrategyReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rou.ROUStrategyReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class SolutionReaderFactory implements Serializable{
	
	private static final long	serialVersionUID	= -1080622571772060722L;
	
	protected Map<String,IStrainOptimizationReader> readerMap;

    public SolutionReaderFactory() {
        readerMap = new HashMap<>();
        readerMap.put("RK",new RKStrategyReader());
        readerMap.put("GK",new GKStrategyReader());
        readerMap.put("ROU",new ROUStrategyReader());
        readerMap.put("GOU",new GOUStrategyReader());
        readerMap.put("RKRS",new RKRSStrategyReader());
    }

    public <E extends IJecoliConfiguration> GeneticConditions getSolution(E baseConfiguration, String line) throws Exception {
        String strategyId = baseConfiguration.getOptimizationStrategy();
        IStrainOptimizationReader reader = readerMap.get(strategyId);
        return reader.readSolutionFromStream(new DataInputStream(new StringBufferInputStream(line)),baseConfiguration);
    }

    public void register(String strategyId, IStrainOptimizationReader reader){
        readerMap.put(strategyId,reader);
    }

    public void unRegister(String strategyId) {
        readerMap.remove(strategyId);
    }
}
