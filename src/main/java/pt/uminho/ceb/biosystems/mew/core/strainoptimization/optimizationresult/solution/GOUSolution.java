package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUSolution extends AbstractSolution {
	
	private static final long serialVersionUID = 1L;
	
	public GOUSolution(GeneticConditions solutionGeneticConditions) {
		super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
	}
	
//	public GOUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
//		super(solutionGeneticConditions, simulationResultMap);
//	}
	
	public GOUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap, List<Double> fitnesses) {
        super(solutionGeneticConditions, simulationResultMap,fitnesses);
    }
	
	@Override
	public void write(OutputStreamWriter outputStream) throws Exception {
		GeneChangesList geneChangeList = solutionGeneticConditions.getGeneList();
		List<Pair<String, Double>> geneExpressionList = geneChangeList.getPairsList();
		
		if (fitnesses != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, fitnesses);
			outputStream.write(fitString);
			outputStream.write(INNER_DELIMITER);
        }else{
        	outputStream.write(OUTTER_DELIMITER);
        }
		
		for (Pair<String, Double> geneExpression : geneExpressionList) {
			outputStream.write(INNER_DELIMITER + geneExpression.getA() + "=" + geneExpression.getB());
		}
		
	}
}
