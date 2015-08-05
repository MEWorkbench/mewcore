package pt.uminho.ceb.biosystems.mew.core.optimization.objectivefunctions.interfaces;

public interface IStateWiseObjectiveFunction extends IObjectiveFunction{
	
	int getState();
	void setState(int state);
}
