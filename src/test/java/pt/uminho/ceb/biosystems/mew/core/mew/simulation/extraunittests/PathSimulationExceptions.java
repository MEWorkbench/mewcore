package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.solvers.builders.CLPSolverBuilder;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.SolverDefinitionException;

public class PathSimulationExceptions {

	static private SteadyStateModel model;

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	static public void setData() throws Exception {
		// input Modelo
		//exception.expect(FileNotFoundException.class);
		JSBMLReader reader = new JSBMLReader("files/models/ecoli_core_model.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
	}
	
	
	@Test
	public void FBA() throws Exception {
		exception.expect(SolverDefinitionException.class);
		
		simulate(SimulationProperties.FBA);
	
	}
	

	@Test
	public void MOMA() throws Exception {
		exception.expect(SolverDefinitionException.class);
		simulate(SimulationProperties.MOMA);
	}
	
	@Test
	public void LMOMA() throws Exception {
		exception.expect(SolverDefinitionException.class);
		simulate(SimulationProperties.LMOMA);
	}
	
	@Test
	public void ROOM() throws Exception {
		exception.expect(SolverDefinitionException.class);
		simulate(SimulationProperties.ROOM);
	}
	
	@Test
	public void PFBA() throws Exception {
		exception.expect(SolverDefinitionException.class);
		simulate(SimulationProperties.PFBA);
	}
	
//	@Test
//	public void NormLMoma() {
//		fail("Not yet implemented");
//	}

	
	private void simulate(String method) throws Exception {
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, method);
		cc.setMaximization(true);
		cc.setSolver(CLPSolverBuilder.ID);
		cc.simulate();
	}
}
