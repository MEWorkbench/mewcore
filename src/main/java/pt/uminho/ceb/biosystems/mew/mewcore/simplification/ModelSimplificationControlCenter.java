package pt.uminho.ceb.biosystems.mew.mewcore.simplification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;


public class ModelSimplificationControlCenter implements IContainerBuilder{
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Container oldContainer;

	protected ZeroValueFluxes zeroValues;
	protected EquivalentFluxes equivalentFluxes;
	protected ZeroValueFluxes finalZeroValues;
	protected EquivalentFluxes finalEquivalentFluxes;
//	private Map<String,ListEquivalentReactions> mapOfEquivalences;
	
	protected String name;
	protected String organism;
	protected String notes;
	protected Integer version;
	
	protected Map<String, ReactionCI> reactions;
	protected Map<String, MetaboliteCI> metabolites;
	protected Map<String, CompartmentCI> compartments;
	protected Map<String, GeneCI> genes;
	protected Map<String, Map<String, String>> metabolitesExtraInfo;
	protected Map<String, Map<String, String>> reactionsExtraInfo;
	
	protected Map<String, ReactionConstraintCI> defaultEC;
	protected String biomassId;
	protected String ext_compartment = null;
	
	protected 	Set<String> toRemove;
	
	protected String sepToJoinedReactions;
	
	public ModelSimplificationControlCenter(Container oldcont, ZeroValueFluxes zvF, EquivalentFluxes eqF){
		this(oldcont, zvF, eqF, "_");
	}
	
	
	public ModelSimplificationControlCenter(Container oldcont, ZeroValueFluxes zvF, EquivalentFluxes eqF, String sepToJoinedReactions){
		
		this.oldContainer = oldcont;
		this.zeroValues = zvF;
		this.equivalentFluxes = eqF;
		this.sepToJoinedReactions = sepToJoinedReactions;
		
		
//		/**
//		 * FIXME:temporary map for tests
//		 */
//		this.mapOfEquivalences = new HashMap<String, ListEquivalentReactions>();
		
		
		reactions = new HashMap<String, ReactionCI>();
		metabolites = new HashMap<String, MetaboliteCI>();
		compartments = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		
		metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
		reactionsExtraInfo = new HashMap<String, Map<String,String>>();
		
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		
		name = oldcont.getModelName();
		organism = oldcont.getOrganismName();
		notes = oldcont.getNotes();
		version = oldcont.getVersion();
		
		biomassId = oldcont.getBiomassId();
		ext_compartment = oldcont.getExternalCompartmentId();
		
		toRemove = new HashSet<String>();
	}
		
//	public Map<String, ListEquivalentReactions> getMapOfEquivalences() {
//		return mapOfEquivalences;
//	}
//
//	public void setMapOfEquivalences(
//			Map<String, ListEquivalentReactions> mapOfEquivalences) {
//		this.mapOfEquivalences = mapOfEquivalences;
//	}

	/**
	 * Simplification with zero values and equivalent reactions
	 * @throws Exception
	 */
	public void simplifyContainer() throws Exception{
		
		
		if(equivalentFluxes!=null && zeroValues!=null)
			analyzeSimplifications();
		else{
			copyInitialToFinal();
		}
		
		identifyReactionsToRemove();
		
		createCompartmentsMap();
		createGenesMap();
		
		createReactionsMap();
		

		if(this.equivalentFluxes!=null)
			addReactionsFromEquivalence();
			
		
	}
	
	private void createGenesMap() {

		if(oldContainer.hasGeneInformation()){
			
			
			for(String gId : oldContainer.getGenes().keySet()){
				
				Set<String> reactionsNotEliminated = new HashSet<String>();
				if(!genes.containsKey(gId) && 
						!(toRemove.containsAll(oldContainer.getGene(gId).getReactionIds()))
					){
					
					for(String rId : oldContainer.getGene(gId).getReactionIds()){
						if(!toRemove.contains(rId))
							reactionsNotEliminated.add(rId);
					}
				}
					
				GeneCI gene = new GeneCI(gId,oldContainer.getGene(gId).getGeneName());
				for(String s : reactionsNotEliminated)
					gene.addReactionId(s);
				genes.put(gId, gene);
			}
		}
		
	}

