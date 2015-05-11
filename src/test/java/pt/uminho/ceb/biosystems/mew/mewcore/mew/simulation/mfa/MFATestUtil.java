package pt.uminho.ceb.biosystems.mew.mewcore.mew.simulation.mfa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.exceptions.InvalidExpressionException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.fluxratios.grammar.ValidateFluxRatios;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.FluxRatioConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.MathmlRatiosParser;

public class MFATestUtil {
	
	public static FluxRatioConstraintList getFluxRatiosFromCSV(String file, String delimiter){
		
		
		FluxRatioConstraintList ratioConstraints = new FluxRatioConstraintList();
		StringBuffer errors = null;
		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);			
			
			String line;
			while((line = br.readLine()) != null)
			{
				try {
					String[] tokens = line.split(delimiter);
					String ratioConstraint = tokens[0];
					Map<String, Double> fluxesCoeffs = ValidateFluxRatios.validate(ratioConstraint);
					String ratioDescription = (tokens.length>1 && !tokens[1].equals("")) ? tokens[1] : null;
					ratioConstraints.add(new FluxRatioConstraint(ratioDescription, ratioConstraint, fluxesCoeffs));
				} catch (InvalidExpressionException e) {
					if(errors == null)
					{
						errors = new StringBuffer();
						errors.append(e.getMessage());
					}
					else
						errors.append("\n" + e.getMessage());
					e.printStackTrace();
				}
			}
			br.close();
			fr.close();
		} catch (IOException e) {e.printStackTrace();}
		
		if(errors!=null)
			System.out.println(errors);
		
		return ratioConstraints;	
	}
	
	public static FluxRatioConstraintList getFluxRatiosFromMathML(String file){		
		MathmlRatiosParser parser = new MathmlRatiosParser();
		try {
			parser.parseRatioExpressions(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parser.getRatioConstraints();
	}
	
	public static FluxRatioConstraintList getFluxRatiosFromFile(String file){
		if(file.endsWith(".txt")||file.endsWith(".csv"))
			return getFluxRatiosFromCSV(file, ",");
		else
			return getFluxRatiosFromMathML(file);
	}
}
