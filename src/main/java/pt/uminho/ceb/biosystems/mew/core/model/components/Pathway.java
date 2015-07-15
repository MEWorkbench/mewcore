package pt.uminho.ceb.biosystems.mew.core.model.components;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Pathway implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private Set<String> metabolites;
	private Set<String> reactions;
	
	public Pathway(String id){
		this.id = id;
		this.metabolites = new HashSet<String>();
		this.reactions = new HashSet<String>();
	}
	
	public Pathway() {
		this("");
	}
	
	public Pathway (String id, HashSet<String> metaboliteIDs, HashSet<String> reactionIDs){
		this.id = id;
		this.metabolites = metaboliteIDs;
		this.reactions = reactionIDs;
	}
	
	public Set<String> getMetaboliteIDs() {
		return metabolites;
	}
	
	public Set<String> getReactionIDs() {
		return reactions;
	}
	
	public String getId() {
		return id;
	}
	
	public void setMetaboliteIDs(Set<String> metaboliteIDs) {
		this.metabolites = metaboliteIDs;
	}
	
	public void setReactionIDs(Set<String> reactionIDs) {
		this.reactions = reactionIDs;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void addMetabolite(String m){
		metabolites.add(m);
	}
	
	public void addReaction(String r){
		reactions.add(r);
	}
	
}
