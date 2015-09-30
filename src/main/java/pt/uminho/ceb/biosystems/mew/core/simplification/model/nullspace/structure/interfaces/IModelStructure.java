package pt.uminho.ceb.biosystems.mew.core.simplification.model.nullspace.structure.interfaces;

public interface IModelStructure {
	
	public int getBlockedClusters();
	
	public int[] getClusterBelongings();
	
	public String[] getReactionNames();

}
