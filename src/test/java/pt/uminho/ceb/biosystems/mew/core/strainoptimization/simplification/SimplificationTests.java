package pt.uminho.ceb.biosystems.mew.core.strainoptimization.simplification;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class SimplificationTests {

	public static void main(String[] args) throws Exception {
		
//		simplification();
		
		anotherTest();
		
	}
	
//	private static void simplification() throws Exception{
//		OverUnderSolutionSet set = new OverUnderSolutionSet();
//		set.loadFromCSVFile("/home/hgiesteira/Documents/simplificationTestsData/model2#[model2_v4_7AHPT_lumped,fructose_aerobic,NONE,BASE,7ahpt,FBA_LMOMA,SPEA2,WYIELD_BPCY,20150903_1935,RK,CDT]#run17.ss");
//		
//		EnvironmentalConditions conditions = EnvironmentalConditions.readFromFile("/home/hgiesteira/Documents/simplificationTestsData/envcond#model2_v4_7AHPT_lumped#[fructose_aerobic].env", ",");
//		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Documents/simplificationTestsData/model2_v4_7AHPT_lumped.xml", "model");
//		Container container = new Container(reader);
//		Set<String> toRemove = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
//		container.removeMetabolites(toRemove);
//		container.putDrainsInReactantsDirection();
//		
//		ISteadyStateModel model = ContainerConverter.convert(container);
//		model.setBiomassFlux("R_Biomass_Clim");
//		
//		IndexedHashMap<IObjectiveFunction,String> ofs = new IndexedHashMap<>();
//		
//		BPCYObjectiveFunction bpcy = new BPCYObjectiveFunction("R_Biomass_Clim", "R_step_drain", "R_EX_fru_");
//		FVASenseObjectiveFunction fva = new FVASenseObjectiveFunction("R_Biomass_Clim", "R_step_drain", true, false, SolverType.CPLEX3);
//		ofs.put(fva, SimulationProperties.FBA);
//		ofs.put(bpcy, SimulationProperties.LMOMA);
//		
//		//OldSimp
//		SolutionSimplificationMulti oldSimp = new SolutionSimplificationMulti(model, ofs, null, conditions, SolverType.CPLEX3);
//		
//		long init = System.currentTimeMillis();
//		
//		for(int i=0; i<set.size(); i++){
//			List<Pair<String, Double>> solList = set.getSolution(i);
//			ReactionChangesList rcl = new ReactionChangesList();
//			for(Pair<String,Double> p : solList)
//				rcl.addReaction(p);
//			GeneticConditions gc = new GeneticConditions(rcl);
//			
//			SolutionSimplificationResult simRes = oldSimp.simplifyReactionsSolution(gc);
//			GeneticConditions simpGC = simRes.getSimplifiedSolution();
////			System.out.println(simpGC.toString());
//		}
//		//
//		
//		System.out.println("TIME ELAPSED: " + (System.currentTimeMillis() - init));
//		System.err.println("---------------------------------------------------------------------------------------------------------------------------");
//		
//		long init2 = System.currentTimeMillis();
//		//NewSimp
//		JecoliGenericConfiguration configuration = new JecoliGenericConfiguration();
//		configuration.setEnvironmentalConditions(conditions);
//		configuration.setModel(model);
//		configuration.setMapOF2Sim(ofs);
//		configuration.setSolver(SolverType.CPLEX3);
//		
////		RKSolutionSimplifier newSimp = new RKSolutionSimplifier(configuration);
//		
//		for(int i=0; i<set.size(); i++){
//			List<Pair<String, Double>> solList = set.getSolution(i);
//			ReactionChangesList rcl = new ReactionChangesList();
//			for(Pair<String,Double> p : solList)
//				rcl.addReaction(p);
//			GeneticConditions gc = new GeneticConditions(rcl);
//			
//			GeneticConditionSimplifiedResult geneticCond = null;
//			
//			if(gc.isGenes()){
//				GenesSimplifier simp = new GenesSimplifier((ISteadyStateGeneReactionModel)model, null, conditions, configuration.getSolver());
//				geneticCond = (GeneticConditionSimplifiedResult) simp.simplifyGeneticConditions(gc, configuration.getObjectiveFunctionsMap());
//			}else{
//				ReactionsSimplifier simp = new ReactionsSimplifier(model, null, conditions, configuration.getSolver());
//				geneticCond = (GeneticConditionSimplifiedResult)simp.simplifyGeneticConditions(gc, configuration.getObjectiveFunctionsMap());
//			}
//			
//			System.out.println(geneticCond.getSimplifiedGeneticConditions());
//			
////			System.out.println(simpGC.toString());
//		}
//		//
//		System.out.println("TIME ELAPSED: " + (System.currentTimeMillis() - init2));
//	}
	
	private static void anotherTest() throws Exception{
		System.out.println(SimplificationTests.class.getResource("resources/models/ecoli_core_model.xml"));
		JSBMLReader reader = new JSBMLReader(SimplificationTests.class.getResource("/mewcore/src/test/resources/models/ecoli_core_model.xml").toString(), "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
	}

}
