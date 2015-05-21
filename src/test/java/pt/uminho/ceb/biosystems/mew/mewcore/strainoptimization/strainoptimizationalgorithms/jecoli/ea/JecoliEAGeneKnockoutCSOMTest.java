package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.solutionset.GKSolutionSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOMConfig;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 26-03-2015.
 */
public class JecoliEAGeneKnockoutCSOMTest {

    @Test
    public void executeAlgorithmTest() throws Exception {
        JecoliEAGeneKnockoutCSOM algorithm = new JecoliEAGeneKnockoutCSOM();

        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        config.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,"SA");

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
        config.setOptimizationStrategy("GK");
        config.setOptimizationStrategyConverter(new JecoliGKConverter<>());
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
        config.setMaxSetSize(10);
        algorithm.setAlgorithmConfiguration(config);
        IStrainOptimizationResultSet resultSet = (IStrainOptimizationResultSet) algorithm.execute();
        resultSet.writeToFile("PT");

        IStrainOptimizationResultSet newResultSet = new GKSolutionSet(config);
        newResultSet.readSolutionsFromFile("PT");


        algorithm.execute();
    }
}
