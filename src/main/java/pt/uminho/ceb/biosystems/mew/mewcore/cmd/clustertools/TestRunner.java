package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;

import pt.uminho.ceb.biosystems.mew.mewcore.criticality.CriticalReactions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.SteadyStateOptimizationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.controlcenter.StrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.EnsembleObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidFieldException;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.ObjectiveFunctionType;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.SolutionSimplification;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;

public class TestRunner extends AbstractTestRunner{
	
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
	public		String					PRODUCT;
	public		String					SUBSTRATE;
	public		boolean					MIN_KNOCKOUTS;
	public		boolean					MIN_FLUX_SUM;
	public		boolean					MIN_CHANGES;
	public		boolean					MIN_SQUARED_DIFFS;
	public		boolean					OVERUNDER;
	public		AlgorithmTypeEnum 		ALGORITHM;
	public		String					SIMULATION_METHOD;
	public		int						ITERATIONS;
	public		int						FUNC_EVALS;
	public		int						MAX_SET;
	public		int[]					MULT_MAX_SET;
	public		boolean					VAR_SIZE;
	public		String				 	OBJECTIVE_FUNCTION = null;
	public		String					KNOCKOUT_TYPE;
	public		double					BIOMASS_PERCENTAGE;
	public		double[]				MULT_BIOMASS_PERCENTAGES;
	public		int						RUN_NUMBER;

	public		Properties				CONFIGURATION;
	public		String					BASE_NAME;	

	public		boolean					isGeneKnockout = false;
	public		boolean					isMultipleKnocks = false;
	public		boolean					isMultipleBiomassPercentages = false;

	public		double					WEIGHTED_BP_ALPHA;

	public		boolean					USE_ARCHIVE = false;
	
	public		int						MAX_THREADS = 1;

