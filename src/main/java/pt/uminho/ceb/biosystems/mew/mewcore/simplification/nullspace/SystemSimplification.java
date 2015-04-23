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
package pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace;

import java.util.ArrayList;
import java.util.Arrays;

import pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.ModelStructureDouble;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.ModelStructureLong;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.ReducedModelDouble;
import pt.uminho.ceb.biosystems.mew.mewcore.simplification.nullspace.structure.ReducedModelLong;


/**
 * <p> SystemSimplification class </p>
 * <p> Handles double and long integer cases </p>
 * 
 * @author Marcellinus Pont - Marcellinus.Pont@usa.dupont.com
 */
public class SystemSimplification{

	public static double EPSILON = 1.0E-10;	    
	public static double ZERO = 0.0E0;
	public static double ONE  = 1.0E0;
	static boolean DIAGNOSTICS = true;

	public static void main(String args[]) {

		String inputFileName=new  String("BATCHX");
		GetInputData Stoich = new GetInputData(inputFileName);
		double[][] A = Stoich.STOICH_VALUES;

		int m = A.length;
		int n = A[0].length;

		boolean isInteger = true;
		for(int i=0;i<m;i++) {
			for(int j=0;j<n;j++) {
				long a = Math.round(A[i][j]);
				if((((double) a) - A[i][j]) != ZERO) {
					isInteger = false;
				}
			}
		}

		// isInteger=false;

		System.out.println("main: Read " + m + "X" + n + " stoichiometric matrix;");
		System.out.println(" stoichiometric matrix integer? " + isInteger);

		long [][] Klong=null;

		if(isInteger) {
			Klong = LA.nullSpaceMatrixIntegral(A);
		}

		double [][] K = null;

		if(!isInteger || Klong == null) {
			K = LA.nullSpaceMatrix(A);
		}

		if(Klong != null) {

			ReducedModelLong reducedModel = systemSimplification(Stoich,Klong);
			reducedModel.reportModelStructure();

		} else if(K != null) {

			ReducedModelDouble reducedModel = systemSimplification(Stoich,K);
			reducedModel.setReportAsFraction(true);
			reducedModel.reportModelStructure();

		} else {

			System.out.println("Failure of SystemSimplification");

		}


	} // main


	private static long gcd(long [] array) {


		long [] arrayTmp = array.clone(); 
		// local vars
		int listSize=array.length;
		for(int k=0;k<listSize;k++) {
			arrayTmp[k]=Math.abs(arrayTmp[k]);
		}
		Arrays.sort(arrayTmp);

		ArrayList<Long> arr2 = new ArrayList<Long>();
		long lastValue=0;
		int len=0;
		for(int s=0;s<listSize;s++) {
			if((long) arrayTmp[s]==lastValue) {
				// do nothing
			} else {
				arr2.add(arrayTmp[s]);
				len++;
			}
			lastValue = (long) arrayTmp[s];
		}
		long [] arrayScale = new long[len]; 
		for(int q=0;q<len;q++) {
			arrayScale[q] = (long) arr2.get(q);
		}

		long a, b, gcd1;
		long ONE = (long) 1;

		if(len == 0 || arrayScale[len-1]==0) {
			return(ONE);
		}

		a = arrayScale[0];
		gcd1 = a;
		for(int i = 1;i<len;i++) {
			if(gcd1 == 1) {
				break;
			}
			gcd1 = a;
			a = arrayScale[i];
			b = gcd1;
			// System.out.println("i" + arrayScale[i]);
			while( b != 0) {
				gcd1 = b;
				b = a % gcd1;
				a = gcd1;
			}
			// System.out.println(gcd1);
		}
		return(gcd1);
	}

