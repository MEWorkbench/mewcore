package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.variability;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;

public class MFAFvaResult extends MFATightBoundsResult{

	private static final long serialVersionUID = 7932241644515762115L;
	
	protected String objectiveFlux;
	protected double objectiveFluxValue;
	protected double minPercentage;
	protected String minPercentageFlux;
	protected double minPercentageFluxValue;
	
	
	public MFAFvaResult(ISteadyStateModel model, String method) {
		super(model, method);
	}

	
	public String getObjectiveFlux() {return objectiveFlux;}
	public void setObjectiveFlux(String objectiveFlux) {this.objectiveFlux = objectiveFlux;}
	public double getObjectiveFluxValue() {return objectiveFluxValue;}
	public void setObjectiveFluxValue(double objectiveFluxValue) {this.objectiveFluxValue = objectiveFluxValue;}
	public double getMinPercentage() {return minPercentage;}
	public void setMinPercentage(double minPercentage) {this.minPercentage = minPercentage;}
	public String getMinPercentageFlux() {return minPercentageFlux;}
	public void setMinPercentageFlux(String minPercentageFlux) {this.minPercentageFlux = minPercentageFlux;}
	public double getMinPercentageFluxValue() {return minPercentageFluxValue;}
	public void setMinPercentageFluxValue(double minPercentageFluxValue) {this.minPercentageFluxValue = minPercentageFluxValue;}
}
