package pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.interfaces;

public interface IReducedModel {
	
	public String[] getMetaboliteNames();
	
	public String[] getReactionNames();
	
	public double[] getLowerBounds();
	
	public double[] getUpperBounds();
	
	public IModelStructure getModelStructure();
	
	public void reportModelStructure();
}
