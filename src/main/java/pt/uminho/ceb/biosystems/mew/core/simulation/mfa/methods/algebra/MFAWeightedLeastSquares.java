package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra;

import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.Blas;
import cern.colt.matrix.linalg.SeqBlas;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.utils.AlgebraUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/** This class is used to solve MFA problems when the system is overdevermined */
public class MFAWeightedLeastSquares extends MFAClassicAlgebra{
	
    public MFAWeightedLeastSquares(ISteadyStateModel model) {
		super(model);
	}
    

	/* -Gc^+ . Gm . (Gc . Y^-1 . GcT)^-1 . Gc - Y^-1 . vm */
	@Override
	public DoubleMatrix2D solveSystem(DoubleMatrix2D gmTransposeMatrix, 
    		DoubleMatrix2D gcTransposeMatrix, 
    		DoubleMatrix2D fluxMeasureMatrix) throws Exception {
    	
    	Double alpha = null;
		try {
			alpha = getAlpha();
		} catch (Exception e) {e.printStackTrace();}
		
  		Algebra algebra = new Algebra();
    	Blas blas = SeqBlas.seqBlas;
    	 
    	DoubleMatrix2D pIgC = AlgebraUtils.pseudoInverse(gcTransposeMatrix);
    	
    	DoubleMatrix2D redundancyMatrix = calculateRedundancyMatrix(algebra, gmTransposeMatrix, gcTransposeMatrix, pIgC);
    	DoubleMatrix2D reducedRedundancyMatrix = calculateReducedRedundancyMatrix(redundancyMatrix);
    	DoubleMatrix2D meauredFluxVarianceMatrix = buildMeasuredFluxVarianceMatrix(extendedS, vmModelIndexesMap, alpha);
    	DoubleMatrix2D varianceCovarianceMatrix = calculateVarianceCovariance(algebra, reducedRedundancyMatrix, meauredFluxVarianceMatrix);
    	
    	DoubleMatrix2D vmLikelihood = calculateVmBestEstimate(algebra, meauredFluxVarianceMatrix, reducedRedundancyMatrix, varianceCovarianceMatrix, fluxMeasureMatrix);
System.out.println("### VM best estimate:\n" + vmLikelihood.toString());	
    	blas.dscal(-1.0, pIgC);
    	DoubleMatrix2D res = algebra.mult(pIgC, gmTransposeMatrix);
    	res = algebra.mult(res, vmLikelihood);
    	
    	return res;
    }
    
    /** Calculate the m x m variance-covariance matrix for the residuals of the measured fluxes 
     * @param reducedRedundancyMatrix is the reduced (only linearly independent rows) redundancy matrix that conveys the relations between all the measured reaction fluxes.
     * @param meauredFluxVarianceMatrix is an m x m diagonal matrix, called measured flux variance matrix, such that the 
     * element in the i-th row and in the i-th column correspond to the variance of the flux i 
     * Rr . E . Rr_T */
    public DoubleMatrix2D calculateVarianceCovariance(Algebra algebra, DoubleMatrix2D reducedRedundancyMatrix, DoubleMatrix2D meauredFluxVarianceMatrix){
    	DoubleMatrix2D rTimesE = algebra.mult(reducedRedundancyMatrix, meauredFluxVarianceMatrix);
    	DoubleMatrix2D rT = algebra.transpose(reducedRedundancyMatrix);
    	return algebra.mult(rTimesE, rT);
    }
    
    /** Reduce the redundancy matrix in order to contain only its linearly independent rows.
     * For a nonzero matrix in row echelon form, its non zero rows are always linearly independent.
     * If the transformed matrix contains rows with only zero values, it means that the corresponding rows in the 
	 * original matrix are linearly dependent */
    public DoubleMatrix2D calculateReducedRedundancyMatrix(DoubleMatrix2D redundancyMatrix){
    	EchelonTransformation echelon = new EchelonTransformation(redundancyMatrix.toArray());
    	echelon.transform();
    	double[][] reducedMatrix = echelon.getLinearlyIndependentRowsMatrix();
    	return new DenseDoubleMatrix2D(reducedMatrix);
    }
    
    /** R = Sm - (Sc . Sc^+) . Sm 
     * R is the redundancy matrix that conveys the relations between all the measured reaction fluxes. A zero column in R 
     * indicates that the other measured rates do not have significant expression for the corresponding measured flux, i.e.,
     *  it is not balanceable. Thus, the columns of matrix R can be inspected to identify the columns of the matrix R that 
     *  can be used to distinguish between the balanceable reaction rates from the non- balanceable ones. */
    public DoubleMatrix2D calculateRedundancyMatrix(Algebra algebra, DoubleMatrix2D gM, DoubleMatrix2D gC, DoubleMatrix2D pIgC){
    	DoubleMatrix2D gcTimespIgC = algebra.mult(gC, pIgC);
    	DoubleMatrix2D gcTimespIgcTimesGm = algebra.mult(gcTimespIgC, gM);
    	return AlgebraUtils.matrixSubtraction(gM, gcTimespIgcTimesGm);
    }
    
