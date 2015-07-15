package pt.uminho.ceb.biosystems.mew.core.matlab.cobra;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.DatabaseCSVFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.NewDatabaseCSVFilesReader;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab.MatlabConnection;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.formulations.cobra.simulation.CobraFBAFormulation;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public class SimulationDifferentModelReaders {

	@Test
	public void FBAFromSBML() throws Exception {
		JSBMLReader reader = new JSBMLReader("files/models/ecoli_core_model.xml", "1",false);
		
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));

		cont.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_FBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println(result.getOFString() + ": " + result.getOFvalue());
	}
	
	@Test
	public void FBAFromCSVWithMetab() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File("../biocomponents/files/CSVFiles/Corrected/iBB814_Reactions"), 
				new File("../biocomponents/files/CSVFiles/Corrected/iBB814_Metabolites"), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"MyModel", 
				"MyModel", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container cont = new Container(reader);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_FBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println(result.getOFString() + ": " + result.getOFvalue());
		
	}
	
	@Test
	public void FBAFromCSVWithoutMetab() throws Exception {
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File("../biocomponents/files/CSVFiles/Corrected/iBB814_Reactions"), 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"MyModel", 
				"MyModel",
				tab.toString(), 
				new HashMap<String, Integer>(),  
				true, 
				null, 
				null);
		
		Container cont = new Container(reader);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		model.setId("model");
		
		MatlabConnection conn = new MatlabConnection();
		conn.init();
		
		SimulationSteadyStateControlCenter.registerMethod("MATLAB_FBA", CobraFBAFormulation.class);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, "MATLAB_FBA");
		
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		
		System.out.println(result.getOFString() + ": " + result.getOFvalue());
		
	}

}
