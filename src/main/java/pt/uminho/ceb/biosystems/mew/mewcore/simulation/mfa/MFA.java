/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.ILPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.solvers.qp.IQPSolver;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.qp.QPProblemRow;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.IOverrideReactionBounds;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.OverrideSteadyStateModel;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.Blas;
import cern.colt.matrix.linalg.SeqBlas;
import cern.colt.matrix.linalg.SingularValueDecomposition;


//NOTE: Gtranspose == stoichiometricMatrix 

public class MFA implements IMFASolver {
    protected ISteadyStateModel model;
    protected FluxValueMap fluxMeasureList;
    protected LPObjectiveFunction objectiveFunction;
    protected Boolean isMaximization;
    protected DoubleMatrix2D measuredFluxVarianceMatrix;
    protected boolean isUsingQP;
    protected EnvironmentalConditions env = null;
    protected SolverType solverType = SolverType.CLP;
    
    private List<Integer> listIndexes;

    public MFA(ISteadyStateModel model, FluxValueMap fluxMeasures) {
        this.model = model;
        this.fluxMeasureList = fluxMeasures;
    }

    public MFA(ISteadyStateModel model, FluxValueMap fluxMeasureList, LPObjectiveFunction objectiveFunction, boolean isMaximization) throws Exception {
        this.model = model;
        this.fluxMeasureList = fluxMeasureList;
        this.listIndexes = fluxMeasureList.getIndexesList(model);
        this.isMaximization = isMaximization;
        this.objectiveFunction = objectiveFunction;
    }
    
    public MFA(ISteadyStateModel model, FluxValueMap fluxMeasureList, LPObjectiveFunction objectiveFunction, 
    		boolean isMaximization, boolean isUsingQP, EnvironmentalConditions envConditions) throws Exception {
        this.model = model;
        this.fluxMeasureList = fluxMeasureList;
        this.listIndexes = fluxMeasureList.getIndexesList(model);
        this.isMaximization = isMaximization;
        this.objectiveFunction = objectiveFunction;
        this.isUsingQP = isUsingQP;
        this.env = envConditions;
    }

