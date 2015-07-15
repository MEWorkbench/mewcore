package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.criticality.experimental.IExperimentalGeneEssentiality;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.optimization.components.OptimizationStrategy;

public interface IOptimizationTargetsStrategy {
	
	Set<String> getNonTargets();
	
	Set<String> getTargets();
	
	void processNonTargets() throws Exception;
	
	Set<String> identifyCritical(Set<String> ignore) throws Exception;
	
	Set<String> identifyZeros();
	
	Set<String> identifyEquivalences(Set<String> ignore);
	
	Set<String> identifyNonGeneAssociated(Set<String> ignore);
	
	Set<String> identifyDrainsTransports();
	
	Set<String> identifyPathwayRelated();
	
	Set<String> identifyHighCarbonRelated(Set<String> ignore);
	
	Set<String> identifyNoFluxWT(Set<String> ignore) throws Exception;
	
	Set<String> identifyExperimental(Set<String> ignore) throws Exception;
	
	OptimizationStrategy getOptimizationStrategy();
	
	Set<String> findHighCarbonMetabolites();
	
	void addExperimentalValidations(IExperimentalGeneEssentiality... experimental);
	
	void addPathways(String... pathways);
	
	void addCofactorsToIgnore(String... cofactors);
	
	void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions);
	
	void setCarbonOffset(int carbonOffset);
	
	Map<TargetIDStrategy,Flag> getFlags();
	
	Flag getCriticalFlag();
	
	Flag getZerosFlag();
	
	Flag getEquivalencesFlag();
	
	Flag getNonGeneAssociatedFlag();
	
	Flag getDrainsTransportsFlag();
	
	Flag getPathwayRelatedFlag();
	
	Flag getHighCarbonRelatedFlag();
	
	Flag getNoFluxWTFlag();
	
	Flag getExperimentalFlag();
	
	void saveTargetsToFile(String file) throws IOException;
	
	void saveNonTargetsToFile(String file) throws IOException;
	
	void saveNonTargetsPerStrategyToFile(String file,boolean includeConfiguration) throws IOException;

	void setOnlyDrains(boolean onlyDrains);
	
	boolean isOnlyDrains();
}
