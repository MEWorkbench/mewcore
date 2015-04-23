package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;


/** Map<Flux Id, <Value, Confidence Interval>> */
public class ExpMeasuredFluxes extends LinkedHashMap<String, Pair<Double, Double>>{

	private static final long serialVersionUID = 2250514675456568270L;
	
	public ExpMeasuredFluxes(){
		super();
	}

	public void put(String fluxId, Double fluxValue, Double stdev){
		put(fluxId, new Pair<Double, Double>(fluxValue, stdev));
	}
	
	public Double getFluxValue(String fluxId){
		return (containsKey(fluxId)) ? get(fluxId).getA() : null;
	}
	
	/** @return [0] the lower bound and [1] the upper bound of the measured flux. If there is no margin of error the bounds are equal to the measured value */
	public Double[] getFluxValueBounds(String fluxId){
		if(!containsKey(fluxId))
			return null;
		
		double value = get(fluxId).getA();
		double margin = (get(fluxId).getB()==null) ? 0 : get(fluxId).getB();
		
		return new Double[]{value-margin, value+margin};
	}
	
	public Double getFluxConfInterval(String fluxId){
		return (containsKey(fluxId)) ? get(fluxId).getB() : null;
	}
	
	/** @return [0] mean, [1] standard deviation */
	private double[] calculateMeanStd(String[] values){
		// Skip the first position
		
		int n = values.length-1;
		double sum = 0;
		
		for(int i=1; i<=n; i++)
			sum += Double.valueOf(values[i]);
		
		double mean = sum / n;
		sum = 0;
		
		for(int i=1; i<=n; i++)
		{
			double v = Double.valueOf(values[i]);
			sum += Math.pow(v-mean, 2);
		}
		
		return new double[]{mean, Math.sqrt(sum/n)};
	}
	
	public HashMap<Integer,Double> getIndexesFluxeValuesMap (ISteadyStateModel model) throws Exception
	{
		HashMap<Integer, Double> res = new HashMap<Integer, Double>();
		for(String id: keySet())
		{
			int idx = model.getReactionIndex(id);
			res.put(idx, getFluxValue(id));
		}
		return res;
	}
	
	public Map<Integer,Pair<Double, Double>> getIndexesFluxeValuesErrorMap (ISteadyStateModel model) throws Exception
	{
		HashMap<Integer, Pair<Double, Double>> res = new HashMap<Integer, Pair<Double, Double>>();
		for(String id: keySet())
		{
			int idx = model.getReactionIndex(id);
			res.put(idx, get(id));
		}
		return res;
	}
	
	public Set<String> getFluxIds(){
		return this.keySet();
	}
	
	/** @param isFluxValueList if each row contains a list of measurements for the corresponding reaction, this parameter should be true, 
	 * otherwise, i.e., each row contains the flux value and it's variance, this parameter should be false */
	public void loadFromFile(File file, String columnSeparator, boolean isFluxValueList) throws Exception{
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		String line=null;
		
		while((line = br.readLine()) != null)
		{
			if(!line.equals("")) // Ignore empty lines
			{
				if(!line.contains(columnSeparator))
					throw new Exception("Invalid Format! Delimiter ["+columnSeparator+"] expected in line ["+line+"]");
				
				String[] tokens = line.split(columnSeparator);
				
				String fluxId = tokens[0];
				
				if(isFluxValueList)
				{
					double[] meanStd = calculateMeanStd(tokens);
					put(fluxId, meanStd[0], meanStd[1]);
				}
				else
				{
					Double measuredValue = Double.valueOf(tokens[1]);
					Double std = (tokens.length>2) ? Double.valueOf(tokens[2]) : null;
					put(fluxId, measuredValue, std);
				}
	
				
			}
		}
		
		br.close();
		fr.close();
	}
}
