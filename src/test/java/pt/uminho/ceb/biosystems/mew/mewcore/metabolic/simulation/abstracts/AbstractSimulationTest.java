package pt.uminho.ceb.biosystems.mew.mewcore.metabolic.simulation.abstracts;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.InfeasibleProblemException;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.NormLMoma;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;

public abstract class AbstractSimulationTest {
	
	public static final String WILDTYPE = "WILDTYPE";
	public static final String KO_REACTIONS = "KO_REACTIONS";
	public static final String KO_GENETICS = "KO_GENETICS";
	public static final String UO_REACTIONS = "UO_REACTIONS";
	public static final String UO_GENETICS = "UO_GENETICS";
	public static final String UO_REACTIONSGENETICS = "UO_REACTIONSGENETICS";
	public static final String KO_REACTIONSGENETICS = "KO_REACTIONSGENETICS";
	
	private static final String NOFLUXESMSG = "All the fluxes are zero";
	private static final String NANFLUXESMSG = "There are fluxes with NaN values";
	
	protected SimulationSteadyStateControlCenter cc;
	
	protected SteadyStateModel model;
	protected EnvironmentalConditions envCond, envCondExtra;
	protected GeneticConditions genCond, genKO, envKO, genCondExtra, genEnvKO;
	protected AbstractSSBasicSimulation<?> method;
	
	protected static final double TOLERANCE = 1e-4;
	
	protected abstract String getMethodString();
	protected abstract Map<String, Double> getResults();
	protected abstract boolean isMaximization();
	
	abstract public SolverType getSolver();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	@Before
	public void setData() throws Exception {
		// input Model
		//exception.expect(FileNotFoundException.class);
		JSBMLReader reader = new JSBMLReader(getFile("models/ecoli_core_model.xml"), "1", false);
		
		// Container
		Container cont = new Container(reader);
		Set<String> met = cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		cont.removeMetabolites(met);
		model = (SteadyStateModel) ContainerConverter.convert(cont);
		
		// Define Genetic Conditions
		setGeneticConditions();
		
		// Define Environmental Conditions
		setEnvironmentalConditions();
		
		setParameters();
		
	}
	
	private void setEnvironmentalConditions()
	{
		// input overunderReactions
		envCond = new EnvironmentalConditions();
		envCond.put("R_EX_o2_e", new ReactionConstraint(0, 1000));
		envCond.put("R_EX_glc_e", new ReactionConstraint(-20, 1000));
						

		// input koreactions
		ReactionChangesList envKOList = new ReactionChangesList(Arrays.asList("R_ACKr", "R_ACt2r", "R_H2Ot"));
		envKO = new GeneticConditions(new ReactionChangesList(envKOList), false);
		
		// Add reactions from GeneKO to create both env and gene KOs
		for (String reaction : genKO.getReactionList().getReactionIds()) {
			envKOList.addReactionKnockout(reaction);
		}
		genEnvKO = new GeneticConditions(envKOList);		
		
		
		envCondExtra = new EnvironmentalConditions();
		envCondExtra.put("R_EX_o2_e", new ReactionConstraint(1001, 1000));
	}
	
	private void setGeneticConditions() throws Exception
	{
		// input kogenes
		GeneChangesList geneChangesListKO = new GeneChangesList();
		geneChangesListKO.put("b0720", 0.0);
		geneChangesListKO.put("b1779", 0.0);
		geneChangesListKO.put("b2029", 0.0);
		geneChangesListKO.put("b4015", 0.0);			
		
		
		// input overundergenes
		GeneChangesList geneChangesList = new GeneChangesList();
		geneChangesList.put("b0451", 0.5);
		geneChangesList.put("b0767", 2.0);
		geneChangesList.put("b1101", 0.0);
		geneChangesList.put("b1702", 0.25);
		geneChangesList.put("b2029", 4.0);
		

		genKO = new GeneticConditions(geneChangesListKO, (ISteadyStateGeneReactionModel)model, false);
		genCond = new GeneticConditions(geneChangesList, (ISteadyStateGeneReactionModel)model, true);
	
	}

