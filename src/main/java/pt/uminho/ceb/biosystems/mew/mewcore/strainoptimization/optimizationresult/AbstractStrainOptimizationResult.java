package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 04-03-2015.
 */
public abstract class  AbstractStrainOptimizationResult<T extends JecoliGenericConfiguration> implements IStrainOptimizationResult {
    protected T configuration;
    protected Map<String,SteadyStateSimulationResult> simulationResultMap;
    protected GeneticConditions solutionGeneticConditions;

    public AbstractStrainOptimizationResult(T configuration, Map<String, SteadyStateSimulationResult> simulationResultMap, GeneticConditions solutionGeneticConditions) {
        this.configuration = configuration;
        this.simulationResultMap = simulationResultMap;
        this.solutionGeneticConditions = solutionGeneticConditions;
    }

    public AbstractStrainOptimizationResult(T configuration, GeneticConditions solutionGeneticConditions) {
        this.configuration = configuration;
        this.simulationResultMap = new HashMap<>();
        this.solutionGeneticConditions = solutionGeneticConditions;
    }



    @Override
    public SteadyStateSimulationResult getFluxDistribution(String method) throws Exception {
        SteadyStateSimulationResult result = simulationResultMap.get(method);
        if(result == null){
            Map<String,Object> propertyMap = configuration.getPropertyMapCopy();
            EnvironmentalConditions environmentalConditions = (EnvironmentalConditions) configuration.getProperty(JecoliOptimizationProperties.ENVIRONMENTAL_CONDITIONS);
            ISteadyStateModel model = (ISteadyStateModel) configuration.getProperty(JecoliOptimizationProperties.STEADY_STATE_MODEL);
            SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions,solutionGeneticConditions,model,method);
            cc.setSolver(configuration.getSolver());
            cc.setMaximization(configuration.getIsMaximization());
            result = cc.simulate();
            simulationResultMap.put(method,result);
        }

        return result;
    }

    @Override
    public GeneticConditions getGeneticConditions(){
        return solutionGeneticConditions;
    }

    //1 conf por metodo de sim
    //No futuro refactoring do Map<String,Object> para config
    @Override
    public Set<Map<String,Object>> getConfigurationSetForSimulation(){
        Set<Map<String,Object>> propertyMapSet = new HashSet<>();
        List<String> simulationMethodList = configuration.getSimulationMethodList();

        for(String simulationMethod:simulationMethodList){
            Map<String,Object> propertyMap = configuration.getPropertyMapCopy();
            propertyMap.put(SimulationProperties.GENETIC_CONDITIONS,solutionGeneticConditions);
            propertyMap.put(SimulationProperties.METHOD_NAME,simulationMethod);
            propertyMap.put(SimulationProperties.IS_OVERUNDER_SIMULATION,configuration.getIsOverUnderExpression());
            propertyMap.put(SimulationProperties.OVERUNDER_REFERENCE_FLUXES,configuration.getReferenceFluxDistribution()); //Ver este reference
            propertyMap.put(SimulationProperties.OVERUNDER_2STEP_APPROACH,configuration.getOu2StepApproach());
            propertyMapSet.add(propertyMap);
        }

        return propertyMapSet;
    }

    protected void writeMapOf2SimMap(OutputStreamWriter outputStream, IndexedHashMap<IObjectiveFunction, String> mapOf2SimMap) throws Exception {
        int counter = 0;
        for(IObjectiveFunction objectiveFunction:mapOf2SimMap.keySet()) {
            String simulationMethod = mapOf2SimMap.get(objectiveFunction);
            SteadyStateSimulationResult simulationResult = getFluxDistribution(simulationMethod);
            if(counter > 0)
                outputStream.write(",");
            outputStream.write(String.valueOf(simulationResult.getOFvalue()));
            counter++;
        }
        outputStream.write(",");
        outputStream.flush();
    }


}
