package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 18-03-2015.
 */
public class RKSolution extends AbstractStrainOptimizationResult<JecoliGenericConfiguration> {

	private static final long	serialVersionUID	= 1L;

	public RKSolution(JecoliGenericConfiguration configuration, Map<String, SteadyStateSimulationResult> simulationResultMap, GeneticConditions solutionGeneticConditions) {
        super(configuration, simulationResultMap, solutionGeneticConditions);
    }

    public RKSolution(JecoliGenericConfiguration configuration, GeneticConditions solutionGeneticConditions) {
        super(configuration, solutionGeneticConditions);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        ReactionChangesList reactionChangeList = solutionGeneticConditions.getReactionList();
        List<String> reactionKnockoutList = reactionChangeList.getReactionKnockoutList();
        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getMapOf2Sim();
        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(String reactionKnockout:reactionKnockoutList)
            outputStream.write(","+reactionKnockout);

        outputStream.write("\n");
    }


}