	private void createSimulationSteadyStateControlCenter()
	{	
		cc = new SimulationSteadyStateControlCenter(null, null, model, getMethodString());
		
		setCCParameters();
	}
	
	private void createSimulationSteadyStateControlCenter(EnvironmentalConditions envConditions)
	{
		cc = new SimulationSteadyStateControlCenter(envConditions, null, model, getMethodString());
		
		setCCParameters();
	}
	
	private void createSimulationSteadyStateControlCenter(GeneticConditions geneConditions)
	{
		cc = new SimulationSteadyStateControlCenter(null, geneConditions, model, getMethodString());
		
		setCCParameters();
	}
	
	private void createSimulationSteadyStateControlCenter(EnvironmentalConditions envConditions, GeneticConditions geneConditions)
	{
		cc = new SimulationSteadyStateControlCenter(envConditions, geneConditions, model, getMethodString());
		
		setCCParameters();
	}
	
	
	protected void setCCParameters()
	{
		cc.setSolver(getSolver());
		cc.setMaximization(isMaximization());
		
		if(getMethodString() == SimulationProperties.NORM_LMOMA)
			cc.registerMethod(getMethodString(), NormLMoma.class);
	}
	
	protected void setParameters()
	{
		method.setSolverType(getSolver());
	}
	

	///////////////////////////////////////////////////////////
	////////////         Property Tests            ////////////
	///////////////////////////////////////////////////////////
	
	//====================================================
	// 						SOLVER
	//====================================================
	// The Property SOLVER is a Strings from enum SolverType
	
	/** Test for the cases when property SOLVER is not registered*/ 
//	@Test
	public void runSolverMandatoryPropertyException() throws Exception
	{
		thrown.expect(MandatoryPropertyException.class);
		
		method.setProperty(SimulationProperties.SOLVER, null);
		// An exception is thrown with the message
		// solver	class solvers.SolverType
		
		method.simulate();
		
//		createSimulationSteadyStateControlCenter();
//		cc.setSolver(null);
//		cc.simulate();
	}
	
	/** Test for the cases when property SOLVER is registered
	with the wrong data type*/
//	@Test
	public void runSolverPropertyCastException() throws Exception
	{
		thrown.expect(PropertyCastException.class);
		
		// SimulationProperties.SOLVER recebe uma das Strings do enum SolverType
		// Example: 
		//method.setProperty(SimulationProperties.SOLVER, SolverType.CLP);
		
		method.setProperty(SimulationProperties.SOLVER, 10);
		// An exception is thrown with the message:
		// Type Mismach in Property solver
		// Expected Class: class solvers.SolverType
		// Introduced Class: class java.lang.Integer	
		
		method.simulate();
		
//		Using the ControlCenter it's not possible to make the register  
//		of the solver with the wrong data type
//		createSimulationSteadyStateControlCenter();
//		cc.setSolver(SolverType.CLP);
//		cc.simulate();
	}

	
	//====================================================
	// 				ENVIRONMENTAL_CONDITIONS
	//====================================================
	// The property ENVIRONMENTAL_CONDITIONS is a LinkedHashMap <String, ReactionConstraint>
	
	/** Teste para o caso da propriedade ENVIRONMENTAL_CONDITIONS ser registada
	 com environmental conditions que não existem no modelo ou a Null*/
//	@Test
	public void runEnvironmentalInexistent() throws Exception
	{
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("UmaEnv" , new ReactionConstraint(0,0));

		method.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, env);
		method.setEnvironmentalConditions(env);
		// When the property is registered with environmental conditions  
		// that do not exist in the model they are ignored and the simulation
		// is performed has if no environmental conditions were used 

		method.simulate();
		
		//Control Center
//		createSimulationSteadyStateControlCenter(env);
//		cc.simulate();
		
		
		method.setEnvironmentalConditions(null);
		method.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, null);
		// Quando a propriedade é registada a null a simulação é executada 
		// normalmente como se não existisse environmental conditions
		
		method.simulate();
		
		//Control Center - A simulação tem o mesmo comportamento