	public static void main(String...args)
	{
		try {

//			TestRunner tr = new TestRunner("files/ecolicoretext.conf");
//			TestRunner tr = new TestRunner("files/iJR904textMT.conf");
			TestRunner tr = new TestRunner("files/iAF1260_orig_simpG/iAF1260.conf");
			tr.run();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TestRunner(Properties prop){
		CONFIGURATION = prop;
		convertParameters();
	}


	public TestRunner(String configurationFile){
		CONFIGURATION = new Properties();
		readConfig(CONFIGURATION,configurationFile);
		convertParameters();
	}

	public void convertParameters(){

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
		PRODUCT				=		CONFIGURATION.getProperty(GeneratorConstants.PRODUCT_KEY);
		SUBSTRATE			=		CONFIGURATION.getProperty(GeneratorConstants.SUBSTRATE_KEY);
		MIN_KNOCKOUTS		=		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.MIN_KNOCKOUTS_KEY));
		MIN_FLUX_SUM		=		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.MIN_FLUX_SUM_KEY));
		MIN_CHANGES			=		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.MIN_FLUX_CHANGES_KEY));
		MIN_SQUARED_DIFFS	=		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.MIN_SQUARED_DIFFS_KEY));
		
		OVERUNDER			= 		Boolean.parseBoolean(CONFIGURATION.getProperty(GeneratorConstants.OVERUNDER_EXPRESSION));

		ALGORITHM			=		getAlgorithm(CONFIGURATION.getProperty(GeneratorConstants.ALGORITHM_KEY));
		SIMULATION_METHOD	=		CONFIGURATION.getProperty(GeneratorConstants.SIMULATION_METHOD_KEY);

		ITERATIONS			=		Integer.parseInt(CONFIGURATION.getProperty(GeneratorConstants.ITERATIONS_KEY));
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
	
	protected List<IObjectiveFunction> loadObjectiveFunctions() throws  InvalidObjectiveFunctionConfiguration, InvalidFieldException {
		
		String[] ofList = OBJECTIVE_FUNCTION.split(";\\s*");
		
		for(String s : ofList)
			System.out.println(s);
		if(ofList.length == 0)
			throw new InvalidFieldException(
					"ObjectiveFunction",
					"At least one Objective function must be provided!",
					new ArrayIndexOutOfBoundsException(-1));
		
		List<IObjectiveFunction> objFunctions = new ArrayList<IObjectiveFunction>();
		
		for(String ofInfo : ofList) {			
			IObjectiveFunction of = processOFString(ofInfo); 
			objFunctions.add(of);
		}
		
		return objFunctions;
	}
	
	public IObjectiveFunction processOFString(String ofString) throws InvalidObjectiveFunctionConfiguration{
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		System.out.println("TAG= "+ofTag);
		System.out.println("ARGS= "+ofArgs);
		ObjectiveFunctionType oft = ObjectiveFunctionType.valueOf(ofTag);
		if(oft.equals(ObjectiveFunctionType.EN)){
			String[] argsList = ofArgs.split(",\\s+",2);
			String simMethod = argsList[0];			
			System.out.println("SIM= "+simMethod);
			System.out.println("ARGS= "+argsList[1]);
			IObjectiveFunction ofIN = processOFString(argsList[1]);
			return new EnsembleObjectiveFunction(simMethod, ofIN);
		}
		else {
			String[] argsList = ofArgs.split(",\\s+");
			return oft.getObjectiveFunction(argsList);
		}
	}

	public void run() throws Exception, InvalidConfigurationException{

		// model
		FlatFilesReader reader = new FlatFilesReader(FLUXES, MATRIX, METABOLITES, GENE_RULES, "Test Model");
		Container container = new Container(reader);
				
//		container.putDrainsInReactantsDirection();
		Set<String> bMetabolites = container.identifyMetabolitesWithDrain();
		Set<String> drains = container.getDrains();
		
		for(String m: bMetabolites)
			System.out.println(m);

		for(String d : drains){
			ReactionConstraintCI constraint = container.getDefaultEC().get(d);
			if(constraint==null){
				constraint = new ReactionConstraintCI(-10000, 100000);
				container.getDefaultEC().put(d, constraint);
			}
			String bounds = constraint.getLowerLimit()+"/"+constraint.getUpperLimit();
			System.out.println(d+" = "+bounds);			
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
		
		List<IObjectiveFunction> objectiveFunctions = loadObjectiveFunctions();
		
		BASE_NAME = generateOutputFileName(ORGANISM, RUN_NUMBER, isGeneKnockout, OVERUNDER, ALGORITHM, SIMULATION_METHOD, objectiveFunctions);

		// strain opt
		StrainOptimizationControlCenter optCenter = new StrainOptimizationControlCenter(
				model, 
				null, 
				isGeneKnockout, 
				OVERUNDER, 
				VAR_SIZE, 
				MAX_SET, 
				ALGORITHM,
				null,
				objectiveFunctions,
				solver,
				SIMULATION_METHOD,
				null,
				SimulationProperties.PARSIMONIUS, //NOTE: should this be the default?? probably include selection possibility
				null,
				notAllowedIDs);
		
		optCenter.configureDefaultArchive();
		optCenter.setTerminationCriteria(new NumFunctionEvaluationsListenerHybridTerminationCriteria(FUNC_EVALS));
//		optCenter.getArchive().setPlotter(new Plot2DGUI<IElementsRepresentation<?>>(optCenter.getOptimizationAlgorithm()));

		// run optimization
		SteadyStateOptimizationResult result = optCenter.run();

		if(result.getNumberOfResults()>0){
			System.out.print("Simplifying solutions...");
			SolutionSimplification simplification = new SolutionSimplification(model, objectiveFunctions, SIMULATION_METHOD,null, null, solver);		
			SteadyStateOptimizationResult simplifiedResult = simplification.simplifySteadyStateOptimizationResult(result, isGeneKnockout);
			System.out.println(" DONE!");
			
			System.out.println("Saving...");
			simplifiedResult.writeToFile(BASE_NAME+".ss", GeneratorConstants.DELIMITER);
			System.out.println("Saved to "+BASE_NAME+".ss");

		}
		
		
	}	
	

	private static String generateOutputFileName(
			String organism, 
			int run_number,
			boolean isGeneKnockout,
			boolean isOverUnder,
			AlgorithmTypeEnum algorithm,
			String simulationMethod,
			List<IObjectiveFunction> objectiveFunctions
			) {
		
		StringBuffer buff = new StringBuffer();

		buff.append(organism+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(algorithm.getShortName()+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(simulationMethod+GeneratorConstants.DEFAULT_NAME_CONNECTOR);
		buff.append((isGeneKnockout ? "G" : "R")+(isOverUnder ? "OU" : "K")+GeneratorConstants.DEFAULT_NAME_CONNECTOR);	
		
		for(IObjectiveFunction of: objectiveFunctions){
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



	public String getPRODUCT() {
		return PRODUCT;
	}



	public boolean isMIN_KNOCKOUTS() {
		return MIN_KNOCKOUTS;
	}



	public boolean isMIN_FLUX_SUM() {
		return MIN_FLUX_SUM;
	}



	public AlgorithmTypeEnum getALGORITHM() {
		return ALGORITHM;
	}



	public int getITERATIONS() {
		return ITERATIONS;
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


	public String getSUBSTRATE() {
		return SUBSTRATE;
	}


	public String getSIMULATION_METHOD() {
		return SIMULATION_METHOD;
	}


	public int[] getMULT_MAX_SET() {
		return MULT_MAX_SET;
	}


	public String getOBJECTIVE_FUNCTION() {
		return OBJECTIVE_FUNCTION;
	}


	public String getKNOCKOUT_TYPE() {
		return KNOCKOUT_TYPE;
	}


	public double getBIOMASS_PERCENTAGE() {
		return BIOMASS_PERCENTAGE;
	}


	public double[] getMULT_BIOMASS_PERCENTAGES() {
		return MULT_BIOMASS_PERCENTAGES;
	}


	public boolean isGeneKnockout() {
		return isGeneKnockout;
	}


	public boolean isMultipleKnocks() {
		return isMultipleKnocks;
	}


	public boolean isMultipleBiomassPercentages() {
		return isMultipleBiomassPercentages;
	}

	public double getWEIGHTED_BP_ALPHA() {
		return WEIGHTED_BP_ALPHA;
	}

	public void setWEIGHTED_BP_ALPHA(double wEIGHTED_BP_ALPHA) {
		WEIGHTED_BP_ALPHA = wEIGHTED_BP_ALPHA;
	}

}
