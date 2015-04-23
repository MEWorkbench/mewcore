/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.components;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

// Representation of simulation result. Append list of new reactions and database.
public class AddReactionsSimulationResult extends SteadyStateSimulationResult {

	private static final long serialVersionUID = 1L;

	protected Container databaseReactions;
	protected Set<String> addReactions;

	public AddReactionsSimulationResult(SteadyStateSimulationResult result,
			Set<String> addReactionList, Container database) throws Exception {

		super(result.getModel(), result.getEnvironmentalConditions(), result
				.getGeneticConditions(), result.getMethod(), result
				.getFluxValues(), result.getSolverOutput(),
				result.getOFvalue(), result.getOFString(), result
						.getSolutionType());
		this.addReactions =  addReactionList;		
		this.databaseReactions = database;

	}

	public AddReactionsSimulationResult(ISteadyStateModel model,
			EnvironmentalConditions environmentalConditions,
			GeneticConditions geneticConditions, String method,
			FluxValueMap fluxValues, String solverOutput, double oFvalue,
			String ofString, LPSolutionType solutionType, Container database,Set<String> addReactionList ) {
		super(model, environmentalConditions, geneticConditions, method,
				fluxValues, solverOutput, oFvalue, ofString, solutionType);
		this.databaseReactions = database;
		this.addReactions =  addReactionList;	

	}

	public Set<String> getAddReactiontList() {
		return addReactions;
	}

	public void setAddReactionList(Set<String> addReactionList) {
		this.addReactions = addReactionList;
	}

	public Container getDBReactions() {
		return databaseReactions;
	}
}
