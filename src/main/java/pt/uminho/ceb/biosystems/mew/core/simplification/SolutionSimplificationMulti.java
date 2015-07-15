package pt.uminho.ceb.biosystems.mew.core.simplification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateMTOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateMultiSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.SteadyStateOptimizationResult;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Solution Simplification class that allows simplification of solutions with
 * two or more flux distributions.
 * 
 * @author pmaia Jan 13, 2015
 */
public class SolutionSimplificationMulti implements Serializable {

	private static final long serialVersionUID = 1L;

	private double delta = 0.000001;

	protected ISteadyStateModel model;

	protected IndexedHashMap<IObjectiveFunction, String> objectiveFunctions;

	protected Map<String, SimulationSteadyStateControlCenter> ccs = null;

	protected FluxValueMap referenceFD = null;

	protected EnvironmentalConditions envConds = null;

	public SolutionSimplificationMulti(ISteadyStateModel model,
			IndexedHashMap<IObjectiveFunction, String> objFunctions, FluxValueMap referenceFD,
			EnvironmentalConditions envCond, SolverType solver) {
		this.model = model;
		this.objectiveFunctions = objFunctions;
		this.envConds = envCond;
		ccs = new HashMap<String, SimulationSteadyStateControlCenter>();
		for (IObjectiveFunction of : objFunctions.keySet()) {
			String method = objFunctions.get(of);
			SimulationSteadyStateControlCenter center = new SimulationSteadyStateControlCenter(envCond, null, model,
					method);
			center.setMaximization(true);
			center.setSolver(solver);
			center.setWTReference(referenceFD);
			ccs.put(method, center);
		}
	}

