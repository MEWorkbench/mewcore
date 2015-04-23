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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.nullspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// MFA class
// Handles over-, under- & determined case
// Unique features: deals explicitly with the singular case
// Provides interface to include directionality using QP
//
// copyright Marcellinus.Pont@usa.dupont.com

public class NullSpaceMethod implements Serializable{

	private static final long serialVersionUID = 8799720207800374023L;
	
	protected boolean computeSensitivity = false;
	protected boolean calculateAlternativeFluxes = false;
	protected boolean[] isMeasured;
	protected double[] measuredFluxes;
	protected double[][] stoichiometricMatrix;
	protected double[][] K;
	protected double[][] sensitivityMatrix;
	
	protected double[] fluxPrincipal;
	
	/* Variables related to the KmeasuredT matrix: */
	protected double[][] KmeasuredT;
	protected int[] rowPermutation;
	protected int[] colPermutation;
	protected double[][] LT;
	/** Contains the rank-first rows of U */
	protected double[][] U_smaller;
	/** Invertible leading matrix */
	protected double[][]U_blockT;
	protected double[][] U_smallerT;
	/** The rank is always smaller or equal to number of measurements and always smaller or equal to the unknowns */
	protected int rankKmT;
	
	

	public NullSpaceMethod(double[][] stoichiometricMatrix, boolean computeSensitivity, boolean calculateAlternativeFluxes, boolean[] isMeasured,double[] measuredFluxes) throws Exception {
		this.computeSensitivity = computeSensitivity;
		this.calculateAlternativeFluxes = calculateAlternativeFluxes;
		this.isMeasured = isMeasured;
		this.measuredFluxes = measuredFluxes;
		this.stoichiometricMatrix = stoichiometricMatrix;
		
		System.out.println("1 HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH " + computeSensitivity);		
		System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH " + computeSensitivity);
		System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH " + computeSensitivity);
		System.out.println("1 HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH " + computeSensitivity);

		int m = stoichiometricMatrix.length;
		int n = stoichiometricMatrix[0].length;
		// alias
		int rxns = n;

		System.out.println("main: Read " + m + "X" + n	+ " stoichiometric matrix;");

		System.out.println("#Matrix Start#");
		System.out.println();

		printM(stoichiometricMatrix, "S");
		System.out.println();
		System.out.println("#Matrix End#");

		K = LA.nullSpaceMatrix(stoichiometricMatrix);

		printM(K, "K = nullspace(S)");

		
		if (K.length != rxns) 
			throw new Exception("main: Size is inconsistent");
	}
	
	public static void printM(double[][] m, String name){
		System.out.println("------ " + name + " -------");
		for(int i = 0; i < m.length;i++){		
			for(int j = 0 ;j < m[0].length;j++){
				System.out.print(m[i][j]+" ");
			}
			System.out.println(";");
		}
		System.out.println("------ /" + name + " -------");
	}

	public MFANullSpaceSolution solveAllFixed() {

		computeKmAndRelatedMatrixes();

		System.out.println("Measured input flux values: " + Arrays.toString(measuredFluxes));
		FluxSet principalFluxSet = calculatePrincipalFluxSolution();
		
		if(computeSensitivity)
			sensitivityMatrix = computeSensitivityMatrix();
		
		List<FluxSet> fluxSetList = new ArrayList<FluxSet>();
		fluxSetList.add(principalFluxSet);

		// deal with alternative solutions
		if(calculateAlternativeFluxes)
		{
			List<FluxSet> alternativeSolutions = calculateAlternativeSolutions();
			fluxSetList.addAll(alternativeSolutions);
		}
		return new MFANullSpaceSolution(fluxSetList, this);
	}
	
