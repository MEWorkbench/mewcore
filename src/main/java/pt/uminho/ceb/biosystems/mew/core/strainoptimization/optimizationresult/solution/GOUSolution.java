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
		
		if (attributes != null) {
			String fitString = StringUtils.concat(INNER_DELIMITER, attributes);
			outputStream.write(fitString);
        }
		outputStream.write(INNER_DELIMITER);
		
		for (Pair<String, Double> geneExpression : geneExpressionList) {
			outputStream.write(INNER_DELIMITER + geneExpression.getA() + "=" + geneExpression.getB());
		}
	}
	
	@Override
	public String toStringHumanReadableGC(String delimiter) {
		GeneChangesList geneChangeList = solutionGeneticConditions.getGeneList();
		List<Pair<String, Double>> geneExpressionList = geneChangeList.getPairsList();
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<geneExpressionList.size(); i++){
			Pair<String,Double> geneExpression = geneExpressionList.get(i);
			sb.append(geneExpression.getA() + "=" + geneExpression.getB());
			if(i<geneExpressionList.size()){
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}