    /** Builds a diagonal variance matrix of the measured fluxes
     * @return mxm square matrix with the flux measurements variances in the diagonal */
    public DoubleMatrix2D buildMeasuredFluxVarianceMatrix(DoubleMatrix2D stoichiometricMatrix, Map<Integer,Pair<Double, Double>> vmModelIndexesMap){
    	
    	double[][] varianceMatrix = new double[vmModelIndexesMap.size()][vmModelIndexesMap.size()];
    	int vmIndex = 0;
    	// Keep the order that the fluxes occur in the stoichiometric matrix
    	for(int fModelI=0; fModelI<stoichiometricMatrix.columns(); fModelI++)
    		if(vmModelIndexesMap.containsKey(fModelI))
    			varianceMatrix[vmIndex][vmIndex++] = vmModelIndexesMap.get(fModelI).getB();
    	
    	return new DenseDoubleMatrix2D(varianceMatrix);
    }
    
    /** Builds a diagonal matrix, but instead having the variances of the measurements, the values in the diagonal are equal
     * to a given alpha value times the flux measurement powered to 2 = (a*vm)^2
     * @param stoichiometricMatrix model stoichiometric matrix
     * @param alpha a default value to be multiplied to the flux measurements and powered to 2
     * @return mxm square matrix */
    public DoubleMatrix2D buildMeasuredFluxVarianceMatrix(DoubleMatrix2D stoichiometricMatrix, Map<Integer,Pair<Double, Double>> vmModelIndexesMap, double alpha){
    	
    	double[][] varianceMatrix = new double[vmModelIndexesMap.size()][vmModelIndexesMap.size()];
    	int vmIndex = 0;
    	// Keep the order that the fluxes occur in the stoichiometric matrix
    	for(int fModelI=0; fModelI<stoichiometricMatrix.columns(); fModelI++)
    		if(vmModelIndexesMap.containsKey(fModelI))
    		{
    			double vm = vmModelIndexesMap.get(fModelI).getA();
    			if(vm==0)
    				vm = alpha;
    			varianceMatrix[vmIndex][vmIndex++] = Math.pow(alpha * vm, 2);
    		}
    	
    	return new DenseDoubleMatrix2D(varianceMatrix);
    }
    
    /** Calculates the maximum likelihood weighted estimation of the measured fluxes
     * (Sc.Y^-1.ScT)^-1.Sc.Y^-1.vm, where Y is the variance-covariance matrix for the residuals of the measured fluxes */
    protected DoubleMatrix2D maximumLikelihood_BCK(Algebra algebra, DoubleMatrix2D gC, DoubleMatrix2D varianceCovarianceMatrix, DoubleMatrix2D vm){
    	DoubleMatrix2D vCvI = algebra.inverse(varianceCovarianceMatrix);
    	DoubleMatrix2D gcTimesVcvI = algebra.mult(gC, vCvI); // to reuse in the expression, since matrix multiplication is associative
    	DoubleMatrix2D gcT = algebra.transpose(gC);
    	DoubleMatrix2D gcTimesVcvITimeGcT = algebra.mult(gcTimesVcvI, gcT);
    	DoubleMatrix2D result = algebra.inverse(gcTimesVcvITimeGcT);
    	result = algebra.mult(result, gcTimesVcvI);
    	return algebra.mult(result, vm);
    }
    
    /** Calculates the the best estimation of the measured fluxes
     * The best estimate of measured rates have a smaller standard deviation than the raw measurements and the estimate therefore is likely to be 
     * more reliable than the measured data.
     * (I - E . Rr_T . Y^-1 . Rr) . vm, where Y is the variance-covariance matrix for the residuals of the measured fluxes */
    public DoubleMatrix2D calculateVmBestEstimate(Algebra algebra,
    		DoubleMatrix2D meauredFluxVarianceMatrix, 
    		DoubleMatrix2D reducedRedundancyMatrix, 
    		DoubleMatrix2D varianceCovarianceMatrix, 
    		DoubleMatrix2D vm){
    	
//    	DoubleMatrix2D vCvI = algebra.inverse(varianceCovarianceMatrix);
    	DoubleMatrix2D vCvI = varianceCovarianceMatrix;
    	DoubleMatrix2D rRT = algebra.transpose(reducedRedundancyMatrix);
    	
    	DoubleMatrix2D result = algebra.mult(meauredFluxVarianceMatrix, rRT); 
    	result = algebra.mult(result, vCvI);
    	result = algebra.mult(result, reducedRedundancyMatrix);
    	
    	DoubleMatrix2D identity = AlgebraUtils.buildIdentityMatrix(result.rows());
    	result = AlgebraUtils.matrixSubtraction(identity, result);

    	return algebra.mult(result, vm);
    }
    
	
	/** Used when the system is overdetermined and the select fitting is the Weighted LSQ) */
	public Double getAlpha() throws PropertyCastException, MandatoryPropertyException{
		Double alpha = ManagerExceptionUtils.testCast(propreties, Double.class, MFAProperties.MFA_ALGEBRA_WLSQ_ALPHA, true); 
		return alpha;
	}
}
