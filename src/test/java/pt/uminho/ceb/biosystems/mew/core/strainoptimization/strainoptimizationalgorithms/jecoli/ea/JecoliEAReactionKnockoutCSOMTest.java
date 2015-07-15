package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter.StrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOMConfig;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 26-03-2015.
 */
public class JecoliEAReactionKnockoutCSOMTest {

    public void executeAlgorithm(String algorithm,String strategy) throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        config.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,algorithm);
        config.setOptimizationStrategy(strategy);
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        EnvironmentalConditions env = new EnvironmentalConditions();
        env.addReactionConstraint("R_EX_glc_e_",new ReactionConstraint(-10,0));
        config.setModel(model);
        config.setMaxSetSize(2);
        config.setNotAllowedIds(notAllowedIDs);

        //config.setOptimizationStrategyConverter(new JecoliRKConverter<>());
        config.setIsVariableSizeGenome(true);
        config.setEnvironmentalConditions(env);
        config.setSolver(SolverType.CPLEX3);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");
        config.setSimulationMethod(simulationMethodList);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setOu2StepApproach(true);
        config.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA,new NumFunctionEvaluationsListenerHybridTerminationCriteria(100));
/*
        IStrainOptimizationResultSet resultSet = (IStrainOptimizationResultSet) cc.execute(config);
        resultSet.writeToFile("PT");

        IStrainOptimizationResultSet newResultSet = new RKSolutionSet(config);

        newResultSet.readSolutionsFromFile("PT");*/
        System.out.println("PT");
    }

    @Test
    public void testEAAlgorithm() throws Exception {
        executeAlgorithm("EA","RK");
        executeAlgorithm("EA","GK");
        executeAlgorithm("EA","ROU");
        executeAlgorithm("EA","GOU");
    }

    @Test
    public void testSAAlgorithm() throws Exception {
        executeAlgorithm("SA","RK");
        executeAlgorithm("SA","GK");
        executeAlgorithm("SA","ROU");
        executeAlgorithm("SA","GOU");
    }

    @Test
    public void testSPEA2lgorithm() throws Exception {
        executeAlgorithm("SPEA2","RK");
        executeAlgorithm("SPEA2","GK");
        executeAlgorithm("SPEA2","ROU");
        executeAlgorithm("SPEA2","GOU");
    }
}
