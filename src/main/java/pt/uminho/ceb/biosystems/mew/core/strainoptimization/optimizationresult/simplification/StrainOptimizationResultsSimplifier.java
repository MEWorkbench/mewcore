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

public abstract class StrainOptimizationResultsSimplifier<C extends JecoliGenericConfiguration, T extends IStrainOptimizationResult> implements IStrainOptimizationResultsSimplifier<C, T> {

	protected ISimplifierGeneticConditions simplifier;
	protected C configuration;

	public StrainOptimizationResultsSimplifier(C configuration) {
		this.configuration = configuration;
	}

	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier#simplifySolution(T)
	 */
	@Override
	public List<T> simplifySolution(T solution) throws Exception {
		
		IGeneticConditionsSimplifiedResult simpResults = getSimplifier().simplifyGeneticConditions(solution.getGeneticConditions(),configuration.getObjectiveFunctionsMap());

		List<T> toret = new ArrayList<T>();
		for (int i = 0; i < simpResults.size(); i++) {
			GeneticConditions gc = simpResults.getSimplifiedGeneticConditions().get(i);
			SteadyStateMultiSimulationResult res = simpResults.getSimplifiedSimulationResults().get(i);
			T simpSol = createSolution(gc, res.getSimulations(),simpResults.getSimplifiedFitnesses());
			toret.add(simpSol);
		}

		return toret;
	}

	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier#getSimplifier()
	 */
	@Override
	public ISimplifierGeneticConditions getSimplifier() {
		if (simplifier == null) {
			simplifier = getSimplifierGeneticConditions();
		}
		return simplifier;
	}
	
	protected List<T> getSimplifiedResultList(IStrainOptimizationResultSet<C, T> resultSet) throws Exception{
		List<T> originalList = resultSet.getResultList();
		List<T> simplifiedList = new ArrayList<>();
		
		for (T solution : originalList)
			simplifiedList.addAll(simplifySolution(solution));
		
		return simplifiedList;
	}
	
	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier#getSimplifyResultSet(pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet)
	 */
	@Override
	public IStrainOptimizationResultSet<C,T> getSimplifiedResultSet(IStrainOptimizationResultSet<C,T> resultSet) throws Exception{
		List<T> simplifiedList = getSimplifiedResultList(resultSet);
		return createResultSetInstance(simplifiedList);
	};
	
	@Override
	public IStrainOptimizationResultSet<C, T> getSimplifiedResultSetDiscardRepeated(IStrainOptimizationResultSet<C, T> resultSet) throws Exception{
		IStrainOptimizationResultSet<C, T> emptySet = createResultSetInstance(new ArrayList<T>());
		IStrainOptimizationResultSet<C, T> resSetRepeated = getSimplifiedResultSet(resultSet);
		
//		System.out.println("RESULT SET WITH REPEATITIONS = "+resSetRepeated.getResultList().size());
		emptySet = emptySet.merge(resSetRepeated);
//		System.out.println("RESULT SET WITHOUT REPEATITIONS = "+emptySet.getResultList().size());
		return emptySet;
	}
	
	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier#createSolution(pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions, java.util.Map, java.util.List)
	 */
	@Override
	public abstract T createSolution(GeneticConditions gc, Map<String, SteadyStateSimulationResult> res, List<Double> fitnesses);

	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier#getSimplifierGeneticConditions()
	 */
	@Override
	public abstract ISimplifierGeneticConditions getSimplifierGeneticConditions();
	
	public abstract IStrainOptimizationResultSet<C,T> createResultSetInstance(List<T> resultList);
	
}