	/** Sensitivity to the measured fluxes, each column (i) corresponds to a zero measured flux vector, with only one position with the value 1 to the
	 * corresponding ith measured flux */
	public double[][] computeSensitivityMatrix(){
		int loop = 0;
		int rxns = K.length;
		int measurements = measuredFluxes.length;
		double[][] sensitivityMatrix = new double[rxns][measurements];
		
		do {
			for(int i=0;i<measurements;i++)
				measuredFluxes[i]= NullSpaceDefaults.ZERO;
			measuredFluxes[loop]=NullSpaceDefaults.ONE;

			System.out.println("Measured input flux values: " + Arrays.toString(measuredFluxes));

			double[] beta = calculateBeta();
			double[] flux_ = LA.matvec(K,beta);
			for(int k=0;k<rxns;k++) 
				sensitivityMatrix[k][loop]=flux_[k];
			
			loop++;

		} while(loop < measurements);

		for(int k=0;k<rxns;k++)
		{
			double[] row=new double[measurements];
			for(int s=0;s<measurements;s++)
				row[s]=sensitivityMatrix[k][s];
			String str=Arrays.toString(row);
			System.out.println("Sensitivity Matrix row[" + k + "]=" + str);
		}
		
		return sensitivityMatrix;
	}

	public void computeKmAndRelatedMatrixes(){
		int measurements = measuredFluxes.length;
		int unknowns = numberOfUnknowns();
		int rxns = K.length;
		
		KmeasuredT = new double[unknowns][measurements];
		int rowCntr=0;
		for(int i=0;i<rxns;i++) {
			if(isMeasured[i]) {
				for(int j=0;j<unknowns;j++) {
					KmeasuredT[j][rowCntr]=K[i][j];
				}
				rowCntr++;
			}
		}
		printM(KmeasuredT, "KmeasuredT");
		
		// on ouput of LUP KmeasuredT will be overwritten by LU decomposition
		rowPermutation=new int[unknowns];
		colPermutation=new int[measurements];	
		
		rankKmT = LA.LUP(KmeasuredT,rowPermutation,colPermutation);
		double[][] L= LA.getL(KmeasuredT);
		double[][] U= LA.getU(KmeasuredT);
		
		printM(KmeasuredT, "LU(KmeasuredT)");
		printM(L, "L");
		printM(U, "U");
		System.out.println("Rank: " + rankKmT);

		// the rank is ALWAYS smaller or equal to #measurements and ALWAYS smaller or equal to the "unknowns"    
		if(isRankDefficient()) {

			boolean hasNullSpace = true;
			System.out.println("The solution is not unique");
			int rankDeficiency=unknowns-rankKmT;
			System.out.println("Rank deficiency: " + rankDeficiency);
			int solutions = rankDeficiency + 1;
			System.out.println(solutions + " linearly independent solutions exist");
			computeSensitivity=false;
		}
		
		/* fluxPrincipal is the Principal Flux
	    later we compute Alternate Solutions if Km turns out to be rank deficient
		To deal with the rank deficient case we define "smaller" version that has a unique solution
		The nullpace can be computed elegantly while  simultaneously obtaining the uniqified solution by making an LU decomposition of the TRANSPOSE of Km as is implemented here */

		LT = LA.transpose(L);
		U_smaller=new double[rankKmT][measurements]; // contains the rank-first rows of U
		
		for(int i=0;i<rankKmT;i++) {
			for(int j=0;j<measurements;j++) {
				U_smaller[i][j]=U[i][j];
			}
		}
		
		printM(U_smaller, "U_smaller");
		
		// define invertible leading matrix
		U_blockT  = new double[rankKmT][rankKmT];
		U_smallerT = LA.transpose(U_smaller);
		for(int i=0;i<rankKmT;i++) {
			for(int j=0;j<rankKmT;j++) {
				U_blockT[i][j]=U_smallerT[i][j];
			}
		}
	}
	
