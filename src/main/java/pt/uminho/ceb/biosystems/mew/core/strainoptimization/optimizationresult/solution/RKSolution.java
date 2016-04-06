package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * Created by ptiago on 18-03-2015.
 */
public class RKSolution extends AbstractSolution {
	
	private static final long serialVersionUID = 1L;
	
	public RKSolution(GeneticConditions solutionGeneticConditions) {
		super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
	}
	
//	public RKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
//		super(solutionGeneticConditions, simulationResultMap);
//	}
	
	public RKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> fitnesses) {
		super(solutionGeneticConditions, simulationResultMap, fitnesses);
	}
	
	@Override
	public void write(OutputStreamWriter outputStream) throws Exception {
		ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
		List<String> reactionKnockoutList = reactionChangeList.getReactionKnockoutList();
		
		if (attributes != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, attributes);
			outputStream.write(fitString);
        }
		outputStream.write(INNER_DELIMITER);
		
		for (String reactionKnockout : reactionKnockoutList) {
			outputStream.write(INNER_DELIMITER + reactionKnockout);
		}
		
	}
	
	@Override
	public String toStringHumanReadableGC(String delimiter) {
		ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
		List<String> reactionKnockoutList = reactionChangeList.getReactionKnockoutList();		
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<reactionKnockoutList.size(); i++){
			sb.append(reactionKnockoutList.get(i));
			if(i<reactionKnockoutList.size()){
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}
