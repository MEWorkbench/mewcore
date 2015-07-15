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
package pt.uminho.ceb.biosystems.mew.core.simplification.nullspace.structure;

import pt.uminho.ceb.biosystems.mew.core.simplification.nullspace.SystemSimplification;
import pt.uminho.ceb.biosystems.mew.core.simplification.nullspace.structure.interfaces.IModelStructure;
import pt.uminho.ceb.biosystems.mew.core.simplification.nullspace.structure.interfaces.IReducedModelDouble;

public class ReducedModelDouble implements IReducedModelDouble{

	protected 	String[] 	METNAMES;
	protected 	String[] 	RXNNAMES;
	protected 	double[][] 	STOICH_VALUES;
	protected 	double[][] 	STOICH_EXT;
	protected 	double[] 	LB;
	protected 	double[] 	UB;
	protected 	boolean 	reportAsFraction;
	private 	ModelStructureDouble myModelStructure;


	public ReducedModelDouble(int mets,int rxns, int internals, int externals, ModelStructureDouble modelStructure) {

		STOICH_VALUES 	=	new double[internals][rxns];
		STOICH_EXT 		=	new double[externals][rxns];
		RXNNAMES  		= 	new String[rxns];
		METNAMES 		=	new String[mets];
		LB       		= 	new double[rxns];
		UB       		= 	new double[rxns];
		reportAsFraction = true;
		myModelStructure = modelStructure;
	}

	public void reportModelStructure() {

		int blockedCluster = myModelStructure.getBlockedClusters();
		int[] belongsToCluster = myModelStructure.getClusterBelongings();
		double[] ratio = myModelStructure.getRatios();
		String[] rxnNames = myModelStructure.getReactionNames();

		int clusterId=0;
		while(clusterId < belongsToCluster.length) {
			StringBuffer str = new StringBuffer(); 
			int hits=0;
			for(int k=0;k<belongsToCluster.length;k++) {
				if(belongsToCluster[k] == clusterId) {
					if(Math.abs(Math.round(ratio[k])-ratio[k]) == (double) 0.0E0) {
						long longRatio=Math.round(ratio[k]);
						if(longRatio != (long) 1) {
							str.append(" ; (" + longRatio + ") " + rxnNames[k]);
						} else {
							str.append(" ; " + rxnNames[k]);
						}

					} else {
						String ratioString = "" + ratio[k];
						if(reportAsFraction) {
							ratioString = SystemSimplification.continuedFraction(ratio[k]);
						}
						str.append(" ; (" + ratioString + ") " + rxnNames[k]);
					}
					hits++;
				}
			}
			String text = str.toString();
			String text1 = text.replaceFirst(" ;","");

			if(hits > 0) {
				System.out.println("Cluster:" + clusterId);
				if(clusterId == blockedCluster) {
					System.out.println("blocked reactions");
				}
				System.out.println("Composition:" + text1);
				clusterId++;
			} else {
				break;
			}
		}
		for(int k=0;k<RXNNAMES.length;k++) {
			System.out.println("reference reaction:" + RXNNAMES[k] + "," + LB[k] + "," + UB[k]);
		}
		int internals=STOICH_VALUES.length;
		for(int k=0;k<METNAMES.length;k++) {
			String location=null;
			if(k<internals) {
				location="I";
			} else if(k>= internals) {
				location="E";
			}
			System.out.println("reference metabolites:" + METNAMES[k] + "," + location);
		}

	} // reportModelStructure

	@Override
	public double[][] getStoichValues() {
		return STOICH_VALUES;
	}

	@Override
	public double[][] getExternalStoichValues() {
		return STOICH_EXT;
	}

	@Override
	public double[] getLowerBounds() {
		return LB;
	}

	@Override
	public double[] getUpperBounds() {
		return UB;
	}

	@Override
	public String[] getMetaboliteNames() {
		return METNAMES;
	}

	@Override
	public String[] getReactionNames() {
		return RXNNAMES;
	}

	@Override
	public IModelStructure getModelStructure() {
		return myModelStructure;
	}

	@Override
	public void setReportAsFraction(boolean asFraction) {
		this.reportAsFraction = asFraction;
	}

	@Override
	public boolean isReportAsFraction() {
		return reportAsFraction;
	}
}
