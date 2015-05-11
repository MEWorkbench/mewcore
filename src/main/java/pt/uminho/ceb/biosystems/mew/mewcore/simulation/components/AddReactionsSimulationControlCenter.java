package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;


import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class AddReactionsSimulationControlCenter extends SimulationSteadyStateControlCenter{
	private static final long serialVersionUID = 1L;

	// Necessary information to build the result simulation
	protected Container dbReactions;
	protected Container origContainer;
	protected Set<String> addReactions;
	
	// constructors
	public AddReactionsSimulationControlCenter(
			EnvironmentalConditions envConditions,
			GeneticConditions geneticConditions, 
			ISteadyStateModel model,
			String methodType,
			Container dbReactions,
			Container origContainer,
			Set<String> addReactions)
			throws Exception {
		super(envConditions, geneticConditions, model, methodType);
		this.dbReactions = dbReactions;
		this.origContainer = origContainer;
		this.addReactions = addReactions;
	}

	public AddReactionsSimulationResult simulate() throws Exception {
		SteadyStateSimulationResult retSS = super.simulate();
//		System.out.println("SGC : Result of simulation  "+ retSS);
		
		if (retSS == null)
			return null;
		AddReactionsSimulationResult ret = new AddReactionsSimulationResult(
				retSS, addReactions, dbReactions);
		return ret;
	}
}
	