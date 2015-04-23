package pt.uminho.ceb.biosystems.mew.mewcore.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringBoolean;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;

public class BinaryFluxValueMap extends MapStringBoolean {
	
	private static final long serialVersionUID = 1L;
	public static final double THRESHOLD = 0.0;
	
	public BinaryFluxValueMap ()
	{
		super();
	}
	
	public BinaryFluxValueMap (FluxValueMap fluxValues){
		this();
		for(Pair<String,Double> pair: fluxValues.getListFluxes()){
			boolean bin = pair.getPairValue()!=0.0;
			this.put(pair.getValue(), bin);
		}		
	}

	public Boolean getValue (String id){
		return get(id);
	}
	
	public void setValue (String id, boolean value){
		put(id, value);
	}
	
	public boolean hasReactionId (String reactionId){
		return this.containsKey(reactionId);
	}
	
	public Set<String> getReactionIds (){
		return keySet();
	}
	
	public List<Boolean> getFluxList(){
		return new ArrayList<Boolean>(values());
	}
	
	public List<Pair<String,Boolean>> getListFluxes (){
		
		List<Pair<String,Boolean>> res = new ArrayList<Pair<String,Boolean>>();
		for(String id: keySet())
			res.add(new Pair<String, Boolean>(id, getValue(id)));
		
		return res;
	}
	
	public HashMap<Integer,Boolean> getIndexesMap (ISteadyStateModel model) throws Exception
	{
		HashMap<Integer, Boolean> res = new HashMap<Integer, Boolean>();
		for(String id: keySet()){
			int idx = model.getReactionIndex(id);
			res.put(idx, getValue(id));
		}
		return res;
	}
	
	public List<Integer> getIndexesList (ISteadyStateModel model) throws Exception
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		for(String id: keySet())
		{
			int idx = model.getReactionIndex(id);
			res.add(idx);
		}
		Collections.sort(res);
		return res;
	}
}
