package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.tdps;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;

public class SolveContraint {
	
	static double epsilon = 1e-9;
	
	public static boolean solveContraint(LPConstraint constraint, Map<Integer,Double> values){
		
		double sum = 0.0;
		for(Integer idx : constraint.getLeftSide().getVarIdxs()){
			Double coeff = constraint.getLeftSide().getTermCoefficient(idx);
			System.out.println(idx);
			Double value = values.get(idx);
//			System.out.println(v);
			sum += (coeff * value);
		}
		
		double rhs = constraint.getRightSide();
		
		switch(constraint.getType()){	
			case EQUALITY:
				return !(Math.abs(sum-rhs) <= epsilon);
			case GREATER_THAN:				
				return !(sum +  epsilon >= rhs);
			case LESS_THAN:
				return !(sum - epsilon <= rhs);
			default:
				return false;
		}				
	}	
	
	public static String constraintString(LPConstraint constraint, Map<Integer,Double> values){
		
		double sum = 0.0;
		String str = "";
		for(Integer idx : constraint.getLeftSide().getVarIdxs()){
			Double coeff = constraint.getLeftSide().getTermCoefficient(idx);
			
			Double value = values.get(idx);
			sum += (coeff * value);
			if(!str.isEmpty())
				str+=" + ";
			str += ("("+coeff +"*"+value+")");
		}
		
		double rhs = constraint.getRightSide();
		
		switch(constraint.getType()){	
			case EQUALITY:
				str+= "("+sum+")"+" = "+rhs;
				break;
			case GREATER_THAN:
				str+= "("+sum+")"+" >= "+rhs;
				break;
			case LESS_THAN:
				str+= "("+sum+")"+" <= "+rhs;
				break;
			default:
				return str;				
		}				
		return str;
	}

}
