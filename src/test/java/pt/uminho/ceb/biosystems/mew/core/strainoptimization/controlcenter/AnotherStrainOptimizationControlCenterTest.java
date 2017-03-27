package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.criticality.CriticalReactions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ofs.BPCYObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.IStrainOptimizationResultsSimplifier;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.simplification.StrainOptimizationSimplificationFactory;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.GOUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.RKSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solution.ROUSolution;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.GOUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.RKSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.solutionset.ROUSolutionSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;


/**
 * 
 * @author hgiesteira
 *
 */
public class AnotherStrainOptimizationControlCenterTest {
    
    @Test
	public void testUOZeroFluxesException() throws Exception {
    	
    	SolverType solverToUse = SolverType.CPLEX3;
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(10000);
		
		CriticalReactions cr = new CriticalReactions(model, null, solverToUse);
		cr.identifyCriticalReactions();

		cr.setDrainReactionsAsCritical();
		cr.setTransportReactionsAsCritical();
		
		
		FluxValueMap wtReference = SimulationProperties.simulateWT(model, null, solverToUse);
		
		Set<String> allZeroFluxes = new HashSet<String>();
		
		for (String flux : wtReference.keySet()) {
			if(wtReference.get(flux) == 0.0){
				allZeroFluxes.add(flux);
			}
		}

		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
	
		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
		objFunctions.put(objFunc1, SimulationProperties.PFBA + "T");

		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);

		// FBA
		Map<String, Object> methodConf = new HashMap<>();
		methodConf.put(SimulationProperties.METHOD_ID, SimulationProperties.PFBA);
		methodConf.put(SimulationProperties.MODEL, model);
		methodConf.put(SimulationProperties.IS_MAXIMIZATION, true);
		methodConf.put(SimulationProperties.SOLVER, solverToUse);
		methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
		methodConf.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
//		methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
//		 methodConf.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, envCond);
//		 methodConf.put(SimulationProperties.GENETIC_CONDITIONS,new GeneticConditions(new ReactionChangesList()));
		simulationConfiguration.put(SimulationProperties.PFBA + "T", methodConf);

		
		
		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "ROU");
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SPEA2");
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
		
//		System.out.println("Number of solutions:" +solutionList.size());
		
		HashMap<String, List<Double>> fluxesFromOptimization = new HashMap<String, List<Double>>();
		
		for (int i = 0; i < solutionList.size(); i++) {
			GeneticConditions geneCond = solutionList.get(i).getGeneticConditions();
//			System.out.println(geneCond + "\t" + solutionList.get(i).getAttributes());
			for (String reaction : geneCond.getReactionList().keySet()) {
				
				List<Double> values = new ArrayList<>();
				if(fluxesFromOptimization.containsKey(reaction))
					values = fluxesFromOptimization.get(reaction); 
					
				values.add(geneCond.getReactionList().get(reaction).doubleValue());
				fluxesFromOptimization.put(reaction, values);
			}
		}
		
		
		boolean solutionContainsZero = false;
		for (String flux : fluxesFromOptimization.keySet()) {
			if(allZeroFluxes.contains(flux)){
				System.err.println("Reaction: " + flux + "\tValues: " + fluxesFromOptimization.get(flux));
				for (Double d : fluxesFromOptimization.get(flux)) {
					if(d != 0.0){
						System.out.println("Reaction: " + flux + "\tValue: " + d);
						solutionContainsZero = true;
					}
				}
			}
		}
		
		Assert.assertEquals("There are wt zero fluxes that are suggested as modifications", false, solutionContainsZero); 
		
	}
    
    
    @Test
   	public void testUOMultipleReferencesException() throws Exception {
       	
       	SolverType solverToUse = SolverType.CPLEX3;
       	
       	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

   		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);

   		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

   		Container cont = new Container(reader);
   		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

   		cont.removeMetabolites(met);
   		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

   		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

   		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(5000);
   		