	private static ModelStructureLong coupling(long[][] Klong) {

		int n  = Klong.length;
		int n_ = Klong[0].length;

		long[] gcd_=new long[n];

		ModelStructureLong modelStructure=new ModelStructureLong(n);
		boolean[ ] blocked=new boolean[n];

		for(int i=0;i<n;i++) {
			long[] longrow=new long[n_];
			for(int j=0;j<n_;j++) {
				longrow[j]=Klong[i][j];
			}
			long gcd0=gcd(longrow);
			gcd_[i]=gcd0;
			for(int j = 0;j < n_;j++) {
				longrow[j] /= gcd0;
				Klong[i][j] = longrow[j];
			}
			long L1norm=(long) 0;
			for(int j = 0;j < n_;j++) {
				if(Math.abs(Klong[i][j]) > L1norm) {
					L1norm=Math.abs(Klong[i][j]);
				}
			}
			if(L1norm == (long) 0) {
				blocked[i]=true;
			}
		}

		boolean[] done = new boolean[n];
		boolean[] founder = new boolean[n];
		boolean[][] Mask = new boolean[n][n];

		long[] rowi = new long[n_];
		long[] rowj = new long[n_];


		for(int i=0;i<n;i++) {
			if(done[i]) {
				continue;
			}
			for(int k=0;k<n_;k++) {
				rowi[k]=Klong[i][k];
			}
			for(int j=i+1;j<n;j++) {
				if(done[j]) {
					continue;
				}
				for(int k=0;k<n_;k++) {
					rowj[k]=Klong[j][k];
				}
				boolean hit=false;
				if(LA.isSameTest(rowi,rowj) == (long) 0) {
					hit = true;
				} else if(LA.isSameTest(rowi,LA.negate(rowj)) == (long) 0) {
					gcd_[j] = -gcd_[j];
					hit = true;
				}
				if(hit) {
					done[j] = true;
					founder[i]= true;
					Mask[i][i]=true;
					Mask[i][j]=true;
				}
			}
		}
		int clusterId = 0;
		int total = 0;
		int blockedCluster=-1;

		for(int i=0;i<n;i++) {
			if(founder[i]) {
				int clusterSize=0;
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						clusterSize++;
					}
				}
				if(clusterSize == 1) {
					continue;
				}
				total += clusterSize;
				long[] gcdCluster = new long[clusterSize];
				int cntr=0;
				int absIndex = 0;
				long absValue =  Math.abs(gcdCluster[0]);
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						gcdCluster[cntr]=gcd_[k];
						if(Math.abs(gcdCluster[cntr]) < absValue) {
							absIndex=cntr;
							absValue=Math.abs(gcdCluster[cntr]);
						}
						cntr++;
					}
				}
				long gcd0 = gcd(gcdCluster);
				// sign convention
				if(gcdCluster[absIndex] < (long) 0) {
					gcd0=-gcd0;
				}
				int index=0;
				// System.out.println("Cluster:" + clusterId);
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						modelStructure.belongsToCluster[k]=clusterId;
						modelStructure.ratio[k] = gcd_[k]/gcd0;
						// System.out.println("reaction index:" + k + "; ratio (long):" + modelStructure.ratio[k] ); 
					}
				}
				if(blocked[i]) {
					blockedCluster=clusterId;
				}
				clusterId++;
			}
		}


		int clusters=0;
		if(blockedCluster != -1) { // there is a blocked cluster
			clusters = clusterId - 1;
		} else {
			clusters = clusterId;	    
		}
		int m = n - total + clusters;
		modelStructure.allocateTransfo(n,m);

		int s = 0;
		for(int k=0;k<n;k++) {
			if(modelStructure.belongsToCluster[k] != -1) {
				clusterId=modelStructure.belongsToCluster[k];
				if(blockedCluster != -1) {
					if(clusterId < blockedCluster) {
						modelStructure.transfo[k][clusterId] = modelStructure.ratio[k];
					} else if(clusterId > blockedCluster) {
						modelStructure.transfo[k][clusterId-1] = modelStructure.ratio[k];
					}
				} else {
					modelStructure.transfo[k][clusterId] = modelStructure.ratio[k];
				}
			} else {
				modelStructure.transfo[k][clusters + s] = (long) 1;
				s++;
			}
		}

		modelStructure.blockedCluster = blockedCluster;

		return(modelStructure);

	} // coupling for longs

	private static ModelStructureDouble coupling(double[][] K) {

		int n  = K.length;
		int n_ = K[0].length;

		ModelStructureDouble modelStructure=new ModelStructureDouble(n);
		boolean[] blocked=new boolean[n];
		double[] factor=new double[n];

		for(int i=0;i<n;i++) {
			double L1norm=ZERO;
			for(int j=0;j<n_;j++) {
				if(Math.abs(K[i][j]) > L1norm) {
					L1norm = Math.abs(K[i][j]);
				}
			}
			if(Math.abs((double) Math.round(L1norm) - L1norm) < EPSILON) {
				L1norm = (double) Math.round(L1norm);
			}
			if(L1norm > EPSILON) {
				for(int j=0;j<n_;j++) {
					K[i][j] /= L1norm;
				}
				factor[i]=L1norm;
			} else {
				for(int j=0;j<n_;j++) {
					K[i][j] = ZERO;
				}
				factor[i]=ONE;
				blocked[i]=true;
			}
		}

		boolean[] done    = new boolean[n];
		boolean[] founder = new boolean[n];
		boolean[][] Mask  = new boolean[n][n];

		double[] rowi = new double[n_];
		double[] rowj = new double[n_];


		for(int i=0;i<n;i++) {

			if(done[i]) {
				continue;
			}
			for(int k=0;k<n_;k++) {
				rowi[k]=K[i][k];
			}
			for(int j=i+1;j<n;j++) {
				if(done[j]) {
					continue;
				}
				for(int k=0;k<n_;k++) {
					rowj[k]=K[j][k];
				}
				boolean hit=false;
				if(LA.isSameTest(rowi,rowj) < EPSILON) {
					hit = true;
				} else if(LA.isSameTest(rowi,LA.negate(rowj)) < EPSILON) {
					factor[j] = -factor[j];
					hit = true;
				}
				if(hit) {
					done[j] = true;
					founder[i]= true;
					Mask[i][i]=true;
					Mask[i][j]=true;
				}
			}
		}

		int clusterId = 0;
		int total=0;
		int blockedCluster=-1;
		for(int i=0;i<n;i++) {
			if(founder[i]) {
				int clusterSize=0;
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						clusterSize++;
					}
				}
				if(clusterSize == 1) {
					continue;
				}
				total += clusterSize;

				int absMin = -1;
				double absVal = ZERO;
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						if(absVal == ZERO) {
							absVal=Math.abs(factor[k]);
							absMin=k;
						} else if(Math.abs(factor[k]) < absVal) {
							absVal=Math.abs(factor[k]);
							absMin=k;
						}
					}
				}

				if(factor[absMin] < ZERO) {
					absVal=-absVal;
				}
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						factor[k] /= absVal;
						double abserr = Math.abs(Math.round(factor[k]) - factor[k]);
						if(abserr < EPSILON) {
							factor[k]=Math.round(factor[k]);
						} else if(Math.abs(factor[k]) > ONE) {
							double relerr = abserr/Math.abs(factor[k]);
							if(relerr < EPSILON) {
								factor[k]=Math.round(factor[k]);
							}
						} 
					} 
				}

				int index=0;
				// System.out.println("Cluster:" + clusterId);
				for(int k=0;k<n;k++) {
					if(Mask[i][k]) {
						modelStructure.belongsToCluster[k]=clusterId;
						modelStructure.ratio[k] = factor[k];
						// System.out.println("reaction index:" + k + "; ratio (double):" + modelStructure.ratio[k]); 
					}
				}
				if(blocked[i]) {
					blockedCluster=clusterId;
				}
				clusterId++;
			}
		}

		int clusters=0;
		if(blockedCluster != -1) {  // there is a blocked cluster
			clusters = clusterId - 1;
		} else {
			clusters = clusterId;	    
		}
		int m = n - total + clusters;
		modelStructure.allocateTransfo(n,m);
		int s = 0;
		for(int k=0;k<n;k++) {
			if(modelStructure.belongsToCluster[k] != -1) {
				clusterId=modelStructure.belongsToCluster[k];
				if(blockedCluster != -1) {
					if(clusterId < blockedCluster) {
						modelStructure.transfo[k][clusterId] = modelStructure.ratio[k];
					} else if(clusterId > blockedCluster) {
						modelStructure.transfo[k][clusterId-1] = modelStructure.ratio[k];
					}
				} else {
					modelStructure.transfo[k][clusterId] = modelStructure.ratio[k];
				}
			} else {
				modelStructure.transfo[k][clusters + s] = ONE;
				s++;
			}
		}

		modelStructure.blockedCluster = blockedCluster;

		return(modelStructure);

	} // coupling for doubles 


	// After computing the nullSpaceMatrix K, we invoke "coupling"
	// to compute the transformation matrix that connects
	// the full model and the reduced model
	// case of non-integer stoichiometric matrix
	// handled as type double

	public static ReducedModelDouble systemSimplification(GetInputData Stoich,double[][] K) {

		String[] metNames = Stoich.METNAMES;
		String[] rxnNames = new String[Stoich.RXNNAMES.length];

		for(int k=0;k<Stoich.RXNNAMES.length;k++) {
			rxnNames[k]=Stoich.RXNNAMES[k];
		}

		ModelStructureDouble modelStructure = coupling(K);
		modelStructure.addRxnNames(Stoich.RXNNAMES);
		double[][] transfo=modelStructure.transfo;

		double[][] A=Stoich.STOICH_VALUES;
		int m = A.length; 
		int n = A[0].length; 


		double[][] AIntRed = LA.matmul(A,transfo);

		// pick reference reactions for each column

		int s = transfo[0].length;
		double[] clusterLb=new double[s];
		double[] clusterUb=new double[s];

		int[] clusterSize=new int[s];
		for(int k=0;k<s;k++) {
			clusterSize[k]=0;
			for(int j=0;j<n;j++) {
				if(transfo[j][k] != ZERO) {
					clusterSize[k]++;
				}
			}
			double[] lb=new double[clusterSize[k]];
			double[] ub=new double[clusterSize[k]];
			int cntr=0;
			for(int j=0;j<n;j++) {
				if(transfo[j][k] > ZERO) {
					lb[cntr]=Stoich.lb[j]/transfo[j][k];
					ub[cntr]=Stoich.ub[j]/transfo[j][k];
					cntr++;
				} else if(transfo[j][k] < ZERO) {
					lb[cntr]=Stoich.ub[j]/transfo[j][k];
					ub[cntr]=Stoich.lb[j]/transfo[j][k];
					cntr++;
				}
			}
			clusterLb[k]=maximize(lb);
			clusterUb[k]=minimize(ub);
		}

		int[] refRxnIndex=new int[s];
		for(int k=0;k<s;k++) {
			int absMin = -1;
			double absVal = ZERO;
			for(int j=0;j<n;j++) {
				if(transfo[j][k] > ZERO) {
					if(absVal == ZERO) {
						absVal=transfo[j][k];
						absMin=j;
					} else if(absVal > transfo[j][k]) {
						absVal=transfo[j][k];
						absMin=j;
					}
				}
			}
			refRxnIndex[k] = absMin;
			if(clusterSize[k] > 1) {
				rxnNames[absMin] += "(founder)";		
				if(absVal != ONE) {
					rxnNames[absMin] += ":";
					rxnNames[absMin] += absVal;
				}
			}
		}


		// internal metabolites

		int internals=0;
		for(int i=0;i<m;i++) {
			double L1norm = ZERO;
			for(int j=0;j<s;j++) {
				if(Math.abs(AIntRed[i][j]) > L1norm) {
					L1norm=Math.abs(AIntRed[i][j]);
				}
			}
			if(L1norm < EPSILON) {

				if(DIAGNOSTICS) {
					System.out.println("Intermediate metabolite" + metNames[i]);
					for(int j=0;j<n;j++) {
						if(A[i][j] != ZERO) {
							System.out.println("Occurs in reaction " + rxnNames[j]);
							for(int k=0;k<s;k++) {
								if(transfo[j][k] != ZERO) {
									System.out.println("Cluster " + k + ": Intermediate metabolite " + metNames[i] + " in reaction " +  rxnNames[j]);
								}
							}
						}
					}
				}
				metNames[i]=null;

			} else {

				internals++;

			}

		}


		// external metabolites


		int externals=0;
		double[][] Aext = Stoich.STOICH_EXT;
		int p = Aext.length; 
		double[][] AExtRed = new double[0][AIntRed[0].length]; 
		if(p > 0) { 
			AExtRed = LA.matmul(Aext,transfo); 
		}     

		for(int i=0;i<p;i++) {
			double L1norm = ZERO;
			for(int j=0;j<s;j++) {
				if(Math.abs(AExtRed[i][j]) > L1norm) {
					L1norm=Math.abs(AExtRed[i][j]);
				}
			}
			if(L1norm < EPSILON) {

				if(DIAGNOSTICS) {
					System.out.println("Intermediate metabolite" + i);
					for(int j=0;j<n;j++) {
						if(Aext[i][j] != ZERO) {
							System.out.println("Occurs in reaction " + j);
							for(int k=0;k<s;k++) {
								if(transfo[j][k] != ZERO) {
									System.out.println("Cluster " + k + ": Intermediate metabolite " + metNames[i+m] + " in reaction" +  rxnNames[j]);
								}
							}
						}
					}
				}
				metNames[i+m]=null;

			} else {

				externals++;

			}

		}


		int[] refMetIndex = new int[internals+externals];
		int cntr=0;
		for(int k=0;k<metNames.length;k++) {
			if(metNames[k] != null) {
				refMetIndex[cntr]=k;
				cntr++;
			} 
		} 

		ReducedModelDouble reducedModel = reduceModel(metNames, rxnNames, refMetIndex, refRxnIndex, AIntRed, AExtRed, internals, externals, clusterLb, clusterUb, modelStructure);

		return(reducedModel);

	} // systemSimplification

	// After computing the nullSpaceMatrix K, we invoke "coupling"
	// to compute the transformation matrix that connects
	// the full model and the reduced model
	// case of (long) integer stoichiometric matrix

	public static ReducedModelLong systemSimplification(GetInputData Stoich, long[][] K) {

		String[] rxnNames = new String[Stoich.RXNNAMES.length];
		for(int k=0;k<Stoich.RXNNAMES.length;k++) {
			rxnNames[k]=Stoich.RXNNAMES[k];
		}	

		String[] metNames = Stoich.METNAMES;

		ModelStructureLong modelStructure = coupling(K);
		modelStructure.addRxnNames(Stoich.RXNNAMES);

		long[][] transfo = modelStructure.transfo;

		double[][] A       = Stoich.STOICH_VALUES;
		int m = A.length; 
		int n = A[0].length; 

		long[][] AIntRed = LA.matmul(A,transfo);

		// pick reference reactions for each column

		int s = transfo[0].length;
		double[] clusterLb=new double[s];
		double[] clusterUb=new double[s];
		int[] clusterSize=new int[s];
		for(int k=0;k<s;k++) {
			clusterSize[k]=0;
			for(int j=0;j<n;j++) {
				if(transfo[j][k] != ZERO) {
					clusterSize[k]++;
				}
			}

			double[] lb=new double[clusterSize[k]];
			double[] ub=new double[clusterSize[k]];
			int cntr=0;
			for(int j=0;j<n;j++) {
				if(transfo[j][k] > ZERO) {
					lb[cntr]=Stoich.lb[j]/transfo[j][k];
					ub[cntr]=Stoich.ub[j]/transfo[j][k];
					cntr++;
				} else if(transfo[j][k] < ZERO) {
					lb[cntr]=Stoich.ub[j]/transfo[j][k];
					ub[cntr]=Stoich.lb[j]/transfo[j][k];
					cntr++;
				}
			}
			clusterLb[k]=maximize(lb);
			clusterUb[k]=minimize(ub);

		}

		int[] refRxnIndex=new int[s];
		for(int k=0;k<s;k++) {
			int absMin = -1;
			long absVal = (long) (0);
			for(int j=0;j<n;j++) {
				if(transfo[j][k] > (long) 0) {
					if(absVal == (long) 0) {
						absVal=transfo[j][k];
						absMin=j;
					} else if(absVal > transfo[j][k]) {
						absVal=transfo[j][k];
						absMin=j;
					}
				}
			}
			refRxnIndex[k]=absMin;
			if(clusterSize[k] > 1) {
				rxnNames[absMin] += "(founder)";
				if(absVal != (long) (1)) {
					rxnNames[absMin] += ":";
					rxnNames[absMin] += absVal;
				}
			}
		}

		// internal metabolites

		int internals=0;
		for(int i=0;i<m;i++) {
			long L1norm = (long) 0;
			for(int j=0;j<s;j++) {
				if(Math.abs(AIntRed[i][j]) > L1norm) {
					L1norm=Math.abs(AIntRed[i][j]);
				}
			}
			if(L1norm == (long) 0) {

				if(DIAGNOSTICS) {
					System.out.println("Intermediate metabolite" + metNames[i]);
					for(int j=0;j<n;j++) {
						if(A[i][j] != ZERO) {
							System.out.println("Occurs in reaction " + rxnNames[j]);
							for(int k=0;k<s;k++) {
								if(transfo[j][k] != (long) 0) {
									System.out.println("Cluster " + k + ": Intermediate metabolite " + metNames[i] + " in reaction " +  rxnNames[j]);
								}
							}
						}
					}
				}

				metNames[i]=null;

			} else {

				internals++;

			}

		}


		// external metabolites

		int externals=0;
		double[][] Aext = Stoich.STOICH_EXT;
		int p = Aext.length; 
		long[][] AExtRed = new long[0][AIntRed[0].length]; 
		if(p > 0) { 
			AExtRed = LA.matmul(Aext,transfo); 
		}     

		for(int i=0;i<p;i++) {
			long L1norm = (long) 0;
			for(int j=0;j<s;j++) {
				if(Math.abs(AExtRed[i][j]) > L1norm) {
					L1norm=Math.abs(AExtRed[i][j]);
				}
			}
			if(L1norm == (long) 0) {

				if(DIAGNOSTICS) {
					System.out.println("Intermediate metabolite" + i);
					for(int j=0;j<n;j++) {
						if(Aext[i][j] != ZERO) {
							System.out.println("Occurs in reaction " + j);
							for(int k=0;k<s;k++) {
								if(transfo[j][k] != ZERO) {
									System.out.println("Cluster " + k + ": Intermediate metabolite" + metNames[i+m] + " in reaction" +  rxnNames[j]);
								}
							}
						}
					}
				}
				metNames[i+m]=null;

			} else {

				externals++;

			}
		}

		int[] refMetIndex = new int[internals+externals];

		int cntr=0;
		for(int k=0;k<metNames.length;k++) {
			if(metNames[k] != null) {
				refMetIndex[cntr]=k;
				cntr++;
			}
		}

		ReducedModelLong reducedModel = reduceModel(metNames,rxnNames,refMetIndex,refRxnIndex, AIntRed, AExtRed, internals, externals, clusterLb, clusterUb, modelStructure);

		return(reducedModel);

	} // systemSimplification

	public static ReducedModelLong reduceModel(String[] metNames,String[] rxnNames,int[] refMetIndex, int[] refRxnIndex,long[][] AIntRed, long[][] AExtRed, int internals, int externals, double[] lb, double ub[], ModelStructureLong modelStructure) {

		int mets      = refMetIndex.length;
		int metInt0 = AIntRed.length;
		int metExt0 = AExtRed.length;
		int rxns      = refRxnIndex.length;

		ReducedModelLong reducedModel = new ReducedModelLong(mets,rxns,internals,externals,modelStructure);

		for(int i=0;i<internals;i++) {
			int k=refMetIndex[i];
			// k must be < metInt0
			for(int j=0;j<rxns;j++) {
				reducedModel.getStoichValues()[i][j]=AIntRed[k][j];
			} 
		}

		for(int i=0;i<externals;i++) {
			int k=refMetIndex[i+internals] - metInt0;
			for(int j=0;j<rxns;j++) {
				reducedModel.getExternalStoichValues()[i][j]=AExtRed[k][j];
			} 
		}
		for(int i=0;i<mets;i++) {
			int k=refMetIndex[i];
			reducedModel.getMetaboliteNames()[i]=metNames[k];
		}
		for(int i=0;i<rxns;i++) {
			int k=refRxnIndex[i];
			reducedModel.getReactionNames()[i]=rxnNames[k];
		}
		for(int i=0;i<rxns;i++) {
			reducedModel.getLowerBounds()[i]=lb[i];
		}
		for(int i=0;i<rxns;i++) {
			reducedModel.getUpperBounds()[i]=ub[i];
		}

		return(reducedModel);

	} // end reduceModel for longs

	public static ReducedModelDouble reduceModel(String[] metNames,String[] rxnNames,int[] refMetIndex, int[] refRxnIndex,double[][] AIntRed, double[][] AExtRed, int internals, int externals, double[] lb, double[] ub, ModelStructureDouble modelStructure) {

		int mets      = refMetIndex.length;
		int metInt0   = AIntRed.length;
		int metExt0   = AExtRed.length;
		int rxns      = refRxnIndex.length;

		ReducedModelDouble reducedModel = new ReducedModelDouble(mets, rxns, internals, externals, modelStructure);

		for(int i=0;i<internals;i++) {
			int k=refMetIndex[i];
			// k must be < metInt0
			for(int j=0;j<rxns;j++) {
				reducedModel.getStoichValues()[i][j]=AIntRed[k][j];
			} 
		}
		for(int i=0;i<externals;i++) {
			int k=refMetIndex[i+internals] - metInt0;
			for(int j=0;j<rxns;j++) {
				reducedModel.getExternalStoichValues()[i][j]=AExtRed[k][j];
			} 
		}
		for(int i=0;i<mets;i++) {
			int k=refMetIndex[i];
			reducedModel.getMetaboliteNames()[i]=metNames[k];
		}
		for(int i=0;i<rxns;i++) {
			int k=refRxnIndex[i];
			reducedModel.getReactionNames()[i]=rxnNames[k];
		}
		for(int i=0;i<rxns;i++) {
			reducedModel.getLowerBounds()[i]=lb[i];
		}
		for(int i=0;i<rxns;i++) {
			reducedModel.getUpperBounds()[i]=ub[i];
		}

		return(reducedModel);

	} // end reduceModel for longs


	public static double minimize(double[] array) {
		double min=array[0];
		for(int k=0;k<array.length;k++) {
			if(array[k] < min) {
				min=array[k];
			}

		}
		return(min);
	}
	public static double maximize(double[] array) {
		double max=array[0];
		for(int k=0;k<array.length;k++) {
			if(array[k] > max) {
				max=array[k];
			}

		}
		return(max);
	}

	public static String continuedFraction(double number) {

		String sign="";
		if(number < SystemSimplification.ZERO) {
			sign = "-";
			number=Math.abs(number);
		}
		// System.out.println(number);
		double EPSILON = SystemSimplification.EPSILON;	    
		double ONE     = SystemSimplification.ONE;	    
		int MAXDEPTH=100;
		long[] a=new long[MAXDEPTH];
		String ratioString=null;

		double res=number;

		for(int k=0;k<MAXDEPTH;k++) {

			a[k]=Math.round(res+EPSILON);
			// System.out.println(k + " a[k] " + a[k]);
			if(res != a[k]) {
				res= 1/(res - a[k]);
			}

			long[] num   = new long[k+3];
			long[] denom = new long[k+3];

			num[0]   = (long) 0;
			num[1]   = (long) 1;
			denom[0] = (long) 1;
			denom[1] = (long) 0;

			for(int i=0;i<=k;i++) {
				num[i+2]   = a[i]*num[i+1] + num[i];
				denom[i+2] = a[i]*denom[i+1] + denom[i];
				ratioString = sign + Math.abs(num[i+2]) + "/" + Math.abs(denom[i+2]);
				double ratio = (double) num[i+2]/(double) denom[i+2];
				double abserr = Math.abs(ratio-number);
				if(abserr < EPSILON) {
					// System.out.println("iter " + i + " " + ratioString + " " + ratio);
					return(ratioString);
				} else if(Math.abs(number) > ONE) {
					double relerr = abserr/Math.abs(number);
					if(relerr < EPSILON) {
						return(ratioString);
					}
				}
			}

		}
		return(ratioString);

	} // continuedFraction

} // end class