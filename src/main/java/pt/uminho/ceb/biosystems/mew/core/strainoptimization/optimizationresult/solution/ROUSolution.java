package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * Created by ptiago on 18-03-2015.
 */
public class ROUSolution extends AbstractSolution {
	
	private static final long serialVersionUID = 1L;
	
	public ROUSolution(GeneticConditions solutionGeneticConditions) {
		super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
	}
	
//	public ROUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
//		super(solutionGeneticConditions, simulationResultMap);
//	}
	
	public ROUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> fitnesses) {
		super(solutionGeneticConditions, simulationResultMap, fitnesses);
	}
	
	@Override
	public void write(OutputStreamWriter outputStream) throws Exception {
		ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
		List<Pair<String, Double>> reactionExpressionList = reactionChangeList.getPairsList();
		
		if (attributes != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, attributes);
			outputStream.write(fitString);
		}
		
		outputStream.write(INNER_DELIMITER);
		
		for (Pair<String, Double> reactionExpression : reactionExpressionList) {
			outputStream.write(INNER_DELIMITER + reactionExpression.getA() + "=" + reactionExpression.getB());
		}
		
	}
	
	@Override
	public String toStringHumanReadableGC(String delimiter) {
		ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
		if (reactionChangeList != null && reactionChangeList.size() > 0) {
			List<Pair<String, Double>> reactionExpressionList = reactionChangeList.getPairsList();
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < reactionExpressionList.size(); i++) {
				Pair<String, Double> reactionExpression = reactionExpressionList.get(i);
				sb.append(reactionExpression.getA() + "=" + reactionExpression.getB());
				if (i < reactionExpressionList.size()) {
					sb.append(delimiter);
				}
			}
			return sb.toString();
		}
		return  "";
	}
}
