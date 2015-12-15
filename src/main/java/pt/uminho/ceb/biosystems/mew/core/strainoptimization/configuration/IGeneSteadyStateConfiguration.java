package pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;

public interface IGeneSteadyStateConfiguration {
	
	public ISteadyStateGeneReactionModel getGeneReactionSteadyStateModel() throws Exception;
	
}
