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
package pt.uminho.ceb.biosystems.mew.core.simplification.model.nullspace.structure;

import java.util.Arrays;

import pt.uminho.ceb.biosystems.mew.core.simplification.model.nullspace.structure.interfaces.IModelStructureLong;

public class ModelStructureLong implements IModelStructureLong{
	
	public static int blockedCluster;
	public static int[] belongsToCluster;
	public static long[] ratio;
	public static long[][] transfo;
	public static String[] reactionNames;

	public ModelStructureLong(int n) {

		reactionNames=new String[n];
		belongsToCluster = new int[n];
		ratio=new long[n];
		Arrays.fill(belongsToCluster, -1); //NOTE: using Arrays.fill for increased performance.
	}

	public void allocateTransfo(int n, int m) {
		transfo = new long[n][m];
	}
	public void addRxnNames(String[] rxnNames) {
		reactionNames = rxnNames;
	}

	@Override
	public long[] getRatios() {
		return ratio;
	}

	@Override
	public long[][] getTransformations() {
		return transfo;
	}

	@Override
	public int getBlockedClusters() {
		return blockedCluster;
	}

	@Override
	public int[] getClusterBelongings() {
		return belongsToCluster;
	}

	@Override
	public String[] getReactionNames() {
		return reactionNames;
	}

}