//		EnvironmentalConditions envCC = null;
//		createSimulationSteadyStateControlCenter(envCC);
//		cc.simulate();
	}
		
	/** Teste para o caso da propriedade ENVIRONMENTAL_CONDITIONS  
	 ser registada com um tipo de dados diferente do esperado*/
//	@Test
	public void runEnvironmentalPropertyCastException() throws Exception
	{
		thrown.expect(PropertyCastException.class);

		// Example: 
		/*
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("R_H2Ot", new ReactionConstraint(0, 0));
		method.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, env);
		// ou
		method.setEnvironmentalConditions(env);
		*/

		method.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, true);
		// Este set não dá exception pois não há validação do tipo de dados recebido
		// O método a utilizar é:
		//method.setEnvironmentalConditions(environmentalConditions);		
		
		method.simulate();
		// A exception PropertyCastException é dada ao executar a simulação
		// e é apresentada a seguinte mensagem: 
		// Type Mismach in Property environmentalConditions
		// Expected Class: class metabolic.model.components.EnvironmentalConditions
		// Introduced Class: class java.lang.Boolean
		
//		No caso do ControlCenter não é possivel registar  
//		environmental conditions com outro tipo de dados
//		EnvironmentalConditions env = new EnvironmentalConditions();
//		env.put("R_H2Ot", new ReactionConstraint(0, 0));
//		createSimulationSteadyStateControlCenter();
//		cc.setEnvironmentalConditions(env);
//		cc.simulate();
	}
	
	/** Teste para o caso da propriedade ENVIRONMENTAL_CONDITIONS 
	 ser registada com o LowerBounds maior que o UpperBounds*/
//	@Test
	public void runEnvironmentalLowerUpperTest() throws Exception
	{		
		thrown.expect(InfeasibleProblemException.class);
		
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("R_H2Ot", new ReactionConstraint(1000, 0));
		method.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, env);
		
		method.setEnvironmentalConditions(env);

		//try {
			method.simulate();
		//} catch (Exception e) {
		//	System.out.println(e.getClass().toString());
		//}
		
		// É lançada exception ao executar com o solver CLP
		
		
		// Control Center - A simulação tem o mesmo comportamento
//		EnvironmentalConditions envCC = new EnvironmentalConditions();
//		env.put("R_H2Ot", new ReactionConstraint(1000, 0));
//		createSimulationSteadyStateControlCenter(env);
//		cc.simulate();
	}
		
	
	//====================================================
	// 					GENETIC_CONDITIONS
	//====================================================
	// A propriedade GENETIC_CONDITIONS um objecto constituido por uma lista de 
	// genes, um ISteadyStateGeneReactionModel e um boolean indicando se é UnderOver
	
	/**Teste para o caso da propriedade GENETIC_CONDITIONS ser registada
	com genetic conditions que não existem no modelo ou a null*/
//	@Test
	public void runGeneticInexistent() throws Exception
	{
		// Quando a propriedade GENETIC_CONDITIONS é registada com genetic conditions
		// que não existem no modelo esta é ignorada e a simulação é executada normalmente
		GeneChangesList geneList = new GeneChangesList();
		geneList.put("UmGene", 0.0);

		GeneticConditions gene = new GeneticConditions(geneList, (ISteadyStateGeneReactionModel)model, true);
		
		method.setProperty(SimulationProperties.GENETIC_CONDITIONS, gene);
		method.setGeneticConditions(gene);
		
		System.out.println(method.simulate().getOFvalue());
		
		// Control Center
		createSimulationSteadyStateControlCenter();
		cc.setGeneticConditions(gene);
		System.out.println(cc.simulate().getOFvalue());
		
		
		// Quando a propriedade GENETIC_CONDITIONS é registada a null a simulação
		// é executada normalmente como se não existisse genetic conditions
		method.setGeneticConditions(null);
		method.setProperty(SimulationProperties.GENETIC_CONDITIONS, null);
		
		method.simulate();
	}
		
	/**Teste para o caso da propriedade GENETIC_CONDITIONS  
	ser registada com um tipo diferente do esperado*/
