package pt.uminho.ceb.biosystems.mew.core.mew.simulation.extraunittests;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class FBASingularSimulationKO {
	
	protected SimulationSteadyStateControlCenter cc;
	protected SteadyStateModel model;
	
	//protected EnvironmentalConditions envCond, envKO, envConditions;

	@Before
	public void setData() throws Exception {
		// input Modelo
		//exception.expect(FileNotFoundException.class);
//		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Desktop/Models/Ec_iAF1260.xml", "1", false);
		JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Desktop/Models/ecoli_core_model.xml", "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
	}
	
	
	@Test
	public void pfbaMultipleTests() throws Exception
	{
		testSimulations();
	}
	
	protected void testSimulations() throws Exception{
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		
		compareSimulations("R_ALCD2x=0.5,R_ACKr=0.0,R_ACALD=0.0,R_FUM=0.0,R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);

		compareSimulations("R_ALCD2x=0.5,R_ACKr=0.0,R_FUM=0.0,R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);

		compareSimulations("R_ALCD2x=0.5,R_FUM=0.0,R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);

		compareSimulations("R_FUM=0.0,R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);

		compareSimulations("R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);

		compareSimulations("R_RPE=0.0,R_PDH=0.5", cc);

		compareSimulations("R_RPE=0.0", cc);
		
		compareSimulations("R_ALCD2x=0.5,R_FUM=0.0,R_RPE=0.0,R_MDH=0.0,R_PDH=0.5", cc);
	}
	
	protected void compareSimulations(String geneCondString, SimulationSteadyStateControlCenter cc) throws Exception{
		GeneticConditions geneCond = getGeneCondFromString(geneCondString, ",");
		cc.setGeneticConditions(geneCond);
		SteadyStateSimulationResult result = cc.simulate();
		SteadyStateSimulationResult resultSim = singularSim(geneCond);
		
		findNaNInResults(result);
		findNaNInResults(resultSim);
		
		double epsilon = 0.000001;
		
		double ofResCC = result.getOFvalue();
		double ofResSing = resultSim.getOFvalue();
		if(Math.abs(ofResCC/ofResSing -1) < epsilon)
			System.out.println(ofResCC + " VS " + ofResSing);
		else
			System.err.println(ofResCC + " VS " + ofResSing);
		
		double ofResBioCC = result.getFluxValues().get(model.getBiomassFlux());
		double ofResBioSing = resultSim.getFluxValues().get(model.getBiomassFlux());
		if(Math.abs(ofResBioCC/ofResBioSing-1) < epsilon)
			System.out.println("BIOMASS: " + result.getFluxValues().get(model.getBiomassFlux()) + " VS " + singularSim(geneCond).getFluxValues().get(model.getBiomassFlux()));
		else
			System.err.println("BIOMASS: " + result.getFluxValues().get(model.getBiomassFlux()) + " VS " + singularSim(geneCond).getFluxValues().get(model.getBiomassFlux()));
	}
	
	protected void findNaNInResults(SteadyStateSimulationResult result) {
		FluxValueMap resultsMap = result.getFluxValues();
		for (String s : resultsMap.keySet()) {
			if(Double.isNaN(resultsMap.get(s)))
				System.err.println(s + " Is NaN!");
		}
	}

	protected SteadyStateSimulationResult singularSim(GeneticConditions geneCond) throws Exception{
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationProperties.FBA);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux("R_EX_succ_e", 1.0);
		EnvironmentalConditions ec = new EnvironmentalConditions();
		ec.addReactionConstraint(model.getBiomassFlux(), new ReactionConstraint(0.8, 100000.0));
		cc.setEnvironmentalConditions(ec);
		
		cc.setGeneticConditions(geneCond);
		SteadyStateSimulationResult result = cc.simulate();
		return result;
	}
	
	public static GeneticConditions getGeneCondFromString(String geneCondString, String delimiter){
		ReactionChangesList reactList = new ReactionChangesList();
		String aux = geneCondString.replace(" ", "");
		String[] splitted = aux.split(delimiter);
		for (int i = 0; i < splitted.length; i++) {
			String[] auxGene = splitted[i].split("=");
			reactList.addReaction(auxGene[0], Double.parseDouble(auxGene[1]));
		}
		
		return new GeneticConditions(reactList);
	}

}
