package pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.dualset.DualSetRepresentation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.ReactionSwapData;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * 
 * 
 * 
 * @author pmaia
 * @date Jan 16, 2015
 * @version 0.1
 * @since metabolic3persistent
 */
public class ReactionSwapDualSetRepresentationDecoder extends SteadyStateKnockoutDecoder {
	
	private static final long					serialVersionUID	= 1L;
	public static final String					SWAP_DELIMITER		= "~";
	protected Map<String, List<String>>			_swapsMap			= null;
	protected Map<String, String>				_reverseSwapsMap	= null;
	protected Map<Integer, ReactionSwapData>	_reactionIndexMap	= null;
	
	public ReactionSwapDualSetRepresentationDecoder(ISteadyStateModel model, Map<String, List<String>> swapsMap) {
		super(model);
		_swapsMap = swapsMap;
		buildReverseSwapMap();
		buildIndexMap();
	}
	
	protected void buildReverseSwapMap() {
		_reverseSwapsMap = new HashMap<String, String>();
		for (Entry<String, List<String>> entry : _swapsMap.entrySet())
			for (String swap : entry.getValue())
				_reverseSwapsMap.put(swap, entry.getKey());
	}
	
	private void buildIndexMap() {
		_reactionIndexMap = new HashMap<Integer, ReactionSwapData>();
		int counter = 0;
		for (Map.Entry<String, List<String>> entry : _swapsMap.entrySet()) {
			String originalReactionId = entry.getKey();
			List<String> replacementReactionIdList = entry.getValue();
			for (String reactionId : replacementReactionIdList) {
				_reactionIndexMap.put(counter, new ReactionSwapData(originalReactionId, reactionId));
				counter += 1;
			}
		}
	}
	
	@Override
	public GeneticConditions decode(IRepresentation solutionRepresentation) throws Exception {
		/**
		 * deal with knockout set
		 */
		TreeSet<Integer> koSet = ((DualSetRepresentation) solutionRepresentation).getIndividualRepresentation(0);
		List<Integer> koList = super.decodeReactionKnockouts(koSet);
		ReactionChangesList rcl = new ReactionChangesList(koList, model);
		
		/**
		 * deal with swaps set
		 */
		Set<Integer> swapSet = ((DualSetRepresentation) solutionRepresentation).getIndividualRepresentation(1);
		Map<String, String> activeReactionMap = new HashMap<String, String>();
		for (Integer activeReactionIndex : swapSet) {
			ReactionSwapData activeReactionSwapData = _reactionIndexMap.get(activeReactionIndex); //devolve original e a para troca
			activeReactionMap.put(activeReactionSwapData.getOriginalReaction(), activeReactionSwapData.getSwapReaction());
		}
		
		Set<String> swapList = computeNonActiveReactionSet(activeReactionMap);
		for (String k : swapList)
			rcl.addReactionKnockout(k);
		
		GeneticConditions gc = new GeneticConditions(rcl, false);
		return gc;
	}
	
	public String toStringOptions(String innerDelimiter, String outterDelimiter, GeneticConditions gc, boolean excludeExpressionValue) {
		
		String toret = "";
		
		MapStringNum map = (gc.isGenes()) ? gc.getGeneList() : gc.getReactionList();
		
		Set<String> swaps = new HashSet<String>();
		Set<String> other = new HashSet<String>();
		
		for (String k : map.keySet()) {
			if (_swapsMap.containsKey(k)) {
				List<String> k_swaps = _swapsMap.get(k);
				Set<String> diff = CollectionUtils.getSetDiferenceValues(k_swaps, map.keySet());
				if(diff.size()>0){
					swaps.add(k + SWAP_DELIMITER + StringUtils.concat(SWAP_DELIMITER, diff));
				}
				else {
					String mod = k + ((excludeExpressionValue) ? "" : "=" + map.get(k));
					other.add(mod);
				}
			}else if(_reverseSwapsMap.containsKey(k)){
				String orig = _reverseSwapsMap.get(k);
				if(map.keySet().containsAll(_swapsMap.get(orig)));
			}
			else {
				String mod = k + ((excludeExpressionValue) ? "" : "=" + map.get(k));
				other.add(mod);
			}
		}
		
		if(other.size()>0)
			toret += StringUtils.concat(innerDelimiter, other);
		
		if(swaps.size()>0){
			if(other.size()>0) 
				toret+= outterDelimiter;			
			toret += StringUtils.concat(innerDelimiter, swaps);
		}
		
		return toret;
	}
	
	private Set<String> computeNonActiveReactionSet(Map<String, String> activeReactionMap) {
		Set<String> nonActiveReactionSet = new HashSet<>();
		
		for (Map.Entry<String, List<String>> entry : _swapsMap.entrySet()) {
			String originalReactionId = entry.getKey();
			List<String> alternativeReactionList = entry.getValue();
			String newActiveReaction = activeReactionMap.get(originalReactionId);
			
			if (newActiveReaction == null) newActiveReaction = originalReactionId;
			
			for (String nonActiveReactionId : alternativeReactionList)
				if (nonActiveReactionId.compareTo(newActiveReaction) != 0) nonActiveReactionSet.add(nonActiveReactionId);
			
		}
		
		return nonActiveReactionSet;
	}
	
	@Override
	public Object deepCopy() throws Exception {
		return null;
	}
	
	public static void main(String[] args) {
		Set<String> set1 = new HashSet<String>();
		set1.add("a");
		set1.add("b");
		
		Set<String> set2 = new HashSet<String>();
		set2.add("a");
		set2.add("c");
		set2.add("d");
		
		System.out.println(CollectionUtils.getSetDiferenceValues(set1, set2));
	}
}
