package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLLevel3Reader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CLPSolverBuilder;

public class JSbmlV3Tests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	@Test
	public void fbaTest01() throws Exception{
		JSBMLLevel3Reader reader = new JSBMLLevel3Reader("./src/test/resources/models/iAF1260.xml", "NoName", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CLPSolverBuilder.ID);
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFString() + "\t" + result.getOFvalue());
	}
	
	@Test
	public void fbaTest02() throws Exception{
		JSBMLLevel3Reader reader = new JSBMLLevel3Reader("./src/test/resources/models/Recon2_v04.xml", "NoName", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);

		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(CLPSolverBuilder.ID);
		cc.setMaximization(true);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFString() + "\t" + result.getOFvalue());
	}
	
	@Test
	public void fbaTestTutti() throws Exception{
		ArrayList<String> models = new ArrayList<String>();
		models.add("./src/test/resources/models/Recon2_v04.xml");
		models.add("./src/test/resources/models/iAF1260.xml");
		models.add("./src/test/resources/models/iMM904.xml");
		
		
		for (String modelPath : models) {
			try{
				if(!(new File(modelPath)).exists())
					throw new Exception(modelPath + " not found");
				JSBMLLevel3Reader reader = new JSBMLLevel3Reader(modelPath, "NoName", false);
			
				// Container
				Container cont = new Container(reader);
				Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
				cont.removeMetabolites(met);
				SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
				SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
				cc.setSolver(CLPSolverBuilder.ID);
				cc.setMaximization(true);
				
				SteadyStateSimulationResult result = cc.simulate();
				System.out.println(result.getOFString() + "\t" + result.getOFvalue());
			}catch(Exception e){
				System.err.println("---------------- " + modelPath + " ---------------");
				e.printStackTrace();
			}
		}
	}

}
