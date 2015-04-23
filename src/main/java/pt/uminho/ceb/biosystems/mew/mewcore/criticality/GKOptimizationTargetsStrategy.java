package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationStrategy;

public class GKOptimizationTargetsStrategy extends AbstractOptimizationTargetsStrategy {
	
	public static final OptimizationStrategy	OPTIMIZATION_STRATEGY			= OptimizationStrategy.GK;
	
	private RKOptimizationTargetsStrategy		_rkOptimizationTargetsStrategy	= null;
	
	public GKOptimizationTargetsStrategy(
			Container container,
			ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			SolverType solver,
			Set<String> ignoredPathways,
			Set<String> ignoredCofactors,
			Integer carbonOffset) {
		
		super(container, model, environmentalConditions, solver, ignoredPathways, ignoredCofactors, carbonOffset);
		_rkOptimizationTargetsStrategy = new RKOptimizationTargetsStrategy(container, model, environmentalConditions,
				solver, ignoredPathways, ignoredCofactors, carbonOffset);
	}
	
	@Override
	public Set<String> getNonTargets() {
		Set<String> totalGenes = _container.getGenes().keySet();
		Set<String> targetGenes = getTargets();
		return CollectionUtils.getSetDiferenceValues(totalGenes, targetGenes);
	}
	
	@Override
	public Set<String> getTargets() {
		try {			
			_rkOptimizationTargetsStrategy.processNonTargets();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<String> targetReactions = _rkOptimizationTargetsStrategy.getTargets();
		Set<String> targetGenes = new HashSet<String>();
		for (String r : targetReactions)
			targetGenes.addAll(_container.getReactions().get(r).getGenesIDs());
		Set<String> criticalGenes = _flags.get(IDENTIFY_CRITICAL);
		if(criticalGenes==null)
			criticalGenes = new HashSet<String>();
		
		return CollectionUtils.getSetDiferenceValues(targetGenes, criticalGenes);
	}
	
	public Map<Flag, Set<String>> processNonTargets() throws Exception {
		
		ArrayList<Flag> flags = new ArrayList<Flag>();
		flags.addAll(_flags.keySet());
		
		for (Flag flag : flags) {
			
			if (flag.isOn()) {
				Set<String> tempIds = new HashSet<String>();
				switch (flag._strategy) {
					case IDENTIFY_CRITICAL:
						tempIds = identifyCritical();
						break;
					case IDENTIFY_ZEROS:
						break;
					case IDENTIFY_EQUIVALENCES:
						break;
					case IDENTIFY_DRAINS_TRANSPORTS:
						break;
					case IDENTIFY_NONGENE_ASSOCIATED:
						break;
					case IDENTIFY_PATHWAY_RELATED:
						break;
					case IDENTIFY_HIGH_CARBON_RELATED:
						break;
					default:
						break;
				}
				flag.off();
				System.out.println("GK flag = "+flag._strategy+" / "+tempIds.size());
				_flags.put(flag, tempIds);
			}
		}
		return _flags;
	}
	
	@Override
	public Set<String> identifyCritical() throws Exception {
		Set<String> toignore = new HashSet<String>();
		CriticalGenes critical = new CriticalGenes(_model, _environmentalConditions, _solver);
		critical.identifyCriticalGenes();
		toignore.addAll(critical.getCriticalGenesIds());
		return toignore;
	}
	
	@Override
	public Set<String> identifyZeros() {
		return null;
	}
	
	@Override
	public Set<String> identifyEquivalences() {
		return null;
	}
	
	@Override
	public Set<String> identifyNonGeneAssociated() {
		return null;
	}
	
	@Override
	public Set<String> identifyDrainsTransports() {
		return null;
	}
	
	@Override
	public Set<String> identifyPathwayRelated() {
		return null;
	}
	
	@Override
	public Set<String> identifyHighCarbonRelated() {
		return null;
	}
	
	@Override
	public OptimizationStrategy getOptimizationStrategy() {
		return OPTIMIZATION_STRATEGY;
	}
	
	public void addPathways(String... pathways) {
		_rkOptimizationTargetsStrategy.addPathways(pathways);
	}
	
	public void addCofactorsToIgnore(String... cofactors) {
		_rkOptimizationTargetsStrategy.addCofactorsToIgnore(cofactors);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		_environmentalConditions = environmentalConditions;
		IDENTIFY_CRITICAL.on();
		_rkOptimizationTargetsStrategy.setEnvironmentalConditions(environmentalConditions);
	}
	
	public void setCarbonOffset(int carbonOffset) {		
		_rkOptimizationTargetsStrategy.setCarbonOffset(carbonOffset);		
	}
	
}
