package pt.uminho.ceb.biosystems.mew.core.mew.simulation.fva;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.fva.FBAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;

public class FVAUnitTest {

	@Test
	public void firstTest() throws Exception {

		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);

		JSBMLReader reader = new JSBMLReader(getClass().getClassLoader().getResource("models/ecoli_core_model.xml").getPath(), "1", false);

		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		SteadyStateModel model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX3);
		
		fva.fluxVariation(model.getBiomassFlux(), new ArrayList<String>(){{add("R_EX_h_e");}}, 20, null, null);
		

//		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.PFBA);
//		cc.setSolver(SolverType.CPLEX3);
//		cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
//		cc.setMaximization(true);
//
//		FluxValueMap wt = cc.simulate().getFluxValues();

//		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX3);
//
//		Map<String, double[]> fluxLimits_persistent = fva.limitsAllFluxes(0.1 * 0.99999);
//
//		TreeSet<String> keys = new TreeSet<>(fluxLimits_persistent.keySet());
//
//		FBAFluxVariabilityAnalysis fva_volatile = new FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX);
//		Map<String, double[]> fluxLimits_volatile = fva_volatile.limitsAllFluxes(0.1 * 0.99999);
//
//		for (String k : keys) {
//
//			System.out.println("[" + k + "]");
//			double[] k_persist = fluxLimits_persistent.get(k);
//			double[] k_volatile = fluxLimits_volatile.get(k);
//
//			System.out.println("\tLower: " + k_persist[0] + " | " + k_volatile[0] + "\tDiff: " + (Math.abs(k_persist[0]) - Math.abs(k_volatile[0])));
//			if((Math.abs(k_persist[0]) - Math.abs(k_volatile[0])) > 0.0001)
//				System.err.println("BUG!!!!!");
//			System.out.println("\tUpper: " + k_persist[1] + " | " + k_volatile[1] + "\tDiff: " + (Math.abs(k_persist[1]) - Math.abs(k_volatile[1])));
//			if((Math.abs(k_persist[1]) - Math.abs(k_volatile[1])) > 0.0001)
//				System.err.println("BUG!!!!!");
//		}
		
//		int numSteps = 20;
//		
//		Map<Pair<Integer, Double>, FluxValueMap> allFluxesByBiomass = new LinkedHashMap<>();
//		
//		double minBiomass = simulate(model, false, model.getBiomassFlux(), SolverType.CPLEX, null, null).getOFvalue();
//		double maxBiomass = simulate(model, true, model.getBiomassFlux(), SolverType.CPLEX, null, null).getOFvalue();
//		
//		double bioSteps = (maxBiomass - minBiomass) / numSteps;
//		double actualStep = minBiomass;
//		
//		long init = System.currentTimeMillis();
//		int i = 1;
//		while (actualStep < maxBiomass) {
//			
//
////			Pair<Integer, Double> pair = new Pair<Integer, Double>(i, actualStep);
////			allFluxesByBiomass.put(pair, );
//			
//			EnvironmentalConditions envCond = new EnvironmentalConditions();
//			ReactionConstraint rconst = model.getReactionConstraint(model.getBiomassFlux());
//			envCond.addReactionConstraint(model.getBiomassFlux(), new ReactionConstraint(actualStep, rconst.getUpperLimit()));
//			
////			SteadyStateSimulationResult resultMin = simulate(model, false, "R_EX_h_e", SolverType.CPLEX3, envCond, null);
////			SteadyStateSimulationResult resultMax = simulate(model, true, "R_EX_h_e", SolverType.CPLEX3, envCond, null);
////			System.out.println("Step " + i +": "+actualStep +"\tMin Succ :" + resultMin.getOFvalue() + "\tMax Succ: " + resultMax.getOFvalue());
//			
//			
//			SteadyStateSimulationResult result1 = simulate(model, true, model.getBiomassFlux(), SolverType.CPLEX3, envCond, null);
//			SteadyStateSimulationResult result2 = simulate(model, false, model.getBiomassFlux(), SolverType.CPLEX3, envCond, null);
//			System.out.println("Step " + i +": "+actualStep +"\tMin Succ :" + result1.getFluxValues().get("R_EX_h_e")+"\tMax Succ :" + result2.getFluxValues().get("R_EX_h_e"));
//			
//			i++;
//			actualStep += bioSteps;
//		}
//		
//		System.out.println("TIME ELAPSED: " + (System.currentTimeMillis()-init));
		
//		System.out.println(minBiomass);
//		System.out.println(maxBiomass);
//		
//		System.out.println("-------------------------------------------------------------");
//		
//		System.out.println(simulate(model, false, "R_EX_succ_e", SolverType.CPLEX, null, null).getOFvalue());
//		System.out.println(simulate(model, true, "R_EX_succ_e", SolverType.CPLEX, null, null).getOFvalue());

	}
	
	private static SteadyStateSimulationResult simulate(ISteadyStateModel model, boolean isMaximization, String fluxID, SolverType solverType, EnvironmentalConditions envCond, GeneticConditions geneCond){
		SteadyStateSimulationResult result = null;
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(envCond, geneCond, model, SimulationProperties.FBA);
		cc.setMaximization(isMaximization);
		cc.setFBAObjSingleFlux(fluxID, 1.0);
		cc.setSolver(solverType);
		try {
			result = cc.simulate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
