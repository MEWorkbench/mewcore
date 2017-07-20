package pt.uminho.ceb.biosystems.mew.core.simplification.solutions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class ReactionsSwapsSimplifier extends ReactionsSimplifier {
	
	protected Map<String, List<String>>	swapsMap	= null;
	protected Map<String, String>		reverseMap	= null;
	protected Set<String>				diff		= null;
													
	public ReactionsSwapsSimplifier(Map<String, Map<String, Object>> simulationConfiguration, Map<String, List<String>> swapsMap) {
		super(simulationConfiguration);
		this.swapsMap = swapsMap;
		buildReverseMap();
		
//		System.out.println("========[ SWAPS MAP ]========");
//		MapUtils.prettyPrint(swapsMap);
//		
//		System.out.println("========[ REVERSE MAP ]========");
//		MapUtils.prettyPrint(reverseMap);
	}
	
	private void buildReverseMap() {
		reverseMap = new HashMap<>();
		for (String id : swapsMap.keySet()) {
			for (String swap : swapsMap.get(id)) {
				if (!swap.equals(id)) {
					reverseMap.put(swap, id);
				}
			}
		}
	}
	
	@Override
	public void removeGeneticCondition(GeneticConditions gc, String id) {
		if (!reverseMap.containsKey(id)) {
			if (swapsMap.containsKey(id)) {
				List<String> swaps = swapsMap.get(id);
				diff = CollectionUtils.getSetDiferenceValues(swaps, gc.getReactionList().keySet());
				for (String d : diff) {
					gc.getReactionList().addReaction(d, 0.0);
//					System.out.println("\t\tRGC-ADD: [" + d + "]");
				}
			}
			gc.getReactionList().removeReaction(id);
//			System.out.println("\t\tRGC-REM: [" + id + "]");
		}
	}
	
	@Override
	public void nextGeneticCondition(GeneticConditions gc, String id, double expressionLevel) {
		if (!reverseMap.containsKey(id)) {
			if (swapsMap.containsKey(id)) {
				for (String d : diff) {
					gc.getReactionList().removeReaction(d);
//					System.out.println("\t\tNGC-REM: [" + d + "]");
				}
			}
			gc.getReactionList().addReaction(id, 0.0);
//			System.out.println("\t\tNGC-ADD: [" + id + "]");
		}
	}
	
}