	public double[] calculateBeta(){
		int unknowns = numberOfUnknowns();
		int measurements = measuredFluxes.length;
		double[] measuredFluxesPermuted = new double[measurements];
		double[] beta = new double[unknowns];
		
		for(int i=0;i<measurements;i++) {
			measuredFluxesPermuted[i]= measuredFluxes[colPermutation[i]]; // para indexar os measured fluxes dos indices originais dos fluxos (antes das permutacoes feitas pela LU decomposition) para os indices na matriz KmT 
		}
		
		double[] rhsSmaller = new double[rankKmT];
		
		//copy the rank-first measured fluxes
		for(int i=0;i<rankKmT;i++) 
			rhsSmaller[i] = measuredFluxesPermuted[i]; 

		double[] gammaSmaller = LA.solveLower(U_blockT,rhsSmaller);	// forward substitution
		double[] computedRhs  = LA.matvec(U_smallerT,gammaSmaller);
		
		if(LA.isSameTest(computedRhs,measuredFluxesPermuted) > NullSpaceDefaults.EPSILON) 
			gammaSmaller = computeGammaLeastSquares(U_smaller, U_smallerT, measuredFluxesPermuted); // Least Squares Approximation, gammaSmaller is recomputed!!
		
		// continue computation for all cases
		double[] gamma= new double[unknowns];
		for(int i=0;i<rankKmT;i++) 
			gamma[i] = gammaSmaller[i];
		
		double[] y    = LA.solveUpper(LT,gamma); // back substitution
		
		for(int k =0;k < unknowns;k++) 
			beta[rowPermutation[k]]=y[k]; // to index for the unknowns (rows) in the original KmeasuredT (before the permutations in the LU decomposition). The index of the unknowns (rows) in KmeasureT is equal in the nullspace of S (K)
		
		return beta;
	}
	
	
	/** Recompute gamma with Least Squares Approximation */
	public static double[] computeGammaLeastSquares(double[][] U_smaller, double[][] U_smallerT, double[] measuredFluxesPermuted){
		double[][] UtU = LA.matmul(U_smaller,U_smallerT);
		double[]   b   = LA.matvec(U_smaller,measuredFluxesPermuted);
		double[] gammaSmaller   = LA.solveSquareMatrix(UtU,b);
		return gammaSmaller;
	}
	
	/** Obtain principal solution */
	public FluxSet calculatePrincipalFluxSolution(){
		List<Flux> principalFluxList = new ArrayList<Flux>();
		List<MeasuredFlux> principalMesuredFluxList = new ArrayList<MeasuredFlux>();
		int rxns = K.length;
		
		double[] beta = calculateBeta();
		fluxPrincipal = LA.matvec(K,beta);
		
		System.out.println("BETA: " + Arrays.toString(beta));
		
		int rowCntr_=0;
		for(int k=0;k<rxns;k++)
		{
			String str="N/A";
			
			if(isMeasured[k])
			{
				str = Double.toString(measuredFluxes[rowCntr_]);
				principalMesuredFluxList.add(new MeasuredFlux(k,fluxPrincipal[k],measuredFluxes[rowCntr_]));
				rowCntr_++;
			}
			else 
				principalFluxList.add(new Flux(k,fluxPrincipal[k]));

			System.out.println("Principal Flux " + k + " : " + fluxPrincipal[k] + " [exp: " + str + "]");
		}
		
		FluxSet principalFluxSet = new FluxSet(principalMesuredFluxList, principalFluxList, beta);
		return principalFluxSet;
	}
	
	/** Compute Alternate Solutions if KmT is rank deficient */
	public List<FluxSet> calculateAlternativeSolutions(){
		List<FluxSet> fluxSetList = new ArrayList<FluxSet>();
		int unknowns = numberOfUnknowns();
		int rxns = K.length;
		
		for(int k=rankKmT;k<unknowns;k++)
		{
			double[] gamma = new double[unknowns];
			gamma[k]=NullSpaceDefaults.ONE;
			System.out.println("Gamma: " + Arrays.toString(gamma));
			double[] y = LA.solveUpper(LT,gamma);
			
			double[] beta = new double[unknowns];
			for(int s =0;s < unknowns;s++) {
				beta[rowPermutation[s]]=y[s];
			}
			System.out.println("BETA: " + Arrays.toString(beta));
			double[] fluxAlternative = LA.matvec(K,beta);
			List<Flux> alternativeFluxList = new ArrayList<Flux>();
			List<MeasuredFlux> alternativeMeasuredFluxList = new ArrayList<MeasuredFlux>();
			int rowCntr_=0;
			
			for(int s=0;s<rxns;s++)
			{
				fluxAlternative[s] += fluxPrincipal[s];
				String str="N/A";
				if(isMeasured[s])
				{
					str = Double.toString(measuredFluxes[rowCntr_]);
					alternativeMeasuredFluxList.add(new MeasuredFlux(s,fluxAlternative[s],measuredFluxes[rowCntr_]));
					rowCntr_++;
				}
				else alternativeFluxList.add(new Flux(s,fluxAlternative[s]));
				
				System.out.println("Alternative Flux " + s + " : " + fluxAlternative[s] + " [exp: " + str + "]");
	
			}
			FluxSet alternativeFluxSet = new FluxSet(alternativeMeasuredFluxList, alternativeFluxList, beta);
			fluxSetList.add(alternativeFluxSet);
		}
		return fluxSetList;
	}