//   		CriticalReactions cr = new CriticalReactions(model, null, solverToUse);
//   		cr.identifyCriticalReactions();
//
//   		cr.setDrainReactionsAsCritical();
//   		cr.setTransportReactionsAsCritical();
   		
   		
//   		FluxValueMap wtReference = SimulationProperties.simulateWT(model, null, solverToUse);
//   		
//   		Set<String> allZeroFluxes = new HashSet<String>();
//   		
//   		for (String flux : wtReference.keySet()) {
//   			if(wtReference.get(flux) == 0.0){
//   				allZeroFluxes.add(flux);
//   			}
//   		}

   		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
   	
   		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
//   		NumKnockoutsObjectiveFunction objFunc2 = new NumKnockoutsObjectiveFunction(false);
//   		objFunctions.put(objFunc1, SimulationProperties.FBA + "T");
   		objFunctions.put(objFunc1, SimulationProperties.MOMA + "T");

   		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
   		Map<String, Double> of = new HashMap<>();
   		of.put(model.getBiomassFlux(), 1.0);

   		SimulationSteadyStateControlCenter simCC = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.PFBA);
   		simCC.setMaximization(true);
   		simCC.setSolver(solverToUse);
   		
   		// FBA
   		Map<String, Object> methodConfFBA1 = new HashMap<>();
   		methodConfFBA1.put(SimulationProperties.METHOD_ID, SimulationProperties.FBA);
   		methodConfFBA1.put(SimulationProperties.MODEL, model);
   		methodConfFBA1.put(SimulationProperties.IS_MAXIMIZATION, true);
   		methodConfFBA1.put(SimulationProperties.SOLVER, solverToUse);
   		methodConfFBA1.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
   		methodConfFBA1.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
//   		methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
//   		 methodConf.put(SimulationProperties.GENETIC_CONDITIONS,new GeneticConditions(new ReactionChangesList()));
//   		simulationConfiguration.put(SimulationProperties.FBA + "T", methodConfFBA1);
   		
   		
   		Map<String, Object> methodConfMOMA1 = new HashMap<>();
   		methodConfMOMA1.put(SimulationProperties.METHOD_ID, SimulationProperties.ROOM);
   		methodConfMOMA1.put(SimulationProperties.MODEL, model);
   		methodConfMOMA1.put(SimulationProperties.IS_MAXIMIZATION, true);
   		methodConfMOMA1.put(SimulationProperties.SOLVER, solverToUse);
//   		methodConfMOMA1.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
   		methodConfMOMA1.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
//   		methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
//   		 methodConf.put(SimulationProperties.GENETIC_CONDITIONS,new GeneticConditions(new ReactionChangesList()));
   		simulationConfiguration.put(SimulationProperties.MOMA + "T", methodConfMOMA1);

   		
   		
   		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
   		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "ROU");
   		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SPEA2");
   		jecoliConf.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
   		jecoliConf.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
   		jecoliConf.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, termination);
   		jecoliConf.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, objFunctions);
   		jecoliConf.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
   		jecoliConf.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, 6);
   		jecoliConf.setProperty(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, true);
//   		jecoliConf.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, cr.getCriticalReactionIds());
   		
   		ROUSolutionSet<JecoliGenericConfiguration> rouSolutionSet = (ROUSolutionSet) cc.execute(jecoliConf);
   		
   		StrainOptimizationSimplificationFactory simpFactory = new StrainOptimizationSimplificationFactory();
   		IStrainOptimizationResultsSimplifier simplifier = simpFactory.getSimplifierInstance("ROU", jecoliConf);
   		ROUSolutionSet<JecoliGenericConfiguration> newROUSolutionSet = (ROUSolutionSet<JecoliGenericConfiguration>) simplifier.getSimplifiedResultSetDiscardRepeated(rouSolutionSet);
   		
   		ArrayList<ROUSolution> solutionList = (ArrayList<ROUSolution>) newROUSolutionSet.getResultList();
   		
   		System.out.println("Number of solutions:" +solutionList.size());
   		
   		HashMap<String, List<Double>> fluxesFromOptimization = new HashMap<String, List<Double>>();
   		
   		for (int i = 0; i < solutionList.size(); i++) {
   			GeneticConditions geneCond = solutionList.get(i).getGeneticConditions();
   			System.out.println(geneCond + "\t" + solutionList.get(i).getAttributes());
   			for (String reaction : geneCond.getReactionList().keySet()) {
   				
   				List<Double> values = new ArrayList<>();
   				if(fluxesFromOptimization.containsKey(reaction))
   					values = fluxesFromOptimization.get(reaction); 
   					
   				values.add(geneCond.getReactionList().get(reaction).doubleValue());
   				fluxesFromOptimization.put(reaction, values);
   			}
   		}
   		
