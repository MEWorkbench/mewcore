package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPMapVariableValues;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class FluxValueMap extends MapStringNum implements Serializable {
	
	private static final long serialVersionUID = 1L;

	
	public FluxValueMap()
	{
		super();
	}
	
	public FluxValueMap (LPMapVariableValues values, ISteadyStateModel model)
	{
		super();
		for(int index : values.getIndexes())
			this.put(model.getReactionId(index), values.get(index));
	}

	public Double getValue (String id)
	{
		return get(id);
	}
	
	public void setValue (String id, double value)
	{
		put(id, value);
	}
	
	public boolean hasReactionId (String reactionId)
	{
		return this.containsKey(reactionId);
	}
	
	public Set<String> getReactionIds ()
	{
		return keySet();
	}
	
	public List<Double> getFluxList()
	{
		return new ArrayList<Double>(values());
	}
	
	public List<Pair<String,Double>> getListFluxes ()
	{
		List<Pair<String,Double>> res = new ArrayList<Pair<String,Double>>();
		for(String id: keySet())
		{
			res.add(new Pair<String, Double>(id, getValue(id)));
		}
		return res;
	}
	
	public HashMap<Integer,Double> getIndexesMap (ISteadyStateModel model) throws Exception
	{
		HashMap<Integer, Double> res = new HashMap<Integer, Double>();
		for(String id: keySet())
		{
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
	
	public Pair<List<Integer>,List<String>> getIndexesListAndUnexistentIDs(ISteadyStateModel model) throws Exception{
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<String> unexistent = new ArrayList<String>();
		for(String id: keySet())
		{
			int idx = model.getReactionIndex(id);
			if(idx!=-1)
				indexes.add(idx);
			else
				unexistent.add(id);
		}
		Collections.sort(indexes);
		return new Pair<List<Integer>,List<String>>(indexes,unexistent);
	} 
	
	public static FluxValueMap loadFromFile(String fileIn,String delimiter) throws IOException{
		FileReader fr = new FileReader(fileIn);
		BufferedReader br = new BufferedReader(fr);
		FluxValueMap map = new FluxValueMap();
		int lineCounter = 0;
		while(br.ready()){
			String line = br.readLine();
			String[] tokens = line.split(delimiter);
			if(tokens.length!=2){
				br.close();
				throw new IOException("Invalid format at line "+lineCounter+". Input must contain only two columns.");
			}
			else{
				String id = tokens[0];
				Double value = null;
				try{
					value = Double.parseDouble(tokens[1]);
				}catch(NumberFormatException e){
					br.close();
					throw new IOException("Invalid format at line "+lineCounter+". Expecting real value at second column but found "+tokens[1]+".");
				}
				
				map.put(id, value);
			}
			lineCounter++;
		}
		br.close();
		
		return map;
	}
	
	public void saveToFile(String fileOut, String delimiter) throws IOException{
		FileWriter fw = new FileWriter(fileOut);
		BufferedWriter bw = new BufferedWriter(fw);
		int lineCounter = 0;
		for(String id : keySet()){
			bw.append(id+delimiter+get(id));
			if(lineCounter < size())
				bw.newLine();
			lineCounter++;
		}
		
		bw.flush();
		bw.close();		
	}
}


