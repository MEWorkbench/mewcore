package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.algebra;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.Blas;
import cern.colt.matrix.linalg.SeqBlas;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.exceptions.ClassicAlgebraInverseException;

public class MFALeastSquares extends MFAClassicAlgebra{

	public MFALeastSquares(ISteadyStateModel model) {
		super(model);
	}
	
	/** Vc = -1 . ((Gc_T)^+) . Gm_T . Vm  
	 * @throws ClassicAlgebraInverseException */
	@Override
	public DoubleMatrix2D solveSystem(DoubleMatrix2D gmTransposeMatrix, 
			DoubleMatrix2D gcTransposeMatrix, 
			DoubleMatrix2D fluxMeasureMatrix) throws ClassicAlgebraInverseException {
		
		Algebra algebra = new Algebra();
        Blas blas = SeqBlas.seqBlas;
        
        DoubleMatrix2D pInverseGcTmatrix;
        try {
        	pInverseGcTmatrix = pseudoInverse(gcTransposeMatrix);
        } catch (Exception e) {throw new ClassicAlgebraInverseException("An error inverting the <b>Gc Matrix</b> has occurred: <font color=red>" + e.getMessage() + "</font>"); }
        
        blas.dscal(-1.0, pInverseGcTmatrix);
        DoubleMatrix2D multiplyInverseGcByGmMatrix = algebra.mult(pInverseGcTmatrix, gmTransposeMatrix);
        DoubleMatrix2D systemSolutionMatrix = algebra.mult(multiplyInverseGcByGmMatrix, fluxMeasureMatrix);
        
//      DoubleMatrix2D redundacyMatrix = calculateRedundacyMatrix(gmTransposeMatrix,gcTransposeMatrix); // Comentada pelo Rafael, nao estava a ser utilizada
//      Algebra algebra = new Algebra(); // Comentada pelo Rafael, nao estava a ser utilizada
//      DoubleMatrix2D errorEstimation = algebra.mult(redundacyMatrix,fluxMeasureMatrix); // Comentada pelo Rafael, nao estava a ser utilizada
       
        return systemSolutionMatrix;
    }

}