	private void createReactionsMap() {
		
			for(String rId : oldContainer.getReactions().keySet()){
				
				if(!toRemove.contains(rId)){
					ReactionCI reaction = oldContainer.getReaction(rId);
					addMetabolitesFromReaction(reaction);
					
					if(oldContainer.getDefaultEC().get(rId)!=null)
						defaultEC.put(rId, oldContainer.getDefaultEC().get(rId));
					
					reactions.put(rId, reaction);
					
					if(oldContainer.getReactionsExtraInfo()!=null){
						//For each category of extra information
						Map<String, Map<String, String>> extraInfo = oldContainer.getReactionsExtraInfo();
						for(String excat : extraInfo.keySet()){
							
							//For each id in each category
							for(String idInExtra : extraInfo.get(excat).keySet()){
								//if Has extra information on the reaction
								if(idInExtra.equals(rId)){
									//and contains the category of extra reaction
									if(reactionsExtraInfo.keySet().contains(excat))
										reactionsExtraInfo.get(excat).put(rId,extraInfo.get(excat).get(rId));
									else{
										//Create the category of extra reaction
										reactionsExtraInfo.put(excat, new HashMap<String, String>());
										reactionsExtraInfo.get(excat).put(rId, extraInfo.get(excat).get(rId));
									}
								}
							}
						}
					}
					
				}	
			}
	}

	private void addMetabolitesFromReaction(ReactionCI reaction) {

		for(String metId : reaction.getMetaboliteSetIds()){

			if(!this.metabolites.containsKey(metId)){
				metabolites.put(metId, oldContainer.getMetabolite(metId));
				if(oldContainer.getMetabolitesExtraInfo()!=null){

					Map<String, Map<String, String>> extraInfo = oldContainer.getMetabolitesExtraInfo();
					//for each category
					for(String extcat : extraInfo.keySet()){
						//if metabolite has information in that category
						if(extraInfo.get(extcat).keySet().contains(metId)){
							//if new extra info has category
							if(metabolitesExtraInfo.keySet().contains(extcat)){
								metabolitesExtraInfo.get(extcat).put(metId,extraInfo.get(extcat).get(metId));
							}
							else{
								//create category
								metabolitesExtraInfo.put(extcat, new HashMap<String, String>());
								metabolitesExtraInfo.get(extcat).put(metId, extraInfo.get(extcat).get(metId));
							}
						}

					}

				}
			}
		}
	}

	private void createCompartmentsMap() {
		for(String cId : oldContainer.getCompartments().keySet()){
			compartments.put(cId, oldContainer.getCompartment(cId));
		}
	}

	private void identifyReactionsToRemove() {

		toRemove = new HashSet<String>();
		if(this.finalZeroValues!=null){
			for(String s : this.finalZeroValues.getZeroValueFluxes()){
				toRemove.add(s);
			}
		}
		
		for(ListEquivalentReactions equivalence : this.finalEquivalentFluxes.getListsOfEquivalences()){
			toRemove.addAll(equivalence.getListIds());
		}
	}

