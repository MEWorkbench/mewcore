package pt.uminho.ceb.biosystems.mew.core.optimization.components;

import java.util.List;

public interface IKnockoutSolutionSet {
	
	public List<String> getSolution (int index);
	
	public double[] getAttributes (int index);
	
	public int size();
	
	public boolean checkIfSolutionExists (List<String> solution, double... attribs);
	
	public void addSimplifiedSolution (List<String> solution, double... attribs);
	
	public void saveToCSVFile(String filename) throws Exception;
	
	public void loadFromCSVFile(String filename) throws Exception;
	
}
