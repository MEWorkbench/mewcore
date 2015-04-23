package pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.interfaces;

public interface IReducedModelDouble extends IReducedModel{

	public double[][] getStoichValues();
	
	public double[][] getExternalStoichValues();
	
	public void setReportAsFraction(boolean asFraction);
	
	public boolean isReportAsFraction();
	
}
