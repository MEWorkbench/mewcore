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
package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.nullspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// MFA class
// Handles over-, under- & determined case
// Unique features: deals explicitly with the singular case
// Provides interface to include directionality using QP
//
// copyright Marcellinus.Pont@usa.dupont.com

public class NullSpaceMethod {
	private static final double MINIMAL_FLUX_DIFERENCE = 0.0001;
	static double EPSILON = 1.0E-10;
	static double ZERO = 0.0E0;
	static double ONE = 1.0E0;
	static boolean DIAGNOSTICS = true;
	protected boolean computeSensitivity = false;
	protected boolean[] isMeasured;
	protected double[] measuredFluxes;
	protected double[][] stoichiometricMatrix;
	protected double[][] K;

	public NullSpaceMethod(double[][] stoichiometricMatrix,boolean computeSensitivity, boolean[] isMeasured,double[] measuredFluxes) throws Exception {
		this.computeSensitivity = computeSensitivity;
		this.isMeasured = isMeasured;
		this.measuredFluxes = measuredFluxes;
		this.stoichiometricMatrix = stoichiometricMatrix;

		int m = stoichiometricMatrix.length;
		int n = stoichiometricMatrix[0].length;
		// alias
		int rxns = n;

		System.out.println("main: Read " + m + "X" + n	+ " stoichiometric matrix;");

		System.out.println("#Matrix Start#");
		System.out.println();

		for(int i = 0; i < stoichiometricMatrix.length;i++){		
			for(int j = 0 ;j < stoichiometricMatrix[0].length;j++){
				System.out.print(stoichiometricMatrix[i][j]+" ");
			}
			System.out.println(";");
		}
		System.out.println();
		System.out.println("#Matrix End#");

		K = LA.nullSpaceMatrix(stoichiometricMatrix);

		if (K.length != rxns) 
			throw new Exception("main: Size is inconsistent");
	}

