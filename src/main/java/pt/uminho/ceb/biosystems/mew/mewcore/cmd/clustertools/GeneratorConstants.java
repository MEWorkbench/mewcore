package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;


import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.searchtools.Walltime;

public final class GeneratorConstants {

	public static final String DELIMITER 				= Delimiter.COMMA.toString();
	public static final String DASH 					= System.getProperty("file.separator");
	public static final String NEW_LINE 				= System.getProperty("line.separator");
	public static final String JAVA_HOME				= System.getenv("JAVA_HOME");
	public static final String JAR_FILE_NAME			= "runner.jar";		
	
	public static final String HELP_ARG					= "--help";
	public static final String RUN_ARG					= "--run";
	public static final String GENERATE_ARG				= "--gen";
	public static final String ALGS_ARG					= "--algs";
	public static final String OFS_ARG					= "--ofs";
	public static final String KS_ARG					= "--ks";
	public static final String BMS_ARG					= "--bms";
	public static final String PROD_ARG					= "--prds";
	public static final String NO_DIR_ARGS				= "--nodir";
	public static final String VAR_SIZE_ARG				= "--vs";
	public static final String ALPHAS_ARG				= "--alphas";
	public static final String SM_ARG					= "--sms";
	public static final String OTS_ARG					= "--ots";
	
	public static final	String SOLUTIONS_SET_SUFFIX		= ".ss";
	public static final	String SOLUTIONS_SUFFIX			= ".solutions";
	public static final	String FITNESSES_SUFFIX			= ".fitnesses";
	public static final	String NONDOM_SETS_SUFFIX		= ".nds";
	public static final	String CONFS_SUFFIX				= ".conf";
	public static final String LOG_SUFFIX				= ".log";
	public static final String SCRIPT_SUFFIX			= ".sh"; 
	
	public static final String RUN_ALL_PREFIX			= "runall";
	public static final String STATS_PREFIX				= "calculateStats";
	
	public static final	String COMMENTS_ENDING			= " TEST CONFIGURATION";
	public static final	String DEFAULT_NAME_CONNECTOR	= "#";
	
	public static final String ORGANISM_KEY 			= "organism";
	public static final String FLUXES_FILE_KEY 			= "reactionsFile";
	public static final String MATRIX_FILE_KEY 			= "matrixFile";
	public static final String METABOLITES_FILE_KEY 	= "metabolitesFile";
	public static final String CRIT_REACTIONS_FILE_KEY 	= "criticalReactionsFile";
	public static final String CRIT_GENES_FILE_KEY 		= "criticalGenesFile";
	public static final String GENE_RULES_FILE_KEY		= "geneRulesFile";
	public static final String BASEDIR_FILE_KEY 		= "baseDirectory";
	public static final String BIOMASS_KEY 				= "biomassReaction";
	public static final String PRODUCT_KEY 				= "desiredProduct";
	public static final String SUBSTRATE_KEY 			= "substrateReaction";
	public static final String MIN_KNOCKOUTS_KEY 		= "minimizeKnockouts";
	public static final String MIN_FLUX_SUM_KEY 		= "minimizeFluxSum";
	public static final String MIN_FLUX_CHANGES_KEY 	= "minimizeFluxChanges";
	public static final String MIN_SQUARED_DIFFS_KEY 	= "minimizeSquaredDiffs";
	public static final String ALGORITHM_KEY 			= "algorithm";
	public static final String SIMULATION_METHOD_KEY	= "simulationMethod";
	public static final String ITERATIONS_KEY 			= "numIterations";
	public static final String FUNC_EVALS_KEY 			= "functionEvaluations";
	public static final String MAX_SET_KEY 				= "maxKnockouts";
	public static final String VAR_SIZE_KEY 			= "variableSize";
	public static final String OBJ_FUNC_KEY				= "objectiveFunction";
	public static final String BIOMASS_PERC_KEY			= "biomassPercentage";
	public static final String WEIGHTED_BP_ALPHA_KEY	= "weigthedBPalpha";
	public static final String KNOCK_TYPE_KEY			= "knockoutType";
	public static final String OVERUNDER_EXPRESSION		= "overUnder";
	public static final String RUN_NUMBER_KEY 			= "run";
	public static final String WALLTIME_KEY 			= "walltime";
	public static final String QSUB_PROPERTIES_KEY		= "qsubProperties";
	public static final String MAX_THREADS_KEY			= "maxThreads";
	
	public static final String SINGLE_OBJECTIVE_ARG		= "SO";
	public static final String MULTI_OBJECTIVE_ARG		= "MO";
	
	public static final Walltime 	WALLTIME			= Walltime.SHORT;
	public static final String		QSUB_PROP_DEFAULT	= "-d. -l "; 	
	
	public static final SolverType LP_SOLVER			= SolverType.CPLEX;
	public static final SolverType MILP_SOLVER			= SolverType.CPLEX;
	public static final SolverType QP_SOLVER			= SolverType.CPLEX;

	
	public static final String USAGE_HELP				= "java -jar "+JAR_FILE_NAME+" "+HELP_ARG;
	public static final String USAGE_RUN				= "java -jar "+JAR_FILE_NAME+" "+RUN_ARG+" [configurationFile] <[run_number]>";
	public static final String USAGE_GENERATE			= "java -jar "+JAR_FILE_NAME+" "+GENERATE_ARG+" [baseConfigurationFile]"+" [numberOfRuns]\n"+
															"\tFor generating mutiples runs it is possible use some options:\n" +
															"\t\t" + ALGS_ARG + "[ALG1,ALG2,...]:\tto run multiple Algorithms\n" + 
															"\t\t" + OFS_ARG + "[OF1,OF2,...]:\tto run multiple objective functions\n" +
															"\t\t" + KS_ARG + "[K1,K2,...]:\tto run multiple number max of kos\n"+
															"\t\t" + BMS_ARG + "[BM1,BM2,...]:\tto use different percentage of biomass in yield objective function\n"+
															"\t\t" + ALPHAS_ARG + "[A1,A2,...]:\tto use different alpha values for the weighted biomass-product objective function\n"+
															"\t\t" + PROD_ARG + "[PROD1,PROD2,...]:\tto use different fluxes of prodution\n"+
															"\t\t" + SM_ARG + "[FBA, PFBA, ROOM, MOMA, LMOMA, MIMBL]:\tto use different simulation methods\n"+
															"\t\t" + OTS_ARG + "[RK,GK,ROU,GOU]:\tto use different simulation methods\n"+
															"\t\t" + NO_DIR_ARGS + "\t\tdisable the directory tree creation\n"+
															"\t\t" + VAR_SIZE_ARG + "\t\t to use the variable size extrategy\n";
	
	public static final String JAR_ARGS					= "-Xmx1024m";
}
