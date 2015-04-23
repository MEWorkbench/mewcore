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


import java.util.Arrays;

/**
 * <p> Class performing many linear algebra (LA) utilities</p>
 * <p> especially solving linear systems</p>
 * 
 * @author Marcellinus Pont - Marcellinus.Pont@usa.dupont.com
 */
public final class LA{

	static double EPSILON = 1.0E-10;	    
	static double ZERO = 0.0E0;
	static double ONE  = 1.0E0;
	static boolean DIAGNOSTICS = true;

	// check if L*U stored in A in std positions
	// yields indeed the matrix that underwent the LUP procedure (AP)

	public static void checkLUP(double[][] A, double[][] AP) {

		int m = A.length;
		int n = A[0].length;

		double [][] L  = new double[m][m];
		double [][] U  = new double[m][n];

		getLandU(A, L, U);

		// check on L
		double sum = ZERO;
		for(int i=0;i<m;i++) {
			double term = (ONE - L[i][i]);
			sum += Math.abs(term);
		}
		System.out.println("DIAGNOSTICS: checkLUP: Deviation diagononal of L from 1: " + sum);

		// test L lower property
		sum=ZERO;
		for(int i=0;i<m;i++) {
			for(int j=0;j>i && j < m;j++) {
				sum += Math.abs(L[i][j]);
			}
		}
		System.out.println("DIAGNOSTICS: checkLUP: Upper triangle check L: " + sum);
		// test U upper property
		sum = ZERO;
		for(int i=0;i<m;i++) {
			for(int j=0;j<i && j < n;j++) {
				sum += Math.abs(U[i][j]);
			}
		}
		System.out.println("DIAGNOSTICS: checkLUP: Lower triangle check U: " + sum);
		// test L*U = A
		System.out.println("DIAGNOSTICS: checkLUP: m and n: " + m + ":" + n);

		double[][] A_ = matmul(L,U);
		double residue=isSameTest(A_,AP);
		System.out.println("DIAGNOSTICS: checkLUP: Final residue L*U - A: " + residue);

	} // checkLUP


	// after LUP has been executed, matrix A contains 
	// contains L and U in std position.
	// rowReduce reduce U to row echelon form

	public static double[][] rowReduce(double[][] A, int rank) {


		int m = A.length;
		int n = A[0].length;

		double [][] U  = getU(A);

		// save square invertible matrix to UP
		double [][] UP = new double[rank][rank];
		for(int i=0;i<rank;i++) {
			for(int j=0;j<rank;j++) {
				UP[i][j] = U[i][j];
			}
		}

		double [][] Usave = new double[rank][n];
		for(int i=0;i<rank;i++) {
			for(int j=0;j<n;j++) {
				if(Math.abs(U[i][j])<EPSILON) {
					U[i][j]=ZERO;
				}
				Usave[i][j]=U[i][j];
			}
		}

		// divide by diagonal element
		for(int i=0;i<rank;i++) {
			if(Math.abs(U[i][i]) < EPSILON) {
				System.out.println("rowReduce: SEVERE ERROR");
				System.exit(1);
			} else {
				double factor=U[i][i];
				for(int j=i;j<n;j++) {
					U[i][j]=U[i][j]/factor;

				}
			}
			for(int j=0;j<n;j++) {
				if(Math.abs(U[i][j])<EPSILON) {
					U[i][j]=ZERO;
				}
			}
		}

		// now there are ones on the diagonal
		//introduce zero column U(j,1:i-1) for each i=1:m 
		for(int i=rank-1;i>=0;i--) {
			for(int j=0;j<i;j++) {
				double factor=U[j][i];
				for(int k=0;k<n;k++) {
					U[j][k] = U[j][k] - factor*U[i][k];
				}
			}
		}
		// clean up
		for(int i=0;i<m;i++) {
			for(int j=0;j<n;j++) {
				if(Math.abs(U[i][j])<EPSILON) {
					U[i][j]=ZERO;
				}
			}
		}
		if(DIAGNOSTICS) {
			for(int i=0;i<rank;i++) {
				double[] row=new double[n];
				for(int k=0;k<n;k++) {
					row[k]=U[i][k];
				}
				String str=Arrays.toString(row);
				System.out.println("rowReduce: U reduced[" + i + "]=" + str);
			}

			// define rowReduced matrix (zero rows are cleaved off)

			double[][] Utmp=new double[rank][n];
			for(int i=0;i<rank;i++) {
				for(int j=0;j<n;j++) {
					Utmp[i][j]=U[i][j];
				}
			}
			double[][] check=matmul(UP,Utmp);
			double residue=isSameTest(check,Usave);
			System.out.println("rowReduce: Remainder:" + residue);
		}

		return(U);

	} // rowReduce 

