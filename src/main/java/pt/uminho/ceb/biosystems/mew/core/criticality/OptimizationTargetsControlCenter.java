package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.io.IOException;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.criticality.experimental.IExperimentalGeneEssentiality;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.OptimizationStrategy;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

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
	public OptimizationTargetsControlCenter(
			OptimizationStrategy strategy,
			SolverType solver,
			Container container,
			ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			Set<String> pathways,
			Set<String> cofactors,
			Integer carbonOffset) throws InvalidSteadyStateModelException {
		
		switch (strategy) {
			case GK:
				_optimizationTargetsStrategy = new GKOptimizationTargetsStrategy(container, model, environmentalConditions, solver, pathways, cofactors, (carbonOffset == null ? DEFAULT_CARBON_OFFSET : carbonOffset));
				break;
			case RK:
				_optimizationTargetsStrategy = new RKOptimizationTargetsStrategy(container, model, environmentalConditions, solver, pathways, cofactors, (carbonOffset == null ? DEFAULT_CARBON_OFFSET : carbonOffset));
				break;
			default:
				break;
		}
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
	
	public void addExperimentalValidations(IExperimentalGeneEssentiality... experimental) {
		_optimizationTargetsStrategy.addExperimentalValidations(experimental);
	}
	
	public void setOnlyDrains(boolean onlyDrains){
		_optimizationTargetsStrategy.setOnlyDrains(onlyDrains);
	}
	
	public void saveTargetsToFile(String file) throws IOException {
		_optimizationTargetsStrategy.saveTargetsToFile(file);
	};
	
	public void saveNonTargetsToFile(String file) throws IOException {
		_optimizationTargetsStrategy.saveNonTargetsToFile(file);
	};
	
	public void saveNonTargetsPerStrategyToFile(String file, boolean includeConfiguration) throws IOException {
		_optimizationTargetsStrategy.saveNonTargetsPerStrategyToFile(file, includeConfiguration);
	}
	
	public void enable(TargetIDStrategy strategy) throws Exception {
		if (!_optimizationTargetsStrategy.getFlags().containsKey(strategy))
			throw new Exception("Invalid strategy [" + strategy + "]");
		else
			_optimizationTargetsStrategy.getFlags().get(strategy).on();
	}
	
	public void disable(TargetIDStrategy strategy) throws Exception {
		if (!_optimizationTargetsStrategy.getFlags().containsKey(strategy))
			throw new Exception("Invalid strategy [" + strategy + "]");
		else
			_optimizationTargetsStrategy.getFlags().get(strategy).off();
	}
	
	public boolean isEnabled(TargetIDStrategy strategy){
		return _optimizationTargetsStrategy.getFlags().get(strategy).isOn();
	}
	
	public boolean isOnlyDrains(){
		return _optimizationTargetsStrategy.isOnlyDrains();
	}
	
	public void setCarbonOffSet(int carbonOffSet){
		_optimizationTargetsStrategy.setCarbonOffset(carbonOffSet);
	}
	
}