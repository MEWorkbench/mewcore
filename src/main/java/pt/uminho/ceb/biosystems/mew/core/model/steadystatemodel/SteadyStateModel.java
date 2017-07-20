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
package pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cern.colt.list.IntArrayList;
import pt.uminho.ceb.biosystems.mew.core.model.components.AbstractModel;
import pt.uminho.ceb.biosystems.mew.core.model.components.Compartment;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.Metabolite;
import pt.uminho.ceb.biosystems.mew.core.model.components.Pathway;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ModelType;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class SteadyStateModel extends AbstractModel implements ISteadyStateModel,Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected IStoichiometricMatrix stoichiometricMatrix;
	
	protected IndexedHashMap<String,Reaction> reactionMap;
	//		String MATRIX_FILE = "files/metatool_model.matrix";
//		String METABOLITE_FILE ="files/metatool_model.metab";
//		String FLUX_FILE="files/metatool_model.fluxes";
	protected IndexedHashMap<String,Metabolite> metaboliteMap;
	
    protected Map<String, Compartment> compartmentMap;
    
    protected IndexedHashMap<String, Pathway> pathwayMap;
    
    
    //Model Information
    protected String biomassFlux = ""; //default
    
    protected List<String> uptakeReactions;
    
    protected Map<Integer, Integer> drainsToMetabolite;
    
    
	public SteadyStateModel(String modelId,
			IStoichiometricMatrix stoichiometricMatrix,
			IndexedHashMap<String,Reaction> reactions,
			IndexedHashMap<String,Metabolite> metabolites,
			Map<String,Compartment> compartments,
			IndexedHashMap<String,Pathway> pathways)
			throws InvalidSteadyStateModelException {
		
		super(modelId);
		
		this.stoichiometricMatrix = stoichiometricMatrix;
		this.reactionMap = reactions;
		this.metaboliteMap = metabolites;
		this.compartmentMap = compartments;
		this.pathwayMap = pathways;
		
		drainsToMetabolite = identifyDrainLinkedToMetabolite();
	}



	public String getMetaboliteId(int metaboliteIndex) {
		return metaboliteMap.getKeyAt(metaboliteIndex);
	}

    public Integer getMetaboliteIndex(String metaboliteId) {
        return metaboliteMap.getIndexOf(metaboliteId);  
    }
    
    public IndexedHashMap<String, Metabolite> getMetabolites(){
    	return metaboliteMap;
    }

    public Integer getNumberOfMetabolites() {
		return metaboliteMap.size();
	}

	public Integer getNumberOfReactions() {
		return reactionMap.size();
	}

	public String getReactionId(int reactionIndex) {
		return reactionMap.getKeyAt(reactionIndex);
	}
	
	public IndexedHashMap<String, Reaction> getReactions(){
		return reactionMap;
	}

	public IStoichiometricMatrix getStoichiometricMatrix()
	{
		return stoichiometricMatrix.copy(); 
	}

	public Double getStoichiometricValue(int metaboliteIndex, int reactionIndex) {
		return stoichiometricMatrix.getValue(metaboliteIndex, reactionIndex);
	}
	
	public IndexedHashMap<String, Pathway> getPathways() {
		return pathwayMap;
	}
	
	public void setPathways(IndexedHashMap<String, Pathway> pathwayMap) {
		this.pathwayMap = pathwayMap;
	}

	public void setStoichiometricValue(int metaboliteIndex,int reactionIndex, double value)
	{
		stoichiometricMatrix.setValue(metaboliteIndex, reactionIndex, value);
	}
	
	public ReactionConstraint getReactionConstraint(int reactionIndex) {
		return reactionMap.getValueAt(reactionIndex).getConstraints();
	}
	
	public ReactionConstraint getReactionConstraint(String reactionId) {
		
		return reactionMap.get(reactionId).getConstraints();
	}

    public Integer getReactionIndex(String reactionId){
    	return reactionMap.getIndexOf(reactionId);
    }


	public void setBiomassFlux(String biomassFlux) {
		this.biomassFlux = biomassFlux;
	}
	
	public Integer getVectorIdIndex(String id,List<String> idVector)
			throws NonExistentIdException {
		int numberOfElements = idVector.size();

		for (int i = 0; i < numberOfElements; i++) {
			String currentId = idVector.get(i);
			if (currentId.equals(id))
				return i;
		}

		throw new NonExistentIdException();
	}

    public Compartment getCompartment(String compartmentId){
        return compartmentMap.get(compartmentId);
    }
    
    public Map<String, Compartment> getCompartments(){
    	return this.compartmentMap;
    }

	public String getBiomassFlux() {
		return biomassFlux;
	}

	public List<String> getUptakeReactions() {
		if (uptakeReactions == null){
			uptakeReactions = identifyUptakeReactions();
		}
			
		return uptakeReactions;
	}
	
	public void setUptakeReactions(ArrayList<String> uptakes) {
		this.uptakeReactions = uptakes;
	}

	/** This method returns a reaction*/
	public Reaction getReaction(String reactionId) {
		return reactionMap.get(reactionId);
	
	}

	@Override
	public Metabolite getMetabolite(int metaboliteIndex){
		return metaboliteMap.getValueAt(metaboliteIndex);
	}


	public Integer countDrains(){
		Integer count=0;
		for(Reaction reaction : reactionMap.values())
			if(reaction.getType().equals(ReactionType.DRAIN))
				count++;
		return count;
	}
	
	public Integer countTransportReactions(){	
		Integer count=0;
		for(Reaction reaction : reactionMap.values())
			if(reaction.getType().equals(ReactionType.TRANSPORT))
				count++;
		return count;
	}
	
	public Integer countInternalReactions(){
		Integer count=0;
		for(Reaction reaction : reactionMap.values())
			if(reaction.getType().equals(ReactionType.INTERNAL))
				count++;
		return count;
	}
	
	public Integer countExternalMetabolites(){
		Integer count=0;
		for(Metabolite metabolite: metaboliteMap.values())
			if(metabolite.isExternal())
				count++;
		return count;
	}
	
	public Integer countInternalMetabolites(){
		Integer count=0;
		for(Metabolite metabolite: metaboliteMap.values())
			if(!metabolite.isExternal())
				count++;
		return count;
	}
	
	public void identifyExternalMetabolitesbyName (String regExpression)
	{
		Compartment external = null;
		// get external compartment; assuming only one exists
		for(Compartment compartment: compartmentMap.values())
			if(compartment.isExternal()) external = compartment;
		
		if (external == null) // create it
			external = new Compartment("extra-celular",true);
		
		for(int i=0; i< this.metaboliteMap.size(); i++)
		{
			Metabolite m = this.metaboliteMap.getValueAt(i);
			if (m.getId().matches(regExpression)) 
				m.setCompartment(external);
		}
	}
		

	public Map<Integer, Integer> identifyDrainLinkedToMetabolite()
	{
		Map<Integer, Integer> detectedDrains = new HashMap<Integer, Integer>();
		
 		for(int i= 0; i < this.getNumberOfReactions(); i++)
		{
			int numberCoefsNotZero = 0;
			int metabolite = -1;
			for(int j=0; j < this.getNumberOfMetabolites() && numberCoefsNotZero<2 ; j++)
				if (this.getStoichiometricValue(j,i)!=0){
					metabolite = j;
					numberCoefsNotZero++;
				}
			if (numberCoefsNotZero == 1){ 
				detectedDrains.put(i, metabolite);
				getMetabolite(metabolite).setBoundaryCondition(true);
			}
		}
		
		return detectedDrains;
	}
	
	
	// identifies all drain reactions: columns with only one coefficient 
	public ArrayList<String> identifyDrainReactionsFromStoichiometry()
	{
		ArrayList<String> detectedDrains = new ArrayList<String>();
		
 		for(int i= 0; i < this.getNumberOfReactions(); i++)
		{
			int numberCoefsNotZero = 0;
			for(int j=0; j < this.getNumberOfMetabolites() && numberCoefsNotZero<2 ; j++)
				if (this.getStoichiometricValue(j,i)!=0) numberCoefsNotZero++;
			if (numberCoefsNotZero == 1) detectedDrains.add(this.getReactionId(i));
		}
		
		return detectedDrains;
	}
	
	public ArrayList<String> identifyUptakeReactions(){
		ArrayList<String> uptakes = new ArrayList<String>();
		
		for(String id: identifyDrainReactionsFromStoichiometry()){
			ReactionConstraint rc = this.getReactionConstraint(id);
			if(rc.getLowerLimit()<0)
				uptakes.add(id);
		}
		return uptakes;
	}
	
	
	public ArrayList<String> identifyTransportReactionsFromStoichiometry(){
		ArrayList<String> transportReactions = new ArrayList<String>();
		for(int i=0; i< this.getNumberOfReactions(); i++){
			
			if(isTransportReaction(i)){
				transportReactions.add(getReactionId(i));
			}
		}
		return transportReactions;
	}
	
	private boolean isTransportReaction(int i) {
		
		return getCompartmentsInReaction(i).size() >1;
	}

	public Set<String> getCompartmentsInReaction(int i){
		Set<String> comp = new TreeSet<String>();
		IntArrayList rowIndx = new IntArrayList();
		getStoichiometricMatrix().convertToColt().viewColumn(i).getNonZeros(rowIndx, null);
		
		for (int j = 0; j < rowIndx.size(); j++) {
			int met = rowIndx.getQuick(j);
			comp.add(getMetabolite(met).getCompartment().getId());
		}
		
		return comp;
	}

