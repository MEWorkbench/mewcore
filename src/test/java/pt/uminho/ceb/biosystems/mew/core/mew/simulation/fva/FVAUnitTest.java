package pt.uminho.ceb.biosystems.mew.core.mew.simulation.fva;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
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

//		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.PFBA);
//		cc.setSolver(SolverType.CPLEX3);
//		cc.setFBAObjSingleFlux(model.getBiomassFlux(), 1.0);
//		cc.setMaximization(true);
//
//		FluxValueMap wt = cc.simulate().getFluxValues();

		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX3);

		Map<String, double[]> fluxLimits_persistent = fva.limitsAllFluxes(0.1 * 0.99999);

		TreeSet<String> keys = new TreeSet<>(fluxLimits_persistent.keySet());

		FBAFluxVariabilityAnalysis fva_volatile = new FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX);
		Map<String, double[]> fluxLimits_volatile = fva_volatile.limitsAllFluxes(0.1 * 0.99999);

		for (String k : keys) {

			System.out.println("[" + k + "]");
			double[] k_persist = fluxLimits_persistent.get(k);
			double[] k_volatile = fluxLimits_volatile.get(k);

			System.out.println("\tLower: " + k_persist[0] + " | " + k_volatile[0] + "\tDiff: " + (Math.abs(k_persist[0]) - Math.abs(k_volatile[0])));
			if((Math.abs(k_persist[0]) - Math.abs(k_volatile[0])) > 0.0001)
				System.err.println("BUG!!!!!");
			System.out.println("\tUpper: " + k_persist[1] + " | " + k_volatile[1] + "\tDiff: " + (Math.abs(k_persist[1]) - Math.abs(k_volatile[1])));
			if((Math.abs(k_persist[1]) - Math.abs(k_volatile[1])) > 0.0001)
				System.err.println("BUG!!!!!");
		}

		// for (String r : fluxLimits_persistent.keySet()) {
		// System.out.print(r);
		// for (double d : fluxLimits_persistent.get(r)) {
		// System.out.print("\t"+d);
		// }
		// System.out.println();
		// }

		// EnvironmentalConditions envCond = new EnvironmentalConditions();
		// envCond.addReactionConstraint("R_Biomass_Ecoli_core_w_GAM", new
		// ReactionConstraint(0.81274653, 1000.0));
		//
		// SimulationSteadyStateControlCenter cc = new
		// SimulationSteadyStateControlCenter(envCond, null, model,
		// SimulationProperties.FBA);
		// cc.setMaximization(true);
		// cc.setSolver(SolverType.CPLEX3);
		// cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
		//
		// SteadyStateSimulationResult result = cc.simulate();
		//
		// System.out.println(result.getOFvalue());

	}

}
