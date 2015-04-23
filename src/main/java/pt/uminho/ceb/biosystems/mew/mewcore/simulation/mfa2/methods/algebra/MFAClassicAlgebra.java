package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.algebra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.Matrix;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.ExpMeasuredFluxes;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.MFAApproaches;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.utils.AlgebraUtils;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.Blas;
import cern.colt.matrix.linalg.SeqBlas;

/** This class is used to solve MFA problems with algebra calculus, 
 * either the system is determined or overdevermined */
public abstract class MFAClassicAlgebra extends MFAAlgebra{
	
	/** Map<Model Index, <Flux measurement, Variance>> */
	protected Map<Integer,Pair<Double, Double>> vmModelIndexesMap;
	
	/** Extended Stoichiometric matrix with the equality flux ratios */
	protected DoubleMatrix2D extendedS;
	
	
	public MFAClassicAlgebra(ISteadyStateModel model){
		super(model);
	}
	

	public abstract DoubleMatrix2D solveSystem(DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D gcTransposeMatrix, DoubleMatrix2D fluxMeasureMatrix) throws Exception;

	
	public void initFluxMeasureList() throws Exception{
		ExpMeasuredFluxes measuredFluxes = getMeasuredFluxes();
		vmModelIndexesMap = measuredFluxes.getIndexesFluxeValuesErrorMap(model);
	}
    
    /** Configures the Gm[number of metabolites]x[number of measured fluxes], 
     * the Gc[number of metabolites]x[number of nom measured fluxes] matrix,
     * the vm column vector [number of measured fluxes][1] with the measurements,
     * creates a list with the model flux indexes for the vectors in vc
     * Copy every column of the Stoichiometric matrix that corresponds to a measured flux to a new matrix (Gm)
     * Copy every column of the Stoichiometric matrix that do not corresponds to a measured flux to a new matrix (Gc)
     * @return [0] gCmatrix : DoubleMatrix2D , [1] gMmatrix : DoubleMatrix2D , [2] vm : DoubleMatrix2D , [3] vc model indexes : List<Integer> */
    public Object[] configureMatrices(DoubleMatrix2D stoichiometricMatrix){
    	int nMetabolites = stoichiometricMatrix.rows();
    	int nMeasurements = vmModelIndexesMap.size();
    	int gCnColumns = stoichiometricMatrix.columns() - nMeasurements;
    	int gMnColumns = nMeasurements;
    	
    	DoubleMatrix2D gCmatrix = new DenseDoubleMatrix2D(nMetabolites, gCnColumns);
    	DoubleMatrix2D gMmatrix = new DenseDoubleMatrix2D(nMetabolites, gMnColumns);
    	DoubleMatrix2D fluxMeasureMatrix = new DenseDoubleMatrix2D(nMeasurements, 1);
    	List<Integer> vcModelIndexes = new ArrayList<Integer>();
    	
    	int gCi=0, gMi=0;
    	
    	for(int fModelI=0; fModelI<stoichiometricMatrix.columns(); fModelI++)
    	{
    		if(vmModelIndexesMap.containsKey(fModelI))
    		{
    			copyMatrixColumnValues(stoichiometricMatrix, gMmatrix, fModelI, gMi);
    			fluxMeasureMatrix.set(gMi++, 0, vmModelIndexesMap.get(fModelI).getA());
    		}
    		else
    		{
    			copyMatrixColumnValues(stoichiometricMatrix, gCmatrix, fModelI, gCi++);
    			vcModelIndexes.add(fModelI);
    		}
    	}
//  System.out.println("-------------> S "+stoichiometricMatrix.rows()+"x"+stoichiometricMatrix.columns());  	
//  System.out.println("-------------> gCmatrix "+gCmatrix.rows()+"x"+gCmatrix.columns()); 
//  System.out.println("-------------> gMmatrix "+gMmatrix.rows()+"x"+gMmatrix.columns()); 
//  System.out.println("-------------> fluxMeasureMatrix "+fluxMeasureMatrix.rows()+"x"+fluxMeasureMatrix.columns()); 
    	return new Object[]{gMmatrix, gCmatrix, fluxMeasureMatrix, vcModelIndexes};
    }
    
    public static void copyMatrixColumnValues(DoubleMatrix2D stoichiometricMatrix, DoubleMatrix2D destinationMatrix, int columnSourceIndex, int columnDestinationIndex) {
        int numberOfMetabolites = stoichiometricMatrix.rows();

        for (int i = 0; i < numberOfMetabolites; i++)
        {
            double value = stoichiometricMatrix.get(i, columnSourceIndex);
            destinationMatrix.set(i, columnDestinationIndex, value);
        }
    }
		
