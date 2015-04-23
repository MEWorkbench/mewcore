package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationStrategy;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.EquivalentFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.FVAZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.StructuralAnalysisFunctions;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.ZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.fva.FBAFluxVariabilityAnalysis;

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
		for (Flag opttarget : _flags.keySet()) {
			Set<String> nt = _flags.get(opttarget);
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
						tempIds = identifyZerosByFVA();
						break;
					case IDENTIFY_EQUIVALENCES:
						tempIds = identifyEquivalences();
						break;
					case IDENTIFY_DRAINS_TRANSPORTS:
						tempIds = identifyDrainsTransports();
						break;
					case IDENTIFY_NONGENE_ASSOCIATED:
						tempIds = identifyNonGeneAssociated();
						break;
					case IDENTIFY_PATHWAY_RELATED:
						tempIds = identifyPathwayRelated();
						break;
					case IDENTIFY_HIGH_CARBON_RELATED:
						tempIds = identifyHighCarbonRelated();
						break;
					default:
						break;
				}
				flag.off();
				System.out.println("RK flag = "+flag._strategy+" / "+tempIds.size());
				_flags.put(flag, tempIds);
			}
		}
		return _flags;
	}
	
	@Override
	public Set<String> identifyCritical() throws Exception {
		Set<String> toignore = new HashSet<String>();
		CriticalReactions critical = new CriticalReactions(_model, _environmentalConditions, _solver);
		critical.identifyCriticalReactions();
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
	
	public Set<String> identifyZerosByFVA() throws Exception {
		Set<String> zerosRet = new HashSet<String>();
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(_model, _environmentalConditions, null, _solver);
		FVAZeroValueFluxes zeros = fva.identifyFVAZeroFluxes();
		zerosRet.addAll(zeros.getZeroValueFluxes());
		
		return zerosRet;
	}
	
	@Override
	public Set<String> identifyEquivalences() {
		EquivalentFluxes equivalences = StructuralAnalysisFunctions.identifyEquivalentFluxes(_model);
		List<Set<String>> listEquiv = equivalences.getEquivalenceLists();
		Set<String> toIgnore = new HashSet<String>();
		
		for (int i = 0; i < listEquiv.size(); i++) {
			Set<String> set = listEquiv.get(i);
			Iterator<String> it = set.iterator();
			it.next(); // skip first
			while (it.hasNext()) {
				toIgnore.add(it.next());
			}
		}
		
		return toIgnore;
	}
	
	@Override
	public Set<String> identifyNonGeneAssociated() {
		Map<String, ReactionCI> reactionMap = _container.getReactions();
		Set<String> toIgnore = new HashSet<String>();
		for (String id : reactionMap.keySet())
			if (reactionMap.get(id).getGenesIDs().isEmpty())
				toIgnore.add(id);
		
		return toIgnore;
	}
	
	@Override
	public Set<String> identifyDrainsTransports() {
		Set<String> toIgnore = new HashSet<String>();
		toIgnore.addAll(_container.getDrains());
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
	public Set<String> identifyHighCarbonRelated() {
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
