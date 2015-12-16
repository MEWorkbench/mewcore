package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ISteadyStateSimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.IMFASimulationMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;

public abstract class MFAAlgebra implements ISteadyStateSimulationMethod, IMFASimulationMethod{
	
	protected ISteadyStateModel model;
	protected Set<String> possibleProperties;
	protected Set<String> mandatoryProps;
	protected Map<String, Object> propreties;
	
	
	public MFAAlgebra(ISteadyStateModel model){
		this.model = model;
		initPropsKeys();
	}
	
	
	protected void initPropsKeys(){
		mandatoryProps = new HashSet<String>();
		propreties = new HashMap<String, Object>();
		possibleProperties = new HashSet<String>();
		
		mandatoryProps.add(MFAProperties.MEASURED_FLUXES);
	}
	
	protected double[][] calculateStoichiometricMatrix(){
		return addRatiosToStoichiometricMatrix().convertToColt().toArray();
	}
	
	/** For each equality flux ratio, a column is added to the stoichiometric matrix 
	 * @return a new stoichiometric matrix is returned with the flux ratios linear equations */
	public IStoichiometricMatrix addRatiosToStoichiometricMatrix() {
		FluxRatioConstraintList ratios = null;
		try {
			ratios = getFluxRatioConstraints();
		} catch (PropertyCastException e1) {e1.printStackTrace();
		} catch (MandatoryPropertyException e1) {e1.printStackTrace();
		}
		return MFAAlgebraStoichConversions.addRatiosToStoichiometricMatrix(model, ratios);
	}
	
	
	@Override
	public FluxRatioConstraintList getFluxRatioConstraints() throws PropertyCastException, MandatoryPropertyException{
		FluxRatioConstraintList fluxRatioConstraints = ManagerExceptionUtils.testCast(propreties, FluxRatioConstraintList.class, MFAProperties.FLUX_RATIO_CONSTRAINTS, true); 
		return fluxRatioConstraints;
	}
	
	@Override
	public ExpMeasuredFluxes getMeasuredFluxes() throws PropertyCastException, MandatoryPropertyException{
		ExpMeasuredFluxes measuredFluxes = ManagerExceptionUtils.testCast(propreties, ExpMeasuredFluxes.class, MFAProperties.MEASURED_FLUXES, true); 
		return measuredFluxes;
	}
	
	@Override
	public void setProperty(String m, Object o) {
		propreties.put(m, o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getProperty(String k) {
		return propreties.get(k);
	}
	
	@Override
	public void putAllProperties(Map<String, Object> p){
		this.propreties.putAll(p);		
	}
	
	@Override
	public ISteadyStateModel getModel() {return this.model;}
	@Override
	public Set<String> getPossibleProperties() {return this.possibleProperties;}
	@Override
	public Set<String> getMandatoryProperties() {return this.mandatoryProps;}
	
	@Override
	public EnvironmentalConditions getEnvironmentalConditions() throws PropertyCastException, MandatoryPropertyException {return null;}
	@Override
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {}
	@Override
	public GeneticConditions getGeneticConditions() throws PropertyCastException, MandatoryPropertyException {return null;}
	@Override
	public void setGeneticConditions(GeneticConditions geneticConditions) {}
	@Override
	public Class<?> getFormulationClass() {return null;}	
	@Override
	public void clearAllProperties(){
		propreties.clear();
	};
}
