/**
 * SilicoLife
 * 
 * Copyright 2010
 * 
 * <p>This is PROPRIETARY software.</p>
 * <p>You can not copy, distribute, modify or for this matter,</p> 
 * <p>proceed into any other type of unauthorized form of use for this code</p>
 * 
 * 
 * www.silicolife.com @see <a href="http://www.silicolife.com">SilicoLife</a>
 * 
 * (c) All rights reserved 
 */
package pt.uminho.ceb.biosystems.mew.core.model.converters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.core.model.components.ColtSparseStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.Compartment;
import pt.uminho.ceb.biosystems.mew.core.model.components.Gene;
import pt.uminho.ceb.biosystems.mew.core.model.components.GeneReactionRule;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.Metabolite;
import pt.uminho.ceb.biosystems.mew.core.model.components.Pathway;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.SteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;


public class ContainerConverter {

	public static final double DEFAULT_BOUND_VALUE = 10000.0; 
	
	protected Container container;
	
	//******************* Model Structures ***************** 
	protected IndexedHashMap<String, Metabolite> metabolites;
	protected IndexedHashMap<String, Reaction> reactions;
	protected IStoichiometricMatrix smatrix;
	protected Map<String,Compartment> compartments;
	protected IndexedHashMap<String, Pathway> pathways;
	
	protected IndexedHashMap<String,Gene> genes;
	protected IndexedHashMap<String,GeneReactionRule> geneReactionRules;
	
	protected boolean needsToAddCompToMet;
	
	//*******************************************************
	
//	protected ArrayList<String> externalMetabolites;
	
	public ContainerConverter(Container container){
		this.container = container;
//		needsToAddCompToMet = !container.identifyIfHasUniqueMetaboliteIds();
	}
	
	public boolean hasGPR(){
		return(container.getGenes()!=null && container.getGenes().size()>0);
	}
	
	public ISteadyStateModel convert() throws InvalidSteadyStateModelException{
		ISteadyStateModel model;
		if(hasGPR()){
			model = convertToGeneReactionModel();
		}
		else
			model = convertToSteadyStateModel();
		
		return model;
	}
	
	public ISteadyStateModel convertToSteadyStateModel() throws InvalidSteadyStateModelException{
		
		getSSModelInfoFromContainer();
		String modelName = container.getModelName();
		if(container.getVersion() != null)
			modelName += ".v" + container.getVersion();
		SteadyStateModel model = new SteadyStateModel(
				modelName, 
				smatrix, 
				reactions, 
				metabolites, 
				compartments,
				pathways);
		
		model.setBiomassFlux(container.getBiomassId());
		return model;
	}
	
	public ISteadyStateGeneReactionModel convertToGeneReactionModel() throws InvalidSteadyStateModelException{
		
		getSSGeneModelInfoFromContainer();
		getSSModelInfoFromContainer();
		String modelName = container.getModelName();
		if(container.getVersion() != null)
			modelName += ".v" + container.getVersion();
		SteadyStateGeneReactionModel model = new SteadyStateGeneReactionModel(
				modelName, 
				smatrix, 
				reactions, 
				metabolites, 
				compartments,
				pathways,
				genes,
				null,
				geneReactionRules,
				null);
		
		model.setBiomassFlux(container.getBiomassId());
		return model;
	}
	
	protected void getSSGeneModelInfoFromContainer() throws InvalidSteadyStateModelException {
		
		if(container.getGenes().size()==0)
			throw new InvalidSteadyStateModelException("The container does not have Gene Information");
		else{
			genes = new IndexedHashMap<String,Gene>();
			for(GeneCI gene : container.getGenes().values())
				genes.put(gene.getGeneId(), new Gene(gene.getGeneId(),gene.getGeneName()));
			
			geneReactionRules = new IndexedHashMap<String, GeneReactionRule>();
			for(ReactionCI reaction : container.getReactions().values()){
				AbstractSyntaxTree<DataTypeEnum, IValue> rule = reaction.getGeneRule();
				if(rule!=null && rule.size()>0){
					GeneReactionRule grule = new GeneReactionRule(rule);			
					geneReactionRules.put(reaction.getId(), grule);
				}
			}
		}
		
	}

