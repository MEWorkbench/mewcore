package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.fluxratios.grammar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.exceptions.InvalidExpressionException;



public class ValidateFluxRatios {

	public static Map<String, Double> validate(String expression) throws InvalidExpressionException
	{
		Map<String, Double> fluxesCoeffs = null;
		try {
			File temp = new File("expression.tmp");
			FileWriter fw = new FileWriter(temp);
		
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(expression);
			
			bw.flush();
			bw.close();
			fw.close();
			
			FluxRatios fluxRatios = new FluxRatios();
			fluxesCoeffs = fluxRatios.run("expression.tmp");
			
			temp.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidExpressionException(e.getMessage());
		}
		return fluxesCoeffs;
	}
	
	public static void main(String... args){
		try {
			Map<String, Double> fluxesCoeffs = validate("Vmae - Vace + -2Veta / -2Vmae + Vpyr -2Vace <= 0.5");
			for(String s : fluxesCoeffs.keySet())
			{
				System.out.print("\nKEY: " + s + " (" + fluxesCoeffs.get(s) + ")");
			}
			
		} catch (InvalidExpressionException e) {
			e.printStackTrace();
		}
	}
}