	// given row echelon form of U, compute null space matrix K

	public static double[][] nullSpace(double[][] U, double[][] A, int rank) {

		int p=A.length;
		int q=A[0].length; 

		double[][] K = new double[q][q-rank];
		for(int i=rank;i<q;i++) {
			K[i][i-rank]=ONE;
		}
		for(int i=0;i<rank;i++) {
			for(int j=0;j<q-rank;j++) {
				K[i][j] = -U[i][j+rank];
			}
		}
		if(DIAGNOSTICS) {
			double[][] Atmp = matmul(A,K);
			double residue = isZeroTest(Atmp);
			System.out.println("nullSpace: Remainder: " + residue);
		}

		return(K);

	} // nullSpace

	// performs L U decomposition
	// to get A into std form it may have to undergo rowPermutation
	// and colPermutation
	// on exit A is overwritten by L and U in std positions

	public static int LUP(double[][] A, int[] rowPermutation,int[] colPermutation) {

		int m = A.length;
		int n = A[0].length;

		for(int i=0;i<m;i++) {
			rowPermutation[i]=i;
		}
		for(int j=0;j<n;j++) {
			colPermutation[j]=j;
		}


		int minSize = (m<n?m:n);
		int maxSize = (m>n?m:n);

		int s=0;
		int rank = m;

		double maxValue=Math.abs(A[0][0]);	
		while(s < minSize && s < rank) {

			int maxI = s;
			double absValue = Math.abs(A[s][s]);
			for(int k=s+1;k<rank;k++) {
				if(Math.abs(A[k][s])> absValue) {
					maxI=k;
					absValue=Math.abs(A[k][s]);
				}
			}
			maxValue =  Math.abs(A[maxI][s]);

			if (maxValue > EPSILON) {

				for(int j=0;j<n;j++) {
					double swap   = A[s][j];
					A[s][j]       = A[maxI][j];
					A[maxI][j]    = swap;
				}
				int swapI           = rowPermutation[s];
				rowPermutation[s]   = rowPermutation[maxI];
				rowPermutation[maxI]= swapI;

				for(int k=s+1;k<rank;k++) {
					A[k][s] = A[k][s]/A[s][s];
				}
				for(int k=s+1;k<rank;k++) {
					for(int l=s+1;l<n;l++) {
						A[k][l] = A[k][l] - A[k][s]*A[s][l];
					}
				}
				s++;

			}

			// The other case, exception: all pivots are zero
			if(maxValue <= EPSILON) {

				maxI=s;
				absValue=Math.abs(A[s][s]);
				for(int k=s+1;k<n;k++) {
					if(Math.abs(A[s][k])> absValue) {
						maxI=k;
						absValue=Math.abs(A[s][k]);
					}
				}
				maxValue =  Math.abs(A[s][maxI]);

				if(maxValue > EPSILON) {

					for(int j=0;j<m;j++) {
						double swap  = A[j][s];
						A[j][s]      = A[j][maxI];
						A[j][maxI]   = swap;
					}
					int swapI         = colPermutation[s];
					colPermutation[s]    = colPermutation[maxI];
					colPermutation[maxI] = swapI;

				} else {

					// notice swapRow is identically zero

					for(int j=0;j<n;j++) {
						double swap   = A[s][j];
						A[s][j]       = A[rank-1][j];
						A[rank-1][j]    = swap;
					}
					int swapI               = rowPermutation[s];
					rowPermutation[s]       = rowPermutation[rank-1];
					rowPermutation[rank-1]    = swapI;
					rank--;

				}
			}
		}
		rank = (rank>minSize?minSize:rank);

		return(rank);


	} // LUP

	// computes the nullSpaceMatrix of A called K 
	// based on L U decomposition
	//  A x K = 0

