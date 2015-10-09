package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/*
 * 
 */
public abstract class AbstractStrainOptimizationResultSet<T extends JecoliGenericConfiguration, E extends IStrainOptimizationResult> implements IStrainOptimizationResultSet<T, E> {
	
	private static final long serialVersionUID = 1L;
	
	protected T							baseConfiguration;
	protected List<E>					resultList;
	protected IStrainOptimizationReader	solutionReader;
	protected HashSet<String>			solutionHash	= null;
	
	public abstract IStrainOptimizationReader getSolutionReaderInstance();
	
	public AbstractStrainOptimizationResultSet(T baseConfiguration, List<E> resultList) {
		this.baseConfiguration = baseConfiguration;
		this.resultList = resultList;
	}
	
	public AbstractStrainOptimizationResultSet(T baseConfiguration) {
		this.baseConfiguration = baseConfiguration;
		this.resultList = new ArrayList<>();
	}
	
	@Override
	public T getBaseConfiguration() {
		return baseConfiguration;
	}
	
	@Override
	public List<E> getResultList() {
		return resultList;
	}
	
	@Override
	public void writeToFile(String file) throws Exception {
		FileWriter fd = new FileWriter(file);
		for (int i = 0; i < resultList.size(); i++) {
			if (i > 0) {
				fd.write("\n");
			}
			IStrainOptimizationResult result = resultList.get(i);
			result.write(fd);
		}
		fd.flush();
		fd.close();
	}
	
	@Override
	public void readSolutionsFromFile(String file) throws Exception {
		Scanner sc = new Scanner(new File(file));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] lineArray = line.split(",,");
			String objectiveValueFunctionString = lineArray[0];
			Double[] objectiveFunctionValueArray = computeObjectiveFunctionValueArray(objectiveValueFunctionString);
			String solutionString = lineArray[1];
			GeneticConditions gc = getSolutionReader().readSolutionFromStream(new ByteArrayInputStream(solutionString.getBytes()));
			resultList.add(createSolution(gc, Arrays.asList(objectiveFunctionValueArray)));
		}
		sc.close();
	}
	
	protected Double[] computeObjectiveFunctionValueArray(String objectiveValueFunctionString) {
		String[] valueArray = objectiveValueFunctionString.split(",");
		Double[] objectiveFunctionArray = new Double[valueArray.length];
		for (int i = 0; i < objectiveFunctionArray.length; i++)
			objectiveFunctionArray[i] = Double.valueOf(valueArray[i]);
		return objectiveFunctionArray;
	}
	
	protected void constructSimulationResultMap(JecoliGenericConfiguration baseConfiguration, E newSolution) throws Exception {
		IndexedHashMap<IObjectiveFunction, String> mapOf2Sim = baseConfiguration.getObjectiveFunctionsMap();
		List<IObjectiveFunction> objectiveFunctionList = mapOf2Sim.getIndexArray();
		for (IObjectiveFunction objectiveFunction : objectiveFunctionList) {
			String simulationMethod = mapOf2Sim.get(objectiveFunction);
			newSolution.getSimulationResultForMethod(simulationMethod);
		}
	}
	
	public void recalculateFitness(IndexedHashMap<IObjectiveFunction, String> ofs) throws Exception {
		Map<String, SimulationSteadyStateControlCenter> ccs = createAllCCs(ofs);
		
		for (E sol : getResultList()) {
			Double[] fitArray = new Double[ofs.size()];
			for (int i = 0; i < ofs.size(); i++) {
				IObjectiveFunction of = ofs.getKeyAt(i);
				String method = ofs.get(of);
				ccs.get(method).setGeneticConditions(sol.getGeneticConditions());
				SteadyStateSimulationResult res = ccs.get(method).simulate();
				sol.addSimulationResultForMethod(method, res);
				double fit = of.evaluate(res);
				fitArray[i] = fit;
			}
			sol.setAttributes(Arrays.asList(fitArray));
		}
	}
	
	public void recalculateFitness() throws Exception {
		recalculateFitness(baseConfiguration.getObjectiveFunctionsMap());
	}
	
	private Map<String, SimulationSteadyStateControlCenter> createAllCCs(Map<IObjectiveFunction, String> ofs) {
		Map<String, SimulationSteadyStateControlCenter> ccs = new HashMap<String, SimulationSteadyStateControlCenter>();
		
		for (IObjectiveFunction of : ofs.keySet()) {
			String method = ofs.get(of);
			SimulationSteadyStateControlCenter center = new SimulationSteadyStateControlCenter(baseConfiguration.getEnvironmentalConditions(), null, baseConfiguration.getSteadyStateModel(), method);
			center.setMaximization(true);
			center.setSolver(baseConfiguration.getSolver());
			center.setWTReference(baseConfiguration.getReferenceFluxDistribution());
			ccs.put(method, center);
		}
		
		return ccs;
	}
	
	public IStrainOptimizationReader getSolutionReader() {
		if (solutionReader == null) {
			solutionReader = getSolutionReaderInstance();
		}
		return solutionReader;
	};
	
	public void addSolution(E solution) {
		resultList.add(solution);
	}
	
	public boolean addSolutionNoRepeat(E solution){
		String unique = solution.getGeneticConditions().toUniqueString();
		if(!getSolutionHash().contains(unique)){
			getSolutionHash().add(unique);
			addSolution(solution);
			return true;
		}
		return false;
	}
	
	public IStrainOptimizationResultSet<T, E> merge(IStrainOptimizationResultSet<T, E> resultSetToMerge) {
		return mergeHash(resultSetToMerge);
	}
	
	public IStrainOptimizationResultSet<T, E> mergeHash(IStrainOptimizationResultSet<T, E> resultSetToMerge) {
		int repeated=0;
		for(E res : resultSetToMerge.getResultList()){			
			repeated += !addSolutionNoRepeat(res) ? 1 : 0;
		}
		System.out.println("Merge finished, found ["+repeated+"] repeated solutions.");
		return this;
	}

	public HashSet<String> getSolutionHash() {
		if(solutionHash==null){
			solutionHash = new HashSet<>();
		}
		return solutionHash;
	}
	
}
