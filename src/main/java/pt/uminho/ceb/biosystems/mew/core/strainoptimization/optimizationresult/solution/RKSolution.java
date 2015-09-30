package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractSolution;

/**
 * Created by ptiago on 18-03-2015.
 */
public class RKSolution extends AbstractSolution {

	private static final long	serialVersionUID	= 1L;

	public RKSolution(GeneticConditions solutionGeneticConditions) {
        super(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
    }

    public RKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
        super(solutionGeneticConditions, simulationResultMap);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
        List<String> reactionKnockoutList = reactionChangeList.getReactionKnockoutList();
//        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getObjectiveFunctionsMap();
//        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(String reactionKnockout:reactionKnockoutList)
            outputStream.write(","+reactionKnockout);

//        outputStream.write("\n");
    }




}
