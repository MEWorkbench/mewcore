package pt.uminho.ceb.biosystems.mew.core.utils;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;

public class FluxDistributionUtils {
	
	public static DistanceMetric DEFAULT_METRIC = DistanceMetric.SAD;
	
	public static double distanceBetweenTwoFluxDistributions(FluxValueMap fluxDist1, FluxValueMap fluxDist2, DistanceMetric metric){
		
		if(metric==null) metric = DEFAULT_METRIC;
				
		int n = fluxDist1.getReactionIds().size();
		
		double[] f1values = new double[n];
		double[] f2values = new double[n];
		
		int i=0;
		for(String id : fluxDist1.getReactionIds()){
			f1values[i] = fluxDist1.getValue(id);
			f2values[i] = fluxDist2.getValue(id);
			i++;
		}
				
		return metric.calculate(f1values, f2values);
	}
		
}
