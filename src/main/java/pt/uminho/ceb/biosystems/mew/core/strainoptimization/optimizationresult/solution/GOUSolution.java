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

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUSolution extends AbstractSolution {

	private static final long	serialVersionUID	= 1L;

	public GOUSolution(GeneticConditions solutionGeneticConditions) {
        super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
    }

    public GOUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
        super(solutionGeneticConditions, simulationResultMap);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        GeneChangesList genenChangeList = solutionGeneticConditions.getGeneList();
        List<Pair<String,Double>> geneExpressionList = genenChangeList.getPairsList();
//        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getObjectiveFunctionsMap();
//        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(Pair<String,Double> reactionExpression:geneExpressionList)
            outputStream.write(","+reactionExpression.getA()+"="+reactionExpression.getB());

//        outputStream.write("\n");
    }
}
