package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.gou;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io.AbstractStrainOptimizationSolutionReader;

/**
 * Created by ptiago on 18-03-2015.
 */
public class GOUStrategyReader extends AbstractStrainOptimizationSolutionReader {
	
	private static final long				serialVersionUID	= 1L;
	private ISteadyStateGeneReactionModel	model;
	
	public GOUStrategyReader(ISteadyStateGeneReactionModel model) {
		super();
		this.model = model;
	}
	
	@Override
	protected GeneticConditions processGeneticConditions(String geneticConditionString) throws Exception {
		List<String> geneIdmodificationList = new ArrayList<>();
		List<Double> geneExpressionModificationList = new ArrayList<>();
		
		if (geneticConditionString != null) {
			String[] lineArray = geneticConditionString.split(",");
			
			for (String modification : lineArray) {
				String[] modificationArray = modification.split("=");
				geneIdmodificationList.add(modificationArray[0].trim());
				geneExpressionModificationList.add(Double.valueOf(modificationArray[1].trim()));
			}
		}
		
		GeneChangesList geneChangesList = new GeneChangesList(geneIdmodificationList, geneExpressionModificationList);
		return new GeneticConditions(geneChangesList, model, true);
	}
}
