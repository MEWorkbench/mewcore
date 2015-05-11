package pt.uminho.ceb.biosystems.mew.mewcore.criticality.experimental;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public interface IExperimentalGeneEssentiality {
	
	public boolean isEssential(String identifier);
	
	public Set<String> getEssentials();
	
	public Set<String> getEssentialGenesFromModel(ISteadyStateModel model) throws Exception;
	
	public Set<String> getEssentialReactionsFromModel(ISteadyStateModel model) throws Exception;
	
}
