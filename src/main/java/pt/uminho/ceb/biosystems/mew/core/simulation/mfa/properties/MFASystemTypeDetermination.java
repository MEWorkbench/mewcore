package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFAAlgebraStoichConversions;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra.MFAClassicAlgebra;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.ratioconstraints.FluxRatioConstraintList;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.utils.ThreeTuple;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class MFASystemTypeDetermination {
	
	public static final int DEGREES_OF_FREEDOM_THRESHOLD = 10;
	
	public static int getNumberOfBalanceEquations(ISteadyStateModel model){
		return model.getNumberOfMetabolites();
	}
	
//	public static int getNumberOfFreedomDegrees(ISteadyStateModel model, FluxRatioConstraintList ratioConstraints){
//		int degreesOfFreedom = model.getNumberOfReactions() - getNumberOfBalanceEquations(model);
//		if(ratioConstraints!=null)
//			degreesOfFreedom -= ratioConstraints.numberOfEqualityRatios();
//	    return degreesOfFreedom;
//	}
	
//	public static MFASystemType getSystemType(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints){
//	    int numberOfFluxMeasures = (measuredFluxes==null) ? 0 : measuredFluxes.size();
//	    int numberOfFreedomDegrees = getNumberOfFreedomDegrees(model, ratioConstraints);
//	    
//	    if (numberOfFluxMeasures < numberOfFreedomDegrees)
//	           return MFASystemType.underdetermined;
//	    
//	    if (numberOfFluxMeasures == numberOfFreedomDegrees)
//            return MFASystemType.determined;
//	    
//        // numberOfFluxMeasures > numberOfFreedomDegrees
//	    return MFASystemType.overdetermined;
//	}
	
//	public static MFASystemType getSystemType(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints, List<?> knockouts){
//	    int numberOfFluxMeasures = 0;
//	    if(measuredFluxes!=null)
//	    	numberOfFluxMeasures += measuredFluxes.size();
//	    if(knockouts!=null)
//	    	numberOfFluxMeasures += knockouts.size();
//	    
//	    int numberOfFreedomDegrees = getNumberOfFreedomDegrees(model, ratioConstraints);
//	    
//	    if (numberOfFluxMeasures < numberOfFreedomDegrees)
//	           return MFASystemType.underdetermined;
//	    
//	    if (numberOfFluxMeasures == numberOfFreedomDegrees)
//            return MFASystemType.determined;
//	    
//        // numberOfFluxMeasures > numberOfFreedomDegrees
//	    return MFASystemType.overdetermined;
//	}
	
	/** The degrees of freedom is obtained from the difference between the rank of Gc and the number of unknwown fluxes */
	public static int getNumberOfFreedomDegrees(ISteadyStateModel model, FluxRatioConstraintList ratioConstraints) throws Exception{
		return getSystemTypeAndDegrees(model, null, ratioConstraints, null).getB();
	}
	
	/** The type of the system is obtained from the comparison of the rank of the Gc matrix, with the number of unknown fluxes in the system (columns of Gc).
	 * If the rank is less than the number of columns, than it is not possible to invert the matrix Gc, and the system is under-determined.
	 * Otherwise if the number of rows of Gc is equal to the number of its columns (square matrix) the system is determined (same number of equations and unknowns)
	 * If the number of rows is grater than the number of columns, than the system is overdetermined.*/
	public static MFASystemType getSystemType(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints) throws Exception{
		return getSystemTypeAndDegrees(model, measuredFluxes, ratioConstraints, null).getA();
	}
	
	/** The type of the system is obtained from the comparison of the rank of the Gc matrix, with the number of unknown fluxes in the system (columns of Gc).
	 * If the rank is less than the number of columns, than it is not possible to invert the matrix Gc, and the system is under-determined.
	 * Otherwise if the number of rows of Gc is equal to the number of its columns (square matrix) the system is determined (same number of equations and unknowns)
	 * If the number of rows is grater than the number of columns, than the system is overdetermined.*/
	public static MFASystemType getSystemType(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints, List<String> knockouts) throws Exception{
		return getSystemTypeAndDegrees(model, measuredFluxes, ratioConstraints, knockouts).getA();
	}
	
	
////	FIXME: Ver o que se passa aqui, e ainda necessario?
//	public static Pair<MFASystemType, Integer> getSystemTypeAndDegress(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints, List<String> knockouts) throws Exception{
//		MFASystemType st = getSystemType(model, measuredFluxes, ratioConstraints);
//		return Pair.createPair(st, 0);
//	}

	
	/** @return <System type, Degrees of freedom, Are real degrees of freedom?> 
	 * 
	 * The type of the system is obtained from the comparison of the rank of the Gc matrix, with the number of unknown fluxes in the system (columns of Gc).
	 * If the rank is less than the number of columns, than it is not possible to invert the matrix Gc, and the system is under-determined.
	 * Otherwise if the number of rows of Gc is equal to the number of its columns (square matrix) the system is determined (same number of equations and unknowns)
	 * If the number of rows is grater than the number of columns, than the system is overdetermined. 
	 * 
	 * The degrees of freedom is obtained from the difference between the rank of Gc and the number of unknwown fluxes */
	public static ThreeTuple<MFASystemType, Integer, Boolean> getSystemTypeAndDegrees(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints) throws Exception{
	    return getSystemTypeAndDegrees(model, measuredFluxes, ratioConstraints, null);
	}
	
	/** @return <System type, Degrees of freedom> 
	 * 
	 * The type of the system is obtained from the comparison of the rank of the Gc matrix, with the number of unknown fluxes in the system (columns of Gc).
	 * If the rank is less than the number of columns, than it is not possible to invert the matrix Gc, and the system is under-determined.
	 * Otherwise if the number of rows of Gc is equal to the number of its columns (square matrix) the system is determined (same number of equations and unknowns)
	 * If the number of rows is grater than the number of columns, than the system is overdetermined. 
	 * 
	 * The degrees of freedom is obtained from the difference between the rank of Gc and the number of unknwown fluxes */
	public static ThreeTuple<MFASystemType, Integer, Boolean> getSystemTypeAndDegrees(ISteadyStateModel model, ExpMeasuredFluxes measuredFluxes, FluxRatioConstraintList ratioConstraints, List<String> knockouts) throws Exception{
		Set<String> vmSet = new TreeSet<String>();
		if(measuredFluxes!=null)
			vmSet.addAll(measuredFluxes.keySet());
		if(knockouts!=null)
			vmSet.addAll(knockouts);
		
		Pair<MFASystemType, Integer> degrees = getTheorethicalDegreesOfFreedom(model, ratioConstraints, vmSet);
		
		if(degrees.getB() > DEGREES_OF_FREEDOM_THRESHOLD)
			return new ThreeTuple<MFASystemType, Integer, Boolean>(degrees.getA(), degrees.getB(), false);
		
		degrees = getRealSystemTypeAndDegrees(model, ratioConstraints, vmSet);
		return new ThreeTuple<MFASystemType, Integer, Boolean>(degrees.getA(), degrees.getB(), true);
	}
	
	
	
	/** Calculate the real number of degrees of the system, based on the rank of the Gc matrix and the number of columns in the Gm matrix
	  * @return The returned degrees of freedom is already the difference between the degrees of freedom and the measurements
	 * so that the result is not actually the degrees of freedom, but the difference between the degrees and measurements necessary to turn the system determined. */
	public static Pair<MFASystemType, Integer> getRealSystemTypeAndDegrees(ISteadyStateModel model, FluxRatioConstraintList ratioConstraints, Set<String> vmSet){
		DoubleMatrix2D stoichiometricMatrix = MFAAlgebraStoichConversions.addRatiosToStoichiometricMatrix(model, ratioConstraints).convertToColt();
		int nMeasurements = vmSet.size();
		int gCnColumns = stoichiometricMatrix.columns() - nMeasurements;

		DoubleMatrix2D gCmatrix = new DenseDoubleMatrix2D(stoichiometricMatrix.rows(), gCnColumns);
		List<Integer> vcModelIndexes = new ArrayList<Integer>();
		int gCi=0;

		for(int fModelI=0; fModelI<stoichiometricMatrix.columns(); fModelI++)
			if(!vmSet.contains(model.getReactionId(fModelI)))
			{
				MFAClassicAlgebra.copyMatrixColumnValues(stoichiometricMatrix, gCmatrix, fModelI, gCi++);
				vcModelIndexes.add(fModelI);
			}
		
		Algebra algebra = new Algebra();

		// If the matrix is an nXm matrix, with n<m, than the system is underdetermined. However for such matrix it is not possible to calculate its rank.
		// However, the row rank of a matrix is equal to its column rank (rank = row rank = column rank), so the rank of Gc can be obtained by the rank of its transpose
		if(gCmatrix.rows() < gCmatrix.columns())
		{
			DoubleMatrix2D gcT = algebra.transpose(gCmatrix);
			int degrees = gCnColumns - algebra.rank(gcT);
			return new Pair<MFASystemType, Integer>(MFASystemType.underdetermined, degrees);
		}

		int degrees = gCnColumns - algebra.rank(gCmatrix);
	
		// If the Gc matrix is rank deficient (rank < unknown fluxes (columns))
		if (degrees > 0)
	        return new Pair<MFASystemType, Integer>(MFASystemType.underdetermined, degrees);
		
		// The Gc matrix is full rank and square
		if(gCmatrix.rows() == gCmatrix.columns())
			return new Pair<MFASystemType, Integer>(MFASystemType.determined, degrees);
		
		// The Gc matrix is full rank but a mxn matrix, with m>n
		return new Pair<MFASystemType, Integer>(MFASystemType.overdetermined, degrees);
	}
	
	/** Calculate the real number of degrees of the system, based on the rank of the Gc matrix and the number of columns in the Gm matrix
	  * @return The returned degrees of freedom is already the difference between the degrees of freedom and the measurements
	 * so that the result is not actually the degrees of freedom, but the difference between the degrees and measurements necessary to turn the system determined. */
	public static Pair<MFASystemType, Integer> getRealSystemTypeAndDegrees(ISteadyStateModel model, FluxRatioConstraintList ratioConstraints, ExpMeasuredFluxes measuredFluxes, List<String> knockouts){
		Set<String> vmSet = new TreeSet<String>();
		if(measuredFluxes!=null)
			vmSet.addAll(measuredFluxes.keySet());
		if(knockouts!=null)
			vmSet.addAll(knockouts);
		
		return getRealSystemTypeAndDegrees(model, ratioConstraints, vmSet);
	}
	
	/** Calculate the theoretical number of degrees of the system, by comparing the number of unknown fluxes with the number of measurements, the number of equality 
	 * ratio constraints and the number of balance equations. 
	 * @param vmSet contains the set of flux measurements and reaction knockouts 
	 * @return The returned degrees of freedom is already the difference between the degrees of freedom and the measurements
	 * so that the result is not actually the degrees of freedom, but the difference between the degrees and measurements necessary to, theoretically, turn the system determined. */
	public static Pair<MFASystemType, Integer> getTheorethicalDegreesOfFreedom(ISteadyStateModel model, FluxRatioConstraintList ratioConstraints, Set<String> vmSet){
		int degreesOfFreedom = model.getNumberOfReactions() - getNumberOfBalanceEquations(model);
		if(ratioConstraints!=null)
			degreesOfFreedom -= ratioConstraints.numberOfEqualityRatios();
		degreesOfFreedom -= vmSet.size();
		
		MFASystemType systemType;
		
		if(degreesOfFreedom > 0)
			systemType = MFASystemType.underdetermined;
	    else if(degreesOfFreedom == 0)
			systemType = MFASystemType.determined;
	    else
	    	systemType = MFASystemType.overdetermined;
		
	    return new Pair<MFASystemType, Integer>(systemType, degreesOfFreedom);
	}
	
	public static int getDegreesOfFreedomThreshold(){
		return DEGREES_OF_FREEDOM_THRESHOLD;
	}
}
