package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class TurnOverProperties {

	/**
	 * Identify the turnover reference
	 */
	public static final String TURNOVER_WT_REFERENCE = "TURNOVER_WT_REFERENCE";
	
	/**
	 * Identify the turnover solution
	 */
	public static final String TURNOVER_MAP_SOLUTION = "TURNOVER_MAP_SOLUTION";
	
	/**
	 * Identify the flux distribution for the first optimization
	 */
	public static final String MIMBL_FIRST_OPTIMIZARION_FLUXVALUE = "MIMBL_FIRST_OPTIMIZARION_FLUXVALUE";
	
	/**
	 * Identify MIMBL Problem
	 */
	public static final String MIMBL = "MIMBL";

	public static final String USE_2OPT = "USE_2OPT";
	
	
//	public static Map<String, Double> getTurnOverCalculation2(ISteadyStateModel model, Map<String, Double> fluxes){
//		
//	}
	
	
	public static Map<String, Double> getTurnOverCalculation(ISteadyStateModel model, Map<String, Double> fluxes){
		
//		System.out.println("Warnig");
		Map<String, Double> ret = new HashMap<String, Double>();		
		
		for(int i =0 ; i < model.getNumberOfMetabolites(); i++){
			
			String metId = model.getMetaboliteId(i);
			double turnover = 0;
			try {
				turnover = getTurnOverMetabolite(model, fluxes, metId);
			} catch (NonExistentIdException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret.put(metId, turnover);
		}
		
		return ret;
	}
	
	
	private static Double getTurnOverMetabolite(ISteadyStateModel model, Map<String, Double> fluxes, String metabolite_id) throws NonExistentIdException {
		double result =0.0;
		
		int met_idx = model.getMetaboliteIndex(metabolite_id);
		
		for(int i =0; i < fluxes.size(); i++){
			
			String r = model.getReactionId(i);
			
			double stoiq_val = model.getStoichiometricValue(met_idx, i);
			double sim_value = fluxes.get(r);
			
			double valueToCompare = sim_value *stoiq_val;
			
			if(valueToCompare>0)
				result+=valueToCompare;
			
		}
		return result;
			
	}
	
//	private static Boolean isReactionSameDirectionFlux(Double value, int reaction_idx, double flag){
//		
//		boolean ret = (flag > 0 && value > 0) || (flag < 0 && value <0);
//		return ret;
//	}
}
