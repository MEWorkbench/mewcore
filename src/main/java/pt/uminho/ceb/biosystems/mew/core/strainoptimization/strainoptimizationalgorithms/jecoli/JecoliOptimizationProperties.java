package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli;

import java.io.Serializable;

/**
 * Created by ptiago on 09-03-2015.
 */
public class JecoliOptimizationProperties implements Serializable {
	
	private static final long	serialVersionUID						= 7229762984584481678L;
																		
	//Jecoli Top Level	
	public static final String	IS_VARIABLE_SIZE_GENOME					= "jecoli.isvariablesizegenome";
																		
	public static final String	SOLUTION_DECODER						= "jecoli.solutiondecoder";
																		
	public static final String	TERMINATION_CRITERIA					= "jecoli.terminationcriteria";
	public static final String	STATISTICS_CONFIGURATION				= "jecoli.statistics.configuration";
	public static final String	ARCHIVE_MANAGER							= "jecoli.archivemanager";
	public static final String	ARCHIVE_MANAGER_BASE_NAME				= "jecoli.archivemanager.basename";
	public static final String	ARCHIVE_MANAGER_MAX_SIZE				= "jecoli.archivemanager.size";
	public static final String	ARCHIVE_MANAGER_INSERT_EVENT_TYPE		= "jecoli.archivemanager.insertioneventtype";
	public static final String	ARCHIVE_MANAGER_INSERT_FILTER			= "jecoli.archivemanager.insertionfilter";
	public static final String	ARCHIVE_MANAGER_PROCESSING_STRATEGY		= "jecoli.archivemanager.processingstrategy";
	public static final String	ARCHIVE_PLOTTER							= "jecoli.archivemanager.plotter";
	public static final String	ARCHIVE_MANAGER_RESIMULATE_WHEN_FINISH	= "jecoli.archivemanager.resimulatewhenfinish";
	public static final String	EVALUATION_LISTENER						= "jecoli.evaluationlistener";
																		
	//Jecoli EA
	public static final String	POPULATION_SIZE							= "jecoli.ea.populationsize";
	public static final String	ELITISM									= "jecoli.ea.elitism";
	public static final String	NUMBER_OF_SURVIVORS						= "jecoli.ea.numberofsurvivors";
	public static final String	OFFSPRING_SIZE							= "jecoli.ea.offspringsize";
																		
	public static final String	CROSSOVER_PROBABILITY					= "jecoli.ea.crossover.probability";
	public static final String	MUTATION_PROBABILITY					= "jecoli.ea.mutation.probability";
	public static final String	GROW_PROBABILITY						= "jecoli.ea.grow.probability";
	public static final String	SHRINK_PROBABILITY						= "jecoli.ea.shrink.probability";
																		
	public static final String	MUTATION_RADIUS_PERCENTAGE				= "jecoli.ea.mutation.radius";
																		
	//JecoliSA
	public static final String	ANNEALING_SCHEDULE						= "jecoli.sa.annealingschedule";
																		
	//JecoliPBIL	
	public static final String	LEARNING_RATE							= "jecoli.pbil.learningrate";
	public static final String	NUMBER_OF_SOLUTIONS_TO_SELECT			= "jecoli.pbil.numberofsolutionstoselect";
	public static final String	NUMBER_OF_SOLUTION_ELEMENTS				= "jecoli.pbil.numberofsolutionelements";
	public static final String	INDIVIDUAL_SIZE							= "jecoli.pbil.individualsize";
	public static final String	INITIAL_PROPERTY_VECTOR					= "jecoli.pbil.initialpropertyvector";
																		
	public static final String	OPTIMIZATION_STRATEGY_CONVERTER			= "jecoli.optimizationstrategyconverter";
	public static final String	REACTION_SWAP_MAP						= "jecoli.reactionswapmap";
	public static final String	IS_REACTION_SWAP						= "jecoli.isreactionswap";
	public static final String	OU_RANGE								= "jecoli.ourange";
	public static final String	OU_NEGATIVE_ALLOWED						= "jecoli.ounegativeallowed";
	public static final String	OU_EXPONENT_BASE						= "jecoli.ouexponentbase";
	public static final String	MAX_ALLOWED_SWAPS						= "jecoli.maxallowedswaps";
																		
}
