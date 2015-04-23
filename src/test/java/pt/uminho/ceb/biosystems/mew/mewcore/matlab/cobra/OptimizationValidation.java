package pt.uminho.ceb.biosystems.mew.mewcore.matlab.cobra;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.CommandExecutionException;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.optimization.CobraOptKnockFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.controlcenter.OptimizationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class OptimizationValidation {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}

	SteadyStateModel model;
	
	@Before
	public void init() throws Exception{
		JSBMLReader reader = new JSBMLReader(getFile("models/iMM904_flux.xml"), "1",false);
		
		//JSBMLReader reader = new JSBMLReader("C:\\Users\\Programador\\Desktop\\Uminho\\2ano\\Tese\\Outros\\PAPER\\iMM904\\iMM904_struct_corr_itacon_drain.xml", "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		MatlabConnection conn = new MatlabConnection();
		
		conn.init();
	}
	
	//@Test
	public void cobraOptKnock() throws Exception{
		//OptimizationSteadyStateControlCenter.registerMethod("MATLAB_OPTKNOCK", CobraOptKnockFormulation.class);
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_OPTKNOCK", CobraOptKnockFormulation.class);
		
		GeneChangesList geneList = new GeneChangesList(Arrays.asList("b0008"), Arrays.asList(10.0));
		ReactionChangesList reactionList = new ReactionChangesList(Arrays.asList("R_DXPRIi"));
		GeneticConditions geneCond = new GeneticConditions(reactionList, false);//geneList, (ISteadyStateGeneReactionModel)model, true);
		
//		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_OPTKNOCK");
		OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(null, null, model, OptimizationProperties.COBRA_OPTKNOCK);
//		
		cc.setMaxKnockouts(3);
		cc.setMinGrowth(0.05);
		cc.setProductFlux("R_EX_mal_L_e_");
		
		Set<String> selectedReactions = new LinkedHashSet<>(model.getReactions().keySet());
		selectedReactions.remove(model.getBiomassFlux());
		
		cc.setSelectedReactions(selectedReactions);
		Set<String> sel = new LinkedHashSet<String>();
		sel.add("R_CO2t");
		sel.add("R_FUM");
		sel.add("R_H2Otm");
		//cc.setSelectedReactions(sel);
		SteadyStateSimulationResult result = cc.optimize();
		
		ArrayList<Double> fitness = new ArrayList<Double>();
		fitness.add(result.getOFvalue());
		
		SteadyStateOptimizationResult optResult = new SteadyStateOptimizationResult();
		optResult.addOptimizationResult(result, fitness);
		
//		double d = result.getFluxValues().get(model.getBiomassFlux());
		System.out.println("Test OptKnock: "+result.getOFString() + " : " +result.getOFvalue());
		System.out.println("List of KOS:");
		for (String string : result.getGeneticConditions().getReactionList().keySet()) {
			System.out.println(string);
		}
		
	}
	
	//@Test
	public void cobraTestGDLS(){
		try{
			//SimulationSteadyStateControlCenter.registerMethod("MATLAB_GDLS", CobraGDLSFormulation.class);
			
			EnvironmentalConditions envCond = new EnvironmentalConditions();
			envCond.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, 0.0));
			GeneChangesList geneList = new GeneChangesList(Arrays.asList("b0008"), Arrays.asList(10.0));
			ReactionChangesList reactionList = new ReactionChangesList(Arrays.asList("R_EX_o2"));
			GeneticConditions geneCond = new GeneticConditions(reactionList, false);//geneList, (ISteadyStateGeneReactionModel)model, true);
			
			//SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_GDLS");
			OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(null, null, model, OptimizationProperties.COBRA_GDLS);
			
			cc.setSearchPaths(2);
			cc.setMinGrowth(0.05);
			cc.setNeighborhoodSize(1);
			cc.setMaxKnockouts(2);
			cc.setProductFlux("R_EX_succ_e_");
			
			Set<String> selectedReactions = new LinkedHashSet<>(model.getReactions().keySet());
			selectedReactions.remove(model.getBiomassFlux());
			
			cc.setSelectedReactions(selectedReactions);
			
			SteadyStateSimulationResult result = cc.optimize();
			
			
	//		double d = result.getFluxValues().get(model.getBiomassFlux());
			//System.out.println("Test GDLS: "+result.getOFvalue());
			System.out.println("List of KOS:");
			for (String string : result.getGeneticConditions().getReactionList().keySet()){
				System.out.println(string);
			}
			
			//SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, geneCond, model, "MATLAB_MOMA");
			//cc.setMaximization(true);
			
			//SteadyStateSimulationResult result = cc.simulate();
			
	//		double d = result.getFluxValues().get(model.getBiomassFlux());
	//		
	//		System.out.println("Test MOMA: "+result.getOFString() +" " + d);
	//		
	//		long tEnd = System.currentTimeMillis();
	//		long tDelta = tEnd - tStart;
	//		double elapsedSeconds = tDelta / 1000.0;
	//		
	//		System.out.println("Time elapsed: " + elapsedSeconds);
			
	//		form.simulate();
		}catch(Exception e){
			System.out.println("AQUIIIIIIIIIIIIIIIII");
			System.out.println(e.getClass().getName());
			if(e instanceof MatlabConnectionException)
				System.out.println("A problem occur while connection with MATLAB\nPlease make sure all configurations are correct.");
			if(e instanceof CommandExecutionException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			if(e instanceof MatlabInvocationException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			e.printStackTrace();
		}
	}
}
