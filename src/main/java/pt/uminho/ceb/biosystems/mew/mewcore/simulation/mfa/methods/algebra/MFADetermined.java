package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.methods.algebra;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.exceptions.ClassicAlgebraInverseException;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.Blas;
import cern.colt.matrix.linalg.SeqBlas;

public class MFADetermined extends MFAClassicAlgebra{

	public MFADetermined(ISteadyStateModel model) {
		super(model);
	}
	

	/** Vc = -1 . ((Gc_T)^-1) . Gm_T . Vm  
	 * @throws ClassicAlgebraInverseException */
	@Override
	public DoubleMatrix2D solveSystem(DoubleMatrix2D gmTransposeMatrix, 
			DoubleMatrix2D gcTransposeMatrix, 
			DoubleMatrix2D fluxMeasureMatrix) throws ClassicAlgebraInverseException{
		
		 Algebra algebra = new Algebra();
        Blas blas = SeqBlas.seqBlas;
        
        DoubleMatrix2D inverseGcTransposeMatrix;
        try {
        	 inverseGcTransposeMatrix = algebra.inverse(gcTransposeMatrix);
        } catch (Exception e) {throw new ClassicAlgebraInverseException("An error inverting the <b>Gc Matrix</b> has occurred: <font color=red>" + e.getMessage() + "</font>"); }
        
        blas.dscal(-1.0, inverseGcTransposeMatrix);
        DoubleMatrix2D multiplyInverseGcByGmMatrix = algebra.mult(inverseGcTransposeMatrix, gmTransposeMatrix);
        DoubleMatrix2D resultMatrix = algebra.mult(multiplyInverseGcByGmMatrix, fluxMeasureMatrix);
        //Sensitivity matrix
        //DoubleMatrix2D sensitivityMatrix = calculateDetSystemSensitivity(gmTransposeMatrix, gcTransposeMatrix);
        return resultMatrix;
	}
}
