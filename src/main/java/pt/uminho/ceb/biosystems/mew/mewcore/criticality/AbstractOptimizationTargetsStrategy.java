package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public abstract class AbstractOptimizationTargetsStrategy implements IOptimizationTargetsStrategy {
	
	public Flag								IDENTIFY_CRITICAL				= new Flag(
																					TargetIDStrategy.IDENTIFY_CRITICAL);
	public Flag								IDENTIFY_ZEROS					= new Flag(TargetIDStrategy.IDENTIFY_ZEROS);
	public Flag								IDENTIFY_EQUIVALENCES			= new Flag(
																					TargetIDStrategy.IDENTIFY_EQUIVALENCES);
	public Flag								IDENTIFY_NONGENE_ASSOCIATED		= new Flag(
																					TargetIDStrategy.IDENTIFY_NONGENE_ASSOCIATED);
	public Flag								IDENTIFY_DRAINS_TRANSPORTS		= new Flag(
																					TargetIDStrategy.IDENTIFY_DRAINS_TRANSPORTS);
	public Flag								IDENTIFY_PATHWAY_RELATED		= new Flag(
																					TargetIDStrategy.IDENTIFY_PATHWAY_RELATED);
	public Flag								IDENTIFY_HIGH_CARBON_RELATED	= new Flag(
																					TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED);
	
	protected Integer						_carbonOffset					= null;
	
	protected Container						_container						= null;
	
	protected ISteadyStateModel				_model							= null;
	
	protected EnvironmentalConditions		_environmentalConditions		= null;
	
	protected SolverType					_solver							= null;
	
	protected Set<String>					_ignoredCofactors				= null;
	
	protected Set<String>					_ignoredPathways				= null;
	
	protected final Map<Flag, Set<String>>	_flags							= new IndexedHashMap<Flag, Set<String>>();
	
	public AbstractOptimizationTargetsStrategy(
			Container container,
			ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			SolverType solver,
			Set<String> ignoredPathways,
			Set<String> ignoredCofactors,
			Integer carbonOffset) {
		
		_carbonOffset = carbonOffset;
		_container = container;
		_model = model;
		_environmentalConditions = environmentalConditions;
		_solver = solver;
		_ignoredCofactors = ignoredCofactors;
		_ignoredPathways = ignoredPathways;
		loadFlags();
	}
	
	private void loadFlags() {
		_flags.put(IDENTIFY_CRITICAL, null);
		_flags.put(IDENTIFY_ZEROS, null);
		_flags.put(IDENTIFY_EQUIVALENCES, null);
		_flags.put(IDENTIFY_NONGENE_ASSOCIATED, null);
		_flags.put(IDENTIFY_DRAINS_TRANSPORTS, null);
		_flags.put(IDENTIFY_PATHWAY_RELATED, null);
		_flags.put(IDENTIFY_HIGH_CARBON_RELATED, null);
	}
	
	public Set<String> findHighCarbonMetabolites() {
		BalanceValidator balance = new BalanceValidator(_container);
		balance.setFormulasFromContainer();
		Map<String, MetaboliteCI> metabolites = _container.getMetabolites();
		Set<String> metabolitesNoFormula = balance.getMetabolitesWithoutFormulas();
		
		Set<String> metabolitesToAnalyze = CollectionUtils.getSetDiferenceValues(
				metabolites.keySet(),
				metabolitesNoFormula);
		Set<String> highCarbonMetabolites = new HashSet<String>();
		
		if (_ignoredCofactors != null && !_ignoredCofactors.isEmpty())
			metabolitesToAnalyze = CollectionUtils.getSetDiferenceValues(metabolitesToAnalyze, _ignoredCofactors);
		
		for (String met : metabolitesToAnalyze) {
			MetaboliteFormula formula = balance.getformula(met);
			int numCarbons = formula.getValue(OptimizationTargetsControlCenter.CARBON);
			if (numCarbons > _carbonOffset)
				highCarbonMetabolites.add(met);
		}
		return highCarbonMetabolites;
	}
	
	public void addPathways(String... pathways) {
		for (String p : pathways) {
			if (!_container.getPathwaysIDs().contains(p))
				throw new IllegalArgumentException("[" + p
						+ "] is not a valid pathway identifier for the current model.");
			else {
				getIgnoredPathways().add(p);
				IDENTIFY_PATHWAY_RELATED.on();
				;
			}
		}
	}
	
	public void addCofactorsToIgnore(String... cofactors) {
		for (String c : cofactors) {
			if (_container.getMetabolite(c) == null)
				throw new IllegalArgumentException("[" + c
						+ "] is not a valid metabolite (co-factor) identifier for the current model.");
			else {
				getIgnoredCofactors().add(c);
				IDENTIFY_HIGH_CARBON_RELATED.on();
			}
		}
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		_environmentalConditions = environmentalConditions;
		IDENTIFY_CRITICAL.on();
	}
	
	public void setCarbonOffset(int carbonOffset) {
		if (carbonOffset > 0) {
			_carbonOffset = carbonOffset;
			IDENTIFY_HIGH_CARBON_RELATED.on();
		} else
			throw new IllegalArgumentException("Carbon offset must be a value higher than 0");
	}
	
	/**
	 * @return the iDENTIFY_CRITICAL
	 */
	public Flag getCriticalFlag() {
		return IDENTIFY_CRITICAL;
	}
	
	/**
	 * @return the iDENTIFY_ZEROS
	 */
	public Flag getZerosFlag() {
		return IDENTIFY_ZEROS;
	}
	
	/**
	 * @return the iDENTIFY_EQUIVALENCES
	 */
	public Flag getEquivalencesFlag() {
		return IDENTIFY_EQUIVALENCES;
	}
	
	/**
	 * @return the iDENTIFY_NONGENE_ASSOCIATED
	 */
	public Flag getNonGeneAssociatedFlag() {
		return IDENTIFY_NONGENE_ASSOCIATED;
	}
	
	/**
	 * @return the iDENTIFY_DRAINS_TRANSPORTS
	 */
	public Flag getDrainsTransportsFlag() {
		return IDENTIFY_DRAINS_TRANSPORTS;
	}
	
	/**
	 * @return the iDENTIFY_PATHWAY_RELATED
	 */
	public Flag getPathwayRelatedFlag() {
		return IDENTIFY_PATHWAY_RELATED;
	}
	
	/**
	 * @return the iDENTIFY_HIGH_CARBON_RELATED
	 */
	public Flag getHighCarbonRelatedFlag() {
		return IDENTIFY_HIGH_CARBON_RELATED;
	}
	
	public Set<String> getIgnoredPathways() {
		if (_ignoredPathways == null)
			_ignoredPathways = new HashSet<String>();
		
		return _ignoredPathways;
	}
	
	public Set<String> getIgnoredCofactors() {
		if (_ignoredCofactors == null)
			_ignoredCofactors = new HashSet<String>();
		
		return _ignoredCofactors;
	}
	
}
