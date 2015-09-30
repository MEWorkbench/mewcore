package pt.uminho.ceb.biosystems.mew.core.simplificationnew;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public interface ISimplifierGeneticConditions {
	
	IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions geneticConditions, IndexedHashMap<IObjectiveFunction,String> objectiveFunctions) throws Exception;
	
	IGeneticConditionsSimplifiedResult simplifyGeneticConditions(GeneticConditions geneticConditions, IndexedHashMap<IObjectiveFunction,String> objectiveFunctions, double[] initialFitnesses) throws Exception;
	
	Set<String> getGeneticConditionsIDs(GeneticConditions gc /*Can be in this abstract*/);
	
	void nextGeneticCondition(GeneticConditions solution, String geneID, double expressionLevel) throws Exception;
	
	void removeGeneticCondition(GeneticConditions gc, String id) throws Exception;
	
	double getExpressionLevel(GeneticConditions gc, String id);
	

}
