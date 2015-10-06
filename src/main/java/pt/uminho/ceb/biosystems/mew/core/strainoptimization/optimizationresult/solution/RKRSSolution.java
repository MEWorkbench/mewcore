package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * Created by ptiago on 23-03-2015.
 */
public class RKRSSolution extends AbstractSolution {
	
	private static final long			serialVersionUID	= 1L;
	private Map<String, List<String>>	swapsMap;
	
	public RKRSSolution(GeneticConditions solutionGeneticConditions, Map<String, List<String>> swapsMap) {
		this(solutionGeneticConditions, swapsMap, new HashMap<String, SteadyStateSimulationResult>(),null);
	}
	
//	public RKRSSolution(GeneticConditions solutionGeneticConditions, Map<String, List<String>> swapsMap, Map<String, SteadyStateSimulationResult> simulationResultMap) {
//		super(solutionGeneticConditions, simulationResultMap);
//		this.swapsMap = swapsMap;
//	}
	
	public RKRSSolution(GeneticConditions solutionGeneticConditions, Map<String, List<String>> swapsMap, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> fitnesses) {
        super(solutionGeneticConditions, simulationResultMap,fitnesses);
        this.swapsMap = swapsMap;
    }
	
	@Override
	public void write(OutputStreamWriter outputStream) throws Exception {
		ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
		List<String> reactionKnockoutList = reactionChangeList.getReactionKnockoutList();
		
		if (fitnesses != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, fitnesses);
			outputStream.write(fitString);
			outputStream.write(INNER_DELIMITER);
        }else{
        	outputStream.write(OUTTER_DELIMITER);
        }
		
		for (String reactionKnockout : reactionKnockoutList) {
			outputStream.write(INNER_DELIMITER + reactionKnockout);
		}
		
	}
	
	public List<Pair<String, String>> getReactionSwapList(Map<String, List<String>> swapMap, GeneticConditions gc) {
		List<Pair<String, String>> swapList = new ArrayList<>();
		
		MapStringNum map = (gc.isGenes()) ? gc.getGeneList() : gc.getReactionList();
		
		for (String k : map.keySet()) {
			if (swapMap.containsKey(k)) {
				List<String> k_swaps = swapMap.get(k);
				Set<String> diff = CollectionUtils.getSetDiferenceValues(k_swaps, map.keySet());
				if (diff.size() > 0) {
					for (String reactionId : diff)
						swapList.add(new Pair<String, String>(k, reactionId));
				}
			}
		}
		
		return swapList;
	}
	
	public List<Pair<String, String>> getReactionSwapList() {
		return getReactionSwapList(swapsMap, solutionGeneticConditions);
	}
	
	public Set<String> getKnockoutSet() {
		Set<String> knockoutSet = new HashSet<>();
		MapStringNum map = (solutionGeneticConditions.isGenes()) ? solutionGeneticConditions.getGeneList() : solutionGeneticConditions.getReactionList();
		for (String k : map.keySet()) {
			if (!swapsMap.containsKey(k)) {
				boolean isKeyOnSwap = false;
				for (Map.Entry<String, List<String>> swapEntry : swapsMap.entrySet())
					for (String swapReaction : swapEntry.getValue()) {
						if (isKeyOnSwap) break;
						
						if (swapReaction.compareTo(k) == 0) {
							isKeyOnSwap = true;
							break;
						}
					}
				if (!isKeyOnSwap) knockoutSet.add(k);
			}
		}
		return knockoutSet;
	}
	
	public Map<String, List<String>> getSwapsMap() {
		return swapsMap;
	}
	
}