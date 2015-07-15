package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.nullspace;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.IMultipleSolutionsMethod;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFAAlgebra;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;

/** This class is used to solve MFA problems formulated as a NullSpace problem, 
 * either the system is undetermined, determined or overdevermined */
public class MFANullSpace extends MFAAlgebra implements IMultipleSolutionsMethod{
	
	protected NullSpaceMethod nullSpaceMethod;
	protected MFANullSpaceSolution nullSpaceSolution;
	
	
	public MFANullSpace(ISteadyStateModel model) throws Exception {
		super(model);
	}

	
	protected void createProblemIfEmpty() throws Exception {
		if(nullSpaceMethod==null)
		{
			boolean computeSentitivity = isComputeSensitivity();
			boolean calculateAlternativeFluxes = isCalculateAlternativeFluxes();
			
			double[][] stoichiometricMatrix = calculateStoichiometricMatrix();
		
			ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
			
			boolean[] measuredFluxesPositionArray = calculateMeasuredFluxPositionArray(measuredFluxes);
			double[] measuredFluxesArray = calculateMeasureFluxesArray(measuredFluxes);

			nullSpaceMethod = new NullSpaceMethod(stoichiometricMatrix, computeSentitivity, calculateAlternativeFluxes, measuredFluxesPositionArray, measuredFluxesArray);
		}
	}
	
	private boolean[] calculateMeasuredFluxPositionArray(ExpMeasuredFluxes measuredFluxes) {

		boolean[] fluxPositionArray = new boolean[model.getNumberOfReactions()];
		
		if(measuredFluxes!=null)
		{
			for(int fluxIndex=0; fluxIndex<model.getNumberOfReactions(); fluxIndex++)
			{
				String rId = model.getReaction(fluxIndex).getId();
				if(measuredFluxes.containsKey(rId))
					fluxPositionArray[fluxIndex] = true;
			}
		}
		
		return fluxPositionArray;
	}
	
	private double[] calculateMeasureFluxesArray(ExpMeasuredFluxes measuredFluxes) {
		
		if(measuredFluxes==null)
			return null;
		
		double[] fluxArray = new double[measuredFluxes.size()];
		
		int i=0;
		for(int fluxIndex=0; fluxIndex<model.getNumberOfReactions(); fluxIndex++)
		{
			String rId = model.getReaction(fluxIndex).getId();
			if(measuredFluxes.containsKey(rId))
				fluxArray[i++] = measuredFluxes.getFluxValue(rId);
		}
		
		return fluxArray;
	}
		
	public boolean isComputeSensitivity() throws MandatoryPropertyException {
		Boolean computeSensitivity = null;
	
		try {
			computeSensitivity = ManagerExceptionUtils.testCast(propreties, Boolean.class, MFAProperties.NULLSPACE_COMPUTESENTITIVITY, true);
		} catch (PropertyCastException e) {e.printStackTrace();
		} catch (MandatoryPropertyException e) {throw e;}
	
		return (computeSensitivity==null) ? false : computeSensitivity;
	}
	
	public boolean isCalculateAlternativeFluxes() throws MandatoryPropertyException {
		Boolean calculateAlternativeFluxes = null;
	
		try {
			calculateAlternativeFluxes = ManagerExceptionUtils.testCast(propreties, Boolean.class, MFAProperties.NULLSPACE_CALCULATEALTERNATIVEFLUXES, true);
		} catch (PropertyCastException e) {e.printStackTrace();
		} catch (MandatoryPropertyException e) {throw e;}
	
		return (calculateAlternativeFluxes==null) ? false : calculateAlternativeFluxes;
	}
	
	public MFANullSpaceSolution getNullSpaceSolution(){
		return nullSpaceSolution;
	}
	
	@Override
	protected void initPropsKeys(){
		super.initPropsKeys();
		possibleProperties.add(MFAProperties.NULLSPACE_COMPUTESENTITIVITY);
	}
	
	@Override
	public SteadyStateSimulationResult simulate() throws Exception {
		createProblemIfEmpty();
		nullSpaceSolution = nullSpaceMethod.runModel();
		return nullSpaceSolution.convertNullSpaceSolution(model, nullSpaceSolution.getPrincipalSolution());
	}
	
	@Override
	public List<SteadyStateSimulationResult> getAllSolutions() throws Exception{
		List<SteadyStateSimulationResult> sols = new ArrayList<SteadyStateSimulationResult>();
		for(FluxSet s : nullSpaceSolution.getAllSolutions())
			sols.add(nullSpaceSolution.convertNullSpaceSolution(model, s));
		return sols;
	}
}