    public void setObjectiveFunction(LPObjectiveFunction objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public void setIsMaximization(boolean isMaximization) {
        this.isMaximization = isMaximization;
    }

    public LPObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    public boolean isMaximization() {
        return isMaximization;
    }
    
    public void setSolverType(SolverType solverType) {
    	this.solverType = solverType;
    }
    
    public SolverType getSolverType() { return this.solverType; }

    @Override
    public MFASolution solve() throws Exception {
        DoubleMatrix2D stoichiometricMatrix = model.getStoichiometricMatrix().convertToColt();

        int numberOfBalanceEquations = model.getNumberOfMetabolites();
        int numberOfReactions = model.getNumberOfReactions();
        int numberOfFluxMeasures = fluxMeasureList.size();
        int numberOfFreedomDegrees = numberOfReactions - numberOfBalanceEquations;
        MFASolution mfaSolution = new MFASolution(fluxMeasureList);
        
        if(isUsingQP){
        	LPSolution qpResult = solveSystemWithQP(stoichiometricMatrix,mfaSolution);
            mfaSolution.setResultType(MFAResultType.QP_SOLUTION);
            mfaSolution.setQpSystemSolution(qpResult);
        }else{

	        if (numberOfFluxMeasures == numberOfFreedomDegrees) {
	            FluxValueMap determinedResult = solveDeterminedSystem(stoichiometricMatrix);
	            mfaSolution.setResultType(MFAResultType.DETERMINED_SYSTEM);
	            mfaSolution.setDeterminedSystemSolution(determinedResult);
	        }
	
	        if (numberOfFluxMeasures < numberOfFreedomDegrees){
	            LPSolution underDeterminedResult = solveUnderDeterminedSystem(stoichiometricMatrix,mfaSolution);
	            mfaSolution.setResultType(MFAResultType.UNDERDETERMINED_SYSTEM);
	            mfaSolution.setUnderDeterminedMFASolution(underDeterminedResult);
	        }
	
	        if (numberOfFluxMeasures > numberOfFreedomDegrees && measuredFluxVarianceMatrix == null ){
	            FluxValueMap overDeterminedResult = solveOverDeterminedSystemLSQ(stoichiometricMatrix);
	            mfaSolution.setResultType(MFAResultType.OVERDETERMINED_SYSTEM_LSQ);
	            mfaSolution.setOverDeterminedSolution(overDeterminedResult);
	        }
	        
	        if (numberOfFluxMeasures > numberOfFreedomDegrees && measuredFluxVarianceMatrix != null ){
	            FluxValueMap overDeterminedResult = solveOverDeterminedSystemWLSQ(stoichiometricMatrix);
	            mfaSolution.setResultType(MFAResultType.OVERDETERMINED_SYSTEM_WLSQ);
	            mfaSolution.setOverDeterminedSolution(overDeterminedResult);
	        }
        }
        

        return mfaSolution;
    }

    
    private FluxValueMap solveOverDeterminedSystemWLSQ(DoubleMatrix2D stoichiometricMatrix) throws AlreadyPresentFluxMeasureException {
    	Algebra algebra = new Algebra();
    	//DoubleMatrix2D fluxMeasureMatrix = buildFluxMeasureMatrix();
    	//DoubleMatrix2D  fluxMeasureMatrixTranspose = algebra.transpose(fluxMeasureMatrix); TODO: ver se isto e necessario
    	DoubleMatrix2D gcTransposeMatrix = buildGcTransposeMatrix(stoichiometricMatrix);
    	DoubleMatrix2D gcMatrix = algebra.transpose(gcTransposeMatrix);
        DoubleMatrix2D gmTransposeMatrix = buildGmTransposeMatrix(stoichiometricMatrix);
        
        DoubleMatrix2D redundancyMatrix = calculateRedundacyMatrix(gmTransposeMatrix,gcTransposeMatrix);
        DoubleMatrix2D varianceCovarianceMatrix  = calculateVarianceCovarianceMatrix(redundancyMatrix);
        DoubleMatrix2D gcTimesVarianceCovarianceMatrix = algebra.mult(gcMatrix,varianceCovarianceMatrix);
        DoubleMatrix2D gcTimesVarianceCovarianceTimesGcTransposeMatrix = algebra.mult(gcTimesVarianceCovarianceMatrix,gcTransposeMatrix);
		DoubleMatrix2D invernseMatrix  = Matrix.inverse(gcTimesVarianceCovarianceTimesGcTransposeMatrix);
		DoubleMatrix2D inverseTimesGcMatrix = algebra.mult(invernseMatrix,gcMatrix);
		DoubleMatrix2D inverseGcTimesVarianceCovarianceMatrix = algebra.mult(inverseTimesGcMatrix,measuredFluxVarianceMatrix);
		DoubleMatrix2D inverseGCVarianceCovarianceTimesmeasuredStoichiometricMatrix = algebra.mult(inverseGcTimesVarianceCovarianceMatrix,gmTransposeMatrix);
		DoubleMatrix2D resultMatrix = algebra.mult(gmTransposeMatrix,inverseGCVarianceCovarianceTimesmeasuredStoichiometricMatrix);
		//DoubleMatrix2D consistencyIndex = calculateConsistencyIndex(redundancyMatrix,fluxMeasureMatrix,varianceCovarianceMatrix);
        return createResult(resultMatrix);
	}

//	private DoubleMatrix2D calculateConsistencyIndex(DoubleMatrix2D redundancyMatrix, DoubleMatrix2D fluxMeasureMatrix, DoubleMatrix2D varianceCovarianceMatrix) {
//		Algebra algebra = new Algebra();
//		DoubleMatrix2D errorMatrix = algebra.mult(redundancyMatrix,fluxMeasureMatrix);
//		DoubleMatrix2D errorTransposeMatrix = algebra.transpose(errorMatrix);
//		DoubleMatrix2D errorTransposeTimesVarianceCovarianceMatrix = algebra.mult(errorTransposeMatrix,varianceCovarianceMatrix);
//		DoubleMatrix2D resultMatrix = algebra.mult(errorTransposeTimesVarianceCovarianceMatrix,errorMatrix) ;
//		return resultMatrix;
//	}

	private DoubleMatrix2D calculateVarianceCovarianceMatrix(DoubleMatrix2D redundacyMatrix) {
		Algebra algebra = new Algebra();
		DoubleMatrix2D redundancyTransposeMatrix = algebra.transpose(redundacyMatrix);
		DoubleMatrix2D redundancyMatrixTimesMeasureVarianceMatrix  = algebra.mult(redundacyMatrix,measuredFluxVarianceMatrix);
		DoubleMatrix2D resultMatrix = algebra.mult(redundancyMatrixTimesMeasureVarianceMatrix,redundancyTransposeMatrix);
		return resultMatrix;
	}

	private DoubleMatrix2D calculateRedundacyMatrix(DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D gcTransposeMatrix) {
		Algebra algebra = new Algebra();
    	//DoubleMatrix2D gmMatrix = algebra.transpose(gmTransposeMatrix);
		//DoubleMatrix2D gcMatrix = algebra.transpose(gcTransposeMatrix);
		DoubleMatrix2D gcPseudoInverseMatrix  =  Matrix.inverse(gcTransposeMatrix);//pseudoInverse(gcMatrix);;
		DoubleMatrix2D gcMatrixTimesgcPseudoInverse = algebra.mult(gcTransposeMatrix,gcPseudoInverseMatrix);
		DoubleMatrix2D gcMatrixAuxiliarTimesgMeasured = algebra.mult(gcMatrixTimesgcPseudoInverse,gmTransposeMatrix);
		DoubleMatrix2D result = matrixSubtraction(gmTransposeMatrix,gcMatrixAuxiliarTimesgMeasured);
		
		return result;
	}
	
	//TODO esta com o minimo dos quadrados
    //vc - -(pseudoInverse)*GmTranspose*Vmeasured
    protected FluxValueMap solveOverDeterminedSystemLSQ(DoubleMatrix2D stoichiometricMatrix) throws Exception {
    	DoubleMatrix2D gcTransposeMatrix = buildGcTransposeMatrix(stoichiometricMatrix);
        DoubleMatrix2D gmTransposeMatrix = buildGmTransposeMatrix(stoichiometricMatrix);
        DoubleMatrix2D fluxMeasureMatrix = buildFluxMeasureMatrix();
        //DoubleMatrix2D gcMatrixPseudoInverse = Matrix.inverse(gcTransposeMatrix);///
        DoubleMatrix2D gcMatrixPseudoInverse  = pseudoInverse(gcTransposeMatrix);///Tirar esta linha ou a de cima
        //DoubleMatrix2D comparationMatrix = pseudoInverse(gcTransposeMatrix);
        Blas blas = SeqBlas.seqBlas;
        blas.dscal(-1, gcMatrixPseudoInverse);
        DoubleMatrix2D systemSolutionMatrix = calculateLeastSquares(gcMatrixPseudoInverse, gmTransposeMatrix, fluxMeasureMatrix);
        //DoubleMatrix2D redundacyMatrix = calculateRedundacyMatrix(gmTransposeMatrix,gcTransposeMatrix);
        //Algebra algebra = new Algebra();
        //DoubleMatrix2D errorEstimation = algebra.mult(redundacyMatrix,fluxMeasureMatrix);
        return createResult(systemSolutionMatrix);
    }

	private DoubleMatrix2D matrixSubtraction(DoubleMatrix2D matrix1, DoubleMatrix2D matrix2) {
		int numberOfRows = matrix1.rows();
		int numberOfColumns = matrix1.columns();
		DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(numberOfRows,numberOfColumns);  
		
		for(int i = 0;i < numberOfRows;i++)
			for(int j = 0; j < numberOfColumns;j++){
				double value = matrix1.get(i,j) - matrix2.get(i,j);
				resultMatrix.set(i,j, value);
			}
		
		return resultMatrix;
	}

	protected DoubleMatrix2D pseudoInverse(DoubleMatrix2D gcTransposeMatrix) {
        Algebra algebra = new Algebra();
        DoubleMatrix2D result = algebra.inverse(gcTransposeMatrix);
        return result;
    }

    protected DoubleMatrix2D calculateLeastSquares(DoubleMatrix2D gcMatrixPseudoInverse, DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D fluxMeasureMatrix) {
        Algebra algebra = new Algebra();
        DoubleMatrix2D pseudoInverseMultplygmTransposeMatrix = algebra.mult(gcMatrixPseudoInverse, gmTransposeMatrix);
        DoubleMatrix2D result = algebra.mult(pseudoInverseMultplygmTransposeMatrix, fluxMeasureMatrix);
        return result;
    }

    protected LPSolution solveUnderDeterminedSystem(DoubleMatrix2D stoichiometricMatrix, MFASolution solution) throws Exception {

    	if ((isMaximization == null) || (objectiveFunction == null))
            throw new InvalidUnderDeterminedSystemParametersException();

    	IOverrideReactionBounds overrideRC = new MeasureFluxOverrideModel(model, fluxMeasureList);
        LPProblem problem = createLinearProgrammingProblem(model, overrideRC);
        ILPSolver solver = solverType.lpSolver(problem);
        LPSolution lpSolution = solver.solve();
        return lpSolution;
    }
    

    protected LPSolution solveSystemWithQP(DoubleMatrix2D stoich, MFASolution solution) throws Exception{

    	if(isMaximization == null) throw new InvalidUnderDeterminedSystemParametersException();

    	IOverrideReactionBounds overrideRC = new OverrideSteadyStateModel(model, env);

        QPProblem problem = createQPProblem(model, overrideRC);
        IQPSolver solver = solverType.qpSolver(problem);
        LPSolution lpSolution = solver.solve();
        return lpSolution;
    	
    }
    
    
    public QPProblem createQPProblem(ISteadyStateModel model, IOverrideReactionBounds overrideRC) 
    		throws LinearProgrammingTermAlreadyPresentException
	{
		QPProblem problem = new QPProblem();
		
		// create variables
		int numberVariables = model.getNumberOfReactions();
		
		for(int i=0; i < numberVariables; i++)
		{
			Reaction r = model.getReaction(i);
			ReactionConstraint rc = overrideRC.getReactionConstraint(i);
			LPVariable var = new LPVariable(r.getId(),rc.getLowerLimit(), rc.getUpperLimit());
			problem.addVariable(var);
		}
		
		// create constraints
		int numberConstraints = model.getNumberOfMetabolites();
		for(int i=0; i < numberConstraints; i++)
		{
			LPProblemRow row = new LPProblemRow();
			for(int j=0; j < numberVariables; j++)
			{
				double value = model.getStoichiometricValue(i, j);
				if (value != 0) row.addTerm(j, value);
			}
			LPConstraint constraint = 
				new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
		}
		
		// create the constant term (offset)
		double offset = 0.0;
		for(int i=0; i < numberVariables; i++){
			String reactionId = model.getReactionId(i);
			if(this.fluxMeasureList.containsKey(reactionId)){
				double value = this.fluxMeasureList.get(reactionId);
				offset += Math.pow(value,2);
			}
		}
		offset *= 0.5;
		problem.setOffset(offset);
		
		// create OF -linear
		LPProblemRow linearOFRow = new LPProblemRow();
		for(int i=0; i < numberVariables ; i++)
		{
			String reactionId = model.getReactionId(i);
			if(this.fluxMeasureList.containsKey(reactionId)){
				double value = this.fluxMeasureList.get(reactionId);
				linearOFRow.addTerm(i, value);
			}else linearOFRow.addTerm(i, 0.0); 
		}
		LPObjectiveFunction objFun = new LPObjectiveFunction(linearOFRow);
		problem.setObjectiveFunction(objFun);
		
		
		// create OF - quadratic
		QPProblemRow quadOFRow = new QPProblemRow();
		for(int i=0; i < numberVariables; i++)
		{
			String reactionId = model.getReactionId(i);
			if(this.fluxMeasureList.containsKey(reactionId)){
				quadOFRow.addTerm(i, 1.0);
			}else quadOFRow.addTerm(i, 0.0);
		}
		QPObjectiveFunction qpObjFun = new QPObjectiveFunction(quadOFRow);
		problem.setQPObjectiveFunction(qpObjFun);
		
		return problem;
	}


    protected LPProblem createLinearProgrammingProblem(ISteadyStateModel modelMapper, IOverrideReactionBounds overrideRC) 
    		throws LinearProgrammingTermAlreadyPresentException {

    	LPProblem problem = new LPProblem();
		
		// create variables
		int numberVariables = modelMapper.getNumberOfReactions();
		for(int i=0; i < numberVariables; i++)
		{
			Reaction r = modelMapper.getReaction(i);
			ReactionConstraint rc = overrideRC.getReactionConstraint(i);
			LPVariable var = new LPVariable(r.getId(),rc.getLowerLimit(), rc.getUpperLimit());
			problem.addVariable(var);
		}
		
		// create constraints
		int numberConstraints = modelMapper.getNumberOfMetabolites();
		for(int i=0; i < numberConstraints; i++)
		{
			LPProblemRow row = new LPProblemRow();
			for(int j=0; j < numberVariables; j++)
			{
				double value = modelMapper.getStoichiometricValue(i, j);
				if (value != 0) row.addTerm(j, value);
			}
			LPConstraint constraint = 
				new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
		}
		
		problem.setObjectiveFunction(objectiveFunction);
		
		return problem;
		
	}

	//TODO retorna os valores dos fluxos e respectivos indices calculados
    //vc = -(GcTranspose)^-1 * GmTranspose*vm
    protected FluxValueMap solveDeterminedSystem(DoubleMatrix2D stoichiometricMatrix) throws AlreadyPresentFluxMeasureException {
        DoubleMatrix2D gcTransposeMatrix = buildGcTransposeMatrix(stoichiometricMatrix);
        DoubleMatrix2D gmTransposeMatrix = buildGmTransposeMatrix(stoichiometricMatrix);
        DoubleMatrix2D fluxMeasureMatrix = buildFluxMeasureMatrix();
        DoubleMatrix2D systemSolutionMatrix = calculateDeterminedSystem(gmTransposeMatrix, gcTransposeMatrix, fluxMeasureMatrix);
        Algebra algebra = new Algebra();
        Blas blas = SeqBlas.seqBlas;
        DoubleMatrix2D inverseGcTransposeMatrix = algebra.inverse(gcTransposeMatrix);
        blas.dscal(-1.0, inverseGcTransposeMatrix);
        //DoubleMatrix2D sensitivityMatrix = algebra.mult(inverseGcTransposeMatrix,gmTransposeMatrix);
        return createResult(systemSolutionMatrix);
    }

    protected FluxValueMap createResult(DoubleMatrix2D systemSolutionMatrix) throws AlreadyPresentFluxMeasureException {

    	int numberOfRows = systemSolutionMatrix.rows();
        FluxValueMap fluxValueList = new FluxValueMap();
        
        int fluxMeasureIndex = 0;
        int currentFluxValueListRowIndex = 0;
        int fluxIndex = 0;

        while (currentFluxValueListRowIndex < numberOfRows) {
        	
        	int fluxCellIndex = listIndexes.get(fluxMeasureIndex);
 
            if (fluxCellIndex != fluxIndex) {           	
            	String id = model.getReactionId(fluxIndex);           	
                fluxValueList.put(id, systemSolutionMatrix.get(currentFluxValueListRowIndex, 0));
                currentFluxValueListRowIndex++;
            } else if (fluxMeasureIndex < (fluxMeasureList.size() - 1))
                fluxMeasureIndex++;

            fluxIndex++;
        }

        return fluxValueList;

    }

    protected DoubleMatrix2D calculateDeterminedSystem(DoubleMatrix2D gmTransposeMatrix, DoubleMatrix2D gcTransposeMatrix, DoubleMatrix2D fluxMeasureMatrix) {
        Algebra algebra = new Algebra();
        Blas blas = SeqBlas.seqBlas;
        DoubleMatrix2D inverseGcTransposeMatrix = algebra.inverse(gcTransposeMatrix);
        blas.dscal(-1.0, inverseGcTransposeMatrix);
        DoubleMatrix2D multiplyInverseGcByGmMatrix = algebra.mult(inverseGcTransposeMatrix, gmTransposeMatrix);
        DoubleMatrix2D resultMatrix = algebra.mult(multiplyInverseGcByGmMatrix, fluxMeasureMatrix);
        return resultMatrix;
    }

    protected DoubleMatrix2D buildFluxMeasureMatrix() {
        int numberOfMeasuredFluxes = fluxMeasureList.size();
        DoubleMatrix2D fluxMeasureMatrix = new DenseDoubleMatrix2D(numberOfMeasuredFluxes, 1);

        for (int i = 0; i < numberOfMeasuredFluxes; i++) {
        	int index = listIndexes.get(i);
        	String id = model.getReactionId(index);
            double measuredFluxValue = this.fluxMeasureList.getValue(id);
            fluxMeasureMatrix.set(i, 0, measuredFluxValue);
        }

        return fluxMeasureMatrix;
    }

    protected DoubleMatrix2D buildGcTransposeMatrix(DoubleMatrix2D stoichiometricMatrix) {
        int numberOfMetabolites = stoichiometricMatrix.rows();
        int gmNumberOfColumns = stoichiometricMatrix.columns() - fluxMeasureList.size();
        DoubleMatrix2D gmMatrix = new DenseDoubleMatrix2D(numberOfMetabolites, gmNumberOfColumns);
        int fluxMeasureIndex = 0;
        int currentStoichiometricColumnIndex = 0;
        int currentGmMatrixColumnIndex = 0;
        while (currentGmMatrixColumnIndex < gmNumberOfColumns) {
        	
        	int fluxCellIndex = listIndexes.get(fluxMeasureIndex);
        	
            if (currentStoichiometricColumnIndex != fluxCellIndex) {
                copyMatrixColumnValues(stoichiometricMatrix, gmMatrix, currentStoichiometricColumnIndex, currentGmMatrixColumnIndex);
                currentGmMatrixColumnIndex++;
            } else if (fluxMeasureIndex < (fluxMeasureList.size() - 1))
                fluxMeasureIndex++;
            currentStoichiometricColumnIndex++;
        }

        return gmMatrix;
    }


    protected DoubleMatrix2D buildGmTransposeMatrix(DoubleMatrix2D stoichiometricMatrix) {
        int numebrOfFluxMeasures = fluxMeasureList.size();
        int numberOfMetabolites = stoichiometricMatrix.rows();
        DoubleMatrix2D gcMatrix = new DenseDoubleMatrix2D(numberOfMetabolites, numebrOfFluxMeasures);

      
        for (int i = 0; i < numebrOfFluxMeasures; i++) {        	
        	int fluxIndex = listIndexes.get(i);
            copyMatrixColumnValues(stoichiometricMatrix, gcMatrix, fluxIndex, i);
        }

        return gcMatrix;
    }

    protected void copyMatrixColumnValues(DoubleMatrix2D stoichiometricMatrix, DoubleMatrix2D destinationMatrix, int columnSourceIndex, int columnDestinationIndex) {
        int numberOfMetabolites = stoichiometricMatrix.rows();

        for (int i = 0; i < numberOfMetabolites; i++) {
            double value = stoichiometricMatrix.get(i, columnSourceIndex);
            destinationMatrix.set(i, columnDestinationIndex, value);
        }

    }

    protected double stoichiometricMatrixConditionNumber(DoubleMatrix2D stoichiometricMatrix) {
        SingularValueDecomposition singularValueDecompostion = new SingularValueDecomposition(stoichiometricMatrix);
        return singularValueDecompostion.cond();
    }


}
