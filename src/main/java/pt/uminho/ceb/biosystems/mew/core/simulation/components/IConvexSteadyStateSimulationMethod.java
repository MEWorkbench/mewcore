package pt.uminho.ceb.biosystems.mew.core.simulation.components;

import java.beans.PropertyChangeListener;

public interface IConvexSteadyStateSimulationMethod extends ISteadyStateSimulationMethod {
	
	void preSimulateActions();
	
	void postSimulateActions();
	
	void setRecreateOF(boolean recreateOF);
	
	boolean isRecreateOF();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void saveModelToMPS(String file, boolean includeTime);
	
	void forceSolverCleanup();
}
