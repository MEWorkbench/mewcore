package pt.uminho.ceb.biosystems.mew.mewcore.criticality;

import pt.uminho.ceb.biosystems.mew.mewcore.utils.IOnOffSwitch;

public class Flag implements IOnOffSwitch {
	
	protected boolean		_on			= false;
	public TargetIDStrategy	_strategy	= null;
	
	public Flag(TargetIDStrategy strategy) {
		_strategy = strategy;
	}
	
	public Flag(TargetIDStrategy strategy,
			boolean isOn) {
		_strategy = strategy;
		_on = isOn;
	}
	
	@Override
	public boolean isOn() {
		return _on;
	}
	
	@Override
	public void on() {
		_on = true;
	}
	
	@Override
	public void off() {
		_on = false;
	}

	public TargetIDStrategy get_strategy() {
		return _strategy;
	}
	
}
