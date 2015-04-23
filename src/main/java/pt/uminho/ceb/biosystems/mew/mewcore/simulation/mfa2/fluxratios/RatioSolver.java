package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.fluxratios;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.exceptions.InvalidExpressionException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.fluxratios.grammar.ValidateFluxRatios;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraint;


public class RatioSolver {
	
	public static final String PLUS_MINUS = "[\\+-]{1}";
	public static final String DOUBLE_VALUE = "[0-9]*\\.?[0-9]*";
	public static final String OPERATOR = "[<>=]+";
	public static final String FLUX_COEFF = "(" + PLUS_MINUS + DOUBLE_VALUE + ")([^\\+-]+)";
	
	
	public static Double solve(FluxRatioConstraint ratioConstraint, Map<String, Double> fluxValues) throws InvalidExpressionException{
		return solve(ratioConstraint.getExpression(), fluxValues);
	}
	
	public static Double solve(String exp, Map<String, Double> fluxValues) throws InvalidExpressionException{
		
		Pattern p = Pattern.compile(OPERATOR);
		Matcher m = p.matcher(exp);
	
		if(m.find())
		{
			String leftSide = exp.substring(0, m.start());
			leftSide = leftSide.replaceAll("\\s", "");
			String[] tokens = leftSide.split("/");
			
			if(tokens.length==2)
			{
				double numerator = solvePart(tokens[0], fluxValues);
				double denominator = solvePart(tokens[1], fluxValues);
				return numerator / denominator;
			}
		}
		throw new InvalidExpressionException("Invalid flux ratio expression: " + exp);		
	}
	
	public static double solvePart(String eqPart, Map<String, Double> fluxValues) throws InvalidExpressionException{
		if(!eqPart.startsWith("-"))
			eqPart = "+" + eqPart;
		Pattern p = Pattern.compile(FLUX_COEFF);
		Matcher m = p.matcher(eqPart);
		double value = 0;
		while(m.find())
		{
			Double s = null;
			Double fluxValue = null;
			if(m.group(1).equals("+"))
				s = 1d;
			else if(m.group(1).equals("-"))
				s = -1d;
			else
				try {
					s = Double.parseDouble(m.group(1));
				} catch (NumberFormatException e) {e.printStackTrace();}
			
			if(s==null)
				throw new InvalidExpressionException("Invalid flux stoichiometry: " + m.group(1) + m.group(2));
			
			if(fluxValues.containsKey(m.group(2)))
				fluxValue = fluxValues.get(m.group(2));
			else
				throw new InvalidExpressionException("Invalid flux in the expression: " + m.group(2));
			
			value += s * fluxValue;
		}
		return value;
	}

	
	public static void main(String[] args) {
		
		String s = " R_PPC / R_PPC - R_PPC <= 20000000";
		
		try {
			Map<String, Double> rs = ValidateFluxRatios.validate(s);
			for(String k : rs.keySet())
				System.out.println(k + " # " +  rs.get(k));
			System.out.println("VALID");
		} catch (InvalidExpressionException e1) {
			e1.printStackTrace();
		}
		
		Map<String, Double> fluxValues = new HashMap<String, Double>();
		
		fluxValues.put("R_PPC", 3d);
		fluxValues.put("R_MDH", 3d);
		fluxValues.put("R_MDH2", 2d);
		fluxValues.put("R_MDH3", 3d);
		
		try {
			double r = RatioSolver.solve(s, fluxValues);
			System.out.println(r);
		} catch (InvalidExpressionException e) {e.printStackTrace();}
	}
}
