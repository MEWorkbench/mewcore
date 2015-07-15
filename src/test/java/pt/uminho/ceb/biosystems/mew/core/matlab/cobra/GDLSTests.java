package pt.uminho.ceb.biosystems.mew.core.matlab.cobra;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.criticality.CriticalReactions;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.CommandExecutionException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.ConstrainedReaction;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.OptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.optimization.controlcenter.OptimizationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;

public class GDLSTests {
	
	SteadyStateModel model;

	@Before
	public void init() throws Exception{
		//JSBMLReader reader = new JSBMLReader("files/models/Ec_iJR904.xml", "1",false);
		
		//JSBMLReader reader = new JSBMLReader("/home/vmsilico/Desktop/Tools/Models/Ec_iAF1260.xml", "1",false);
		JSBMLReader reader = new JSBMLReader("/home/vmsilico/Documents/Files/glds_giesteira/CN127_7AHPT.xml", "1",false);
		
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		model.setBiomassFlux("R_Biomass");
		
		MatlabConnection conn = new MatlabConnection();
		
		conn.init();
	}
	
	@Test
	public void cobraTestGDLS(){
		try{
			//SimulationSteadyStateControlCenter.registerMethod("MATLAB_GDLS", CobraGDLSFormulation.class);
			
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(envCond, null, model, OptimizationProperties.COBRA_GDLS);
			
			cc.setSearchPaths(4);
			cc.setMinGrowth(0.05);
			cc.setNeighborhoodSize(4);
			cc.setMaxKnockouts(25);
			cc.setProductFlux("R_EX_pbhb_e_");
			cc.setTimeLimit(7200);
			//Set<String> selected = new TreeSet<>();
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			
			Set<String> selectedReactions = new TreeSet<String>(model.getReactions().keySet());
			
			CriticalReactions cReactions = new CriticalReactions(model, null, null);
			cReactions.loadCriticalReactionsFromFile("/home/vmsilico/Documents/Files/glds_giesteira/critical.txt");
			
			selectedReactions.removeAll(cReactions.getCriticalReactionIds());
			
			Set<String> selectedRxns = new TreeSet<String>();
			selectedRxns.addAll(Arrays.asList("R_BAPYRT", "R_ACCOAAT1", "R_PYC", "R_GLUR", "R_3OXAPT", "R_SUCOAS", "R_GABAT1", "R_PPC", "R_HISAL", "R_PPS", "R_TRPD", "R_FABC171_CP", "R_ICL"));
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			cc.setSelectedReactions(selectedReactions);
			
			
			
			SteadyStateSimulationResult result = cc.optimize();
					
			
	//		double d = result.getFluxValues().get(model.getBiomassFlux());
			//System.out.println("Test GDLS: "+result.getOFvalue());
			System.out.println("List of KOS:");
			for (String string : result.getGeneticConditions().getReactionList().keySet()){
				System.out.println(string);
			}
			
			System.out.println(result.getOFvalue());
			
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
			if(e instanceof MatlabConnectionException)
				System.out.println("A problem occur while connection with MATLAB\nPlease make sure all configurations are correct.");
			if(e instanceof CommandExecutionException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			if(e instanceof MatlabInvocationException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void cobraTestOptKnock(){
		try{
			//SimulationSteadyStateControlCenter.registerMethod("MATLAB_GDLS", CobraGDLSFormulation.class);
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(envCond, null, model, OptimizationProperties.COBRA_OPTKNOCK);
			
			cc.setMinGrowth(0.05);
			cc.setMaxKnockouts(21);
			cc.setProductFlux("R_EX_pbhb_e_");
			cc.setTimeLimit(7200);
			//Set<String> selected = new TreeSet<>();
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			
			Set<String> selectedReactions = new TreeSet<String>(model.getReactions().keySet());
			selectedReactions.remove("R_Biomass");
			
			CriticalReactions cReactions = new CriticalReactions(model, null, null);
			cReactions.loadCriticalReactionsFromFile("/home/vmsilico/Documents/Files/glds_giesteira/critical.txt");
			
			//selectedReactions.removeAll(cReactions.getCriticalReactionIds());
			
			Set<String> selectedRxns = new TreeSet<String>();
			selectedRxns.addAll(Arrays.asList("R_BAPYRT", "R_NUTD11", "R_PYC", "R_ADNK4", "R_ADNK1", "R_PPNAK", "R_ACORND", "R_PGLCM", "R_DCTPDA1", "R_GLUR", "R_NUDPK1", "R_PTA", "R_IMPDH", "R_SUCOAS", "R_NUDPK5", "R_3OACT", "R_CITS", "R_NUDPK3", "R_R00227", "R_MDH1", "R_NUDPK8"));
			
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			cc.setSelectedReactions(selectedRxns);
			cc.setConstrainedReactions(new TreeSet<ConstrainedReaction>());
			
			
			
			SteadyStateSimulationResult result = cc.optimize();
					
			
	//		double d = result.getFluxValues().get(model.getBiomassFlux());
			//System.out.println("Test GDLS: "+result.getOFvalue());
			System.out.println("List of KOS:");
			for (String string : result.getGeneticConditions().getReactionList().keySet()){
				System.out.println(string);
			}
			
			System.out.println(result.getOFvalue());
			
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
			if(e instanceof MatlabConnectionException)
				System.out.println("A problem occur while connection with MATLAB\nPlease make sure all configurations are correct.");
			if(e instanceof CommandExecutionException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			if(e instanceof MatlabInvocationException)
				System.out.println("A problem occur while sending the command to MATLAB\nPlease make sure all the input parameters are correct.");
			e.printStackTrace();
		}
	}

	
	
	@Test
	public void cobraTestNewOptKnock(){
		try{
			//SimulationSteadyStateControlCenter.registerMethod("MATLAB_GDLS", CobraGDLSFormulation.class);
			
			//model.setBiomassFlux("R_Biomass");
			
			//EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			OptimizationSteadyStateControlCenter cc = new OptimizationSteadyStateControlCenter(null, null, model, OptimizationProperties.COBRA_OPTKNOCK);
			
			cc.setMinGrowth(0.05);
			cc.setMaxKnockouts(21);
			cc.setProductFlux("R_EX_succ_e_");
			cc.setTimeLimit(7200);
			//Set<String> selected = new TreeSet<>();
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			
			Set<String> selectedReactions = new TreeSet<String>(model.getReactions().keySet());
			selectedReactions.remove("R_Ec_biomass_iAF1260_core_59p81M");
			
			//CriticalReactions cReactions = new CriticalReactions(model, null, null);
			//cReactions.loadCriticalReactionsFromFile("/home/vmsilico/Documents/Files/glds_giesteira/critical.txt");
			
			//selectedReactions.removeAll(cReactions.getCriticalReactionIds());
			
			
			//selected.add("R_EX_succ_LPAREN_e_RPAREN_");
			cc.setSelectedReactions(selectedReactions);
			cc.setConstrainedReactions(new TreeSet<ConstrainedReaction>());
			
			
			
			SteadyStateSimulationResult result = cc.optimize();
					
			
	//		double d = result.getFluxValues().get(model.getBiomassFlux());
			//System.out.println("Test GDLS: "+result.getOFvalue());
			System.out.println("List of KOS:");
			for (String string : result.getGeneticConditions().getReactionList().keySet()){
				System.out.println(string);
			}
			
			System.out.println(result.getOFvalue());
			
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
