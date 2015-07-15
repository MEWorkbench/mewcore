package pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.io;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.FluxValueObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter.StrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKRSSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOMConfig;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Created by ptiago on 31-03-2015.
 */
public class RKRSStrategyReaderTest extends AbstractStrategyReader {
    @Test
    public void readSolutionFile() throws Exception {
        List<Pair<Double,List<Pair<String,Double>>>>  originalSolutionList =createOriginalSolutionList();
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        JecoliEASCOMConfig config = new JecoliEASCOMConfig();
        config.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM,"SA");
        String resultFileLocation = "./TestData/RKRS.data";
        String modelFile = "./TestModel/iJO1366_swaps.xml";
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
        config.setOptimizationStrategy("RK");
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

        IStrainOptimizationResultSet newResultSet = new RKRSSolutionSet(config);
        newResultSet.readSolutionsFromFile(resultFileLocation);
        assertTrue(validateSolution(false,originalSolutionList,newResultSet));
    }

    protected List<Pair<Double, List<Pair<String, Double>>>> createOriginalSolutionList() {
        final Pair<Double,List<Pair<String, Double>>> solutionPair = new Pair<Double,List<Pair<String, Double>>>(1.3778761266912078,
                new ArrayList<Pair<String,Double>>(){{
                    add(new Pair<String,Double>("R_GND",0.0));
                    add(new Pair<String,Double>("R_GAPD_swap",0.0));
                    add(new Pair<String,Double>("R_ASAD_swap",0.0));
                    add(new Pair<String,Double>("R_ICDHyr_swap",0.0));
                    add(new Pair<String,Double>("R_PGCD_swap",0.0));
                    add(new Pair<String,Double>("R_DHDPRy_swap",0.0));
                    add(new Pair<String,Double>("R_ACALD_swap",0.0));
                    add(new Pair<String,Double>("R_KARA1_swap",0.0));
                    add(new Pair<String,Double>("R_LDH_D_swap",0.0));
                    add(new Pair<String,Double>("R_LCARR_swap",0.0));
                    add(new Pair<String,Double>("R_KARA1_swap",0.0));
                    add(new Pair<String,Double>("R_GLUDy_swap",0.0));
                    add(new Pair<String,Double>("R_EX_hdcea_LPAREN_e_RPAREN_",0.0));
                    add(new Pair<String,Double>("R_G6PDH2r_swap",0.0));
                    add(new Pair<String,Double>("R_SHK3Dr_swap",0.0));
                    add(new Pair<String,Double>("R_IPMD",0.0));
                    add(new Pair<String,Double>("R_HSDy_swap",0.0));
                    add(new Pair<String,Double>("R_PDH_swap",0.0));
                    add(new Pair<String,Double>("R_NADH16pp_swap",0.0));
                    add(new Pair<String,Double>("R_MTHFD_swap",0.0));
                    add(new Pair<String,Double>("R_ALCD2x_swap",0.0));
                }});


        return new ArrayList<Pair<Double, List<Pair<String, Double>>>>(){{
            add(solutionPair);
        }};
    }
}