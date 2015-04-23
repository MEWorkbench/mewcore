package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationStrategy;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.configuration.ModelConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;

/**
 * The <code> OptimizationTargetsControlCenter </code>.
 * 
 * This class allows for tightening the search space for optimization
 * algorithms.
 * 
 * It allows searching for non-interesting targets based on:
 * <ul>
 * <li>essential reactions/genes</li>
 * <li>zero valued reactions</li>
 * <li>equivalent reactions</li>
 * <li>non-gene associated reactions</li>
 * <li>drains and transport reactions</li>
 * <li>specific pathway related reactions/genes</li>
 * <li>high carbon metabolites related reactions</li>
 * </ul>
 * 
 * @author pmaia
 * @date Nov 11, 2013
 * @version 1.0
 * @since Metabolic3
 */
public class OptimizationTargetsControlCenter {
	
	public static final String				CARBON							= "C";
	
	private static final int				DEFAULT_CARBON_OFFSET			= 7;
	
	public Flag								IDENTIFY_CRITICAL				= null;
	public Flag								IDENTIFY_ZEROS					= null;
	public Flag								IDENTIFY_EQUIVALENCES			= null;
	public Flag								IDENTIFY_NONGENE_ASSOCIATED		= null;
	public Flag								IDENTIFY_DRAINS_TRANSPORTS		= null;
	public Flag								IDENTIFY_PATHWAY_RELATED		= null;
	public Flag								IDENTIFY_HIGH_CARBON_RELATED	= null;
	
	protected IOptimizationTargetsStrategy	_optimizationTargetsStrategy	= null;
	
	/**
	 * Default constructor
	 * 
	 * @param strategy
	 * @param solver
	 * @param container
	 * @param environmentalConditions
	 * @throws InvalidSteadyStateModelException
	 */
	public OptimizationTargetsControlCenter(OptimizationStrategy strategy,
			SolverType solver,
			Container container,
			ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			Set<String> pathways,
			Set<String> cofactors,
			Integer carbonOffset) throws InvalidSteadyStateModelException {
		
		switch (strategy) {
			case GK:
				_optimizationTargetsStrategy = new GKOptimizationTargetsStrategy(container, model, environmentalConditions, solver, pathways, cofactors, (carbonOffset == null ? DEFAULT_CARBON_OFFSET
						: carbonOffset));
				break;
			case RK:
				_optimizationTargetsStrategy = new RKOptimizationTargetsStrategy(container, model, environmentalConditions, solver, pathways, cofactors, (carbonOffset == null ? DEFAULT_CARBON_OFFSET
						: carbonOffset));
				break;
			default:
				break;
		}
		
		IDENTIFY_CRITICAL = _optimizationTargetsStrategy.getCriticalFlag();
		IDENTIFY_ZEROS = _optimizationTargetsStrategy.getZerosFlag();
		IDENTIFY_EQUIVALENCES = _optimizationTargetsStrategy.getEquivalencesFlag();
		IDENTIFY_NONGENE_ASSOCIATED = _optimizationTargetsStrategy.getNonGeneAssociatedFlag();
		IDENTIFY_DRAINS_TRANSPORTS = _optimizationTargetsStrategy.getDrainsTransportsFlag();
		IDENTIFY_PATHWAY_RELATED = _optimizationTargetsStrategy.getPathwayRelatedFlag();
		IDENTIFY_HIGH_CARBON_RELATED = _optimizationTargetsStrategy.getHighCarbonRelatedFlag();
	}
	
	public void process() throws Exception {
		_optimizationTargetsStrategy.processNonTargets();
	}
	
	public Set<String> getNonTargets() {
		return _optimizationTargetsStrategy.getNonTargets();
	}
	
	public Set<String> getTargets() {
		return _optimizationTargetsStrategy.getTargets();
	}
	
	public void addPathways(String... pathways) {
		_optimizationTargetsStrategy.addPathways(pathways);
	}
	
	public void addCofactorsToIgnore(String... cofactors) {
		_optimizationTargetsStrategy.addCofactorsToIgnore(cofactors);
	}
	
	public static void main(String... args) throws Exception {
		
		String test = "imm"; // imm
		boolean anaerobic = false;
		
		String file = null;
		String envfile = null;
		
		if (test.equals("iaf")) {
			file = "files/iAF1260_full/iAF1260.conf";
			if (anaerobic) envfile = "files/iAF1260_full/iAF1260_anaerobic.env";
		} else if(test.equals("imm")){
			file = "files/iMM904/iMM904.conf";
			if (anaerobic) envfile = "files/iMM904/iMM904_anaerobic.env";
		} else if(test.equals("ijr")){
			file = "files/iJR904_full/iJR904full.conf";			
		}
		
		ModelConfiguration conf = new ModelConfiguration(file);
		
		Container container = conf.getContainer();
		ISteadyStateModel model = conf.getModel();
		
		String biomass = conf.getModelBiomass();
		System.out.println("biomass=" + biomass);
		
		Set<String> cofactors = new HashSet<String>(conf.getModelCofactors());
		
		EnvironmentalConditions env = (envfile != null) ? EnvironmentalConditions.readFromFile(envfile, ",") : null;
		
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(env, null, model, SimulationMethodsEnum.PFBA.getSimulationProperty());
		cc.setSolver(SolverType.CPLEX);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomass, 1.0);
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println("BIOMASS = " + res.getFluxValues().getValue(biomass));
		
		OptimizationStrategy strategy = OptimizationStrategy.RK;
		OptimizationTargetsControlCenter targetsCC = new OptimizationTargetsControlCenter(strategy, SolverType.CPLEX, container, model, env, null, cofactors, null);
		targetsCC.IDENTIFY_CRITICAL.off();
		targetsCC.IDENTIFY_DRAINS_TRANSPORTS.off();
		targetsCC.IDENTIFY_EQUIVALENCES.off();
		targetsCC.IDENTIFY_HIGH_CARBON_RELATED.off();
		targetsCC.IDENTIFY_NONGENE_ASSOCIATED.off();
		targetsCC.IDENTIFY_PATHWAY_RELATED.off();
		targetsCC.IDENTIFY_ZEROS.on();
		targetsCC.process();
		
		Set<String> targets = targetsCC.getTargets();
		Set<String> nonTargets = targetsCC.getNonTargets();
		
		System.out.println();
		System.out.println("Model [" + conf.getModelName() + "]");
		System.out.println("Total reactions [" + container.getReactions().size() + "]");
		System.out.println("Total genes = [" + container.getGenes().size() + "]");
		System.out.println("Total targets = [" + targets.size() + "]");
		System.out.println("Total non targets = [" + nonTargets.size() + "]");
		System.out.println();
		
		FileWriter fwt = new FileWriter("files/zerofluxesTest/" + conf.getModelName() + "_"+strategy.name() + (anaerobic ? "_anaerobic" : "_aerobic") + ".targets");
		FileWriter fwi = new FileWriter("files/zerofluxesTest/" + conf.getModelName() + "_"+strategy.name() + (anaerobic ? "_anaerobic" : "_aerobic") + ".nontargets");
		BufferedWriter bwt = new BufferedWriter(fwt);
		BufferedWriter bwi = new BufferedWriter(fwi);
		
		for (String t : targets) {
			bwt.append(t);
			bwt.newLine();
		}
		
		for (String i : nonTargets) {
			bwi.append(i);
			bwi.newLine();
		}
		
		bwt.flush();
		bwt.close();
		bwi.flush();
		bwi.close();
		
	}
	
}
