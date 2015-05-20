package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rou;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class ROUStrategyReader extends AbstractStrainOptimizationSolutionReader {
    
	private static final long	serialVersionUID	= 8987596486229633286L;

	@Override
    protected GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration,String geneticConditionString) throws Exception {
        String[] lineArray = geneticConditionString.split(",");
        List<String> modificationIdList = new ArrayList<>();
        List<Double> modificationValueList = new ArrayList<>();

        for(String modificationString:lineArray){
            String[] modificationArray = modificationString.split("=");
            String reactionId = modificationArray[0];
            modificationIdList.add(reactionId);
            double reactionExpression = Double.valueOf(modificationArray[1]);
            modificationValueList.add(reactionExpression);
        }

        ReactionChangesList reactionChangesList = new ReactionChangesList(modificationIdList,modificationValueList);
        return new GeneticConditions(reactionChangesList);
    }

}
