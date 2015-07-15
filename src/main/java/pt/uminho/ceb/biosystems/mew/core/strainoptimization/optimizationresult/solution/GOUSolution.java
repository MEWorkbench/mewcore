package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.AbstractStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUSolution extends AbstractStrainOptimizationResult<JecoliGenericConfiguration> {

	private static final long	serialVersionUID	= 1L;

	public GOUSolution(JecoliGenericConfiguration configuration, Map<String, SteadyStateSimulationResult> simulationResultMap, GeneticConditions solutionGeneticConditions) {
        super(configuration, simulationResultMap, solutionGeneticConditions);
    }

    public GOUSolution(JecoliGenericConfiguration configuration, GeneticConditions solutionGeneticConditions) {
        super(configuration,solutionGeneticConditions);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        GeneChangesList genenChangeList = solutionGeneticConditions.getGeneList();
        List<Pair<String,Double>> geneExpressionList = genenChangeList.getPairsList();
        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getMapOf2Sim();
        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(Pair<String,Double> reactionExpression:geneExpressionList)
            outputStream.write(","+reactionExpression.getA()+"="+reactionExpression.getB());

        outputStream.write("\n");
    }
}