//	@Test
	public void runGeneticPropertyCastException() throws Exception
	{
		thrown.expect(PropertyCastException.class);
		
		// Example:
		/*
		GeneChangesList geneKOList = new GeneChangesList();
		geneKOList.put("b0451", 0.5);
		GeneticConditions gene = new GeneticConditions(geneKOList, (ISteadyStateGeneReactionModel)model, true);
		*/
		 
		// Para as simulações OverUnder ainda será necessário registar as seguintes propriedades:
		// IS_OVERUNDER_SIMULATION
		// OVERUNDER_REFERENCE_FLUXES
		
		method.setProperty(SimulationProperties.GENETIC_CONDITIONS, true);
		
		method.simulate();
		// Exception lançada pelo testCast realizado no método getGeneticConditions
		// É dada exception e apresentada a seguinte mensagem:
		// Type Mismach in Property geneticConditions
		// Expected Class: class metabolic.simulation.components.GeneticConditions
		// Introduced Class: class java.lang.Boolean
	}
			
		
	//====================================================
	// 				IS_OVERUNDER_SIMULATION
	//====================================================
	// A propriedade IS_OVERUNDER_SIMULATION é registada com um boolean
	
	/** Teste para o caso da propriedade IS_OVERUNDER_SIMULATION ser registada 
	mas faltar o registo da propriedade OVERUNDER_REFERENCE_FLUXES*/
//	@Test
	public void runIsOverUnderMandatoryPropertyException() throws Exception
	{
		thrown.expect(MandatoryPropertyException.class);
				
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
				
		method.simulate();
	}
	
	/** Teste para o caso da propriedade IS_OVERUNDER_SIMULATION  
	ser registada mas com um tipo de dados diferente do esperado*/
//	@Test
	public void runIsOverUnderTest() throws Exception
	{
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, "true");
		// Não dá exception mas apresenta a seguinte mensagem:
		// The property isOverunderSimulation was ignored!!
		// Reason: Type Mismach in Property isOverunderSimulation
		// Expected Class: class java.lang.Boolean
		// Introduced Class: class java.lang.String
		
		method.simulate();
	}
	
	
	//====================================================
	// 				OVERUNDER_REFERENCE_FLUXES
	//====================================================
	// A propriedade OVERUNDER_REFERENCE_FLUXES é registada com um FluxValueMap
	// Pré-requesito: Propriedade IS_OVERUNDER_SIMULATION tem 
	// que estar registada com valor a true
	
	/** Teste para o caso da propriedade OVERUNDER_REFERENCE_FLUXES ser registada a null*/
//	@Test
	public void runOverUnderRefFluxMandatoryPropertyException() throws Exception
	{
		thrown.expect(MandatoryPropertyException.class);

		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, null);
		
		// Se IS_OVERUNDER_SIMULATION for false ignora OVERUNDER_REFERENCE_FLUXES
		// Se OVERUNDER_REFERENCE_FLUXES for registado sozinho também é ignorado
				
		method.simulate();
		//A exception é lançada pelo método createModelOverride
	}
	
	/** Teste para o caso da propriedade OVERUNDER_REFERENCE_FLUXES  
	ser registada com um tipo diferente do esperado*/
