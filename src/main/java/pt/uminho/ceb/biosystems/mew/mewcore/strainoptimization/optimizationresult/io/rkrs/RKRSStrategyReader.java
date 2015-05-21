package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rkrs;
 
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 23-03-2015.
 */
public class RKRSStrategyReader extends AbstractStrainOptimizationSolutionReader {
    
	private static final long	serialVersionUID	= 1L;

	protected GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration,String geneticConditionString) throws Exception {
       List<String> modificationList = new ArrayList<>();


        String[] lineArray = geneticConditionString.split(",");

        for(String knockoutId:lineArray)
            modificationList.add(knockoutId);

        ReactionChangesList reactionChangesList = new ReactionChangesList(modificationList);
        return new GeneticConditions(reactionChangesList);
    }



}