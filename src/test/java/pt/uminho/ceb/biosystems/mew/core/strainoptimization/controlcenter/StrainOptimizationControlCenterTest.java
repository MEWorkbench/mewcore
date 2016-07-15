package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.BPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationSimplificationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

/**
 * Created by ptiago on 26-03-2015.
 */
public class StrainOptimizationControlCenterTest {
    @Test
    public void propertySetControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SPEA2");
        String algorithmValue = (String) cc.getProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM);
        assert(algorithmValue.compareTo("SPEA2") == 0);
    }

    @Test (expected = Exception.class)
    public void noPropertySetControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void invalidAlgorithmControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SPEA1020");
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY,"RK");
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void invalidStrategyControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"SA");
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY,"RKA");
        cc.execute();
    }

    @Test (expected = Exception.class)
    public void strategyNotDefinedControlCenterTest() throws Exception {
        StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
        cc.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM,"EA");
        cc.execute();
    }
    
    @Test
	public void testUOCriticalException() throws Exception {
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(100);
		
//		CriticalReactions cr = new CriticalReactions(model, null, SolverType.CPLEX3);
//		cr.identifyCriticalReactions();
//
//		cr.setDrainReactionsAsCritical();
//		cr.setTransportReactionsAsCritical();

		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
	
		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
//		WeightedBPCYObjectiveFunction objFunc1 = new WeightedBPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", 0.0, SolverType.CPLEX3); 
		objFunctions.put(objFunc1, SimulationProperties.FBA + "T");

		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);

		EnvironmentalConditions envCond = new EnvironmentalConditions();
		envCond.addReactionConstraint("R_PGK", new ReactionConstraint(0.0, 0.0));
		envCond.addReactionConstraint("R_PGM", new ReactionConstraint(0.0, 0.0));
		envCond.addReactionConstraint(model.getBiomassFlux(), new ReactionConstraint(0.87, 0.0));
		
		
		SimulationSteadyStateControlCenter simCC = new SimulationSteadyStateControlCenter(envCond, null, model, SimulationProperties.PFBA);
		simCC.setMaximization(true);
		simCC.setSolver(SolverType.CLP);
		
//		SteadyStateSimulationResult referenceSimulation = simCC.simulate();
//		FluxValueMap fvm = referenceSimulation.getFluxValues();
		
		// FBA
		Map<String, Object> methodConf = new HashMap<>();
		methodConf.put(SimulationProperties.METHOD_ID, SimulationProperties.FBA);
		methodConf.put(SimulationProperties.MODEL, model);
		methodConf.put(SimulationProperties.IS_MAXIMIZATION, true);
		methodConf.put(SimulationProperties.SOLVER, SolverType.CLP);
		methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
		methodConf.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
//		methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
		 methodConf.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, envCond);
//		 methodConf.put(SimulationProperties.GENETIC_CONDITIONS,new GeneticConditions(new ReactionChangesList()));
		simulationConfiguration.put(SimulationProperties.FBA + "T", methodConf);

		
		
		
		
		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "ROU");
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SA");
		jecoliConf.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
		jecoliConf.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		jecoliConf.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, termination);
		jecoliConf.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, objFunctions);
		jecoliConf.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		jecoliConf.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, 6);
		jecoliConf.setProperty(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, true);
//		jecoliConf.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, cr.getCriticalReactionIds());
		
		ROUSolutionSet<JecoliGenericConfiguration> rouSolutionSet = (ROUSolutionSet) cc.execute(jecoliConf);
		
		StrainOptimizationSimplificationFactory simpFactory = new StrainOptimizationSimplificationFactory();
		IStrainOptimizationResultsSimplifier simplifier = simpFactory.getSimplifierInstance("ROU", jecoliConf);
		ROUSolutionSet<JecoliGenericConfiguration> newROUSolutionSet = (ROUSolutionSet<JecoliGenericConfiguration>) simplifier.getSimplifiedResultSetDiscardRepeated(rouSolutionSet);
		
		ArrayList<ROUSolution> solutionList = (ArrayList<ROUSolution>) newROUSolutionSet.getResultList();
		
		System.out.println("Number of solutions:" +solutionList.size());
		
		for (int i = 0; i < solutionList.size(); i++) {
			System.out.println(solutionList.get(i).getGeneticConditions() + "\t" + solutionList.get(i).getAttributes());
		}
		
	}


}