	protected void getSSModelInfoFromContainer() throws InvalidSteadyStateModelException{
		
		
		/** load all compartments */
		Integer numberOfMetabolites =0;
		compartments = new HashMap<String,Compartment>();
		boolean hasMetUniqIds = container.identifyIfHasUniqueMetaboliteIds();
		
//		int debug_i =0;
		for(CompartmentCI compartment : container.getCompartments().values()){
			
//			boolean isExternal = false;
//			if(compartment.getOutside()!=null)
//				isExternal = (compartment.getOutside().equals(""))  ? true: false;
			
			
			
//			System.out.println(compartment.getId() + "\t" + compartment.getMetabolitesInCompartmentID().size());
//			for(String met_id : compartment.getMetabolitesInCompartmentID()){
//				System.out.println(debug_i++ + "\t" + met_id + "_" + compartment.getId());
//			}
				
			
			numberOfMetabolites +=compartment.getMetabolitesInCompartmentID().size();
			Compartment comp = new Compartment(compartment.getId(),compartment.getOutside(),0.0, false);
			compartments.put(comp.getId(), comp);
		}		
		
		/** load all metabolites */
		metabolites = new IndexedHashMap<String, Metabolite>();
		/** load all reactions */
		reactions = new IndexedHashMap<String, Reaction>();
		/** load all pathways  */
		pathways = new IndexedHashMap<String, Pathway>();
		for(String s : container.getPathwaysIDs()){
			pathways.put(s, new Pathway(s));
		}
		
//		System.out.println("met:" + numberOfMetabolites);
//		System.out.println("reac:" + container.getReactions().values().size());
		
		double[][] matrix = new double[numberOfMetabolites][container.getReactions().values().size()];
		
		int j=0;
		for(ReactionCI ogreaction : container.getReactions().values()){
			
			String pathwayID = ogreaction.getSubsystem();
			if(pathways.containsKey(pathwayID)){
				pathways.get(pathwayID).addReaction(ogreaction.getId());
				for(String m : ogreaction.getMetaboliteSetIds()){
//					Compartment comp = null;
//					for(CompartmentCI c : container.getCompartments().values()){
//						if(c.getMetabolitesInCompartmentID().contains(m))
//							comp = new Compartment(c.getId());
//					}
					pathways.get(pathwayID).addMetabolite(m);
				}
			}
			
			Boolean reversibility = ogreaction.isReversible();
			Double upperBound = DEFAULT_BOUND_VALUE;
			Double lowerBound =(ogreaction.isReversible())  ? -DEFAULT_BOUND_VALUE: 0.0;
			
			ReactionConstraintCI reactionConstraint = container.getDefaultEC().get(ogreaction.getId());
			if(reactionConstraint!=null){
				upperBound = reactionConstraint.getUpperLimit();
				lowerBound = reactionConstraint.getLowerLimit();
			}
			
			Reaction reaction = new Reaction(ogreaction.getId(),reversibility,lowerBound,upperBound);
			reaction.setName(ogreaction.getName());
			/*
			 * Construction of drains identifies the reactions as
			 * drains before the creation of the model
			 */

				
			reaction.setType(ReactionType.INTERNAL);
		
				
			for(StoichiometryValueCI ref: ogreaction.getProducts().values()){		
				double stoichiometry = ref.getStoichiometryValue();
				
				String compartmentId = ref.getCompartmentId();
				String metaboliteId = ref.getMetaboliteId();
				String metaboliteInModel = metaboliteId;
				if(!hasMetUniqIds)
					metaboliteInModel+="_" + compartmentId;
			
				
				
				if(!metabolites.containsKey(metaboliteInModel)){

					Metabolite meta = new Metabolite(metaboliteInModel,getMetaboliteName(metaboliteId),compartments.get(compartmentId));
					
					
					metabolites.put(metaboliteInModel, meta);
					
				}
				
				int metaindex = metabolites.getIndexOf(metaboliteInModel);
				if(metaindex==-1)
					throw new InvalidSteadyStateModelException("Container: Error in reaction ["+ogreaction.getId()+"]: no product named ["+ref.getMetaboliteId()+"] found!");
				
//				if(compartmentId.equalsIgnoreCase(externalCompart) && !externalMetabolites.contains(metaboliteInModel)){
//					externalMetabolites.add(metaboliteInModel);
//				}
//					
				matrix[metaindex][j] = stoichiometry;
			}
			
			for(StoichiometryValueCI ref: ogreaction.getReactants().values()){
				double stoichiometry = ref.getStoichiometryValue();
				
				String compartmentId = ref.getCompartmentId();
				String metaboliteId = ref.getMetaboliteId();
				String metaboliteInModel = metaboliteId;
				if(!hasMetUniqIds)
					metaboliteInModel+="_" + compartmentId;
				
//				System.out.println(compartmentId + "\t" + metaboliteId);
				
				
				if(!metabolites.containsKey(metaboliteInModel)){
					
					Metabolite meta = new Metabolite(metaboliteInModel,getMetaboliteName(metaboliteId),compartments.get(compartmentId));
					
					metabolites.put(metaboliteInModel, meta);
				
				}
	
				int metaindex = metabolites.getIndexOf(metaboliteInModel);
//				System.out.println(metaboliteId + "\t" + metaindex);
				if(metaindex==-1)
					throw new InvalidSteadyStateModelException("Container: Error in reaction ["+ogreaction.getId()+"]: no reactant named ["+ref.getMetaboliteId()+"] found!");

//				if(compartmentId.equalsIgnoreCase(externalCompart) && !externalMetabolites.contains(metaboliteInModel)){
//					externalMetabolites.add(metaboliteInModel);
//				}
				
				if(matrix[metaindex][j]!= 0){
					matrix[metaindex][j] = 0;
					System.err.println("The reactions " + reaction.getId() + " produces and consumes, in the same compartment the metabolite " + metaboliteInModel );
				}else
					matrix[metaindex][j] = -stoichiometry;
			}
			
			if(ogreaction.isDrain()){
				reaction.setType(ReactionType.DRAIN);
			}
			
			reactions.put(reaction.getId(), reaction);
			j++;
		}

		
//		if(hasDrains == false){
//			
//			
//		}
//		//Add Drains
//		for(String extMet: externalMetabolites){
//			
//			String reactionId = "R_EX_"+extMet;
//			Reaction reaction = new Reaction(reactionId, true, 0, DEFAULT_BOUND_VALUE);
//			reaction.setType(ReactionType.DRAIN);
//			reactions.put(reactionId, reaction);
//			
//			int idxMet = metabolites.getIndexOf(extMet);
//			matrix[idxMet][j] = -1;
//			j++;
//				
//		}
		
		/** build the sparse stoichiometric matrix */
		SparseDoubleMatrix2D coltmatrix = new SparseDoubleMatrix2D(matrix);
		smatrix = new ColtSparseStoichiometricMatrix(coltmatrix);
		/** builds the steady state model */
		//SteadyStateModel ssmodel = new SteadyStateModel(container.getName(),spmatrix,lreactions,metabolites,compartments);		

	}
	
