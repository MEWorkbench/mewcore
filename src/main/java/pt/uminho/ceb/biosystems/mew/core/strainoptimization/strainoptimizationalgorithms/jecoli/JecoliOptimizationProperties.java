package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.io.Serializable;

/**
 * Created by ptiago on 09-03-2015.
 */
public class JecoliOptimizationProperties implements Serializable {
	
	private static final long	serialVersionUID					= 7229762984584481678L;
	
	//Jecoli Top Level
	public static final String	OPTIMIZATION_ALGORITHM				= "jecoli.optimizationalalgorithm";
	public static final String	OPTIMIZATION_STRATEGY				= "jecoli.optimizationstrategy";
	public static final String	IS_GENE_OPTIMIZATION				= "jecoli.isgeneOptimization";
	public static final String	IS_OVER_UNDER_EXPRESSION			= "jecoli.isoverunderexpression";
	public static final String	IS_VARIABLE_SIZE_GENOME				= "jecoli.isvariablesizegenome";
	public static final String	STEADY_STATE_MODEL					= "jecoli.steadystatemodel";
	public static final String	SOLUTION_DECODER					= "jecoli.solutiondecoder";
//	public static final String	ENVIRONMENTAL_CONDITIONS			= "jecoli.environmentalconditions";
//	public static final String	SOLVER								= "jecoli.solver";
//	public static final String	SIMULATION_METHOD_LIST				= "jecoli.simulationmethod";
//	public static final String	REFERENCE_FLUX_DISTRIBUITION		= "jecoli.referencefluxdistribuition";
//	public static final String	IS_MAXIMIZATION						= "jecoli.isMaximization";
//	public static final String	OU_2_STEP_APPROACH					= "jecoli.ou2stepapproach";
	public static final String	SIMULATION_CONFIGURATION			= "jecoli.simulation.configuration";
	public static final String	MAP_OF2_SIM							= "jecoli.mapof2sim";
	public static final String	MAX_SET_SIZE						= "jecoli.maxsetsize";
	public static final String	NOT_ALLOWED_IDS						= "jecoli.notallowedids";
	public static final String	TERMINATION_CRITERIA				= "jecoli.terminationcriteria";
	public static final String	STATISTICS_CONFIGURATION			= "jecoli.statistics.configuration";
	public static final String	ARCHIVE_MANAGER						= "jecoli.archivemanager";
	public static final String	ARCHIVE_PLOTTER						= "jecoli.archivemanager.plotter";
	public static final String	EVALUATION_LISTENER					= "jecoli.evaluationlistener";
	
	//Jecoli EA
	public static final String	POPULATION_SIZE						= "jecoli.ea.populationsize";
	public static final String	ELITISM								= "jecoli.ea.elitism";
	public static final String	NUMBER_OF_SURVIVORS					= "jecoli.ea.numberofsurvivors";
	public static final String	OFFSPRING_SIZE						= "jecoli.ea.offspringsize";
	
	//JecoliSA
	public static final String	ANNEALING_SCHEDULE					= "jecoli.sa.annealingschedule";
	
	//JecoliPBIL
	public static final String	LEARNING_RATE						= "jecoli.pbil.learningrate";
	public static final String	NUMBER_OF_SOLUTIONS_TO_SELECT		= "jecoli.pbil.numberofsolutionstoselect";
	public static final String	NUMBER_OF_SOLUTION_ELEMENTS			= "jecoli.pbil.numberofsolutionelements";
	public static final String	INDIVIDUAL_SIZE						= "jecoli.pbil.individualsize";
	public static final String	INITIAL_PROPERTY_VECTOR				= "jecoli.pbil.initialpropertyvector";
	
	public static final String	STEADY_STATE_GENE_REACTION_MODEL	= "jecoli.steadystategenereactionmodel";
	public static final String	OPTIMIZATION_STRATEGY_CONVERTER		= "jecoli.optimizationstrategyconverter";
	public static final String	REACTION_SWAP_MAP					= "jecoli.reactionswapmap";
	public static final String	IS_REACTION_SWAP					= "jecoli.isreactionswap";
	public static final String	OU_RANGE							= "jecoli.ourange";
	public static final String	MAX_ALLOWED_SWAPS					= "jecoli.maxallowedswaps";
}
