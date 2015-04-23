package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.solvers.qp.IQPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;

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
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.MFARatiosOverrideModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ratioconstraints.RatioConstraintComparator;

public abstract class MFAWithSolvers<T extends LPProblem> extends AbstractSSBasicSimulation<T> implements IMFASimulationMethod{
	
	public MFAWithSolvers(ISteadyStateModel model) {
		super(model);
		possibleProperties.add(MFAProperties.MEASURED_FLUXES);
		possibleProperties.add(MFAProperties.FLUX_RATIO_CONSTRAINTS);
	}
	
	@Override
	protected void createModelOverride() throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions environmentalConditions = getEnvironmentalConditions();
		GeneticConditions geneticConditions = getGeneticConditions();
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		FluxRatioConstraintList ratioConstraints = getFluxRatioConstraints();

		overrideRC = new MFARatiosOverrideModel(model,environmentalConditions, geneticConditions, measuredFluxes, ratioConstraints, getProblemClass());
	}

	
	protected LPConstraintType getLPConstraintType(RatioConstraintComparator comparator){
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
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(propreties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
	
	public IOverrideReactionBounds getOverrideReactionBounds(){
		return overrideRC;
	}
	
	
	@Override
	public ExpMeasuredFluxes getMeasuredFluxes() throws PropertyCastException, MandatoryPropertyException{
		ExpMeasuredFluxes measuredFluxes = ManagerExceptionUtils.testCast(propreties, ExpMeasuredFluxes.class, MFAProperties.MEASURED_FLUXES, true); 
		return measuredFluxes;
	}
	
	@Override
	public FluxRatioConstraintList getFluxRatioConstraints() throws PropertyCastException, MandatoryPropertyException{
		FluxRatioConstraintList fluxRatioConstraints = ManagerExceptionUtils.testCast(propreties, FluxRatioConstraintList.class, MFAProperties.FLUX_RATIO_CONSTRAINTS, true); 
		return fluxRatioConstraints;
	}
	
	@Override
	protected LPSolution simulateProblem() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException{
		SolverType solverType = getSolverType();
		LPProblem p = getProblem();
		LPSolution solution = null;
		
		Class<T> c = getProblemClass();
		
		if(c.equals(QPProblem.class))
		{
			IQPSolver solver = solverType.qpSolver((QPProblem) p);		
			solution = solver.solve();
		}
		else
		{
			ILPSolver solver = solverType.lpSolver(p);
			solution = solver.solve();
		}
		
		return solution;
	}
	
	@SuppressWarnings("hiding")
	public <T extends LPProblem> Class<T> getProblemClass(){
		ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}
	
	
	@Override
	protected void createConstrains() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException, SolverException {
		super.createConstrains();
		
		FluxRatioConstraintList fluxRatioConstraints = null;
		try{
			fluxRatioConstraints = getFluxRatioConstraints();
		}catch (Exception e) {e.printStackTrace();}
		
		if(fluxRatioConstraints!=null)
		{
			for(FluxRatioConstraint ratioConstraint : fluxRatioConstraints)
			{
				String lpRatioConstraint = "";
				
				LPProblemRow row = new LPProblemRow();
				Map<String, Double> fluxCoeffs = ratioConstraint.getFluxesCoeffs();
				
				for(String flux : fluxCoeffs.keySet())
				{
					double value = fluxCoeffs.get(flux);
					int fluxIndex;
					
					// If the flux is negative, it will remove the negative suffix from the flux id
					String fluxId = fluxRatioConstraints.getFluxIdFromNegativeForm(flux);
					fluxIndex = model.getReactionIndex(fluxId);
					
					if(fluxRatioConstraints.isFluxNegative(fluxId))
						value *= -1;
									
					lpRatioConstraint += " + (" + value + ") " + fluxId;
					try {
						row.addTerm(fluxIndex, value);
					} catch (LinearProgrammingTermAlreadyPresentException e) {
						throw new WrongFormulationException(e);
					}
				}
	
				System.out.println("<RCC>------------------- LP Ratio Constraint: " + lpRatioConstraint);
				LPConstraint constraint = new LPConstraint(getLPConstraintType(ratioConstraint.getComparator()), row, 0.0);
				problem.addConstraint(constraint);
			}
		}
	}
}
