package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEACSOMConfig;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 26-03-2015.
 */
public class JecoliSCOMConfigTest {

    @Test(expected = InvalidConfigurationException.class)
    public void eaDefaultConfigurations() throws InvalidConfigurationException {
        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.validate();
    }

    @Test
    public void eaMandatoryPropertiesTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingModelTest() throws Exception {
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingStrategyTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        //config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingVariableSizeGenomeTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        //config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingEnvironmentalConditionsTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        //config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingSolverTypeTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        //config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingIsMaximizationTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        //config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingMapOf2SimTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        //config.setMapOF2Sim(objectiveMap);
        config.setSimulationMethod(simulationMethodList);

        config.validate();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void eaMandatoryPropertiesMissingSimulationMethodTest() throws Exception {
        String modelFile = "/home/ptiago/Silico/IPCRES/Propanediol/iMM904_Methylglyoxal.xml";
        JSBMLReader modelReader = new JSBMLReader(modelFile,"PT",false);
        Container container = new Container(modelReader);
        List<String> notAllowedIDs = new ArrayList<>();
        IndexedHashMap<IObjectiveFunction,String> objectiveMap = new IndexedHashMap<>();
        objectiveMap.put(new FluxValueObjectiveFunction("R_biomass_published",true),"FBA");
        container.verifyDepBetweenClass();
        ISteadyStateModel model = ContainerConverter.convert(container);
        List<String> simulationMethodList = new ArrayList<>();
        simulationMethodList.add("FBA");

        JecoliEACSOMConfig config = new JecoliEACSOMConfig();
        config.setOptimizationStrategy("RK");
        config.setIsVariableSizeGenome(true);
        config.setModel(model);
        config.setEnvironmentalConditions(new EnvironmentalConditions());
        config.setSolver(SolverType.CPLEX3);
        config.setIsMaximization(true);
        config.setMapOF2Sim(objectiveMap);
        //config.setSimulationMethod(simulationMethodList);

        config.validate();
    }




}
