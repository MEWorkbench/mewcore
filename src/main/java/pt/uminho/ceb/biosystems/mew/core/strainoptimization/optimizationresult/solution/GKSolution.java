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
	
	private static final long	serialVersionUID	= 1L;

	public GKSolution(GeneticConditions solutionGeneticConditions) {
        this(solutionGeneticConditions, new HashMap<String, SteadyStateSimulationResult>());
    }
	
	public GKSolution(GeneticConditions solutionGeneticConditions, Map<String, SteadyStateSimulationResult> simulationResultMap) {
        super(solutionGeneticConditions, simulationResultMap);
    }

    @Override
    public void write(OutputStreamWriter outputStream) throws Exception {
        GeneChangesList geneChangesList = solutionGeneticConditions.getGeneList();
        List<String> geneKnockoutList = geneChangesList.getGeneKnockoutList();
//        IndexedHashMap<IObjectiveFunction,String> mapOf2SimMap = configuration.getObjectiveFunctionsMap();
//        writeMapOf2SimMap(outputStream,mapOf2SimMap);

        for(String geneKnockout:geneKnockoutList)
            outputStream.write(","+geneKnockout);

        StringUtils.concat(",",geneKnockoutList);

//        outputStream.write("\n");
    }


}
