package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.criticality.experimental.IExperimentalGeneEssentiality;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.java.TimeUtils;

public class GKOptimizationTargetsStrategy extends AbstractOptimizationTargetsStrategy {
	
	public static final String GK_OPTIMIZATION_STRATEGY = "GK";
	
	private RKOptimizationTargetsStrategy _rkOptimizationTargetsStrategy = null;
	
	public GKOptimizationTargetsStrategy(Container container, ISteadyStateModel model, EnvironmentalConditions environmentalConditions, String solver, Set<String> ignoredPathways, Set<String> ignoredCofactors, Integer carbonOffset) {
		
		super(container, model, environmentalConditions, solver, ignoredPathways, ignoredCofactors, carbonOffset);
		_rkOptimizationTargetsStrategy = new RKOptimizationTargetsStrategy(container, model, environmentalConditions, solver, ignoredPathways, ignoredCofactors, carbonOffset);
	}
	
	@Override
	public Set<String> getNonTargets() {
		return getNonTargets(null);
	}
	
	@Override
	public Set<String> getNonTargets(Set<String> targets){
		Set<String> totalGenes = _container.getGenes().keySet();
		Set<String> targetGenes = targets==null ? getTargets() : targets;
		return CollectionUtils.getSetDiferenceValues(totalGenes, targetGenes);
		
	}
	
	@Override
	public Set<String> getTargets() {
		try {
			processNonTargets();
			_rkOptimizationTargetsStrategy.processNonTargets();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<String> targetReactions = _rkOptimizationTargetsStrategy.getTargets();
		Set<String> targetGenes = new HashSet<String>();
		for (String r : targetReactions)
			targetGenes.addAll(_container.getReactions().get(r).getGenesIDs());
		Set<String> criticalGenes = _flags_data.get(TargetIDStrategy.IDENTIFY_CRITICAL);
		if (criticalGenes == null){
			criticalGenes = new HashSet<String>();
		}
		
		return CollectionUtils.getSetDiferenceValues(targetGenes, criticalGenes);
	}
	
	public void processNonTargets() throws Exception {
		
		TreeSet<String> nonTargetsSoFar = new TreeSet<String>();
		
		for (TargetIDStrategy f : _flags.keySet()) {
			if (_flags.get(f).isOn()) {
				Set<String> tempIds = new HashSet<String>();
				long inittime = System.currentTimeMillis();
				switch (_flags.get(f).get_strategy()) {
					case IDENTIFY_CRITICAL:
						tempIds = identifyCritical(nonTargetsSoFar);
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
					case IDENTIFY_EXPERIMENTAL:
						tempIds = identifyExperimental(nonTargetsSoFar);
					default:
						break;
				}
				
				//				_flags.get(f).off();
				System.out.println("GK flag = " + _flags.get(f).get_strategy() + " / " + tempIds.size() + " (" + TimeUtils.formatMillis(System.currentTimeMillis() - inittime) + ")");
				_flags_data.put(f, tempIds);
				nonTargetsSoFar.addAll(tempIds);
			}
		}
	}
	
	@Override
	public Set<String> identifyCritical(Set<String> ignoreGenes) throws Exception {
		Set<String> toignore = new HashSet<String>();
		CriticalGenes critical = new CriticalGenes(_model, _environmentalConditions, _solver);
		critical.identifyCriticalGenes();
		toignore.addAll(critical.getCriticalGenesIds());
		return toignore;
	}
	
	@Override
	public Set<String> identifyEquivalences(Set<String> ignore) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> identifyNonGeneAssociated(Set<String> ignore) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> identifyDrainsTransports() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> identifyPathwayRelated() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> identifyHighCarbonRelated(Set<String> ignore) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> identifyZeros() {
		return null;
	}
	
	@Override
	public Set<String> identifyNoFluxWT(Set<String> ignore) throws Exception {
		return null;
	}
	
	public Set<String> identifyExperimental(Set<String> ignoredReactions) throws Exception {
		Set<String> toIgnore = new HashSet<String>();
		for (IExperimentalGeneEssentiality exp : _experimental) {
			Set<String> essential = exp.getEssentialGenesFromModel(_model);
			toIgnore.addAll(essential);
		}
		return toIgnore;
	}
	
	@Override
	public String getOptimizationStrategy() {
		return GK_OPTIMIZATION_STRATEGY;
	}
	
	public void addPathways(String... pathways) {
		_rkOptimizationTargetsStrategy.addPathways(pathways);
	}
	
	public void addCofactorsToIgnore(String... cofactors) {
		_rkOptimizationTargetsStrategy.addCofactorsToIgnore(cofactors);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		_environmentalConditions = environmentalConditions;
		_flags.get(TargetIDStrategy.IDENTIFY_CRITICAL).on();
		_flags.get(TargetIDStrategy.IDENTIFY_ZEROS).on();
		_rkOptimizationTargetsStrategy.setEnvironmentalConditions(environmentalConditions);
	}
	
	public void setCarbonOffset(int carbonOffset) {
		_rkOptimizationTargetsStrategy.setCarbonOffset(carbonOffset);
	}
	
	public void setOnlyDrains(boolean onlyDrains){
		super.setOnlyDrains(onlyDrains);
		_rkOptimizationTargetsStrategy.setOnlyDrains(onlyDrains);
	}

	@Override
	public void enable(TargetIDStrategy strategy) {
		_flags.get(strategy).on();
		_rkOptimizationTargetsStrategy.getFlags().get(strategy).on();
	}

	@Override
	public void disable(TargetIDStrategy strategy) {
		_flags.get(strategy).off();
		_rkOptimizationTargetsStrategy.getFlags().get(strategy).off();
	}
	
}
