package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods;

import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;

public interface IMFASimulationMethod {
	
	public ExpMeasuredFluxes getMeasuredFluxes() throws PropertyCastException, MandatoryPropertyException;

	public FluxRatioConstraintList getFluxRatioConstraints() throws PropertyCastException, MandatoryPropertyException;

}
