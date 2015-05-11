package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.rk;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class RKStrategyReader extends AbstractStrainOptimizationSolutionReader{

    protected GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration,String geneticConditionString) throws Exception {
        boolean isOverUnderExpression = configuration.getIsOverUnderExpression();
        boolean isGeneExpression = configuration.getIsGeneOptimization();
        List<String> modificationList = new ArrayList<>();


        String[] lineArray = geneticConditionString.split(",");

        for(String knockoutId:lineArray)
            modificationList.add(knockoutId);

         if(isGeneExpression) {
             GeneChangesList geneChangesList = new GeneChangesList(modificationList);
             return new GeneticConditions(geneChangesList,new ReactionChangesList(),isOverUnderExpression);
         }

        ReactionChangesList reactionChangesList = new ReactionChangesList(modificationList);
        return new GeneticConditions(reactionChangesList);
    }



}