//   		for (String flux : fluxesFromOptimization.keySet()) {
//   			if(allZeroFluxes.contains(flux))
//   				System.err.println("Reaction: " + flux + "\tValues: " + fluxesFromOptimization.get(flux));
//   		}
   		
   	}
    
    /**
     * 
     * @param geneCondString e.g. R_FUM=0.5,R_PYK=2.0,R_ADK1=0.125,R_ACKr=0.5
     * @return
     */
    protected GeneticConditions parseGeneticCondtionsFromString(String geneCondString, boolean isOverUnder){
    	String[] rawReactions = geneCondString.trim().split(",");
    	Map<String, Double> reactionsMap = new HashMap<>();
    	
    	for (int i = 0; i < rawReactions.length; i++) {
			String[] reactions = rawReactions[i].trim().split("=");
			reactionsMap.put(reactions[0], Double.parseDouble(reactions[1]));
		}
    	
    	ReactionChangesList rcl = new ReactionChangesList(reactionsMap);
    	return new GeneticConditions(rcl, isOverUnder);
    }
    
    @Test
    public void simulationTest() throws Exception {
    	
    	SolverType solverToUse = SolverType.CPLEX3;
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-6);
		CplexParamConfiguration.setDoubleParam("TiLim", 10.0);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		GeneticConditions geneticConditions = parseGeneticCondtionsFromString("R_TKT1=2.0,R_PGL=0.0,R_PFK=0.0", true);
//		GeneticConditions geneticConditions = parseGeneticCondtionsFromString("R_FBA=0.0,R_PPCK=0.03125,R_MALS=16.0,R_GLUDy=0.0,R_PPC=0.0625,R_G6PDH2r=0.0", true);
//		GeneticConditions geneticConditions = parseGeneticCondtionsFromString("R_FBA=0.0,R_PPCK=8.0,R_ADK1=0.5,R_ACALD=4.0,R_TKT2=0.0,R_PDH=0.5", true);
//		GeneticConditions geneticConditions = parseGeneticCondtionsFromString("R_TPI=0.0,R_PPCK=4.0,R_TKT2=0.0,R_SUCDi=2.0,R_NADTRHD=2.0,R_FRD7=8.0", true);
    	
    	SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneticConditions, model, SimulationProperties.MOMA);
    	
    	cc.setSolver(solverToUse);
    	cc.setMaximization(true);
    	cc.setOverUnder2StepApproach(true);
    	
    	SteadyStateSimulationResult result = cc.simulate();
    	System.out.println(result.getOFvalue());
    	System.out.println(result.getSolutionType());
    	
