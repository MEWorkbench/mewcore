package pt.uminho.ceb.biosystems.mew.mewcore.matlab.cobra;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.formulations.cobra.simulation.CobraFBAFormulation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

public class AnotherSimulationTests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}

	@Test
	public void FBASimulation1() {
		try{
			JSBMLReader reader = new JSBMLReader("/home/vmsilico/Documents/Files/glds_giesteira/CN127_7AHPT.xml", "1",false);
			
			Container cont = new Container(reader);
			Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
	
			cont.removeMetabolites(met);
			SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
			model.setBiomassFlux("R_Biomass");
			
			MatlabConnection conn = new MatlabConnection();
			conn.init();
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList(
					"R_BAPYRT", "R_NUTD11", "R_PYC", "R_ADNK4", "R_ADNK1", "R_PPNAK", "R_ACORND", "R_PGLCM", 
					"R_DCTPDA1", "R_GLUR", "R_NUDPK1", "R_PTA", "R_IMPDH", "R_SUCOAS", "R_NUDPK5", "R_3OACT", 
					"R_CITS", "R_NUDPK3", "R_R00227", "R_MDH1", "R_NUDPK8")));
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCond, geneCond, model, "MATLAB_FBA");
			
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux("R_Biomass", 1.0);
			
			SteadyStateSimulationResult result = cc.simulate();
			
			double d = result.getFluxValues().get(model.getBiomassFlux());
			System.out.println("---------- COBRA FBA ----------");
			System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
			
			
		}catch(Exception e){
			
		}
	}
	
	
	@Test
	public void FBASimulation2() {
		try{
			JSBMLReader reader = new JSBMLReader("/home/vmsilico/Documents/Files/glds_giesteira/CN127_7AHPT.xml", "1",false);
			
			Container cont = new Container(reader);
			Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
	
			cont.removeMetabolites(met);
			SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
			model.setBiomassFlux("R_Biomass");
			
			MatlabConnection conn = new MatlabConnection();
			conn.init();
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList(
					"R_BAPYRT", "R_ACCOAAT1", "R_PYC", "R_GLUR", "R_3OXAPT", "R_SUCOAS", "R_GABAT1", "R_PPC", 
					"R_HISAL", "R_PPS", "R_TRPD", "R_FABC171_CP", "R_ICL")));
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCond, geneCond, model, "MATLAB_FBA");
			
			cc.setMaximization(true);
			
			SteadyStateSimulationResult result = cc.simulate();
			
			double d = result.getFluxValues().get(model.getBiomassFlux());
			System.out.println("---------- COBRA FBA ----------");
			System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
			
			
		}catch(Exception e){
		}
	}

	
	@Test
	public void FBASimulation3() {
		try{
			JSBMLReader reader = new JSBMLReader("/home/vmsilico/Documents/Files/glds_giesteira/CN127_7AHPT.xml", "1",false);
			
			Container cont = new Container(reader);
			Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
	
			cont.removeMetabolites(met);
			SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
			model.setBiomassFlux("R_Biomass");
			
			MatlabConnection conn = new MatlabConnection();
			conn.init();
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList(
					"R_METTHFD", "R_MDH1", "R_EX_h_e_", "R_TRPD", "R_R02285", "R_NUDPK1", "R_TRKT1", "R_BKTB", 
					"R_R01557", "R_ADNK", "R_SUCCD2_SL", "R_SUCOAS", "R_EX_ppa_e_", "R_PDH1", "R_GA3PD")));
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCond, geneCond, model, "MATLAB_FBA");
			
			cc.setMaximization(true);
			
			SteadyStateSimulationResult result = cc.simulate();
			
			double d = result.getFluxValues().get(model.getBiomassFlux());
			System.out.println("---------- COBRA FBA ----------");
			System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
			
			
		}catch(Exception e){

		}
	}
	
	@Test
	public void FBASimulationTableFormatModel() {
		try{
			//JSBMLReader reader = new JSBMLReader("/home/vmsilico/Documents/Files/glds_giesteira/CN127_7AHPT.xml", "1",false);
			FlatFilesReader reader = new FlatFilesReader("/home/vmsilico/Desktop/Tools/Models/FlatFiles/EcoliCoreModel.fluxes",
					"/home/vmsilico/Desktop/Tools/Models/FlatFiles/EcoliCoreModel.matrix", 
					"/home/vmsilico/Desktop/Tools/Models/FlatFiles/EcoliCoreModel.metab", 
					null, "Ecoli");
			
			Container cont = new Container(reader);
			Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
	
			cont.removeMetabolites(met);
			SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
			//model.setBiomassFlux("R_Biomass");
			
			MatlabConnection conn = new MatlabConnection();
			conn.init();
			
			GeneticConditions geneCond = new GeneticConditions(new ReactionChangesList(Arrays.asList(
					"R_METTHFD", "R_MDH1", "R_EX_h_e_", "R_TRPD", "R_R02285", "R_NUDPK1", "R_TRKT1", "R_BKTB", 
					"R_R01557", "R_ADNK", "R_SUCCD2_SL", "R_SUCOAS", "R_EX_ppa_e_", "R_PDH1", "R_GA3PD")));
			
			EnvironmentalConditions envCond = EnvironmentalConditions.readFromFile("/home/vmsilico/Documents/Files/glds_giesteira/envconditions.env", ",");
			
			SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_FBA");
			
			cc.setMaximization(true);
			
			SteadyStateSimulationResult result = cc.simulate();
			
			double d = result.getFluxValues().get(model.getBiomassFlux());
			System.out.println("---------- COBRA FBA ----------");
			System.out.println("Reaction: "+ result.getOFString() + " Value: " + d);
			
			
		}catch(Exception e){

		}
	}
}
