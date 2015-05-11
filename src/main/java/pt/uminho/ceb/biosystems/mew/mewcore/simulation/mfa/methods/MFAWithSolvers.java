package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.FluxRatioConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.ratioconstraints.RatioConstraintComparator;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.KeyPropertyChangeEvent;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.listenermap.ListenerHashMap;

public abstract class MFAWithSolvers<T extends LPProblem> extends AbstractSSBasicSimulation<T> implements IMFASimulationMethod {
	
	protected boolean	updateFRConstraints	= false;
	
	public MFAWithSolvers(ISteadyStateModel model) {
		super(model);
		optionalProperties.add(MFAProperties.MEASURED_FLUXES);
		optionalProperties.add(MFAProperties.FLUX_RATIO_CONSTRAINTS);
		optionalProperties.add(MFAProperties.FLUX_RATIO_LP_CONSTRAINTS);
	}
	
	@Override
	protected IOverrideReactionBounds createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();
		
		MFARatiosOverrideModel overrideRC = new MFARatiosOverrideModel(model, environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
		
		return overrideRC;
	}
	
	protected LPConstraintType getLPConstraintType(RatioConstraintComparator comparator) {
		switch (comparator) {
			case EQUAL:
				return LPConstraintType.EQUALITY;
			case MINOR:
				return LPConstraintType.LESS_THAN;
			case MINOR_EQUAL:
				return LPConstraintType.LESS_THAN;
			default:
				return LPConstraintType.GREATER_THAN;
		}
	}
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException {
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
	
	@Override
	public ExpMeasuredFluxes getMeasuredFluxes() throws PropertyCastException, MandatoryPropertyException {
		ExpMeasuredFluxes measuredFluxes = ManagerExceptionUtils.testCast(properties, ExpMeasuredFluxes.class, MFAProperties.MEASURED_FLUXES, true);
		return measuredFluxes;
	}
	
	@Override
	public FluxRatioConstraintList getFluxRatioConstraints() throws PropertyCastException, MandatoryPropertyException {
		FluxRatioConstraintList fluxRatioConstraints = ManagerExceptionUtils.testCast(properties, FluxRatioConstraintList.class, MFAProperties.FLUX_RATIO_CONSTRAINTS, true);
		return fluxRatioConstraints;
	}
	
	
	@SuppressWarnings({ "hiding", "unchecked" })
	public <T extends LPProblem> Class<T> getProblemClass() {
		ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}
	
	protected void updateConstraints() throws PropertyCastException, MandatoryPropertyException {
		
		@SuppressWarnings("unchecked")
		List<LPConstraint> currentFRLPConstraints = (List<LPConstraint>) getProperty(MFAProperties.FLUX_RATIO_LP_CONSTRAINTS);
		
		if (currentFRLPConstraints != null && !currentFRLPConstraints.isEmpty()) {
//			System.out.println("[" + getClass().getSimpleName() + "] Removing current lp constraints -> "+currentFRLPConstraints.toString());
			problem.removeConstraintRange(currentFRLPConstraints);
			
		}
		createFluxRatioConstraints();
	}	
	
	@Override
	protected void createConstraints() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		super.createConstraints();		
		createFluxRatioConstraints();
	}
	
	protected void createFluxRatioConstraints(){
		List<LPConstraint> fluxRatioLPConstraints = null;
		
		FluxRatioConstraintList fluxRatioConstraints = null;
		try {
			fluxRatioConstraints = getFluxRatioConstraints();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (fluxRatioConstraints != null) {
			fluxRatioLPConstraints = new ArrayList<LPConstraint>();
			
			for (FluxRatioConstraint ratioConstraint : fluxRatioConstraints) {
				
				LPProblemRow row = new LPProblemRow();
				Map<String, Double> fluxCoeffs = ratioConstraint.getFluxesCoeffs();
				
				for (String flux : fluxCoeffs.keySet()) {
					double value = fluxCoeffs.get(flux);
					int fluxIndex;
					
					// If the flux is negative, it will remove the negative suffix from the flux id
					String fluxId = fluxRatioConstraints.getFluxIdFromNegativeForm(flux);
					fluxIndex = model.getReactionIndex(fluxId);
					
					if (fluxRatioConstraints.isFluxNegative(fluxId)) value *= -1;
					
					try {
						row.addTerm(fluxIndex, value);
					} catch (LinearProgrammingTermAlreadyPresentException e) {
						throw new WrongFormulationException(e);
					}
				}

				LPConstraint constraint = new LPConstraint(getLPConstraintType(ratioConstraint.getComparator()), row, 0.0);
				fluxRatioLPConstraints.add(constraint);
//				System.out.println("adding LP constraints -> "+fluxRatioLPConstraints.toString());
				problem.addConstraint(constraint);
			}
		}
		setProperty(MFAProperties.FLUX_RATIO_LP_CONSTRAINTS, fluxRatioLPConstraints);
		setUpdateFRConstraints(false);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		KeyPropertyChangeEvent event = (KeyPropertyChangeEvent) evt;
		switch (event.getPropertyName()) {
			
			case ListenerHashMap.PROP_PUT: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [ADD]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (event.getKey().equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					if(problem!=null) setUpdateFRConstraints(true);
				}
				
				if (event.getKey().equals(MFAProperties.MEASURED_FLUXES)) {
					if(problem!=null) setRecreateOF(true);
				}
				
				break;
			}

		
			case ListenerHashMap.PROP_UPDATE: {
				
				if (debug) System.out.println("[" + getClass().getSimpleName() + "]: got event [UPDATE]: " + event.getKey() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
				
				if (event.getKey().equals(MFAProperties.FLUX_RATIO_CONSTRAINTS)) {
					setUpdateFRConstraints(true);
				}
				
				if (event.getKey().equals(MFAProperties.MEASURED_FLUXES)) {
					setRecreateOF(true);
				}
				
				break;
			}
			default:
				break;
		
		}
	}
	
	protected void setUpdateFRConstraints(boolean b) {
		updateFRConstraints = b;
	}
	
	@Override
	public void preSimulateActions() {		
		if (updateFRConstraints) try {
//			System.out.println("update FR constraints");
			updateConstraints();
		} catch (PropertyCastException e) {
			e.printStackTrace();
		} catch (MandatoryPropertyException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void postSimulateActions() {
		setUpdateFRConstraints(false);
	};
	
}