//	@Test
	public void runOverUnderRefFluxPropertyCastException() throws Exception
	{
		thrown.expect(PropertyCastException.class);
		
		// Se não for feito o registo da IS_OVERUNDER_SIMULATION então  
		// qualquer registo do OVERUNDER_REFERENCE_FLUXES é ignorado
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		
		// A propriedade OVERUNDER_REFERENCE_FLUXES recebe um FluxValueMap
		// criado pelo método SimulationProperties.simulateWT
		// Map<String, Double> ref = 
		// 		SimulationProperties.simulateWT(ISteadyStateModel, EnvironmentalConditions, SolverType);
		// method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
		
		method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, "true");
		// É lançada exception e apresentada a seguinte mensagem:
		// Type Mismach in Property overunderReferenceFluxes
		// Expected Class: class metabolic.simulation.components.FluxValueMap
		// Introduced Class: class java.lang.String
		
		method.simulate();
	}
	
	/** Teste para o caso da propriedade OVERUNDER_REFERENCE_FLUXES ser registada
	corretamente mas não for registada a propriedade GENETIC_CONDITIONS*/
//	@Test
	public void runOverUnderRefFluxWrongFormulationException() throws Exception
	{
		thrown.expect(WrongFormulationException.class);
		
		// Se não for feito o registo da IS_OVERUNDER_SIMULATION então  
		// qualquer registo do OVERUNDER_REFERENCE_FLUXES é ignorado
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
				
		
		Map<String, Double> ref = SimulationProperties.simulateWT(model, null, getSolver());
		method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
		
		method.simulate();
		// É lançada exceção através do método createModelOverride
	}
	
	
	
