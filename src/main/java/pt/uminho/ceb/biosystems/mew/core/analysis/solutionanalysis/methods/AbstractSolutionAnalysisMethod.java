package pt.uminho.ceb.biosystems.mew.core.analysis.solutionanalysis.methods;

import pt.uminho.ceb.biosystems.mew.core.analysis.solutionanalysis.ISolutionAnalysis;

public abstract class AbstractSolutionAnalysisMethod implements ISolutionAnalysis{
	
	public String[] descriptions = null;	
	
	public AbstractSolutionAnalysisMethod(int num_outputs){
		descriptions = new String[num_outputs];
		initDescriptions();
	}
	
	public abstract void initDescriptions();

	public String[] getDescriptions(){
		return descriptions;
	}

}