	public MFANullSpaceSolution runModel() {
		return solveAllFixed();
	}
	
	private static void addFluxSetToList(List<FluxSet> fluxSetList,FluxSet fluxSet) {

		if(!isFluxSetNull(fluxSet)){
			if(fluxSetList.isEmpty())
				fluxSetList.add(fluxSet);
			else{
				boolean verifySetEquality = false;

				for(int i = 0; i < fluxSetList.size();i++){
					FluxSet currentFluxSet = fluxSetList.get(i);
					if(verifyFluxSetEquality(currentFluxSet,fluxSet)){
						verifySetEquality = true; 
						break;
					}
				}

				if(!verifySetEquality)
					fluxSetList.add(fluxSet);
			}
		}
	}


	private static boolean verifyFluxSetEquality(FluxSet currentFluxSet,FluxSet fluxSet){
		for(int i = 0; i < currentFluxSet.getNumberFluxes();i++){
			Flux currentFlux = currentFluxSet.getFlux(i);
			int currentFluxIndex = currentFlux.getIndex();
			double currentFluxValue = currentFlux.getValue();
			Flux flux = fluxSet.getFluxByIndex(currentFluxIndex);
			double fluxValue = flux.getValue();
			double absoluteFluxDifference = Math.abs(currentFluxValue-fluxValue);
			if((fluxValue != currentFluxValue) && (absoluteFluxDifference > NullSpaceDefaults.MINIMAL_FLUX_DIFERENCE))
				return false;
		}

		return true;
	}


	private static boolean isFluxSetNull(FluxSet fluxSet) {

		for(int i = 0; i < fluxSet.getNumberFluxes();i++){
			Flux flux = fluxSet.getFlux(i);
			double fluxValue = flux.getValue();
			if(fluxValue != 0.0)
				return false;
		}

		for(int j = 0; j < fluxSet.getNumberMeasuredFluxes();j++){
			MeasuredFlux measuredFlux = fluxSet.getMeasuredFlux(j);
			double expectedFluxValue = measuredFlux.getExpectedValue();
			if(expectedFluxValue != 0.0)
				return false;
		}


		return true;
	}
	
	public boolean isComputeSensitivity() {
		return computeSensitivity;
	}
	

	public void setComputeSensitivity(boolean computeSensitivity) {
		this.computeSensitivity = computeSensitivity;
	}
	
	public boolean isAlternativeSolutionsComputed() {
		return calculateAlternativeFluxes;
	}

	public void setCalculateAlternativeFluxes(boolean calculateAlternativeFluxes) {
		this.calculateAlternativeFluxes = calculateAlternativeFluxes;
	}

	public boolean[] getIsMeasured() {
		return isMeasured;
	}


	public void setIsMeasured(boolean[] isMeasured) {
		this.isMeasured = isMeasured;
	}


	public double[] getMeasuredFluxes() {
		return measuredFluxes;
	}


	public void setMeasuredFluxes(double[] measuredFluxes) {
		this.measuredFluxes = measuredFluxes;
	}


	public double[][] getStoichiometricMatrix() {
		return stoichiometricMatrix;
	}


	public void setStoichiometricMatrix(double[][] stoichiometricMatrix) {
		this.stoichiometricMatrix = stoichiometricMatrix;
	}


	public double[][] getK() {
		return K;
	}


	public void setK(double[][] k) {
		K = k;
	}
	
	public int numberOfUnknowns(){
		return K[0].length;
	}
	
	public boolean hasNullSpace(){
		return rankKmT != numberOfUnknowns();
	}
	
	public int rankDeficiency(){
		return numberOfUnknowns() - rankKmT;
	}
	
	public boolean isRankDefficient(){
		return rankKmT != numberOfUnknowns();
	}
	
	public int numberOfSolutions(){
		if(rankKmT == numberOfUnknowns()) 
			return 1;
		return rankDeficiency() + 1;
	}

	public double[][] getSensitivityMatrix(){
		return sensitivityMatrix;
	}
}