	protected FluxValueMap createResult(DoubleMatrix2D systemSolutionMatrix, List<Integer> vcModelIndexes) {
		FluxValueMap result = new FluxValueMap();
		
		// Calculated fluxes
		for(int i=0; i<systemSolutionMatrix.rows(); i++)
		{
			int modelFluxIndex = vcModelIndexes.get(i);
			String cfluxId = model.getReactionId(modelFluxIndex);
			result.put(cfluxId, systemSolutionMatrix.get(i, 0));
		}
		
		// Calculated fluxes
		for(Integer modelFluxIndex : vmModelIndexesMap.keySet())
		{
			String mfluxId = model.getReactionId(modelFluxIndex);
			result.put(mfluxId, vmModelIndexesMap.get(modelFluxIndex).getA());
		}
		
		return result;
	}
	

    
    /** Calculates the sensitivity matrix of the determined system = -1 . ((Gc_T)^-1) . Gm_T */
	public DoubleMatrix2D calculateDetSystemSensitivity(DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D gcTransposeMatrix){
		Algebra algebra = new Algebra();
		Blas blas = SeqBlas.seqBlas;
		DoubleMatrix2D inverseGcTransposeMatrix = algebra.inverse(gcTransposeMatrix);
		blas.dscal(-1.0, inverseGcTransposeMatrix);
		DoubleMatrix2D sensitivityMatrix = algebra.mult(inverseGcTransposeMatrix,gmTransposeMatrix);
		return sensitivityMatrix;
	}

    
    /** Redundancy Matrix = Gm - ( Gc . Gc^* . Gm) */
    public DoubleMatrix2D calculateRedundacyMatrix(DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D gcTransposeMatrix) {
 		Algebra algebra = new Algebra();
     	//DoubleMatrix2D gmMatrix = algebra.transpose(gmTransposeMatrix);
 		//DoubleMatrix2D gcMatrix = algebra.transpose(gcTransposeMatrix);
 		DoubleMatrix2D gcPseudoInverseMatrix = Matrix.inverse(gcTransposeMatrix);//pseudoInverse(gcMatrix);;
 		DoubleMatrix2D gcMatrixTimesgcPseudoInverse = algebra.mult(gcTransposeMatrix,gcPseudoInverseMatrix);
 		DoubleMatrix2D gcMatrixAuxiliarTimesgMeasured = algebra.mult(gcMatrixTimesgcPseudoInverse,gmTransposeMatrix);
 		DoubleMatrix2D result = AlgebraUtils.matrixSubtraction(gmTransposeMatrix,gcMatrixAuxiliarTimesgMeasured);
 		
 		return result;
 	}
    
        
    /** @return Returns the inverse or pseudo-inverse of matrix.
     * Inverse(matrix) if the matrix is square, pseudoinverse otherwise: ((M. T^T))^-1) . M ) */
    public DoubleMatrix2D pseudoInverse(DoubleMatrix2D gcTransposeMatrix) {
        Algebra algebra = new Algebra();
        DoubleMatrix2D result = algebra.inverse(gcTransposeMatrix);
        return result;
    }

	
	public MFAClassicAlgebraResult convertToSimulationResult(FluxValueMap fluxValues){
		return new MFAClassicAlgebraResult(model, MFAApproaches.classicAlgebra.getPropertyDescriptor(), fluxValues);
	}

	@Override
	public SteadyStateSimulationResult simulate() throws Exception  {
		DoubleMatrix2D systemSolutionMatrix = null;
		initFluxMeasureList();
		this.extendedS = addRatiosToStoichiometricMatrix().convertToColt();
		Object[] data = configureMatrices(extendedS);
		DoubleMatrix2D gM = (DoubleMatrix2D) data[0];
		DoubleMatrix2D gC = (DoubleMatrix2D) data[1];
		DoubleMatrix2D vm = (DoubleMatrix2D) data[2];
		@SuppressWarnings("unchecked")
		List<Integer> vcModelIndexes = (List<Integer>) data[3];
		
		systemSolutionMatrix = solveSystem(gM, gC, vm);
		return convertToSimulationResult(createResult(systemSolutionMatrix, vcModelIndexes));
	}
	
	public void setVmModelIndexesMap(Map<Integer,Pair<Double, Double>> vmModelIndexesMap){
		this.vmModelIndexesMap = vmModelIndexesMap;
	}
	
}
