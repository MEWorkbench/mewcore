package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gou;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.IJecoliConfiguration;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUStrategyReader extends AbstractStrainOptimizationSolutionReader{
    
	private static final long	serialVersionUID	= 1L;

	@Override
    protected GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration, String geneticConditionString) throws Exception {
        List<String> geneIdmodificationList = new ArrayList<>();
        List<Double> geneExpressionModificationList = new ArrayList<>();

        String[] lineArray = geneticConditionString.split(",");

        for(String modification:lineArray) {
            String[] modificationArray = modification.split("=");
            geneIdmodificationList.add(modificationArray[0]);
            geneExpressionModificationList.add(Double.valueOf(modificationArray[1]));
        }

        GeneChangesList geneChangesList = new GeneChangesList(geneIdmodificationList,geneExpressionModificationList);
        return new GeneticConditions(geneChangesList,new ReactionChangesList(),true);
    }
}
