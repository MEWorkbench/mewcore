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
package pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel;

import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Compartment;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.IModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Metabolite;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Pathway;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public interface ISteadyStateModel  extends IModel, IModelInformation {
	
	IStoichiometricMatrix getStoichiometricMatrix();
	
	Double getStoichiometricValue(int metaboliteIndex,int reactionIndex);
	
	void setStoichiometricValue(int metaboliteIndex,int reactionIndex, double value);
	
	ReactionConstraint getReactionConstraint(int reactionIndex);
	
	Integer getNumberOfMetabolites();
	
	String getMetaboliteId(int metaboliteIndex);
	
    Integer getMetaboliteIndex(String metaboliteId);
    
	Integer getNumberOfReactions();
	
	String getReactionId(int reactionIndex);
	
	Integer getReactionIndex(String reactionId);

	//NOTE: added by paulo maia
	Compartment getCompartment(String compartmentID);
	
	//NOTE: added by Paulo Vilaca
	ReactionConstraint getReactionConstraint(String reactionId);

	//NOTE: added by paulo vilaca
	Reaction getReaction(String reactionId);
	
	//NOTE: added by Pedro Tiago
	Metabolite getMetabolite(int metaboliteIndex);
	
	Reaction getReaction(int reactionIndex);
	
	IndexedHashMap<String, Reaction> getReactions();
	
	IndexedHashMap<String,Metabolite> getMetabolites();
	
	IndexedHashMap<String,Pathway> getPathways();
	
	
	//NOTE: added by Paulo Vilaca
	//Nao tenho a certeza se estes metodos devem estar no Interface - Pedro Penso que sim
	
	Integer countDrains();
	Integer countTransportReactions();
	Integer countInternalReactions();
	Integer countExternalMetabolites();
	Integer countInternalMetabolites();
	
	
	//NOTE: added by paulo maia
	List<String> identifyUptakeReactions();
	//NOTE: added by paulo maia
	List<String> identifyDrainReactionsFromStoichiometry();
	
	List<String> identifyTransportReactionsFromStoichiometry();
	
	public int getMetaboliteFromDrainIndex (int drainReactionIndex); 
	
	Integer getDrainIndexFromMetabolite(int metaboiteIndex);
	
	// adds a new reaction to the model representing a drain for a given metabolite
	ISteadyStateModel addDrainReaction (int metaboliteIndex, boolean in, boolean out) throws Exception;
	
	// removes reactions + columns in the stoichiomteric matrix TODO checkar estes metodos
	ISteadyStateModel removeReactions (List<Integer> listReactionsToRemove) throws Exception;
	
	// removes metabolites + rows in the matrix TODO checkar estes metodos
	ISteadyStateModel removeMetabolites (List<Integer> listMetabolitesToRemove) throws Exception;
	
	Map<String, Compartment> getCompartments();


	
}