//    	GeneticConditions noADK1 = parseGeneticCondtionsFromString("R_FUM=0.5,R_PYK=2.0,R_ACKr=0.5", true);
//    	cc.setGeneticConditions(noADK1);
//    	System.out.println(cc.simulate().getOFvalue());
//    	
//    	GeneticConditions koADK1 = parseGeneticCondtionsFromString("R_FUM=0.5,R_PYK=2.0,R_ACKr=0.5,R_ADK1=0.0", true);
//    	cc.setGeneticConditions(koADK1);
//    	System.out.println(cc.simulate().getOFvalue());
    	
    }
    
    
    @Test
	public void testUO2StepApproachException() throws Exception {
    	
    	SolverType solverToUse = SolverType.CPLEX3;
    	
    	String simMethod = SimulationProperties.MOMA;
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

//		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setDoubleParam("TiLim", 10.0);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(500);
		
		CriticalReactions cr = new CriticalReactions(model, null, solverToUse);
		cr.identifyCriticalReactions();

		cr.setDrainReactionsAsCritical();
		cr.setTransportReactionsAsCritical();
		
		
//		FluxValueMap wtReference = SimulationProperties.simulateWT(model, null, solverToUse);
//		
//		Set<String> allZeroFluxes = new HashSet<String>();
//		
//		for (String flux : wtReference.keySet()) {
//			if(wtReference.get(flux) == 0.0){
//				allZeroFluxes.add(flux);
//			}
//		}

		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
	
		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
		objFunctions.put(objFunc1, simMethod + "T");

		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);

		// FBA
		Map<String, Object> methodConf = new HashMap<>();
		methodConf.put(SimulationProperties.METHOD_ID, simMethod);
		methodConf.put(SimulationProperties.MODEL, model);
		methodConf.put(SimulationProperties.IS_MAXIMIZATION, true);
		methodConf.put(SimulationProperties.SOLVER, solverToUse);
		methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
		methodConf.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		simulationConfiguration.put(simMethod + "T", methodConf);

		
		
		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "ROU");
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SPEA2");
		jecoliConf.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
		jecoliConf.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		jecoliConf.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, termination);
		jecoliConf.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, objFunctions);
		jecoliConf.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		jecoliConf.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, 6);
		jecoliConf.setProperty(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, true);
		jecoliConf.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, cr.getCriticalReactionIds());
		
		ROUSolutionSet<JecoliGenericConfiguration> rouSolutionSet = (ROUSolutionSet) cc.execute(jecoliConf);
		
//		StrainOptimizationSimplificationFactory simpFactory = new StrainOptimizationSimplificationFactory();
//		IStrainOptimizationResultsSimplifier simplifier = simpFactory.getSimplifierInstance("ROU", jecoliConf);
//		System.out.println("Number of Solutions before simplification: " + rouSolutionSet.getResultList().size());
//		ROUSolutionSet<JecoliGenericConfiguration> newROUSolutionSet = (ROUSolutionSet<JecoliGenericConfiguration>) simplifier.getSimplifiedResultSetDiscardRepeated(rouSolutionSet);
		
		ArrayList<ROUSolution> solutionList = (ArrayList<ROUSolution>) rouSolutionSet.getResultList();
		
//		System.out.println("Number of solutions:" +solutionList.size());
		
		HashMap<String, List<Double>> fluxesFromOptimization = new HashMap<String, List<Double>>();
		
		for (int i = 0; i < solutionList.size(); i++) {
			GeneticConditions geneCond = solutionList.get(i).getGeneticConditions();
			System.out.println(geneCond + "\t" + solutionList.get(i).getAttributes());
			for (String reaction : geneCond.getReactionList().keySet()) {
				
				List<Double> values = new ArrayList<>();
				if(fluxesFromOptimization.containsKey(reaction))
					values = fluxesFromOptimization.get(reaction); 
					
				values.add(geneCond.getReactionList().get(reaction).doubleValue());
				fluxesFromOptimization.put(reaction, values);
			}
		}
		
		