	public static MFANullSpaceSolution solveAllFixed(double[][] K, boolean[] isMeasured, double[] measuredFluxes, boolean  computeSensitivity) {

		List<FluxSet> fluxSetList = new ArrayList<FluxSet>();
		
		int measurements = measuredFluxes.length;
		int rxns = K.length;
		int unknowns = K[0].length;

		double [][] KmeasuredT = new double[unknowns][measurements];
		int rowCntr=0;
		for(int i=0;i<rxns;i++) {
			if(isMeasured[i]) {
				for(int j=0;j<unknowns;j++) {
					KmeasuredT[j][rowCntr]=K[i][j];
				}
				rowCntr++;
			}
		}

		// on ouput of LUP KmeasuredT will be overwritten by LU decomposition
		int[] rowPermutation=new int[unknowns];
		int[] colPermutation=new int[measurements];	
		int rank = LA.LUP(KmeasuredT,rowPermutation,colPermutation);
		double[][] L= LA.getL(KmeasuredT);
		double[][] U= LA.getU(KmeasuredT);

		boolean hasNullSpace=false;
		// the rank is ALWAYS smaller or equal to #measurements
		// and ALWAYS smaller or equal to the "unknowns"    
		if(rank != unknowns) {

			hasNullSpace=true;
			System.out.println("The solution is not unique");
			int rankDeficiency=unknowns-rank;
			System.out.println("Rank deficiency: " + rankDeficiency);
			int solutions = rankDeficiency + 1;
			System.out.println(solutions + " linearly independent solutions exist");
			computeSensitivity=false;

		}
		// fluxPrincipal is the Principal Flux
		// later we compute Alternate Solutions
		// if Km turns out to be rank deficient
		// To deal with the rank deficient case
		// we define "smaller" version that has a unique
		// solution
		// The nullpace can be computed elegantly while
		// simultaneously obtaining the uniqified solution
		// by making an LU decomposition of the TRANSPOSE
		// of Km as is implemented here

		double[] fluxPrincipal=null;
		int loop=-1;
		double[] measuredFluxesPermuted = new double[measurements];
		double[][] sensitivityMatrix = new double[rxns][measurements];
		double[] beta = new double[unknowns]; 
		double[][] LT = LA.transpose(L);
		double[][] U_smaller=new double[rank][measurements];
		for(int i=0;i<rank;i++) {
			for(int j=0;j<measurements;j++) {
				U_smaller[i][j]=U[i][j];
			}
		}
		// define invertible leading matrix
		double[][] U_blockT  = new double[rank][rank];
		double[][] U_smallerT = LA.transpose(U_smaller);
		for(int i=0;i<rank;i++) {
			for(int j=0;j<rank;j++) {
				U_blockT[i][j]=U_smallerT[i][j];
			}
		}

		// for loop >= 0 we compute sensitivity matrix
		List<Flux> principalFluxList = new ArrayList<Flux>();
		List<MeasuredFlux> principalMesuredFluxList = new ArrayList<MeasuredFlux>();
		do {

			if(loop != -1) {
				for(int i=0;i<measurements;i++) {
					measuredFluxes[i]= ZERO;
				}
				measuredFluxes[loop]=ONE;
			}

			String fluxes=Arrays.toString(measuredFluxes);
			System.out.println("Measured input flux values: " + fluxes);

			for(int i=0;i<measurements;i++) {
				measuredFluxesPermuted[i]= measuredFluxes[colPermutation[i]];
			}
			double[] rhsSmaller = new double[rank];
			for(int i=0;i<rank;i++) {
				rhsSmaller[i] = measuredFluxesPermuted[i];
			}

			double[] gammaSmaller = LA.solveLower(U_blockT,rhsSmaller);	
			double[] computedRhs  = LA.matvec(U_smallerT,gammaSmaller);
			boolean match=true;
			if(LA.isSameTest(computedRhs,measuredFluxesPermuted) > EPSILON) {

				match=false;
				// Least Squares Approximation
				// gammaSmaller is recomputed!!
				double[][] UtU = LA.matmul(U_smaller,U_smallerT);
				double[]   b   = LA.matvec(U_smaller,measuredFluxesPermuted);
				gammaSmaller   = LA.solveSquareMatrix(UtU,b);

			}	
			// continue computation for all cases
			double[] gamma= new double[unknowns];
			for(int i=0;i<rank;i++) {
				gamma[i] = gammaSmaller[i];
			}
			double[] y    = LA.solveUpper(LT,gamma);
			for(int k =0;k < unknowns;k++) {
				beta[rowPermutation[k]]=y[k];
			}
			if(loop == -1) {
				// obtain principal solution
				fluxPrincipal = LA.matvec(K,beta);
				int rowCntr_=0;
				for(int k=0;k<rxns;k++) {
					String str="N/A";
					if(isMeasured[k]) {
						str = Double.toString(measuredFluxes[rowCntr_]);
						principalMesuredFluxList.add(new MeasuredFlux(k,fluxPrincipal[k],measuredFluxes[rowCntr_]));
						rowCntr_++;
					}
					else principalFluxList.add(new Flux(k,fluxPrincipal[k]));

					System.out.println("Principal Flux " + k + " : " + fluxPrincipal[k] + " [exp: " + str + "]");

				}
			} else {
				// compute sensitivity matrix
				double[] flux_ = LA.matvec(K,beta);
				for(int k=0;k<rxns;k++) {
					sensitivityMatrix[k][loop]=flux_[k];
				}
			}

			loop++;

		} while(computeSensitivity && loop < measurements);

		if(computeSensitivity) {
			for(int k=0;k<rxns;k++) {
				double[] row=new double[measurements];
				for(int s=0;s<measurements;s++) {
					row[s]=sensitivityMatrix[k][s];
				}
				String str=Arrays.toString(row);
				System.out.println("Sensitivity Matrix row[" + k + "]=" + str);
			}
		}
		
		FluxSet principalFluxSet = null;

		if(computeSensitivity)
			principalFluxSet = new FluxSet(principalMesuredFluxList,principalFluxList,sensitivityMatrix);
		else
			principalFluxSet = new FluxSet(principalMesuredFluxList,principalFluxList);

		fluxSetList.add(principalFluxSet);

		// deal with alternative solutions

		for(int k=rank;k<unknowns;k++) {
			double[] gamma = new double[unknowns];
			gamma[k]=ONE;
			double[] y = LA.solveUpper(LT,gamma);
			for(int s =0;s < unknowns;s++) {
				beta[rowPermutation[s]]=y[s];
			}
			double[] fluxAlternative = LA.matvec(K,beta);
			List<Flux> alternativeFluxList = new ArrayList<Flux>();
			List<MeasuredFlux> alternativeMeasuredFluxList = new ArrayList<MeasuredFlux>();
			int rowCntr_=0;
			for(int s=0;s<rxns;s++) {
				fluxAlternative[s] += fluxPrincipal[s];
				String str="N/A";
				if(isMeasured[s]) {
					str = Double.toString(measuredFluxes[rowCntr_]);
					alternativeMeasuredFluxList.add(new MeasuredFlux(s,fluxAlternative[s],measuredFluxes[rowCntr_]));
					rowCntr_++;
				}
				else alternativeFluxList.add(new Flux(s,fluxAlternative[s]));
				
				System.out.println("Alternative Flux " + s + " : " + fluxAlternative[s] + " [exp: " + str + "]");
	
			}
			
			FluxSet alternativeFluxSet = new FluxSet(alternativeMeasuredFluxList,alternativeFluxList);

			fluxSetList.add(alternativeFluxSet);
		}
		
		return new MFANullSpaceSolution(fluxSetList);

	} // solveAll


	public MFANullSpaceSolution runModel() {
		return solveAllFixed(K, isMeasured, measuredFluxes, computeSensitivity);
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
			if((fluxValue != currentFluxValue) && (absoluteFluxDifference > MINIMAL_FLUX_DIFERENCE))
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


} // MFA1
