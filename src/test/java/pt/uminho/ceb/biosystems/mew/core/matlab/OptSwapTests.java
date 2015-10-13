package pt.uminho.ceb.biosystems.mew.core.matlab;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;

@Deprecated
public class OptSwapTests {
	
	SteadyStateModel model;
	
	@Before
	public void init() throws Exception{
		//JSBMLReader reader = new JSBMLReader("/home/vmsilico/Desktop/Tools/Models/iJO1366.xml", "1",false);
		JSBMLReader reader = new JSBMLReader("/home/vmsilico/Desktop/Tools/Models/ecoli_core_model.xml", "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		//MatlabConnection conn = new MatlabConnection();
		
		//conn.init();
	}
	
	@Test
	public void rxnGeneMat(){
		SteadyStateGeneReactionModel modelGR = (SteadyStateGeneReactionModel) model;
		
//		DoubleMatrix2D matrix = DoubleFactory2D.dense.make(modelGR.getGenes().size(), modelGR.getReactions().size());
//		
//		System.out.println("SIZE GENES: " + modelGR.getGeneReactionMapping().size());
//		for (String gene : modelGR.getGeneReactionMapping().keySet()) {
//			System.out.println(gene);
//			for (String reactionInList : modelGR.getGeneReactionMapping().get(gene)) {
//				System.out.println("\t\t"+reactionInList);
//			} 
//		}
//		System.out.println("-------------------------------------------------------------");
//		ArrayList<String> arrayGenes = new ArrayList<String>(modelGR.getGeneReactionMapping().keySet());
//		ArrayList<String> arrayReactions = new ArrayList<String>(modelGR.getReactions().keySet());
//		for (String gene : modelGR.getGeneReactionMapping().keySet()) {
//			System.out.println(gene + " - " + (arrayGenes.indexOf(gene)+1));
//			for (String reactionInList : modelGR.getGeneReactionMapping().get(gene)) {
//				System.out.println("\t\t"+reactionInList + " - " + (arrayReactions.indexOf(reactionInList)+1));
//			} 
//		}
		
		for (String gene : modelGR.getGeneReactionMapping().keySet()) {
//			if(modelGR.getGeneReactionMapping().containsKey(gene)){
				for (String rxn : new HashSet<String>(modelGR.getGeneReactionMapping().get(gene))) {
					System.out.println(gene +"\t"+ rxn);
				}
//			}
		}
//		for (int i = 0; i < arrayGenes.size(); i++) {
//			System.out.println(arrayGenes.get(i));
//		}
//		
//		System.out.println("-------------------------------------------------------------");
//		System.out.println("-------------------------------------------------------------");
//		
//		for (int i = 0; i < arrayReactions.size(); i++) {
//			System.out.println(arrayReactions.get(i));
//			System.out.println(arrayReactions.indexOf(o));
//		}
//		
		
	}

//	@Test
//	public void OptSwapTest() throws Exception{
//		//OptimizationSteadyStateControlCenter.registerMethod("MATLAB_OPTKNOCK", CobraOptKnockFormulation.class);
//		OptimizationSteadyStateControlCenter.registerMethod("MATLAB_OPTSWAP", OptSwapFormulation.class);
//		
//		GeneChangesList geneList = new GeneChangesList(Arrays.asList("b0008"), Arrays.asList(10.0));
//		ReactionChangesList reactionList = new ReactionChangesList(Arrays.asList("R_DXPRIi"));
//		GeneticConditions geneCond = new GeneticConditions(reactionList, false);//geneList, (ISteadyStateGeneReactionModel)model, true);
//		
////			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_OPTKNOCK");
//		OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(null, null, model, "MATLAB_OPTSWAP");
////			
//		cc.setMaxKnockouts(3);
//		cc.addProperty("MAX_SWAP", 3);
//		//cc.setMinGrowth(0.05);
//		cc.setProductFlux("EX_succ(e)");
//		
//		Set<String> selectedReactions = new LinkedHashSet<>(model.getReactions().keySet());
//		selectedReactions.remove(model.getBiomassFlux());
//		
//		//cc.setSelectedReactions(selectedReactions);
//		Set<String> sel = new LinkedHashSet<String>();
//		sel.add("R_CO2t");
//		sel.add("R_FUM");
//		sel.add("R_H2Otm");
//		//cc.setSelectedReactions(sel);
//		SteadyStateSimulationResult result = cc.optimize();
//		
////		ArrayList<Double> fitness = new ArrayList<Double>();
////		fitness.add(result.getOFvalue());
////		
////		SteadyStateOptimizationResult optResult = new SteadyStateOptimizationResult();
////		optResult.addOptimizationResult(result, fitness);
////		
//////			double d = result.getFluxValues().get(model.getBiomassFlux());
////		System.out.println("Test OptSwap: "+result.getOFString() + " : " +result.getOFvalue());
////		System.out.println("List of KOS:");
////		for (String string : result.getGeneticConditions().getReactionList().keySet()) {
////			System.out.println(string);
////		}
//			
//	}
	

}