	/*
	 * Added Static Functions from class SGRConversion
	 */
	

//	public static SteadyStateGeneReactionModel convert2SteadyStateGeneReactionModel(Container container, ArrayList<String> toRemove) throws Exception{
//
//		container.removeMetabolites(toRemove);
//		ContainerConverter cont = new ContainerConverter(container);
//		return cont.convertToGeneReactionModel();
//
//	}
//	
	
	//NOTE: this method was changed for the class /Utilities/src/utilities/grammar/syntaxtree/TreeUtils
//	public static ArrayList<String> withdrawVariablesInRule(AbstractSyntaxTree<DataTypeEnum, IValue> booleanRule){
//
//		ArrayList<String> ret = new ArrayList<String>();;
//		AbstractSyntaxTreeNode<DataTypeEnum, IValue> root = null;
//
//		if(booleanRule!=null)
//			root = booleanRule.getRootNode();
//		else
//			return ret;
//
//		Queue<AbstractSyntaxTreeNode<DataTypeEnum, IValue>> nodeQueue = new LinkedList<AbstractSyntaxTreeNode<DataTypeEnum, IValue>>();
//		nodeQueue.offer(root);
//
//		while(!nodeQueue.isEmpty()){
//			AbstractSyntaxTreeNode<DataTypeEnum, IValue> currentNode = nodeQueue.poll();
//
//			for(int i = 0; i < currentNode.getNumberOfChildren(); i++)
//				nodeQueue.offer(currentNode.getChildAt(i));
//
//			if(currentNode.isLeaf())
//				ret.add(currentNode.toString());
//		}
//
//		return ret;
//	}
	
	
	public static ISteadyStateModel convert(Container container) throws InvalidSteadyStateModelException {
		ContainerConverter cont = new ContainerConverter(container);
		return cont.convert();
	}
	
	public static void setBoundaryMetabolitesInModel(ISteadyStateModel model , Set<String> bMetabolites){
		
		for(String id : bMetabolites){
			model.getMetabolites().get(id).setBoundaryCondition(true);
		}
	}
	
	
	private String getMetaboliteName(String id){
		MetaboliteCI m = container.getMetabolite(id);
		
		String name = (m==null)?id+" name not found":m.getName();
		return name;
	}
	
//	
//	private String getExternalCompartment(){
//		String toReturn = "";
//		
//		for(String compId : container.getCompartments().keySet()){
//			
//			CompartmentCI comp = container.getCompartments().get(compId);
//			if(comp.getOutside() == null || comp.getOutside())
//		}
//	}
	
//	private void addMetaboliteInModel(){
//		
//	}
}
