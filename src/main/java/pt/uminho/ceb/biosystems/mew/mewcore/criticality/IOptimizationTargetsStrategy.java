package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationStrategy;

public interface IOptimizationTargetsStrategy {
	
	Set<String> getNonTargets();
	
	Set<String> getTargets();
	
	Map<Flag, Set<String>> processNonTargets() throws Exception;
	
	Set<String> identifyCritical() throws Exception;
	
	Set<String> identifyZeros();
	
	Set<String> identifyEquivalences();
	
	Set<String> identifyNonGeneAssociated();
	
	Set<String> identifyDrainsTransports();
	
	Set<String> identifyPathwayRelated();
	
	Set<String> identifyHighCarbonRelated();
	
	OptimizationStrategy getOptimizationStrategy();
	
	Set<String> findHighCarbonMetabolites();
	
	void addPathways(String... pathways);
	
	void addCofactorsToIgnore(String... cofactors);
	
	void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions);
	
	void setCarbonOffset(int carbonOffset);
	
	Flag getCriticalFlag();
	
	Flag getZerosFlag();
	
	Flag getEquivalencesFlag();
	
	Flag getNonGeneAssociatedFlag();
	
	Flag getDrainsTransportsFlag();
	
	Flag getPathwayRelatedFlag();
	
	Flag getHighCarbonRelatedFlag();
}