//	//NOTE: verify later
//	public ArrayList<String> identifyTransportReactionsFromStoichiometry(){
//		
//		
//		ArrayList<String> transportReactions = new ArrayList<String>();
//
//		for(int i=0;i<this.getNumberOfReactions();i++){
//			ArrayList<Integer> left = new ArrayList<Integer>();
//			ArrayList<Integer> right = new ArrayList<Integer>();
//			for(int j=0;j<this.getNumberOfMetabolites();j++){
//				double val = this.getStoichiometricValue(j, i);
//				if(val<0.0)
//					left.add(j);
//				else if (val>0.0)
//					right.add(j);
//			}
//
//			if(right.size()>0 && left.size()>0){
//
//				boolean validLeft = true, validRight = true;
//				Compartment leftIs, rightIs;
//
//				if(left.size()==1){
//					validLeft= true;
//				} 
//				else if(left.size()==2){
//					if(!(this.getMetabolite(left.get(0)).getCompartment().equals(this.getMetabolite(left.get(1)).getCompartment())))
//						validLeft = false;
//				}
//				else{
//					for(int k =0;k<left.size()-1;k++){
//						if(!(this.getMetabolite(left.get(k)).getCompartment().equals(this.getMetabolite(left.get(k+1)).getCompartment()))){
//							validLeft = false;
//							break;
//						}						
//					}
//				}
//
//				if(validLeft){
//					leftIs = this.getMetabolite(left.get(0)).getCompartment();
//					rightIs = this.getMetabolite(right.get(0)).getCompartment();
//					if(!leftIs.equals(rightIs)){
//						if(right.size()==1){
//							validRight= true;
//						} 
//						else if(right.size()==2){
//							if(!(this.getMetabolite(right.get(0)).getCompartment().equals(this.getMetabolite(right.get(1)).getCompartment())))
//								validRight = false;
//						}
//						else{
//							for(int k=0;k<right.size()-1;k++){
//								if(!(this.getMetabolite(right.get(k)).getCompartment().equals(this.getMetabolite(right.get(k+1)).getCompartment()))){
//									validRight = false;
//									break;
//								}
//							}
//						}
//					} else validRight=false;
//				}
//
//				if(validLeft && validRight)
//				{
//					transportReactions.add(getReactionId(i));
//					this.getReaction(i).setType(ReactionType.TRANSPORT);
//				}
//			}
//		}
//		
//		return transportReactions;
//	}
	
	
	public int getMetaboliteFromDrainIndex (int drainReactionIndex)
	{
		int numberCoefsNotZero = 0;
		int metaboliteIndex = -1;
		
		for(int j=0; j < this.getNumberOfMetabolites() && numberCoefsNotZero<2 ; j++)
			if (this.getStoichiometricValue(j,drainReactionIndex)!=0) {
				numberCoefsNotZero++;
				metaboliteIndex = j;
			}
		if (numberCoefsNotZero != 1) metaboliteIndex = -1;
		
		return metaboliteIndex;
	}
	
	
	public Integer getDrainIndexFromMetabolite(int metaboiteIndex){
		int ret = -1;
		
//		if(metaboliteMap.getValueAt(metaboiteIndex).isExternal()){
			
			for(int i =0; i < this.getNumberOfReactions(); i++){
				if(reactionMap.getValueAt(i).getType().equals(ReactionType.DRAIN) ){
					//System.out.println(reactionMap.getValueAt(i).getId()+"\t"+this.getStoichiometricValue(metaboiteIndex, i));
					if(this.getStoichiometricValue(metaboiteIndex, i) != 0){
						ret = i;
						//System.out.println(reactionMap.getValueAt(i).getId() +" " + metaboliteMap.getValueAt(metaboiteIndex).getId());
						i = getNumberOfReactions();

					}
				}
			}
			
//		}
		return ret;
	}
	
	@Override
	public Reaction getReaction(int reactionIndex) 
	{
		return reactionMap.getValueAt(reactionIndex);
	}




	
	// add a drain reaction allowing a metabolite to be excreted or uptaken
	public SteadyStateModel addDrainReaction (int metaboliteIndex, boolean out, boolean in) throws Exception
	{

		IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Reaction> newReactions = 
			new IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Reaction>();
		
		for(int i=0; i< this.getNumberOfReactions(); i++){				
				Reaction r = this.getReaction(i);
				newReactions.put(r.getId(), r);
		}
		
		String reactionId = "EX_" + this.getMetaboliteId(metaboliteIndex);
		
		boolean rev = false;
		double lb = 0.0, ub = 0.0;
		if (out & in) rev = true;
		if (in) lb = -10000.0;
		if (out) ub = 10000.0;
		
		newReactions.put(reactionId, new Reaction(reactionId, rev,ReactionType.DRAIN, lb, ub));
		
		IStoichiometricMatrix newMatrix = this.stoichiometricMatrix.addColumn(); 

		newMatrix.setValue(metaboliteIndex, this.getNumberOfReactions(), -1);

		SteadyStateModel newModel = null;
		
		if(SteadyStateGeneReactionModel.class.isAssignableFrom(this.getClass()))
		{	
			newModel = new SteadyStateGeneReactionModel(this.id, newMatrix,newReactions,this.metaboliteMap,this.compartmentMap, ((SteadyStateGeneReactionModel)this).getPathways(),
					((SteadyStateGeneReactionModel)this).getGenes(),null,((SteadyStateGeneReactionModel)this).getGeneReactionRules(),null);								
		}
		else
		{
			newModel = 
				new SteadyStateModel(this.id,newMatrix,newReactions,this.metaboliteMap,this.compartmentMap, this.pathwayMap);
		}
		
		return newModel;
	}
	
	
	// removes a set of reactions from the model
	public SteadyStateModel removeReactions (List<Integer> reactionIndexes) throws Exception
	{
		IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Reaction> newReactions = 
			new IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Reaction>();
		
		for(int i=0; i< this.getNumberOfReactions(); i++){
			if(!reactionIndexes.contains(i))
			{
				
				Reaction r = this.getReaction(i);
				newReactions.put(r.getId(), r);
			}
		}
		
		IStoichiometricMatrix newMatrix = 
			this.stoichiometricMatrix.removeColumns(reactionIndexes); 
			
		SteadyStateModel newModel = null;
		
		if(SteadyStateGeneReactionModel.class.isAssignableFrom(this.getClass())){
			
			newModel = new SteadyStateGeneReactionModel(this.id,newMatrix,newReactions,this.metaboliteMap,this.compartmentMap, ((SteadyStateGeneReactionModel)this).getPathways(),
					((SteadyStateGeneReactionModel)this).getGenes(),null,((SteadyStateGeneReactionModel)this).getGeneReactionRules(),null);
					
			
		}else{
			newModel = 
				new SteadyStateModel(this.id,newMatrix,newReactions,this.metaboliteMap,this.compartmentMap, this.pathwayMap);
		}
		return newModel;
	}
	
	public SteadyStateModel removeMetabolites(List<Integer> metabolitesIndexes) throws Exception
	{
		IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Metabolite> newMetabolites = 
			new IndexedHashMap<String,pt.uminho.ceb.biosystems.mew.core.model.components.Metabolite>();
		
		for(int i=0; i< this.getNumberOfMetabolites(); i++)
			if(!metabolitesIndexes.contains(i))
			{
				Metabolite m = this.getMetabolite(i);
				newMetabolites.put(m.getId(), m);
			}
		
		IStoichiometricMatrix newMatrix = this.stoichiometricMatrix.removeRows(metabolitesIndexes); 
		
		SteadyStateModel newModel = null;
		
		if(SteadyStateGeneReactionModel.class.isAssignableFrom(this.getClass())){
			newModel = new SteadyStateGeneReactionModel(this.id,newMatrix,this.reactionMap,newMetabolites,this.compartmentMap, ((SteadyStateGeneReactionModel)this).getPathways(),
					((SteadyStateGeneReactionModel)this).getGenes(),null,((SteadyStateGeneReactionModel)this).getGeneReactionRules(),null);
		}else{
			newModel = new SteadyStateModel(this.id,newMatrix,this.reactionMap,newMetabolites,this.compartmentMap, this.pathwayMap);
		}
		return newModel;
	}



	@Override
	public ModelType getModelType() {
		return ModelType.STEADY_STATE_MODEL;
	}



	@Override
	public void setUptakeReactions(List<String> ids) {
		this.uptakeReactions = ids;
	}

}
