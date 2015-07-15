package pt.uminho.ceb.biosystems.mew.core.simplification.nullspace.structure.interfaces;

public interface IModelStructureLong extends IModelStructure{
	
	public long[] getRatios();
	
	public long[][] getTransformations();
}
