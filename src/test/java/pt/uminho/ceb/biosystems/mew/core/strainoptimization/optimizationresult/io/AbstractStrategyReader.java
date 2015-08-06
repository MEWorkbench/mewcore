package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 31-03-2015.
 */
public class AbstractStrategyReader {
	
	public static final Double THRESHOLD = 1e-6;
	
	@SuppressWarnings("rawtypes")
	public boolean validateSolution(boolean isGeneModification, List<Pair<Double, List<Pair<String, Double>>>> solutionListList, IStrainOptimizationResultSet resultSet) throws Exception {
		for (int i = 0; i < solutionListList.size(); i++) {
			IStrainOptimizationResult result = (IStrainOptimizationResult) resultSet.getResultList().get(i);
			Pair<Double, List<Pair<String, Double>>> solutionTuple = solutionListList.get(i);
			double objectiveFunctionValue = solutionTuple.getA();
			List<Pair<String, Double>> solutionRepresentationList = solutionTuple.getB();
			SteadyStateSimulationResult simulationResult = result.getSimulationResultForMethod("FBA");
			double originalValue = simulationResult.getOFvalue();
			if (Math.abs(objectiveFunctionValue - originalValue) > THRESHOLD) return false;
			
			List<Pair<String, Double>> resultChangeList = null;
			if (isGeneModification)
				resultChangeList = result.getGeneticConditions().getGeneList().getPairsList();
			else
				resultChangeList = result.getGeneticConditions().getReactionList().getPairsList();
			
			for (int j = 0; j < solutionRepresentationList.size(); j++) {
				Pair<String, Double> positionTuple = solutionRepresentationList.get(j);
				String id = positionTuple.getA();
				double value = positionTuple.getB();
				Pair<String, Double> originalPositionTuple = getOriginalTuple(resultChangeList, id);
				
				if (originalPositionTuple == null) return false;
				
				if (originalPositionTuple.getB() != value) return false;
			}
			
		}
		return true;
	}
	
	protected Pair<String, Double> getOriginalTuple(List<Pair<String, Double>> resultChangeList, String targetId) {
		for (Pair<String, Double> resultTuple : resultChangeList)
			if (resultTuple.getA().compareTo(targetId) == 0) return resultTuple;
		
		return null;
	}
}
