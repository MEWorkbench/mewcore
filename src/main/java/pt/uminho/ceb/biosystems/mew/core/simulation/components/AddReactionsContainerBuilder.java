package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class AddReactionsContainerBuilder implements IContainerBuilder{
	private static final long serialVersionUID = 1L;
	private Container origContainer;
	private Container dbContainer;
	private Set<String> listReactions;
	private Set<String> listNewMetabolites;
	
	public AddReactionsContainerBuilder(Container origContainer, Container dbContainer, Set<String> listReactions){
		this.origContainer = origContainer;
		this.dbContainer = dbContainer;
		this.listReactions = listReactions;
	}

	@Override
	public String getModelName() {
		return origContainer.getModelName();
	}

	@Override
	public String getOrganismName() {
		return origContainer.getOrganismName();
	}

	@Override
	public String getNotes() {
		return origContainer.getNotes();
	}

	@Override
	public Integer getVersion() {
		return origContainer.getVersion();
	}

	@Override
	public Map<String, CompartmentCI> getCompartments() {
		 Map<String, CompartmentCI> compartments = new  IndexedHashMap<String, CompartmentCI> ();
		 compartments.putAll(origContainer.getCompartments());
		 
		 // search what compartments are necessary in the new container
		 Set<String> dbCompartments = dbContainer.getCompartments().keySet();
		 Set<String> addComp = new TreeSet<String>();		 
		 for(String comp: dbCompartments){
			 Set<String >metaInComp = dbContainer.getCompartments().get(comp).getMetabolitesInCompartmentID();
			 boolean contains = false;
			 for(String reac: listReactions){
				 for(String meta:dbContainer.getReaction(reac).getMetaboliteSetIds())
					 if(metaInComp.contains(meta)){
						 contains=true;
						 break;			 
				 }
				 if(contains) break;
			 }
			 if(contains)
				 addComp.add(comp);			 
		 }
		 
		 for(String comp: addComp){
			 compartments.put(comp,dbContainer.getCompartment(comp));
		 
		 }		 
		 return compartments;		 
	}

	@Override
	public Map<String, ReactionCI> getReactions() {
		Map<String, ReactionCI> reactions = new  IndexedHashMap<String, ReactionCI> ();
		reactions.putAll(origContainer.getReactions());
		for(String reac: listReactions){
			reactions.put(reac,dbContainer.getReaction(reac));
		}		
		return reactions;
	}

	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		Map<String, MetaboliteCI> metabolites = new  IndexedHashMap<String, MetaboliteCI> ();
		metabolites.putAll(origContainer.getMetabolites());
		Set<String> origMetabolites = origContainer.getMetabolites().keySet();
		listNewMetabolites = new TreeSet<String>();
		Set<String>remove = new TreeSet<String>();
		//set of all metabolites used in new reactions
		for(String reac: listReactions){
			
			listNewMetabolites.addAll(dbContainer.getReactions().get(reac).getMetaboliteSetIds());
		}		
		// set of necessary metabolites to add
		for(String met: listNewMetabolites){
			if(origMetabolites.contains(met))
				remove.add(met);
		}
		listNewMetabolites.removeAll(remove);
		// insert new metabolites in the map
		for(String met: listNewMetabolites){
			metabolites.put(met, dbContainer.getMetabolite(met));
		}
		return metabolites;
	}
	
	@Override
	public Map<String, GeneCI> getGenes() {
		Map<String, GeneCI> genes = new  IndexedHashMap<String, GeneCI> ();
		genes.putAll(origContainer.getGenes());
		// add the geneCI from the database for the adicional reactions presented in listReactions
		for(String reac: listReactions){
			Set<String> newGenesIds = dbContainer.getReaction(reac).getGenesIDs();
			for(String gene: newGenesIds){
				if(!genes.containsKey(gene)){
					genes.put(gene, new GeneCI(gene,dbContainer.getGene(gene).getGeneName()));
				}
			}			
		}
		return genes;
	}

	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		Map<String, Map<String, String>> metExtraInfo = new TreeMap<String, Map<String, String>>();
		if(origContainer.getMetabolitesExtraInfo()!=null){
			metExtraInfo.putAll(origContainer.getMetabolitesExtraInfo());
			for(String met: listNewMetabolites){
				if(dbContainer.getMetabolitesExtraInfo()!=null && dbContainer.getMetabolitesExtraInfo().get(met)!=null)
					metExtraInfo.put(met, dbContainer.getMetabolitesExtraInfo().get(met));
			}
		}
		return metExtraInfo;
	}

	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		Map<String, Map<String, String>> reacExtraInfo = new TreeMap<String, Map<String, String>>();
		if(origContainer.getReactionsExtraInfo()!=null){
			reacExtraInfo.putAll(origContainer.getReactionsExtraInfo());
			for(String reac: listReactions){
				if(dbContainer.getReactionsExtraInfo()!=null && dbContainer.getReactionsExtraInfo().get(reac)!=null)
					reacExtraInfo.put(reac, dbContainer.getReactionsExtraInfo().get(reac));
			}
		}
		return reacExtraInfo;
	}

	@Override
	public String getBiomassId() {
		return origContainer.getBiomassId();
	}

	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		Map<String, ReactionConstraintCI> defaultEC = new TreeMap<String, ReactionConstraintCI>();
		defaultEC.putAll(origContainer.getDefaultEC());
		for(String reac: listReactions){
			defaultEC.put(reac, dbContainer.getDefaultEC().get(reac));
		}		
		return defaultEC;
	}

	@Override
	public String getExternalCompartmentId() {
		return origContainer.getExternalCompartmentId();
	}
}
