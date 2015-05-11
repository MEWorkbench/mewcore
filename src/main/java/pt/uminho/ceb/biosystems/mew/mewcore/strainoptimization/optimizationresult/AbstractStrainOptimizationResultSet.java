package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 04-03-2015.
 */
//public abstract class AbstractStrainOptimizationResultSet<T extends JecoliGenericConfiguration, E extends  IStrainOptimizationResult<T>> implements IStrainOptimizationResultSet<T,E> {
public abstract class AbstractStrainOptimizationResultSet<T extends JecoliGenericConfiguration, E extends  IStrainOptimizationResult> implements IStrainOptimizationResultSet<T,E> {
    protected T baseConfiguration;
    protected List<E> resultList;
    public static SolutionReaderFactory solutionReaderFactory;

    static{
        solutionReaderFactory = new SolutionReaderFactory();
    }


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
        for(IStrainOptimizationResult result:resultList)
            result.write(fd);
        fd.close();
    }

    @Override
    public void  readSolutionsFromFile(String file) throws Exception {
        Scanner sc = new Scanner(new File(file));
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] lineArray = line.split(",,");
            String objectiveValueFunctionString = lineArray[0];
            double[] objectiveFunctionValueArray = computeObjectiveFunctionValueArray(objectiveValueFunctionString);
            String solutionString = lineArray[1];
            GeneticConditions gc = solutionReaderFactory.getSolution(baseConfiguration, solutionString);
            resultList.add(createSolution(baseConfiguration,objectiveFunctionValueArray,gc));
        }
    }

    protected double[] computeObjectiveFunctionValueArray(String objectiveValueFunctionString){
        String[] valueArray = objectiveValueFunctionString.split(",");
        double[] objectiveFunctionArray = new double[valueArray.length];
        for(int i = 0; i < objectiveFunctionArray.length;i++)
            objectiveFunctionArray[i] = Double.valueOf(valueArray[i]);
        return objectiveFunctionArray;
    }

    protected void constructSimulationResultMap(JecoliGenericConfiguration baseConfiguration,E newSolution) throws Exception {
        IndexedHashMap<IObjectiveFunction,String> mapOf2Sim = baseConfiguration.getMapOf2Sim();
        List<IObjectiveFunction> objectiveFunctionList = mapOf2Sim.getIndexArray();
        for(IObjectiveFunction objectiveFunction:objectiveFunctionList){
            String simulationMethod = mapOf2Sim.get(objectiveFunction);
            newSolution.getFluxDistribution(simulationMethod);
        }
    }

}
