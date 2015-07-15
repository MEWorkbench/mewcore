package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gk;

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
public class GKStrategyReader extends AbstractStrainOptimizationSolutionReader {
	
	private static final long	serialVersionUID	= 1L;

	@Override
    protected GeneticConditions computeGeneticConditions(IJecoliConfiguration configuration, String geneticConditionString) throws Exception {
        List<String> modificationList = new ArrayList<>();
        String[] lineArray = geneticConditionString.split(",");

        for(String knockoutId:lineArray)
            modificationList.add(knockoutId);


       GeneChangesList geneChangesList = new GeneChangesList(modificationList);
       return new GeneticConditions(geneChangesList,new ReactionChangesList(),false);
    }
}
