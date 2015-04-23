package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.mt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ArchiveManagerBestSolutions;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.AbstractTestRunner;
import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.GeneratorConstants;
import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.mewcore.criticality.CriticalReactions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateMTOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.archivetrimming.SelectionValueTrimmer;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.controlcenter.MTSmartStrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidFieldException;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.ObjectiveFunctionType;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.utils.TimeUtils;

public class TestRunnerMTSmart extends AbstractTestRunner{
	
	protected static final Pattern OF_PATTERN = Pattern.compile("([A-Z]+\\d?)\\((.+)\\)");

	public		String					ORGANISM;	
	public		String					FLUXES;
	public		String					MATRIX;
	public		String 					METABOLITES;
	public		String					GENE_RULES;
	public		String 					CRITICAL_REACTIONS;
	public		String					CRITICAL_GENES;
	public		String					BASE_DIR;		
	public		String					BIOMASS;
	public		boolean					OVERUNDER;
	public		AlgorithmTypeEnum 		ALGORITHM;
	public		String[]				SIMULATION_METHODS;
	public		int						FUNC_EVALS;
	public		int						MAX_SET;
	public		boolean					VAR_SIZE;
	public		String				 	OBJECTIVE_FUNCTION = null;
	public		String					KNOCKOUT_TYPE;
	public		int						RUN_NUMBER;

	public		Properties				CONFIGURATION;
	public		String					BASE_NAME;	

	public		boolean					isGeneKnockout = false;
	
	public		int						MAX_THREADS = 1;

	public static void main(String...args)
	{
		try {

			if(args.length==0){
				TestRunnerMTSmart tr = new TestRunnerMTSmart("files/iJR904_simp/iJR904textSO.conf");
//				TestRunnerMTSmart tr = new TestRunnerMTSmart("files/ecolicore/ecolicore.conf");
//				TestRunnerMTSmart tr = new TestRunnerMTSmart("files/ecolicore/ecolicore_convergence.conf");
//				TestRunnerMTSmart tr = new TestRunnerMTSmart("files/iJR904_simp/iJR904textMT_YIELD.conf");
				tr.run();
			}
			else{
				TestRunnerMTSmart tr = new TestRunnerMTSmart(args[1],Integer.parseInt(args[2]));
				tr.run();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public TestRunnerMTSmart(Properties prop) throws Exception{
		CONFIGURATION = prop;
		convertParameters();
	}


	public TestRunnerMTSmart(String configurationFile) throws Exception{
		CONFIGURATION = new Properties();
		readConfig(CONFIGURATION,configurationFile);
		convertParameters();
	}
	
	public TestRunnerMTSmart(String configurationFile, int runNumber) throws Exception{
		this(configurationFile);
		this.RUN_NUMBER = runNumber;
	}

	public void convertParameters() throws Exception{

		/****************************************************** 
		 * GET ALL PROPERTIES / CONVERT THEM INTO PARAMETERES *
		 ******************************************************/		
		ORGANISM			=		CONFIGURATION.getProperty(GeneratorConstants.ORGANISM_KEY);

		FLUXES 				=	 	CONFIGURATION.getProperty(GeneratorConstants.FLUXES_FILE_KEY);
		MATRIX 				= 		CONFIGURATION.getProperty(GeneratorConstants.MATRIX_FILE_KEY);
		METABOLITES			= 		CONFIGURATION.getProperty(GeneratorConstants.METABOLITES_FILE_KEY);
		CRITICAL_REACTIONS	=		CONFIGURATION.getProperty(GeneratorConstants.CRIT_REACTIONS_FILE_KEY);
		BASE_DIR			=		CONFIGURATION.getProperty(GeneratorConstants.BASEDIR_FILE_KEY);	
		BIOMASS				=		CONFIGURATION.getProperty(GeneratorConstants.BIOMASS_KEY);	
		OVERUNDER			= 		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.OVERUNDER_EXPRESSION));
		ALGORITHM			=		getAlgorithm(CONFIGURATION.getProperty(GeneratorConstants.ALGORITHM_KEY));
		SIMULATION_METHODS	=		parseSimulationMethods(CONFIGURATION.getProperty(GeneratorConstants.SIMULATION_METHOD_KEY));
		FUNC_EVALS			=		Integer.parseInt(CONFIGURATION.getProperty(GeneratorConstants.FUNC_EVALS_KEY));
		VAR_SIZE			=		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.VAR_SIZE_KEY));
		KNOCKOUT_TYPE		=		CONFIGURATION.getProperty(GeneratorConstants.KNOCK_TYPE_KEY);
		RUN_NUMBER			=		Integer.parseInt(CONFIGURATION.getProperty(GeneratorConstants.RUN_NUMBER_KEY));
		MAX_SET 			=		Integer.parseInt(CONFIGURATION.getProperty(GeneratorConstants.MAX_SET_KEY));
		MAX_THREADS 		=		Integer.parseInt(CONFIGURATION.getProperty(GeneratorConstants.MAX_THREADS_KEY));
		OBJECTIVE_FUNCTION	= 		CONFIGURATION.getProperty(GeneratorConstants.OBJ_FUNC_KEY);
				
