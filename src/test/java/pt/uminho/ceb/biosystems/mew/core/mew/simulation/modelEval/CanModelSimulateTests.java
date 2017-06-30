package pt.uminho.ceb.biosystems.mew.core.mew.simulation.modelEval;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.NewDatabaseCSVFilesReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CPLEX3SolverBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;

public class CanModelSimulateTests {
	
	String sbmlFolder = "/Models/ToConvert/SBML/";
	
	String modelsFolder = "/Models/ToConvert/";
	String convertedFolder = "/Models/ToConvert/SBML/";
	String originalFolder = modelsFolder + "Originals/";
	
//	Models present in sourceforge
//	iYS432: https://sourceforge.net/p/optflux/support-requests/37/
//	iKK446: https://sourceforge.net/p/optflux/support-requests/36/
//	iAN818m: https://sourceforge.net/p/optflux/support-requests/35/
//	iJM658: https://sourceforge.net/p/optflux/support-requests/34/
	
	protected SteadyStateModel readModel(String pathFile) throws Exception{
		JSBMLReader reader = new JSBMLReader(pathFile, "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		return (SteadyStateModel) ContainerConverter.convert(cont);
	}
	
	@Test
	public void simulate_iJM658() throws Exception{
		String modelID = "iJM658.xml";
		SteadyStateModel model = readModel(sbmlFolder+modelID);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setMaximization(true);
		
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println(res.getOFString() + "\t" + res.getOFvalue());
	}
	
	@Test
	public void simulate_iAN818m() throws Exception{
		String modelID = "iAN818m.xml";
		SteadyStateModel model = readModel(sbmlFolder+modelID);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setMaximization(true);
		
		String file = "/Models/ToConvert/iAN818m_EnvCond";
		
		cc.setEnvironmentalConditions(readEnvCond(file));
		
		System.out.println(cc.getEnvironmentalConditions().toString());
		
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println(res.getOFString() + "\t" + res.getOFvalue());
		
		MapUtils.prettyPrint(res.getNetConversionMap(true));
		
//		MapUtils.prettyPrint(res.getFluxValues());
		
	}
	
	@Test
	public void simulate_iAN818m_2() throws Exception{
		String modelID = "iAN818m.xml";
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iAN818m_Reactions"), 
				new File(originalFolder + "iAN818m_Metabolites"),
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.METID,0); 
					put(NewDatabaseCSVFilesReader.METNAME,1);}}, 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACID,0); 
					put(NewDatabaseCSVFilesReader.REACECNUMBER,1);
//					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
//					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
//					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
//					put(NewDatabaseCSVFilesReader.REACLB,8);
					put(NewDatabaseCSVFilesReader.REACEQUATION,1);}},  
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		
		System.out.println(container.getCompartments().keySet());
		container.constructDrains(container.getCompartments().get("Extc").getMetabolitesInCompartmentID(), "Extc", -10, 10000);
		
		
		
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(container);
		
//		SteadyStateModel model = readModel(sbmlFolder+modelID);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setMaximization(true);
		
//		String file = "./Models/ToConvert/iAN818m_EnvCond";
		
//		cc.setEnvironmentalConditions(readEnvCond(file));
		
//		System.out.println(cc.getEnvironmentalConditions().toString());
		
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println(res.getOFString() + "\t" + res.getOFvalue());
		
		MapUtils.prettyPrint(res.getNetConversionMap(true));
		
//		MapUtils.prettyPrint(res.getFluxValues());
		
		
	}
	
	@Test
	public void simulate_iKK446() throws Exception{
		String modelID = "iKK446.xml";
		SteadyStateModel model = readModel(sbmlFolder+modelID);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setMaximization(true);
		
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println(res.getOFString() + "\t" + res.getOFvalue());
	}
	
	@Test
	public void simulate_iKK446_2() throws Exception{
		Delimiter tab = Delimiter.TAB;
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iKK446_Reactions"), 
				new File(originalFolder + "iKK446_Metabolites"),
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.METID,0); 
					put(NewDatabaseCSVFilesReader.METNAME,1);}}, 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACID,0); 
//					put(NewDatabaseCSVFilesReader.REACNAME,1);
//					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
//					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
//					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
//					put(NewDatabaseCSVFilesReader.REACLB,8);
					put(NewDatabaseCSVFilesReader.REACEQUATION,1);}},  
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
//		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iKK446_Reactions"), 
//				new HashMap<String, Integer>(){{
//					put(NewDatabaseCSVFilesReader.REACID,0); 
////					put(NewDatabaseCSVFilesReader.REACNAME,1);
////					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
////					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
////					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
////					put(NewDatabaseCSVFilesReader.REACLB,8);
//					put(NewDatabaseCSVFilesReader.REACEQUATION,1);}},  
//				"", 
//				"", 
//				tab.toString(), 
//				new HashMap<String, Integer>(), 
//				true, 
//				null, 
//				null);
		
		Container container = new Container(reader);
		Pattern pattern = Pattern.compile("(.*)_cytosol");
		container.stripDuplicateMetabolitesInfoById(pattern, false);
		
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*xt"));
//		container.removeMetabolites(met);
//		container.addCompartment(new CompartmentCI("xt", "exterior", "outside"));
//		for (String metaID : met) {
//			container.getCompartment("xt").addMetaboliteInCompartment(metaID);
//			
//		}
		
		
		container.constructDrains(met, "cytosol", -10, 10000);
		
		
		container.constructDrain("PROTEIN", "cytosol", 0, 10000);
		
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(container);
		
		for (String reactionID : model.getReactions().keySet()) {
			if(reactionID.contains("R_EX_")){
				System.out.println(reactionID + "\t" + model.getReaction(reactionID).getConstraints());
				System.out.println(container.getReaction(reactionID).getMetaboliteSetIds());
			}
		}
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CPLEX3SolverBuilder.ID);
		cc.setMaximization(true);

		SteadyStateSimulationResult res = cc.simulate();
		System.out.println(res.getOFString() + "\t" + res.getOFvalue());
		
		MapUtils.prettyPrint(res.getNetConversionMap(true));
		
//		MapUtils.prettyPrint(res.getFluxValues());
		
		
//		container.constructDrains(container.getCompartments().get("Extc").getMetabolitesInCompartmentID(), "Extc", -10, 10000);
	}
	
	public EnvironmentalConditions readEnvCond(String file) throws IOException{
		EnvironmentalConditions toRet = new EnvironmentalConditions();
		
		List<String> ec = FileUtils.readLines(file);
		
		for (String line : ec) {
			String[] splitted = line.split("\t");
			toRet.addReactionConstraint(splitted[0], new ReactionConstraint(Double.parseDouble(splitted[2]), Double.parseDouble(splitted[3])));
		}
		
		return toRet;
	}
	

}