//		boolean solutionContainsZero = false;
//		for (String flux : fluxesFromOptimization.keySet()) {
//			if(allZeroFluxes.contains(flux)){
//				System.err.println("Reaction: " + flux + "\tValues: " + fluxesFromOptimization.get(flux));
//				for (Double d : fluxesFromOptimization.get(flux)) {
//					if(d != 0.0){
//						System.out.println("Reaction: " + flux + "\tValue: " + d);
//						solutionContainsZero = true;
//					}
//				}
//			}
//		}
//		
//		Assert.assertEquals("There are wt zero fluxes that are suggested as modifications", false, solutionContainsZero); 
		
	}

    
    
    @Test
	public void testRKSolutionsWithoutSimplification() throws Exception {
    	
    	SolverType solverToUse = SolverType.CPLEX3;
    	
    	String simMethod = SimulationProperties.FBA;
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setDoubleParam("TiLim", 10.0);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(2000);
		
		CriticalReactions cr = new CriticalReactions(model, null, solverToUse);
		cr.identifyCriticalReactions();

		cr.setDrainReactionsAsCritical();
		cr.setTransportReactionsAsCritical();
		
		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
	
		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
		objFunctions.put(objFunc1, simMethod + "T");

		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);

		// FBA
		Map<String, Object> methodConf = new HashMap<>();
		methodConf.put(SimulationProperties.METHOD_ID, simMethod);
		methodConf.put(SimulationProperties.MODEL, model);
		methodConf.put(SimulationProperties.IS_MAXIMIZATION, true);
		methodConf.put(SimulationProperties.SOLVER, solverToUse);
		methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, false);
		methodConf.put(SimulationProperties.IS_OVERUNDER_SIMULATION, false);
		simulationConfiguration.put(simMethod + "T", methodConf);

		
		
		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "RK");
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SPEA2");
		jecoliConf.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
		jecoliConf.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		jecoliConf.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, termination);
		jecoliConf.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, objFunctions);
		jecoliConf.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		jecoliConf.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, 6);
		jecoliConf.setProperty(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, false);
		jecoliConf.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, cr.getCriticalReactionIds());
		
		for (int j = 0; j < 10; j++) {
			
			boolean hasInfeasible = false;
			
			RKSolutionSet<JecoliGenericConfiguration> rkSolutionSet = (RKSolutionSet) cc.execute(jecoliConf);
			
//			StrainOptimizationSimplificationFactory simpFactory = new StrainOptimizationSimplificationFactory();
//			IStrainOptimizationResultsSimplifier simplifier = simpFactory.getSimplifierInstance("ROU", jecoliConf);
//			System.out.println("Number of Solutions before simplification: " + rouSolutionSet.getResultList().size());
//			ROUSolutionSet<JecoliGenericConfiguration> newROUSolutionSet = (ROUSolutionSet<JecoliGenericConfiguration>) simplifier.getSimplifiedResultSetDiscardRepeated(rouSolutionSet);
			
			ArrayList<RKSolution> solutionList = (ArrayList<RKSolution>) rkSolutionSet.getResultList();
			
			System.out.println(solutionList.size());
			
			HashMap<String, List<Double>> fluxesFromOptimization = new HashMap<String, List<Double>>();
			
			Set<String> methods = new HashSet<String>();
			for (String methodID : jecoliConf.getSimulationConfiguration().keySet()) {
				methods.add((String)jecoliConf.getSimulationConfiguration().get(methodID).get(SimulationProperties.METHOD_ID));
				methods.add(methodID);
			}
			
			for (int i = 0; i < solutionList.size(); i++) {
		//			GeneticConditions geneCond = solutionList.get(i).getGeneticConditions();
		////			System.out.println(geneCond + "\t" + solutionList.get(i).getAttributes());
		//			for (String reaction : geneCond.getReactionList().keySet()) {
		//				
		//				List<Double> values = new ArrayList<>();
		//				if(fluxesFromOptimization.containsKey(reaction))
		//					values = fluxesFromOptimization.get(reaction); 
		//					
		//				values.add(geneCond.getReactionList().get(reaction).doubleValue());
		//				fluxesFromOptimization.put(reaction, values);
		//			}
		//			System.out.println(solutionList.get(i).getAttributes());
				
				RKSolution singularResult = solutionList.get(i);
				
//				List<IStrainOptimizationResult> results = (List<IStrainOptimizationResult>) solutionList.get(i).get.getResultList();
//				for (IStrainOptimizationResult singularResult : res.get) {
					for (String method : methods) {
						if(singularResult.getSimulationResultForMethod(method) != null){
							FluxValueMap fluxes = singularResult.getSimulationResultForMethod(method).getFluxValues();
							if(fluxes.containsValue(Double.NaN)){
								System.err.println(singularResult.getSimulationResultForMethod(method).getSolutionType());
								System.err.println(singularResult.getGeneticConditions().toUniqueString());
								hasInfeasible = true;
		//						return false;
							}
						}
					}
//				}
			
			}
			
			Assert.assertTrue("Found infeasable!", hasInfeasible == false);
			
		}	
	}
    
    
    @Test
	public void testGOUSolutionsWithoutSimplification() throws Exception {
    	
    	SolverType solverToUse = SolverType.CPLEX3;
    	
    	String simMethod = SimulationProperties.FBA;
    	
    	URL nyData = getClass().getClassLoader().getResource("models/ecoli_core_model.xml");

		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setDoubleParam("TiLim", 10.0);

		JSBMLReader reader = new JSBMLReader(nyData.getFile(), "1", false);

		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		ISteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();

		ITerminationCriteria termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(2000);
		
		CriticalReactions cr = new CriticalReactions(model, null, solverToUse);
		cr.identifyCriticalReactions();

		cr.setDrainReactionsAsCritical();
		cr.setTransportReactionsAsCritical();
		
		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
	
		BPCYObjectiveFunction objFunc1 = new BPCYObjectiveFunction(model.getBiomassFlux(), "R_EX_succ_e", "R_EX_glc_e");
		objFunctions.put(objFunc1, simMethod + "T");

		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<>();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);

		// FBA
		Map<String, Object> methodConf = new HashMap<>();
		methodConf.put(SimulationProperties.METHOD_ID, simMethod);
		methodConf.put(SimulationProperties.MODEL, model);
		methodConf.put(SimulationProperties.IS_MAXIMIZATION, true);
		methodConf.put(SimulationProperties.SOLVER, solverToUse);
		methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, true);
		methodConf.put(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		simulationConfiguration.put(simMethod + "T", methodConf);

		
		
		JecoliGenericConfiguration jecoliConf = new JecoliGenericConfiguration();
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, "GOU");
		jecoliConf.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "SA");
		jecoliConf.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, true);
		jecoliConf.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		jecoliConf.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, termination);
		jecoliConf.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, objFunctions);
		jecoliConf.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		jecoliConf.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, 6);
		jecoliConf.setProperty(GenericOptimizationProperties.IS_OVER_UNDER_EXPRESSION, true);
		jecoliConf.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, cr.getCriticalReactionIds());
		
		for (int j = 0; j < 10; j++) {
			
			GOUSolutionSet<JecoliGenericConfiguration> rkSolutionSet = (GOUSolutionSet) cc.execute(jecoliConf);
			
//			StrainOptimizationSimplificationFactory simpFactory = new StrainOptimizationSimplificationFactory();
//			IStrainOptimizationResultsSimplifier simplifier = simpFactory.getSimplifierInstance("ROU", jecoliConf);
//			System.out.println("Number of Solutions before simplification: " + rouSolutionSet.getResultList().size());
//			ROUSolutionSet<JecoliGenericConfiguration> newROUSolutionSet = (ROUSolutionSet<JecoliGenericConfiguration>) simplifier.getSimplifiedResultSetDiscardRepeated(rouSolutionSet);
			
			ArrayList<GOUSolution> solutionList = (ArrayList<GOUSolution>) rkSolutionSet.getResultList();
			
			System.out.println(solutionList.size());
			
			HashMap<String, List<Double>> fluxesFromOptimization = new HashMap<String, List<Double>>();
			
//			Set<String> methods = new HashSet<String>();
//			for (String methodID : jecoliConf.getSimulationConfiguration().keySet()) {
//				methods.add((String)jecoliConf.getSimulationConfiguration().get(methodID).get(SimulationProperties.METHOD_ID));
//				methods.add(methodID);
//			}
			
			
			Assert.assertTrue("No results!", !solutionList.isEmpty());
			
		}	
	}
    
    
}
