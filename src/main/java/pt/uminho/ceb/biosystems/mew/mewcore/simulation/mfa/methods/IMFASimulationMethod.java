package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.FluxRatioConstraintList;

public interface IMFASimulationMethod {
	
	public ExpMeasuredFluxes getMeasuredFluxes() throws PropertyCastException, MandatoryPropertyException;

	public FluxRatioConstraintList getFluxRatioConstraints() throws PropertyCastException, MandatoryPropertyException;

}
