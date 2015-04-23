package pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.interfaces;

public interface IModelStructure {
	
	public int getBlockedClusters();
	
	public int[] getClusterBelongings();
	
	public String[] getReactionNames();

}
