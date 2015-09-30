package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rkrs;
 
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;

/**
 * Created by ptiago on 23-03-2015.
 */
public class RKRSStrategyReader extends AbstractStrainOptimizationSolutionReader {
    
	private static final long	serialVersionUID	= 1L;

	protected GeneticConditions processGeneticConditions(String geneticConditionString) throws Exception {
       List<String> modificationList = new ArrayList<>();


        String[] lineArray = geneticConditionString.split(",");

        for(String knockoutId:lineArray)
            modificationList.add(knockoutId);

        ReactionChangesList reactionChangesList = new ReactionChangesList(modificationList);
        return new GeneticConditions(reactionChangesList);
    }



}