	public static double [][] nullSpaceMatrix(double[][] A) {

		//  local vars
		// initialize permutations
		int p=A.length;
		int q=A[0].length;

		int[] rowPermutation = new int[p];
		int[] colPermutation = new int[q];

		// save A as Asave
		double[][] Asave=new double[p][q];
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				Asave[i][j]=A[i][j];
			}
		}
		// LU decomposition
		int rank = LUP(A, rowPermutation, colPermutation);

		// Apply permutation to source matrix
		double[][] A_=new double[p][q];
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				A_[i][j]=Asave[rowPermutation[i]][colPermutation[j]];
			}
		}
		if(DIAGNOSTICS) {
			// check result of LU decomposition of A
			// LU decomposition is embedded in A!!!
			checkLUP(A, A_);
			System.out.println("nullSpaceMatrix: Rank: " + rank);
		}

		double[][] U = rowReduce(A, rank);

		if(DIAGNOSTICS) {
			for(int i=0;i<rank;i++) {
				double[][] U_ = getU(A);
				double[] row=new double[q];
				for(int j=0;j<q;j++) {
					row[j]=U[i][j];
				}
				String str=Arrays.toString(row);
				double[] row_=new double[q];
				for(int j=0;j<q;j++) {
					row_[j]=U_[i][j];
				}
				String str_=Arrays.toString(row_);

				System.out.println("U before reduction: U_" + str_);
				System.out.println("U in row-echelon form: U" + str);
			}
		}

		double[][] K    = nullSpace(U, A_, rank);
		double[][] K_   = new double[q][q-rank];

		for(int i=0;i<q;i++) {
			for(int j=0;j<q-rank;j++) {
				K_[colPermutation[i]][j] = K[i][j];
			}
		}

		if(DIAGNOSTICS) {
			double[][] check = matmul(Asave,K_);
			double remainder=isZeroTest(check);
			System.out.println("nullSpaceMatrix: Remainder NullSpaceMatrix: " + remainder);
		}

		// restore A
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				A[i][j]=Asave[i][j];
			}
		}
		return(K_);

	} // nullSpaceMatrix

	// get upper and lower triangular matrix U and L embedded in A 
	// A is the result AFTER LUP decomposition

	public static void getLandU(double[][] A, double[][] L, double[][] U) {


		int p=A.length;
		int q=A[0].length;

		int minSize = (p<q?p:q);
		// define L
		for(int i=0;i<p;i++) {
			for(int j=0;j<i && j < minSize;j++) {
				L[i][j] = A[i][j];
			}
		}
		// define U
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				U[i][j]=A[i][j];
			}
		}
		for(int i=0;i<p;i++) {
			for(int j=0;j<minSize;j++) {
				U[i][j] = A[i][j]-L[i][j];
			}
		}
		for(int k=0;k<p;k++) {
			L[k][k] = ONE;
		}

	} // getLandU

	// get upper triangular matrix U, embedded in A

	public static double[][] getU(double[][] A) {

		int p=A.length;
		int q=A[0].length;
		double [][] L  = new double[p][p]; 
		double [][] U  = new double[p][q];                 

		getLandU(A,L,U);

		return(U);

	} // get Upper

	// get lower triangular matrix embedded in A

	public static double[][] getL(double[][] A) {

		int p=A.length;
		int q=A[0].length;
		double [][] L  = new double[p][p]; 
		double [][] U  = new double[p][q];                 

		getLandU(A,L,U);

		return(L);

	} // getLower


	// solve Lower Triangular System L x = b

	public static double[] solveLower(double[][] L,double[] b) {

		int s = L.length;
		if(s != b.length || s != L[0].length) {
			System.out.println("solveLower: Size mismatch");
			System.exit(1);
		}

		double[] x = new double[s];

		for(int i=0;i<s;i++) {
			double sum = b[i];
			for(int k=0;k<i;k++) {
				sum -= L[i][k]*x[k];
			}
			x[i]=sum/L[i][i];
		}

		if(DIAGNOSTICS) {
			// check by back substitution
			double[] b_ = matvec(L, x);
			double residue=isSameTest(b_,b);
			System.out.println("SolveLower Residue:" + residue);
		}

		return(x);

	} // solveLower

	// solve Upper Triangular System  U x = b

	public static double[] solveUpper(double[][] U,double[] b) {

		int s = U.length;
		if(s != b.length || s != U[0].length) {
			System.out.println("solveUpper: Size mismatch");
			System.exit(1);
		}

		double[] x = new double[s];

		for(int i=s-1;i>=0;i--) {
			double sum = b[i];
			for(int k=i+1;k<s;k++) {
				sum -= U[i][k]*x[k];
			}
			x[i]=sum/U[i][i];
		}

		if(DIAGNOSTICS) {
			// check by back substitution
			double[] b_ = matvec(U, x);
			double residue=isSameTest(b_,b);
			System.out.println("SolveUpper Residue:" + residue);
		}

		return(x);


	} // solveUpper

	// solve linear system A x = b, with A an invertible square matrix  

	public static double[] solveSquareMatrix(double[][] A, double[] b) {


		int p=A.length;
		int q=A[0].length;
		if(p != q) {
			System.out.println("solveSquareMatrix: Matrix is not square");
			System.exit(1);
		}
		if(p != b.length) {
			System.out.println("solveSquareMatrix: Size mismatch");
			System.exit(1);
		}
		// make copy
		double[][] Asave=new double[p][p];
		for(int i=0; i<p; i++) {
			for(int j=0; j<p; j++) {
				Asave[i][j]=A[i][j];
			}
		}
		// make LU decomposition

		int[] rowPermutation=new int[p];
		int[] colPermutation=new int[p];
		int rank=LUP(A,rowPermutation,colPermutation);
		if(rank != p) {
			System.out.println("solveSquareMatrix: This is not a full rank matrix");
			System.exit(1);
		}
		double[][] L = getL(A);
		double[][] U = getU(A);

		double[] b_ = new double[p];
		for(int i=0;i<p;i++) {         
			b_[i]=b[rowPermutation[i]];
		}
		double[] y  = solveLower(L, b_);
		double[] x_ = solveUpper(U, y);
		double[] x = new double[p];
		for(int i=0;i<p;i++) {
			x[colPermutation[i]]=x_[i];
		}

		if(DIAGNOSTICS) {
			// check by back substitution
			double[] bsave = matvec(Asave,x);
			double residue = isSameTest(bsave,b);
			System.out.println("solveSquareMatrix Residue:" + residue);
		}

		return(x);

	} // solveSquareMatrix


	// simple utility to do matrix vector multiplication of doubles 

	public static double[] matvec(double[][] A,double[] x) {

		int p= A.length;
		int q= A[0].length;
		if(x.length != q) {
			System.out.println("matvec: Size mismatch");
			System.exit(1);
		}
		double[] b = new double[p];
		for(int i=0;i<p; i++) {
			double sum=ZERO; 
			for(int j=0; j<q; j++) {
				sum += A[i][j]*x[j];   
			}	
			b[i]=sum;
		}
		return(b);

	} //matvec

	// simple utility to do matrix matrix mutiplication of doubles 

	public static double[][] matmul(double[][] A, double[][] B) {

		int p  =  A.length;
		int q  =  A[0].length;
		int q1 =  B.length;
		int r  =  B[0].length;

		if(q1 != q) {
			System.out.println("matmul: sizes inconsistent");
			System.exit(1);
		}
		double[][] C = new double[p][r];
		for(int i=0;i<p;i++) {
			for(int j=0;j<r;j++) {
				double sum=ZERO;
				for(int k=0;k<q;k++) {
					sum += A[i][k]*B[k][j];
				}
				C[i][j]=sum;
			}
		}
		return(C);

	} // matmul

	// simple utility to compute the transpose of double matrix

	public static double[][] transpose(double[][] A) {

		int p=A.length;
		int q=A[0].length;
		double[][] AT=new double[q][p];
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				AT[j][i]=A[i][j];
			}
		}
		return(AT);


	} // transpose

	public static double isSameTest(double[][] A, double[][] B) {

		int p=A.length;
		int q=A[0].length;
		if(p != B.length || q!= B[0].length) {
			System.out.println("isSameTest: Sizes inconsistent");
		}
		double residue=ZERO;
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				residue += Math.abs(A[i][j]-B[i][j]);
			}
		}
		return(residue);

	} // isSameTest

	public static double isZeroTest(double[][] A) {

		int p=A.length;
		int q=A[0].length;

		double residue=ZERO;
		for(int i=0;i<p;i++) {
			for(int j=0;j<q;j++) {
				residue += Math.abs(A[i][j]);
			}
		}
		return(residue);

	} // isZeroTest

	public static double isSameTest(double[] a, double[] b) {

		int p=a.length;
		if(p != b.length) {
			System.out.println("isSameTest: Sizes inconsistent");
			System.exit(1);
		}
		double residue=ZERO;
		for(int i=0;i<p;i++) {
			residue += Math.abs(a[i]-b[i]);
		}

		return(residue);

	} // isSameTest


} // LA
