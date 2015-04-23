package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbml.jsbml.ASTNode;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.fluxratios.grammar.ValidateFluxRatios;



/** Content MathML focuses on the semantics, or meaning, of the expression rather than its layout. Central to Content MathML is the <apply> element that represents function application. */
public class MathmlRatiosParser {

	protected List<String> errorMessages;
	protected FluxRatioConstraintList ratioConstraints;
	
	
	public MathmlRatiosParser(){
		errorMessages = new ArrayList<String>();
		ratioConstraints = new FluxRatioConstraintList();
	}
	
	public void parseRatioExpressions(String fileName) throws IOException {
		parseRatioExpressions(new File(fileName));
	}
	
	public void parseRatioExpressions(File file) throws IOException {
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line, exp="";
		
		int ratioNumber = 1;
		
		Pattern startP = Pattern.compile("<math.*>");
		
		while((line=br.readLine())!=null)
		{
			if(!line.equals(""))
			{
				Matcher m = startP.matcher(line);
				if(m.find())
				{
					if(!exp.equals(""))
					{
						parseExpression(exp, ratioNumber);			
						ratioNumber++;
					}
					exp = "";
				}
				exp += line;
			}
		}
		
		// The last expression
		if(!exp.equals(""))
			parseExpression(exp, ratioNumber);
				
	}
	
	private void parseExpression(String expression, int ratioNumber){
		try{
			ASTNode node = ASTNode.readMathMLFromString(expression);
			String ratioConstraint = node.toString().replaceAll("[\\(\\)]", "").replace("==", "=");
			Map<String, Double> fluxesCoeffs = ValidateFluxRatios.validate(ratioConstraint);		
			String ratioDescription = "Ratio " + ratioNumber;
			ratioConstraints.add(new FluxRatioConstraint(ratioDescription, ratioConstraint, fluxesCoeffs));
		}catch (Exception e) {
			e.printStackTrace();
			errorMessages.add("An error in the ratio " + ratioNumber + " has occurred: " + e.getMessage());
		}
	}
	
	public boolean hasErrors(){
		return errorMessages!=null && errorMessages.size()>0;
	}
	
	public String getErrosAsString(){
		if(!hasErrors())
			return null;
		
		String error = errorMessages.get(0);
		for(int i=1; i<errorMessages.size(); i++)
			error += "\n" + errorMessages.get(i);
		return error;
	}
	
	public List<String> getErrorMessages() {return errorMessages;}
	public FluxRatioConstraintList getRatioConstraints() {return ratioConstraints;}


	public static void main(String[] args) {
		
		MathmlRatiosParser parser = new MathmlRatiosParser();
		try {
			parser.parseRatioExpressions("src/metabolic/simulation/mfa2/expression.xml");
		} catch (IOException e) {e.printStackTrace();}
		
		FluxRatioConstraintList constraints = parser.getRatioConstraints();
		for(FluxRatioConstraint constraint : constraints)
			System.out.println(constraint.toString());
	}
	
}
