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

/**
 * Created by ptiago on 18-03-2015.
 */
public class ROUSolution extends AbstractSolution {

	private static final long	serialVersionUID	= 1L;

	public ROUSolution(GeneticConditions solutionGeneticConditions) {
        super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
    }

    public ROUSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
        super(solutionGeneticConditions, simulationResultMap);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
        List<Pair<String,Double>> reactionExpressionList = reactionChangeList.getPairsList();
//        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getObjectiveFunctionsMap();
//        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(Pair<String,Double> reactionExpression:reactionExpressionList)
            outputStream.write(","+reactionExpression.getA()+"="+reactionExpression.getB());

//        outputStream.write("\n");
    }
}
