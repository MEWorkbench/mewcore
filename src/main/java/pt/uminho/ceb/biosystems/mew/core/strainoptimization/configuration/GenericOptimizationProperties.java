package pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration;

import java.io.Serializable;

/**
 * Created by hgiesteira on 29-01-2016.
 */
public class GenericOptimizationProperties implements Serializable {
	
	private static final long	serialVersionUID					= 7229762984584481678L;
	
	public static final String	OPTIMIZATION_ALGORITHM				= "generic.optimizationalalgorithm";
	public static final String	OPTIMIZATION_STRATEGY				= "generic.optimizationstrategy";
	public static final String	IS_GENE_OPTIMIZATION				= "generic.isgeneOptimization";
	public static final String	IS_OVER_UNDER_EXPRESSION			= "generic.isoverunderexpression";
	public static final String	STEADY_STATE_MODEL					= "generic.steadystatemodel";
	public static final String	SIMULATION_CONFIGURATION			= "generic.simulation.configuration";
	public static final String	MAP_OF2_SIM							= "generic.mapof2sim";
	public static final String	MAX_SET_SIZE						= "generic.maxsetsize";
	public static final String	NOT_ALLOWED_IDS						= "generic.notallowedids";
	public static final String	STEADY_STATE_GENE_REACTION_MODEL	= "generic.steadystategenereactionmodel";
}
