package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GKSolution extends AbstractSolution {
	
	private static final long serialVersionUID = 1L;
	
	public GKSolution(GeneticConditions solutionGeneticConditions) {
		super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
	}
	
	//	public GKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
	//        super(solutionGeneticConditions, simulationResultMap);
	//    }
	
	public GKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> fitnesses) {
		super(solutionGeneticConditions, simulationResultMap, fitnesses);
	}
	
	@Override
	public void write(OutputStreamWriter outputStream) throws Exception {
		GeneChangesList geneChangesList = solutionGeneticConditions.getGeneList();
		
		if (attributes != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, attributes);
			outputStream.write(fitString);
		}
		
		outputStream.write(INNER_DELIMITER);

		if(geneChangesList!=null && geneChangesList.size()>0){
			List<String> geneKnockoutList = geneChangesList.getGeneKnockoutList();
			for (String geneKnockout : geneKnockoutList) {
				outputStream.write(INNER_DELIMITER + geneKnockout);
			}			
		}
		
	}

	
	@Override
	public String toStringHumanReadableGC(String delimiter) {
		GeneChangesList geneChangesList = solutionGeneticConditions.getGeneList();
		if(geneChangesList!=null && geneChangesList.size()>0){
			List<String> geneKnockoutList = geneChangesList.getGeneKnockoutList();
			
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<geneKnockoutList.size(); i++){
				sb.append(geneKnockoutList.get(i));
				if(i<geneKnockoutList.size()){
					sb.append(delimiter);
				}
			}
			return sb.toString();			
		}
		return "";
	}
}
