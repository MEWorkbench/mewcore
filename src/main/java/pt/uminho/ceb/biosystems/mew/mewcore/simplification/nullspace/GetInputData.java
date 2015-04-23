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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GetInputData {
	
	public static int dim1,dim2, dim3;
	public static double[][] STOICH_VALUES;
	public static double[][] STOICH_EXT;
	public static boolean[] REAC_REVERSIBILITIES;
	public static String[] RXNNAMES;
	public static String[] METNAMES;
	public static double[] lb;
	public static double[] ub;

	public GetInputData(int dim1, int dim2, int dim3){
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.dim3 = dim3;
		STOICH_VALUES = new double [dim1][dim2];
		STOICH_EXT = new double [dim3-dim1][dim2];
		REAC_REVERSIBILITIES = new boolean [dim2];
		RXNNAMES = new String[dim2];
		METNAMES = new String[dim3];
		lb       = new double[dim2];
		ub       = new double[dim2];
	}

	public GetInputData(String inputFileName) {

		ArrayList<String> arr = new ArrayList<String>(); 
		arr = fileReader(inputFileName);

		STOICH_VALUES = new double [dim1][dim2];
		STOICH_EXT = new double [dim3-dim1][dim2];
		REAC_REVERSIBILITIES = new boolean [dim2];
		RXNNAMES = new String[dim2];
		METNAMES = new String[dim3];
		lb       = new double[dim2];
		ub       = new double[dim2];

		String reversibility = new String(arr.get(0));
		StringTokenizer st0 = new StringTokenizer(reversibility,",");
		int col = 0;
		while(st0.hasMoreTokens()) {
			String key = st0.nextToken();
			Boolean B = new Boolean(key);
			boolean bool = B.booleanValue();
			REAC_REVERSIBILITIES[col]=bool;
			col++;
		}

		for(int kk=1;kk<=dim1;kk++) {
			int l=0;
			String blah=new String(arr.get(kk));
			StringTokenizer st = new StringTokenizer(blah,",");
			while(st.hasMoreTokens()) {
				String key = st.nextToken();
				Double D = new Double(key);
				double d = D.doubleValue();
				int k = kk - 1;
				STOICH_VALUES[k][l] = d;
				//		System.out.println("(k,l): (" + k + "," + l + ") => " + d); 
				l++;
			}
		}
		for(int kk=dim1+1;kk<=dim3;kk++) {
			int l=0;
			String blah=new String(arr.get(kk));
			StringTokenizer st = new StringTokenizer(blah,",");
			while(st.hasMoreTokens()) {
				String key = st.nextToken();
				Double D = new Double(key);
				double d = D.doubleValue();
				int k = kk - 1 - dim1;
				STOICH_EXT[k][l] = d;
				//		System.out.println("(k,l): (" + k + "," + l + ") => " + d); 
				l++;
			}
		}
		int kk=dim3+1;
		String blah=new String(arr.get(kk));
		StringTokenizer st = new StringTokenizer(blah,",");
		int cntr=0;
		while(st.hasMoreTokens()) {
			String key = st.nextToken();
			// System.out.println("MET:" + key);
			METNAMES[cntr]=key;
			cntr++;
		}
		kk=dim3+2;
		blah=new String(arr.get(kk));
		st = new StringTokenizer(blah,",");
		cntr=0;
		while(st.hasMoreTokens()) {
			String key = st.nextToken();
			// System.out.println("RXN:" + key);
			RXNNAMES[cntr]=key;
			cntr++;
		}

		kk=dim3+3;
		blah=new String(arr.get(kk));
		st = new StringTokenizer(blah,",");
		cntr=0;
		while(st.hasMoreTokens()) {
			String key = st.nextToken();
			Double D = new Double(key);
			double d = D.doubleValue();
			System.out.println("LB:" + key);
			lb[cntr]=d;
			cntr++;
		}
		kk=dim3+4;
		blah=new String(arr.get(kk));
		st = new StringTokenizer(blah,",");
		cntr=0;
		while(st.hasMoreTokens()) {
			String key = st.nextToken();
			Double D = new Double(key);
			double d = D.doubleValue();
			System.out.println("UB:" + key);
			ub[cntr]=d;
			cntr++;
		}


	} // constructor GetInputData

	public static ArrayList<String> fileReader(String fileName) {

		ArrayList<String> arr = new ArrayList<String>();
		// String fileName = "largeExample.Terzer.long";
		if ((fileName == null) || (fileName == "")) {
			throw new IllegalArgumentException();
		}

		String line;

		try{    

			BufferedReader in = new BufferedReader(new FileReader(fileName));

			if (!in.ready()) {
				throw new IOException();
			}
			line = in.readLine();
			StringTokenizer st = new StringTokenizer(line,",");
			while(st.hasMoreTokens()) {
				String str1 = st.nextToken();
				String str2 = st.nextToken();
				String str3 = st.nextToken();
				Integer I1 = new Integer(str1);
				Integer I2 = new Integer(str2);
				Integer I3 = new Integer(str3);
				dim1 = I1.intValue();
				dim2 = I2.intValue();
				dim3 = I3.intValue();
			}
			while ((line = in.readLine()) != null) {
				arr.add(line);
			}
			in.close();

		} catch (IOException e) {
			
			System.out.println(e);
		}
		return(arr);

	}

 // GetInputData

}