	public void setMargin(double delta) {
		this.delta = delta;
	}

	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution) throws Exception {
		return simplifyReactionsSolution(initialSolution, null);
	}

	public SolutionSimplificationResult simplifyReactionsSolution(GeneticConditions initialSolution,
			SteadyStateMultiSimulationResult initialRes) throws Exception {

		SteadyStateMultiSimulationResult origRes = null;
		if (initialRes == null) {
			Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
			for (String method : ccs.keySet()) {
				SimulationSteadyStateControlCenter center = ccs.get(method);
				center.setGeneticConditions(initialSolution);
				try {
					SteadyStateSimulationResult mres = center.simulate();
					results.put(method, mres);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			origRes = new SteadyStateMultiSimulationResult("sol", initialSolution, envConds, results);
		} else
			origRes = initialRes;

		double[] initialFitnesses = evaluateSolution(origRes);
		// System.out.println("InitFit: "+Arrays.toString(initialFitnesses));

		Set<String> reactionIds = initialSolution.getReactionList().getReactionIds();

		List<String> rIDsIterator = new ArrayList<String>(reactionIds);

		GeneticConditions finalSolution = initialSolution;
		double[] finalFitnesses = initialFitnesses;
		SteadyStateMultiSimulationResult finalRes = origRes;

		for (String reactionId : rIDsIterator) {

			double expressionLevel = initialSolution.getReactionList().getReactionFlux(reactionId);

			finalSolution.getReactionList().removeReaction(reactionId);

			Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
			for (String method : ccs.keySet()) {
				SimulationSteadyStateControlCenter center = ccs.get(method);
				center.setGeneticConditions(finalSolution);
				try {
					SteadyStateSimulationResult mres = center.simulate();
					results.put(method, mres);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SteadyStateMultiSimulationResult res = new SteadyStateMultiSimulationResult("sol", initialSolution,
					envConds, results);

			double[] simpfitnesses = evaluateSolution(res);

			if (compare(finalFitnesses, simpfitnesses)) {
				finalFitnesses = simpfitnesses;
				finalRes = res;
			} else {
				finalSolution.getReactionList().addReaction(reactionId, expressionLevel);
			}
		}

		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(
				finalRes.getSimulationResultForMethod(objectiveFunctions.getValueAt(0)), finalSolution,
				objectiveFunctions.getIndexArray(), finalFitnesses);

		return optimizationSimplification;

	}

	public SteadyStateOptimizationResult simplifySteadyStateOptimizationResult(
			SteadyStateMTOptimizationResult optResultIN, boolean isGeneOpt) throws Exception {

		SteadyStateOptimizationResult optResultOut = new SteadyStateOptimizationResult(model, objectiveFunctions.getIndexArray());

		for (String id : optResultIN.getSimulationsMap().keySet()) {

			SteadyStateMultiSimulationResult resOrig = optResultIN.getSimulationResult(id);
			SolutionSimplificationResult simp = null;

			if (isGeneOpt)
				simp = simplifyGenesSolution(resOrig.getGeneticConditions(), resOrig);
			else
				simp = simplifyReactionsSolution(resOrig.getGeneticConditions(), resOrig);

			if (simp != null) {
				SteadyStateSimulationResult resOut = simp.getSimulationResult();
				double[] fits = simp.getFitnesses();

				ArrayList<Double> fitnesses = new ArrayList<Double>();
				for (double f : fits)
					fitnesses.add(f);

				optResultOut.addOptimizationResultNoRepeated(resOut, fitnesses);
			}
		}

		return optResultOut;
	}

	public SolutionSimplificationResult simplifyGenesSolution(GeneticConditions initialSolution) throws Exception {
		return simplifyGenesSolution(initialSolution, null);
	}

	public SolutionSimplificationResult simplifyGenesSolution(GeneticConditions initialSolution,
			SteadyStateMultiSimulationResult initialRes) throws Exception {

		SteadyStateMultiSimulationResult origRes;
		if (initialRes == null) {
			Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
			for (String method : ccs.keySet()) {
				SimulationSteadyStateControlCenter center = ccs.get(method);
				center.setGeneticConditions(initialSolution);
				try {
					SteadyStateSimulationResult mres = center.simulate();
					results.put(method, mres);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			origRes = new SteadyStateMultiSimulationResult("sol", initialSolution, envConds, results);
		} else
			origRes = initialRes;

		double[] initialFitnesses = evaluateSolution(origRes);

		Set<String> geneIds = initialSolution.getGeneList().getGeneIds();

		List<String> gIDsIterator = new ArrayList<String>(geneIds);

		GeneticConditions finalSolution = initialSolution;
		double[] finalFitnesses = initialFitnesses;
		SteadyStateMultiSimulationResult finalRes = origRes;

		for (String geneId : gIDsIterator) {
			double expressionLevel = initialSolution.getGeneList().getGeneExpression(geneId);

			finalSolution.getGeneList().removeGene(geneId);
			finalSolution.updateReactionsList((ISteadyStateGeneReactionModel) model);

			Map<String, SteadyStateSimulationResult> results = new HashMap<String, SteadyStateSimulationResult>();
			for (String method : ccs.keySet()) {
				SimulationSteadyStateControlCenter center = ccs.get(method);
				center.setGeneticConditions(finalSolution);
				try {
					SteadyStateSimulationResult mres = center.simulate();
					results.put(method, mres);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SteadyStateMultiSimulationResult res = new SteadyStateMultiSimulationResult("sol", initialSolution,
					envConds, results);

			double[] simpfitnesses = evaluateSolution(res);

			// System.out.println("\tTest removal of ["+geneId+"] fits="+Arrays.toString(simpfitnesses));

			if (compare(finalFitnesses, simpfitnesses)) {

				finalFitnesses = simpfitnesses;
				finalRes = res;
			} else {
				finalSolution.getGeneList().addGene(geneId, expressionLevel);
				finalSolution.updateReactionsList((ISteadyStateGeneReactionModel) model);

			}
		}

		SolutionSimplificationResult optimizationSimplification = new SolutionSimplificationResult(
				finalRes.getSimulationResultForMethod(objectiveFunctions.getValueAt(0)), finalSolution,
				objectiveFunctions.getIndexArray(), finalFitnesses);

		return optimizationSimplification;
	}

	private double[] evaluateSolution(SteadyStateMultiSimulationResult result) throws Exception {
		int size = objectiveFunctions.size();
		double resultList[] = new double[size];
		for (int i = 0; i < size; i++) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			String method = objectiveFunctions.get(of);
			resultList[i] = of.evaluate(result.getSimulationResultForMethod(method));
		}
		return resultList;
	}

	private boolean compare(double[] fitnesses, double[] simplifiedFitness) {
		boolean res = true;
		int i = 0;

		while (res && i < objectiveFunctions.size()) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			if (of.isMaximization()) {
				if (fitnesses[i] - simplifiedFitness[i] > delta)
					res = false;
			} else if (simplifiedFitness[i] - fitnesses[i] > delta)
				res = false;
			i++;
		}

		return res;
	}

}