	private void copyInitialToFinal() {

		if(zeroValues!=null){
			List<String> newZeroList = new ArrayList<String>();
			//copy the zero values
			for(String zv : this.zeroValues.getZeroValueFluxes()){
				newZeroList.add(zv);
			}
			finalZeroValues = new ZeroValueFluxes(newZeroList);
		}
		
		List<ListEquivalentReactions> newEquivalences = new ArrayList<ListEquivalentReactions>();
		if(equivalentFluxes!=null){
			for(ListEquivalentReactions equivalence : equivalentFluxes.getListsOfEquivalences()){
					newEquivalences.add(equivalence);
			}
		}
		finalEquivalentFluxes = new EquivalentFluxes(newEquivalences);
	}

	
	private void addReactionsFromEquivalence() throws Exception {
		
		//CREATING THE REACTION
		int counterEquivalences = 1;
				
		for(ListEquivalentReactions equivalence : this.finalEquivalentFluxes.getListsOfEquivalences()){
			
			
			Set<String> rIdsToBalance = equivalence.getListIds();	
			
			String shortName = CollectionUtils.join(rIdsToBalance, sepToJoinedReactions);
			
			
			ReactionCI reaction = ContainerUtils.joinReactions(shortName, name, rIdsToBalance, oldContainer);
			
			if(rIdsToBalance.contains(oldContainer.getBiomassId())){
				reaction.setId(oldContainer.getBiomassId());
				reaction.setType(ReactionTypeEnum.Biomass);
			}
			
//			ReactionCI reaction = createReactionFromEquivalence(rIdsToBalance, equivalence.toString(),counterEquivalences);
			counterEquivalences++;
			
//			this.mapOfEquivalences.put(reaction.getId(), equivalence);
			
			//DEFAULT ENVIRONMENTAL CONDITIONS
			double lowerLimit = Double.NEGATIVE_INFINITY;
			double upperLimit = Double.POSITIVE_INFINITY;
			boolean hasDefaultEC = false;
		
			for(String rId : rIdsToBalance){
				if(oldContainer.getDefaultEC().containsKey(rId)){
					hasDefaultEC = true;
					double tempLL = oldContainer.getDefaultEC().get(rId).getLowerLimit();
					double tempUL = oldContainer.getDefaultEC().get(rId).getUpperLimit();
					if(tempLL > lowerLimit) lowerLimit = tempLL;
					if(tempUL < upperLimit) upperLimit = tempUL;
				}
			}
			
			if(hasDefaultEC)
				defaultEC.put(reaction.getId(), new ReactionConstraintCI(lowerLimit,upperLimit));
			
			//GENE RULES
			String geneRule = "";
			for(String rId : rIdsToBalance){
				if(oldContainer.getReaction(rId).getGeneRule()!=null && !oldContainer.getReaction(rId).getGeneRule().toString().equals("")){
					if(geneRule.equals(""))
						geneRule +=  oldContainer.getReaction(rId).getGeneRule().toString();
					else 
						geneRule += " and " + oldContainer.getReaction(rId).getGeneRule().toString();
				}
			}

			if(!geneRule.equals(""))
				try {
					reaction.setGeneRule(geneRule);
				} catch (InvalidBooleanRuleException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			//PROTEIN RULES
			String proteinRule = "";
			for(String rId : rIdsToBalance){
				if(oldContainer.getReaction(rId).getProteinRule()!=null && !oldContainer.getReaction(rId).getProteinRule().toString().equals("")){
					if(proteinRule.equals(""))
						proteinRule +=  oldContainer.getReaction(rId).getProteinRule().toString();
					else 
						proteinRule += " and " + oldContainer.getReaction(rId).getProteinRule().toString();
				}
			}

			if(!proteinRule.equals(""))
				reaction.setProteinRule(proteinRule);
			
			//REACTION TYPE
			ReactionTypeEnum renum = null;
			boolean allEqual = true;
			
			for(String rId : rIdsToBalance){
				if(renum == null)
					renum = oldContainer.getReaction(rId).getType();
				else{
					if(!renum.equals(oldContainer.getReaction(rId).getType()))
						allEqual = false;
				}
			}
			if(allEqual)
				reaction.setType(renum);
				else
					reaction.setType(ReactionTypeEnum.Undefined);
			
			//ADD METABOLITES AND REACTIONS
			addMetabolitesFromReaction(reaction);
			reactions.put(reaction.getId(),reaction);
			
			//EXTRA INFO
			for(String rId : rIdsToBalance){
				if(oldContainer.getReactionsExtraInfo()!= null){

					Map<String,Map<String,String>> extraInfo = oldContainer.getReactionsExtraInfo();
					
					//For each category of extra information
					for(String extraId : extraInfo.keySet()){
						
						//if that category has reaction Id
						if(extraInfo.get(extraId).keySet().contains(rId)){
							
							//See if new container extra info already has the category
							if(reactionsExtraInfo.keySet().contains(extraId)){
								//If it has, checks if there is already extra information of the equivalence 
								if(reactionsExtraInfo.get(extraId).keySet().contains(reaction.getId())){
									//If it has adds the separator "-" and the other extra info of the same category
									String extraInfoString = reactionsExtraInfo.get(extraId).get(rId) + " - " + extraInfo.get(extraId).get(rId);
									reactionsExtraInfo.get(extraId).put(reaction.getId(),extraInfoString);
								}
								else
									//Has the category but not the extra information of that equivalence
									reactionsExtraInfo.get(extraId).put(reaction.getId(), extraInfo.get(extraId).get(rId));
							}
							else{
								//Adds the category and the equivalence extra information
								reactionsExtraInfo.put(extraId, new HashMap<String, String>());
								if(extraInfo.get(extraId).keySet().contains(rId))
									reactionsExtraInfo.get(extraId).put(reaction.getId(), extraInfo.get(extraId).get(rId));
							}
						}
					}
				}
			}
		}
	}
	
//	private ReactionCI createReactionFromEquivalence(String id, Set<String> listOfReactions, String name, int counterEquivalences) throws Exception{
//	
////		Map<String, Double> balance = ContainerUtils.balanceOfReactions(listOfReactions, oldContainer);
//		String shortName = CollectionUtils.join(listOfReactions, "_");
//		
////		Map<String,StoichiometryValueCI> reactants = new HashMap<String, StoichiometryValueCI>();
////		Map<String,StoichiometryValueCI> products = new HashMap<String, StoichiometryValueCI>();
////		String comp = "";
//		name = "";
//		
//		ReactionCI reaction = ContainerUtils.joinReactions(shortName, name, listOfReactions, oldContainer);
//		
////		for(String metId : balance.keySet()){
////			
////			
////			for(String auxID : listOfReactions){
////				
////				ReactionCI r = oldContainer.getReaction(auxID);
////				name += auxID + " ";
////				
////				if(r.getReactants().containsKey(metId))
////					comp = r.getReactants().get(metId).getCompartmentId();
////				if(r.getProducts().containsKey(metId))
////					comp = r.getProducts().get(metId).getCompartmentId();
////			}
////				
////			if(balance.get(metId) > 0.0){
////				products.put(metId, new StoichiometryValueCI(metId, Math.abs(balance.get(metId)), comp));
////			}
////			if(balance.get(metId) < 0.0)
////				reactants.put(metId, new StoichiometryValueCI(metId, Math.abs(balance.get(metId)), comp));
////		}
//		
////		ReactionCI reaction = new ReactionCI(shortName, name, reversible, reactants,products);
//		
//		return reaction;
//	}

	public void analyzeSimplifications(){
		
		List<ListEquivalentReactions> newEquiList = new ArrayList<ListEquivalentReactions>();
		List<String> newZeroList = new ArrayList<String>();
		
		//copy the zero values
		for(String zv : this.zeroValues.getZeroValueFluxes()){
			newZeroList.add(zv);
		}
		
		for(ListEquivalentReactions equivalence : this.equivalentFluxes.getListsOfEquivalences()){
			 
			boolean hasZeroReaction = false;
			
			for(String rId : equivalence.getListIds()){
				if(this.zeroValues.getZeroValueFluxes().contains(rId)){
					hasZeroReaction = true;
				}
			}
			if(hasZeroReaction){
				for(String rId : equivalence.getListIds())
					if(!newZeroList.contains(rId))
						newZeroList.add(rId);
			}
			else{
				newEquiList.add(equivalence);
			}
		}
		
		EquivalentFluxes ef = new EquivalentFluxes(newEquiList);
		ZeroValueFluxes zf = new ZeroValueFluxes(newZeroList);
		
		this.finalEquivalentFluxes = ef;
		this.finalZeroValues = zf;
	}

	@Override
	public String getModelName() {
		return this.name;
	}

	@Override
	public String getOrganismName() {
		return this.organism;
	}

	@Override
	public String getNotes() {
		return this.notes;
	}

	@Override
	public Integer getVersion() {
		return this.version;
	}

	@Override
	public Map<String, CompartmentCI> getCompartments() {
		return this.compartments;
	}

	@Override
	public Map<String, ReactionCI> getReactions() {
		return this.reactions;
	}

	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return this.metabolites;
	}

	@Override
	public Map<String, GeneCI> getGenes() {
		
		return this.genes;
	}

	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return (this.metabolitesExtraInfo.isEmpty())? null:this.metabolitesExtraInfo;
	}

	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return (this.reactionsExtraInfo.isEmpty())? null:this.reactionsExtraInfo;
	}

	@Override
	public String getBiomassId() {
		return this.biomassId;
	}

	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return this.defaultEC;
	}

	@Override
	public String getExternalCompartmentId() {
		return this.ext_compartment;
	}


}