		if(KNOCKOUT_TYPE.equalsIgnoreCase("gene") || KNOCKOUT_TYPE.equalsIgnoreCase("g") || KNOCKOUT_TYPE.equalsIgnoreCase("genes")){
			this.isGeneKnockout = true;
			GENE_RULES = CONFIGURATION.getProperty(GeneratorConstants.GENE_RULES_FILE_KEY);
			CRITICAL_GENES = CONFIGURATION.getProperty(GeneratorConstants.CRIT_GENES_FILE_KEY);
		}
		else 
			this.isGeneKnockout = false;	
		
	}
	
	public String[] parseSimulationMethods(String s) throws Exception{
		String[] tokens = s.split(GeneratorConstants.DELIMITER);
		String[] ret = new String[tokens.length];
		for(int i=0; i<tokens.length; i++)
			ret[i] = getSimulationMethod(tokens[i]);
	
		return ret;  
	}

	public List<String> loadCriticalIDs(String filename) throws Exception{
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		ArrayList<String> critical = new ArrayList<String>();

		while(br.ready()){
			String str = br.readLine().trim();			
			critical.add(str);
		}

		br.close();
		fr.close();

		return critical;
	}
	
	protected IndexedHashMap<IObjectiveFunction,String> loadObjectiveFunctions() throws  Exception {
		
		String[] ofList = OBJECTIVE_FUNCTION.split(";\\s*");
		
		for(String s : ofList)
			System.out.println(s);
		if(ofList.length == 0)
			throw new InvalidFieldException(
					"ObjectiveFunction",
					"At least one Objective function must be provided!",
					new ArrayIndexOutOfBoundsException(-1));
		
		IndexedHashMap<IObjectiveFunction,String> objFunctions = new IndexedHashMap<IObjectiveFunction,String>();
		
		for(String ofInfo : ofList) {			
			Pair<IObjectiveFunction,String> of = processOFString(ofInfo); 
			objFunctions.put(of.getValue(),of.getPairValue());
		}
		
		return objFunctions;
	}
	
	public Pair<IObjectiveFunction,String> processOFString(String ofString) throws Exception{
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		System.out.println("TAG= "+ofTag);
		System.out.println("ARGS= "+ofArgs);
		if(ofTag.equalsIgnoreCase("LINK")){
			String[] argsList = ofArgs.split(",\\s+",2);
			String simMethod = getSimulationMethod(argsList[0].trim());			
			System.out.println("SIM= "+simMethod);
			System.out.println("ARGS= "+argsList[1].trim());
			IObjectiveFunction ofIN = processOFParams(argsList[1]);
			return new Pair<IObjectiveFunction,String>(ofIN,simMethod);
		}
		else throw new InvalidObjectiveFunctionConfiguration("Objective functions incorrectly linked to simulation methods. Must follow this syntax LINK([simMethod1], OF1(param,...); LINK([simMethod2], OF2(param,...))");
	}
	
	public IObjectiveFunction processOFParams(String ofString) throws InvalidObjectiveFunctionConfiguration{
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		ObjectiveFunctionType oft = ObjectiveFunctionType.valueOf(ofTag);
		String[] argsList = ofArgs.split(",\\s+");
		return oft.getObjectiveFunction(argsList);
	}

	public void run() throws Exception, InvalidConfigurationException{

		// model
		FlatFilesReader reader = new FlatFilesReader(FLUXES, MATRIX, METABOLITES, GENE_RULES, "Test Model");
		Container container = new Container(reader);
				
		container.putDrainsInReactantsDirection();
		Set<String> bMetabolites = container.identifyMetabolitesWithDrain();
		Set<String> drains = container.getDrains();
		
		for(String m: bMetabolites)
			System.out.println(m);

		for(String d : drains){
			ReactionConstraintCI constraint = container.getDefaultEC().get(d);
			if(constraint==null){
				constraint = new ReactionConstraintCI(-10000, 10000);
				container.getDefaultEC().put(d, constraint);
				String bounds = constraint.getLowerLimit()+"/"+constraint.getUpperLimit();
				System.out.println("Added drain constraint: "+d+" = "+bounds);			
			}
		}
		
		ISteadyStateModel model = ContainerConverter.convert(container);
		ContainerConverter.setBoundaryMetabolitesInModel(model, bMetabolites);
		
		model.setBiomassFlux(BIOMASS);

		// solver
		SolverType solver = SolverType.CPLEX;

		// critical
		List<String> notAllowedIDs = new ArrayList<String>();
		if(CRITICAL_GENES!=null || CRITICAL_REACTIONS!=null)
			if (isGeneKnockout)
				notAllowedIDs = loadCriticalIDs(CRITICAL_GENES);		
			else{
				CriticalReactions cr = new CriticalReactions(model, null, solver);
				cr.loadCriticalReactionsFromFile(CRITICAL_REACTIONS);
				cr.setDrainReactionsAsCritical();
				cr.setTransportReactionsAsCritical();
				notAllowedIDs = cr.getCriticalReactionIds();
			}
		
		System.out.println("REACTIONS= "+model.getNumberOfReactions());
		System.out.println("METABOLITES= "+model.getNumberOfMetabolites());
		if(isGeneKnockout)
		System.out.println("GENES= "+((ISteadyStateGeneReactionModel)model).getNumberOfGenes());
		if(!isGeneKnockout)
			System.out.println("VARIABLES= "+(model.getNumberOfReactions()-notAllowedIDs.size()));
		else
			System.out.println("VARIABLES= "+(((ISteadyStateGeneReactionModel)model).getNumberOfGenes()-notAllowedIDs.size()));
		
		IndexedHashMap<IObjectiveFunction,String> objectiveFunctions = loadObjectiveFunctions();
		
		BASE_NAME = generateOutputFileName(ORGANISM, RUN_NUMBER, isGeneKnockout, OVERUNDER, ALGORITHM, SIMULATION_METHODS, objectiveFunctions);

		List<String> simmethods = new ArrayList<String>();
		for(String s : SIMULATION_METHODS)
			simmethods.add(s);
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(null, null, model, SimulationMethodsEnum.PFBA.getSimulationProperty());
		cc.setSolver(solver);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(BIOMASS, 1.0);
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println("BIOMASS = "+res.getFluxValues().getValue(BIOMASS));
		
		// strain opt
		MTSmartStrainOptimizationControlCenter optCenter = new MTSmartStrainOptimizationControlCenter(
				model, 
				null, 
				isGeneKnockout, 
				OVERUNDER, 
				VAR_SIZE, 
				MAX_SET, 
				ALGORITHM,
				simmethods,
				objectiveFunctions,
				null,
				solver,
				SimulationProperties.PARSIMONIUS, //NOTE: should this be the default?? probably include selection possibility
				null,
				notAllowedIDs,
				MAX_THREADS);
		
		ArchiveManagerBestSolutions archive = new ArchiveManagerBestSolutions(
				optCenter.getOptimizationAlgorithm(),
				InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
				InsertionStrategy.ADD_SMART_KEEP_BEST,
				ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
				optCenter.getEvaluationFunction().isMaximization());
		
//		ArchiveManager archive = new ArchiveManager(
//				optCenter.getOptimizationAlgorithm(),
//				InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT,
//				InsertionStrategy.ADD_SMART,
//				ProcessingStrategy.PROCESS_ARCHIVE_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT
//				);

		archive.setMaximumArchiveSize(100);

		ITrimmingFunction trimmer = (optCenter.getEvaluationFunction().getNumberOfObjectives()>1) 	? new ZitzlerTruncation(archive.getMaximumArchiveSize(), optCenter.getEvaluationFunction())
																									: new SelectionValueTrimmer(archive.getMaximumArchiveSize(), 0.000001,true, optCenter.getEvaluationFunction().isMaximization());

		archive.addTrimmingFunction(trimmer);
		
		
		optCenter.setArchive(archive);
		optCenter.setTerminationCriteria(new NumFunctionEvaluationsListenerHybridTerminationCriteria(FUNC_EVALS));
		// run optimization
		
		// redirect java output
		PrintStream console = System.out;

		File file = new File(BASE_NAME+".log");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		
		PrintStream console_err = System.err;

		File file_err = new File(BASE_NAME+".err");
		FileOutputStream fos_err = new FileOutputStream(file_err);
		PrintStream ps_err = new PrintStream(fos_err);
		System.setErr(ps_err);						
		
		// 
		long timestart = System.currentTimeMillis();
		SteadyStateMTOptimizationResult result = optCenter.run();
		long timeend = System.currentTimeMillis();
		
		long timetotal = timeend-timestart;
		System.out.println("Execution took: "+TimeUtils.formatMillis(timetotal));

		if(result.getNumberOfResults()>0){
			System.out.println("Saving...");
			result.writeToFile(BASE_NAME+".ss", GeneratorConstants.DELIMITER);
			System.out.println("Saved to "+BASE_NAME+".ss");
			System.out.println("Saving bests...");
			archive.writeBestSolutionsToFile(BASE_NAME+".best", GeneratorConstants.DELIMITER, true, optCenter.getEvaluationFunction().getDecoder());
			System.out.println("Saved bests to "+BASE_NAME+".best");
		}
		
		// set console as default output again
		System.setOut(console);
		System.setErr(console_err);
		
		
	}	
	

	private static String generateOutputFileName(
			String organism, 
			int run_number,
			boolean isGeneKnockout,
			boolean isOverUnder,
			AlgorithmTypeEnum algorithm,
			String[] simulationMethods,
			IndexedHashMap<IObjectiveFunction,String> objectiveFunctions
			) {
		
		StringBuffer buff = new StringBuffer();

		buff.append(organism+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(algorithm.getShortName()+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		for(String sm : simulationMethods)
			buff.append(sm+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		buff.append((isGeneKnockout ? "G" : "R")+(isOverUnder ? "OU" : "K")+GeneratorConstants.DEFAULT_NAME_CONNECTOR);	
		
		for(int i=0; i<objectiveFunctions.size(); i++){
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			String ofn = (of.isMaximization() ? "MAX" : "MIN")+"~"+of.getShortString();
			buff.append(ofn+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		}
		
		buff.append("run"+run_number);

		return buff.toString();
	}

	/**
	 * GET THE ALGORITH STRING, RETURN THE CORRESPONDING ENUM CONSTANT
	 * 
	 * @param property
	 * @return
	 */
	private static AlgorithmTypeEnum getAlgorithm(String property) {

		property = property.toUpperCase();
		AlgorithmTypeEnum alg  = null;
		try{
			alg = Enum.valueOf(AlgorithmTypeEnum.class,property); 
		} catch(IllegalArgumentException e){
			alg = AlgorithmTypeEnum.SPEA2;
		}

		return alg; 
	}


	/**
	 * READ THE CONFIGURATION
	 * 
	 * @param properties
	 * @param confFile
	 */
	public static void readConfig(final Properties properties,String confFile){
		try {			
//			properties.loadFromXML(new FileInputStream(confFile));
			properties.load(new FileInputStream(confFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public String getORGANISM() {
		return ORGANISM;
	}



	public String getFLUXES() {
		return FLUXES;
	}



	public String getMATRIX() {
		return MATRIX;
	}



	public String getMETABOLITES() {
		return METABOLITES;
	}



	public String getCRITICAL() {
		return CRITICAL_REACTIONS;
	}



	public String getBASE_DIR() {
		return BASE_DIR;
	}



	public String getBIOMASS() {
		return BIOMASS;
	}


	public int getFUNC_EVALS() {
		return FUNC_EVALS;
	}



	public int getMAX_SET() {
		return MAX_SET;
	}



	public boolean isVAR_SIZE() {
		return VAR_SIZE;
	}



	public int getRUN_NUMBER() {
		return RUN_NUMBER;
	}



	public Properties getCONFIGURATION() {
		return CONFIGURATION;
	}



	public String getBASE_NAME() {
		return BASE_NAME;
	}


	public String getGENE_RULES() {
		return GENE_RULES;
	}


	public String getCRITICAL_REACTIONS() {
		return CRITICAL_REACTIONS;
	}


	public String getCRITICAL_GENES() {
		return CRITICAL_GENES;
	}

	public String[] getSIMULATION_METHOD() {
		return SIMULATION_METHODS;
	}

	public String getOBJECTIVE_FUNCTION() {
		return OBJECTIVE_FUNCTION;
	}


	public String getKNOCKOUT_TYPE() {
		return KNOCKOUT_TYPE;
	}


	public boolean isGeneKnockout() {
		return isGeneKnockout;
	}
}
