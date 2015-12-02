package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class ROOMGLPKUnitTest {

	@Test
	public void test() throws Exception{
		
		Container container = new Container(new JSBMLReader(getClass().getClassLoader().getResource("models/ecoli_core_model.xml").getPath(), "Organism"));
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		container.removeMetabolites(met);
		ISteadyStateModel model = ContainerConverter.convert(container);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.ROOM);
		
		Set<String> kos = new HashSet<>();
//		kos.add("R_GLUDy");
		kos.add("R_ACKr"); kos.add("R_ACt2r"); kos.add("R_H2Ot");
//		cc.setReactionsKnockoutConditions(kos);
		cc.setMaximization(true);
		cc.setSolver(SolverType.GLPK);
		cc.addProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, true);
		
		SteadyStateSimulationResult reswt = cc.simulate();
		
//		Assert.assertEquals(0.87392151, reswt.getFluxValues().get(model.getBiomassFlux()), 0.01);
		
//		MapUtils.prettyPrint(reswt.getFluxValues());
//		MapUtils.prettyPrint(reswt.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES"));
		System.err.println("##############################################################");
//		double wtvalue = countBooleans(reswt.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES"));
//		System.out.println(wtvalue);
		
//		cc.setReactionsKnockoutConditions(kos);
//		
		SteadyStateSimulationResult resGLPK = cc.simulate();
		System.out.println(resGLPK.getOFvalue());
		
		MapUtils.prettyPrint(removeZeroBooleans(resGLPK.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES")));
		
		SimulationSteadyStateControlCenter ccCPLEX = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.ROOM);
		ccCPLEX.setMaximization(true);
		ccCPLEX.setSolver(SolverType.CPLEX);
//		ccCPLEX.setReactionsKnockoutConditions(kos);
		ccCPLEX.addProperty(SimulationProperties.USE_DRAINS_IN_WT_REFERENCE, true);
		
		SteadyStateSimulationResult resCPLEX = ccCPLEX.simulate();
		System.out.println(resCPLEX.getOFvalue());
		
		MapUtils.prettyPrint(removeZeroBooleans(resCPLEX.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES")));
////		
//		System.out.println("Expected OFValue: " + res.getOFvalue());
////		
//		System.out.println("Real Test: " + countBooleansWithoutWT(res.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES"), reswt.getComplementaryInfoReactions().get("ROOM_BOOLEAN_VAR_VALUES")));
		
	}
	
	private double countBooleansWithoutWT(MapStringNum komsn, MapStringNum wtmsn){
		
		System.out.println("111111111111111111111111111111111111111111111111111111111");
		MapStringNum koaux = removeZeroBooleans(komsn);
		System.out.println("222222222222222222222222222222222222222222222222222222222");
		MapStringNum wtaux = removeZeroBooleans(wtmsn);
		
		
		for (String reaction : wtaux.keySet())
			koaux.remove(reaction);
		
		return koaux.size();
	}
	
	private MapStringNum removeZeroBooleans(MapStringNum map){
		MapStringNum msn = new MapStringNum();
		msn.putAll(map);
		
		for (String key : map.keySet())
			if(map.get(key) < 0.1)
				msn.remove(key);
		
		return msn;
	}

}
