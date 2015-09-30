package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.IGeneticConditionsSimplifiedResult;
import pt.uminho.ceb.biosystems.mew.core.simplification.solutions.ISimplifierGeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

public abstract class StrainOptimizationResultsSimplifier<C extends JecoliGenericConfiguration, T extends IStrainOptimizationResult> {

	protected ISimplifierGeneticConditions simplifier;
	protected C configuration;

	public StrainOptimizationResultsSimplifier(C configuration) {
		this.configuration = configuration;
	}

	public List<T> simplifySolution(T solution) throws Exception {
		
		IGeneticConditionsSimplifiedResult simpResults = getSimplifier().simplifyGeneticConditions(solution.getGeneticConditions(),configuration.getObjectiveFunctionsMap());

		List<T> toret = new ArrayList<T>();
		for (int i = 0; i < simpResults.size(); i++) {
			GeneticConditions gc = simpResults.getSimplifiedGeneticConditions().get(i);
			SteadyStateMultiSimulationResult res = simpResults.getSimplifiedSimulationResults().get(i);
			T simpSol = createSolution(gc, res.getSimulations());
			toret.add(simpSol);
		}

		return toret;
	}

	public ISimplifierGeneticConditions getSimplifier() {
		if (simplifier == null) {
			simplifier = getSimplifierGeneticConditions();
		}
		return getSimplifier();
	}
	
	protected List<T> getSimplifiedResultList(IStrainOptimizationResultSet<C, T> resultSet) throws Exception{
		List<T> originalList = resultSet.getResultList();
		List<T> simplifiedList = new ArrayList<>();
		
		for (T solution : originalList)
			simplifiedList.addAll(simplifySolution(solution));
		
		return simplifiedList;
	}
	
	public IStrainOptimizationResultSet<C,T> getSimplifyResultSet(IStrainOptimizationResultSet<C,T> resultSet) throws Exception{
		List<T> simplifiedList = getSimplifiedResultList(resultSet);
		return createResultSetInstance(simplifiedList);
	};

	public abstract T createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res);

	public abstract ISimplifierGeneticConditions getSimplifierGeneticConditions();
	
	public abstract IStrainOptimizationResultSet<C,T> createResultSetInstance(List<T> resultList);
	
}
