package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.mains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace.Flux;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace.FluxSet;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace.MFANullSpaceSolution;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace.MeasuredFlux;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.nullspace.NullSpaceMethod;

public class TestNullSpace {
	
	public static void main(String[] args) {
		
		double[][] s = getS("/Users/rafael/Desktop/nullSpace/sMatrix.csv", ",");
//		double[][] s = getS();
		boolean[] isMeasured = new boolean[s[0].length];
		for(int i=0; i<s[0].length; i++)
			isMeasured[i] = false;
		
		isMeasured[26] = true;
		isMeasured[40] = true;
		double[] vms = new double[]{-20,2.001};
		
		
		try {
			NullSpaceMethod nullSpaceMethod = new NullSpaceMethod(s, false, true, isMeasured, vms);
			
			MFANullSpaceSolution nullSpaceSolution = nullSpaceMethod.runModel();
			
			for(int i=0; i<nullSpaceSolution.getNumberOfSolutions(); i++)
			{
				FluxSet solution = nullSpaceSolution.getFluxSet(i);
				FluxValueMap fluxValues = getNullSpaceSolutionFluxes(solution);

			}
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	public static double[][] getS(String file, String separator){
		double[][] s = null;
		try {
			FileReader fr = new FileReader(new File(file));
			BufferedReader br = new BufferedReader(fr);
			
			List<String> lines = new ArrayList<String>();
			
			String line;
			
			while((line = br.readLine())!=null)
				lines.add(line);
			
			br.close();
			fr.close();
			
			s = new double[lines.size()][];

			for(int i=0; i<lines.size(); i++)
			{
				String[] tokens = lines.get(i).split(separator);
				s[i] = new double[tokens.length];
				for(int j=0; j<tokens.length; j++)
					s[i][j] = Double.parseDouble(tokens[j]);
			}
		} catch (NumberFormatException e) {e.printStackTrace();
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();
		}
		return s;
	}
	
	public static double[][] getS(){
		return new double[][]{{1,3,3,2},{2,6,9,7},{-1,-3,3,4}};
	}
	
	private static FluxValueMap getNullSpaceSolutionFluxes(FluxSet solution){
		FluxValueMap fluxes = new FluxValueMap();
		
		for(int f=0; f<solution.getNumberFluxes(); f++)
		{
			Flux flux = solution.getFlux(f);
			int fluxIndex = flux.getIndex();
			String rId = "vc_" + fluxIndex;
			double fluxValue = flux.getValue();
			fluxes.put(rId, fluxValue);
		}
		
		for(int f=0; f<solution.getNumberMeasuredFluxes(); f++)
		{
			MeasuredFlux measuredFlux = solution.getMeasuredFlux(f);
			int fluxIndex = measuredFlux.getIndex();
			String rId = "vm_" + fluxIndex;
			double fluxValue = measuredFlux.getExpectedValue();
			fluxes.put(rId, fluxValue);
		}
		
		return fluxes;
	}
		

}
