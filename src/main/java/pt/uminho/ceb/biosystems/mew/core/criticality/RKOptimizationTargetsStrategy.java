package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.core.criticality.experimental.IExperimentalGeneEssentiality;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.OptimizationStrategy;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.EquivalentFluxes;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.FVAZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.StructuralAnalysisFunctions;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.ZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.fva.FBAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.java.TimeUtils;

public class RKOptimizationTargetsStrategy extends AbstractOptimizationTargetsStrategy {
	
	public static final OptimizationStrategy	OPTIMIZATION_STRATEGY	= OptimizationStrategy.RK;
	
	public RKOptimizationTargetsStrategy(
			Container container,
			ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			SolverType solver,
			Set<String> ignoredPathways,
			Set<String> ignoredCofactors,
			Integer carbonOffset) {
		super(container, model, environmentalConditions, solver, ignoredPathways, ignoredCofactors, carbonOffset);
	}
	
	@Override
	public Set<String> getNonTargets() {
		TreeSet<String> nonTargets = new TreeSet<String>();
		for (TargetIDStrategy opttarget : _flags.keySet()) {
			Set<String> nt = _flags_data.get(opttarget);
			if (nt != null && !nt.isEmpty())
				nonTargets.addAll(nt);
		}
		
		return nonTargets;
	}
	
	@Override
	public Set<String> getTargets() {
		Set<String> totalReactions = _container.getReactions().keySet();
		Set<String> nonTargets = getNonTargets();
		Set<String> targetReactions = CollectionUtils.getSetDiferenceValues(totalReactions, nonTargets);
		return targetReactions;
	}
	
	@Override
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
						tempIds = identifyZerosByFVA(nonTargetsSoFar);
						break;
					case IDENTIFY_EQUIVALENCES:
						tempIds = identifyEquivalences(nonTargetsSoFar);
						break;
					case IDENTIFY_DRAINS_TRANSPORTS:
						tempIds = identifyDrainsTransports();
						break;
					case IDENTIFY_NONGENE_ASSOCIATED:
						tempIds = identifyNonGeneAssociated(nonTargetsSoFar);
						break;
					case IDENTIFY_PATHWAY_RELATED:
						tempIds = identifyPathwayRelated();
						break;
					case IDENTIFY_HIGH_CARBON_RELATED:
						tempIds = identifyHighCarbonRelated(nonTargetsSoFar);
						break;
					case IDENTIFY_NO_FLUX_WT:
						tempIds = identifyNoFluxWT(nonTargetsSoFar);
						break;
					case IDENTIFY_EXPERIMENTAL:
						tempIds = identifyExperimental(nonTargetsSoFar);
						break;
					default:
						break;
				}
//				_flags.get(f).off();
				System.out.println("RK flag = "+_flags.get(f).get_strategy()+" / "+tempIds.size()+" ("+TimeUtils.formatMillis(System.currentTimeMillis() - inittime)+")");
				_flags_data.put(f, tempIds);
				nonTargetsSoFar.addAll(getNonTargets());
			}
		}
	}
	
	public Set<String> identifyExperimental(Set<String> ignoredReactions) throws Exception{
		Set<String> toIgnore = new HashSet<String>();
		for(IExperimentalGeneEssentiality exp: _experimental){
			Set<String> essential = exp.getEssentialReactionsFromModel(_model);
			toIgnore.addAll(essential);
		}
		return toIgnore;
	}
	
	public Set<String> identifyNoFluxWT(Set<String> ignoredReactions) throws Exception{
		Set<String> toignore = new HashSet<String>();
		FluxValueMap wtMap = SimulationProperties.simulateWT(_model, _environmentalConditions, _solver);
		for(String s: wtMap.keySet()){
			if(wtMap.get(s)==0.0)
				toignore.add(s);
		}
		return toignore;
	}
	
	@Override
	public Set<String> identifyCritical(Set<String> ignoreReactions) throws Exception {
		Set<String> toignore = new HashSet<String>();
		pt.uminho.ceb.biosystems.mew.core.criticality.CriticalReactions critical = new pt.uminho.ceb.biosystems.mew.core.criticality.CriticalReactions(_model, _environmentalConditions, _solver);
		critical.identifyCriticalReactionIgnoring(ignoreReactions);
		toignore.addAll(critical.getCriticalReactionIds());
		return toignore;
	}
	
	@Override
	public Set<String> identifyZeros() {
		Set<String> toret = new HashSet<String>();
		ZeroValueFluxes zeros = StructuralAnalysisFunctions.identifyZeroValuesFromStoichiometry(_model);
		toret.addAll(zeros.getZeroValueFluxes());
		return toret;
	}
	
	public Set<String> identifyZerosByFVA(Set<String> ignoreReactions) throws Exception {
		Set<String> zerosRet = new HashSet<String>();
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(_model, _environmentalConditions, null, _solver);
		FVAZeroValueFluxes zeros = fva.identifyFVAZeroFluxesIgnore(ignoreReactions);
		zerosRet.addAll(zeros.getZeroValueFluxes());
		
		return zerosRet;
	}
	
	@Override
	public Set<String> identifyEquivalences(Set<String> ignoreReactions) {
		EquivalentFluxes equivalences = StructuralAnalysisFunctions.identifyEquivalentFluxes(_model);
		List<Set<String>> listEquiv = equivalences.getEquivalenceLists();
		Set<String> toIgnore = new HashSet<String>();
		
		for (int i = 0; i < listEquiv.size(); i++) {
			Set<String> set = listEquiv.get(i);
			
			boolean skipedOne = false;
			for(String eq: set){
				if(!ignoreReactions.contains(eq)){
					if(skipedOne)
						toIgnore.add(eq);
					else
						skipedOne = true;
				}
			}
		}
		
		return toIgnore;
	}
	
	@Override
	public Set<String> identifyNonGeneAssociated(Set<String> ignoreReactions) {
		Map<String, ReactionCI> reactionMap = _container.getReactions();
		Set<String> toIgnore = new HashSet<String>();
		for (String id : reactionMap.keySet())
			if (!ignoreReactions.contains(id) && reactionMap.get(id).getGenesIDs().isEmpty())
				toIgnore.add(id);
		
		return toIgnore;
	}
	
	@Override
	public Set<String> identifyDrainsTransports() {
		Set<String> toIgnore = new HashSet<String>();
		toIgnore.addAll(_container.getDrains());
		if(!_onlyDrains)
			toIgnore.addAll(_container.identifyTransportReactions());
		
		return toIgnore;
	}
	
	
	@Override
	public Set<String> identifyPathwayRelated() {
		Set<String> toignore = new HashSet<String>();
		
		if (_ignoredPathways != null && !_ignoredPathways.isEmpty()) {
			for (String pathway : _ignoredPathways) {
				Map<String, ReactionCI> pathwayReactions = _container.getReactionsByPathway(pathway);
				toignore.addAll(pathwayReactions.keySet());
			}
		}
		
		return toignore;
	}
	
	@Override
	public Set<String> identifyHighCarbonRelated(Set<String> ignoreReactions) {
		Map<String, MetaboliteCI> metabolites = _container.getMetabolites();
		Set<String> toIgnore = new HashSet<String>();
		Set<String> highCarbonMetabolites = findHighCarbonMetabolites();
		
		Set<String> ignore = new HashSet<String>();
		for (String met : highCarbonMetabolites) {
			ignore.addAll(metabolites.get(met).getReactionsId());
		}
		toIgnore.addAll(ignore);
		
		return toIgnore;
	}
	
	@Override
	public OptimizationStrategy getOptimizationStrategy() {
		return OPTIMIZATION_STRATEGY;
	}
}