//	@Test// (expected = MandatoryPropertyException.class)
	public void runCheckPossibleProperties() throws Exception
	{
		for(String str: method.getMandatoryProperties())
			System.out.println(str);
		
		System.out.println();
		
		for(String str : method.getPossibleProperties())
			System.out.println(str);
	}
	
	
	
	
	
	///////////////////////////////////////////////////////////
	////////////         Simulation Tests          ////////////
	///////////////////////////////////////////////////////////
	@Test
	public void runWildType() throws Exception
	{
		System.out.println("runWildType:");
		
		createSimulationSteadyStateControlCenter();
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
		
//		System.out.println("Num Metabolites: "+cc.getModel().getNumberOfMetabolites());
//		System.out.println("Num Reactions: "+cc.getModel().getNumberOfReactions());
//		
//		System.out.println("CompMetabolites: "+simulationResult.getComplementaryInfoMetabolites().size());
//		for (String str : simulationResult.getComplementaryInfoMetabolites().keySet()) {
//			System.out.println(str);
//			//System.out.println("CompMetabolites ShadowPrices: "+simulationResult.getComplementaryInfoReactions().get("ShadowPrices").size());
//		}
//		System.out.println("CompReactions: "+simulationResult.getComplementaryInfoReactions().size());
		//System.out.println("CompMetabolites ReducedCosts: "+simulationResult.getComplementaryInfoMetabolites().get("ReducedCosts").size());
		
		
		/////////////////////////////////////////////////////////////////////////////////////
			
		assertEquals(getResults().get(WILDTYPE), method.simulate().getOFvalue(), TOLERANCE);
		
	}
	
	@Test
	public void runKOReactions() throws Exception
	{
		System.out.println("runKOReactions:");		
		
		createSimulationSteadyStateControlCenter(envKO);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		setParameters();
		
		method.setGeneticConditions(envKO);
		assertEquals(getResults().get(KO_REACTIONS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	@Test
	public void runKOGenes() throws Exception
	{
		System.out.println("runKOGenes:");
		
		createSimulationSteadyStateControlCenter(genKO);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		// If the sum of the fluxes is zero then an Exception is thrown
		// Also if there is a NaN value another Exception is thrown
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		setParameters();
		method.setGeneticConditions(genKO);
		assertEquals(getResults().get(KO_GENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	@Test
	public void runUnderOverReactions() throws Exception
	{
		System.out.println("runUnderOverReactions:");
		
		createSimulationSteadyStateControlCenter(envCond);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		//setParameters();
		method.setEnvironmentalConditions(envCond);
		assertEquals(getResults().get(UO_REACTIONS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	@Test
	public void runUnderOverGenes() throws Exception
	{
		System.out.println("runUnderOverGenes:");
		
		createSimulationSteadyStateControlCenter(genCond);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
			
		/////////////////////////////////////////////////////////////////////////////////////
	
		//setParameters();
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		
		Map<String, Double> ref = SimulationProperties.simulateWT(model, null, getSolver());
		method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
		
		method.setGeneticConditions(genCond);
		assertEquals(getResults().get(UO_GENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	@Test
	public void runKOReactionsGene() throws Exception
	{
		System.out.println("runKOReactionsGene:");
		
		createSimulationSteadyStateControlCenter(genEnvKO);
			
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		//setParameters();
		method.setGeneticConditions(genEnvKO);
		assertEquals(getResults().get(KO_REACTIONSGENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
		
	@Test
	public void runUnderOverReactionsGene() throws Exception
	{
		System.out.println("runUnderOverReactionsGene:");
		
		createSimulationSteadyStateControlCenter(envCond, genCond);
			
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
			
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		//setParameters();
		method.setEnvironmentalConditions(envCond);
		
		method.setProperty(SimulationProperties.IS_OVERUNDER_SIMULATION, true);
		
		Map<String, Double> ref = SimulationProperties.simulateWT(model, envCond, getSolver());
		method.setProperty(SimulationProperties.OVERUNDER_REFERENCE_FLUXES, ref);
		
		method.setGeneticConditions(genCond);
		assertEquals(getResults().get(UO_REACTIONSGENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	
	
	
	
	
	@Test
	public void runKOGenesExtra() throws Exception
	{
		System.out.println("runKOGenesExtra:");
		
		// KO Fum
		GeneticConditions geneticKO = new GeneticConditions(
				new GeneChangesList(Arrays.asList("b1612", "b4122", "b1611"), Arrays.asList(0.0, 0.0, 0.0)),
				(ISteadyStateGeneReactionModel)model, false);
		createSimulationSteadyStateControlCenter(geneticKO);
		
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		// If the sum of the fluxes is zero then an Exception is thrown
		// Also if there is a NaN value another Exception is thrown
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		setParameters();
		method.setGeneticConditions(geneticKO);
		//assertEquals(getResults().get(KO_GENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	
	@Test
	public void runKOReactionsGeneExtra() throws Exception
	{
		System.out.println("runKOReactionsGeneExtra:");
		
		// KO Fum
		GeneticConditions geneticKO = new GeneticConditions(
				new GeneChangesList(Arrays.asList("b1612", "b4122", "b1611"), Arrays.asList(0.0, 0.0, 0.0)),
				(ISteadyStateGeneReactionModel)model, false);
		
		ReactionChangesList envKOList = envKO.getReactionList();
		for (String reaction : geneticKO.getReactionList().keySet()) {
			envKOList.addReactionKnockout(reaction);
		}
		
		GeneticConditions geneticEnvironmentalKOs = new GeneticConditions(envKOList);
		
		createSimulationSteadyStateControlCenter(geneticEnvironmentalKOs);
			
		SteadyStateSimulationResult result = cc.simulate();
		System.out.println(result.getOFvalue());
		
//		System.out.println("-------------------");
//		
//		MapUtils.prettyPrint(result.getFluxValues());
//		
//		System.out.println("-------------------");
		
		hasFluxesValuesValid(result);
		
		/////////////////////////////////////////////////////////////////////////////////////
		
		//setParameters();
		method.setGeneticConditions(geneticEnvironmentalKOs);
		//assertEquals(getResults().get(KO_REACTIONSGENETICS), method.simulate().getOFvalue(), TOLERANCE);
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	//								   Utils											  //
	////////////////////////////////////////////////////////////////////////////////////////
	
	private void hasFluxesValuesValid(SteadyStateSimulationResult result) throws Exception
	{
		double sumFluxes = 0.0;
		for (String flux : result.getFluxValues().keySet())
			sumFluxes+=result.getFluxValues().get(flux);
		
		if(sumFluxes==0)
			throw new Exception(NOFLUXESMSG);
		
		if(result.getFluxValues().containsValue(Double.NaN))
			throw new Exception(NANFLUXESMSG);
	}
	
}
