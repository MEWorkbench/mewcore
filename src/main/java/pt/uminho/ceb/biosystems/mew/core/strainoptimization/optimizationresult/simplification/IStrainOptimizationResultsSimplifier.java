package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public interface IStrainOptimizationResultsSimplifier<C extends JecoliGenericConfiguration, T extends IStrainOptimizationResult> {
	
	List<T> simplifySolution(T solution) throws Exception;
	
	ISimplifierGeneticConditions getSimplifier();
	
	IStrainOptimizationResultSet<C, T> getSimplifiedResultSet(IStrainOptimizationResultSet<C, T> resultSet) throws Exception;
	
	IStrainOptimizationResultSet<C, T> getSimplifiedResultSetDiscardRepeated(IStrainOptimizationResultSet<C, T> resultSet) throws Exception;
	
	T createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res, List<Double> fitnesses);
	
	ISimplifierGeneticConditions getSimplifierGeneticConditions();
	
}