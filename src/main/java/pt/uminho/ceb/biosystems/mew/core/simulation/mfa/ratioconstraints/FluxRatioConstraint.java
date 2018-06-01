package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.utils.MathUtils;


public class FluxRatioConstraint implements Serializable {
	
	private static final long serialVersionUID = 8067974560176358037L;
	

	private String description;
	
	/** Expression that contains the fluxes constraint */
	private String expression;
	
	/** Map with the fluxes' identifiers, and its coefficients, that are included in the constraint expression */
	private Map<String, Double> fluxesCoeffs;
	
	/** The comparator of the constraint */
	private RatioConstraintComparator comparator;
	
	
	public FluxRatioConstraint(String description, String expression, Map<String, Double> fluxesCoeffs) {
		this(expression, fluxesCoeffs);
		this.description = description;
	}
	
	public FluxRatioConstraint(String expression, Map<String, Double> fluxesCoeffs) {
		this.fluxesCoeffs = fluxesCoeffs;
		this.expression = expression;
		this.comparator = obtainComparator();
	}
	
	public FluxRatioConstraint(String description, String expression, Map<String, Double> fluxesCoeffs, RatioConstraintComparator comparator) {
		this(expression, fluxesCoeffs, comparator);
		this.description = description;
	}
	
	public FluxRatioConstraint(String expression, Map<String, Double> fluxesCoeffs, RatioConstraintComparator comparator) {
		this.fluxesCoeffs = fluxesCoeffs;
		this.expression = expression;
		this.comparator = comparator;
	}
	
	
	private RatioConstraintComparator obtainComparator(){
		if(expression.contains("<="))
			return RatioConstraintComparator.MINOR_EQUAL;
		if(expression.contains(">="))
			return RatioConstraintComparator.MAJOR_EQUAL;
		if(expression.contains("<"))
			return RatioConstraintComparator.MINOR;
		if(expression.contains(">"))
			return RatioConstraintComparator.MAJOR;
		else
			return RatioConstraintComparator.EQUAL;
	}
	
	public void negateCoeffiecients(){
		for(String f: this.fluxesCoeffs.keySet())
		{
			double c = this.fluxesCoeffs.get(f) * -1;
			this.fluxesCoeffs.put(f, c);
		}
	}
	
	/** Returns two new restrictions. One for le and another for le */
	public FluxRatioConstraint[] convertEquality(){
				
		if(!this.comparator.equals(RatioConstraintComparator.EQUAL))
			return null;
		
		FluxRatioConstraint[] restrictions = new FluxRatioConstraint[2];
		
		restrictions[0] = this.clone();
		restrictions[1] = this.clone();
		
		restrictions[0].setExpression(expression.replace("=", "<="));
		restrictions[1].setExpression(expression.replace("=", ">="));
		
		restrictions[0].setComparator(RatioConstraintComparator.MINOR_EQUAL);
		restrictions[1].setComparator(RatioConstraintComparator.MAJOR_EQUAL);
		
		return restrictions;
	}
	
	
	public boolean isEqualityRatio(){
		return comparator.equals(RatioConstraintComparator.EQUAL);
	}
	
	public String getLinearConstraintForm(){
		String str = "";
		boolean first = true;
		for(String flux : fluxesCoeffs.keySet())
		{
			double coeff = MathUtils.roundValue(fluxesCoeffs.get(flux), 3);
			
			if(first)
			{
				str += coeff + "*" + flux;
				first = false;
			}
			else
				if(coeff>0)
					str += " +" + coeff + "*" + flux;
				else
					str += " " + coeff + "*" + flux;
		}		
		str += " " + comparator.toString() + " 0";
		return str;
	}
	
	
	
	public Map<String, Double> getFluxesCoeffs() {return fluxesCoeffs;}
	public void setFluxesCoeffs(Map<String, Double> fluxesCoeffs) {this.fluxesCoeffs = fluxesCoeffs;}
	public String getExpression() {return expression;}
	public void setExpression(String expression) {this.expression = expression;}
	public RatioConstraintComparator getComparator(){return comparator;}
	public void setComparator(RatioConstraintComparator comparator){this.comparator = comparator;}
	public String getDescription() {return description;}
	public void setDescription(String description) {this.description = description;}

	
	public FluxRatioConstraint clone(){
		Map<String, Double> fCoeffs = new HashMap<String, Double>();
		for(String f : this.fluxesCoeffs.keySet())
			fCoeffs.put(f, this.fluxesCoeffs.get(f));
		
		String exp = this.expression;
		
		RatioConstraintComparator comp = this.comparator;
		
		return new FluxRatioConstraint(exp, fCoeffs, comp);
	}
	
	@Override
	public String toString() {
		String str = getExpression();
		str += " ----- ";
		str += getLinearConstraintForm();
		return str;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		FluxRatioConstraint r2 = (FluxRatioConstraint) obj;
		
		if(!this.comparator.equals(r2.getComparator()))
			return false;
		
		Map<String, Double> r2fCoeffs = r2.getFluxesCoeffs();
		
		for(String v : fluxesCoeffs.keySet())
			if(!(r2fCoeffs.containsKey(v) && fluxesCoeffs.get(v).equals(r2fCoeffs.get(v))))
				return false;
		
		for(String v : r2fCoeffs.keySet())
			if(!fluxesCoeffs.containsKey(v))
				return false;
		
		return true;
	}
	
	public static void main(String[] args) {
		Map<String, Double> c1 = new HashMap<String, Double>();
		c1.put("v1", 2.0);
		c1.put("v2", 1.0);
		c1.put("v3", -0.5);
		FluxRatioConstraint r1 = new FluxRatioConstraint("v1 + 2v2 / v3 = 0.5", c1);
		
		Map<String, Double> c2 = new HashMap<String, Double>();
		c2.put("v1", 2.0);
		c2.put("v2", 1.0);
		c2.put("v3", -0.5);
		FluxRatioConstraint r2 = new FluxRatioConstraint("v1 + 2v2 / v3 = 0.5", c2);
		
		
		r1.equals(r2);
	}
}
