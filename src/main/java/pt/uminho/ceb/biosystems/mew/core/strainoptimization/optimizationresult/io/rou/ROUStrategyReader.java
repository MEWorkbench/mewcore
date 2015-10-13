package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.rou;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;

/**
 * Created by ptiago on 18-03-2015.
 */
public class ROUStrategyReader extends AbstractStrainOptimizationSolutionReader {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected GeneticConditions processGeneticConditions(String geneticConditionString) throws Exception {
		String[] lineArray = geneticConditionString.split(",");
		List<String> modificationIdList = new ArrayList<>();
		List<Double> modificationValueList = new ArrayList<>();
		
		if (geneticConditionString != null) {
			for (String modificationString : lineArray) {
				String[] modificationArray = modificationString.split("=");
				String reactionId = modificationArray[0];
				modificationIdList.add(reactionId.trim());
				double reactionExpression = Double.valueOf(modificationArray[1].trim());
				modificationValueList.add(reactionExpression);
			}
		}
		
		ReactionChangesList reactionChangesList = new ReactionChangesList(modificationIdList, modificationValueList);
		return new GeneticConditions(reactionChangesList);
	}
	
}
