package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.utils;

import java.math.BigDecimal;

public class MathUtils {
	
	public static double roundValue(double value, int decimalPlaces){	 
		BigDecimal bd = new BigDecimal(value);  
		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);  
		return bd.doubleValue();  
	}

}
