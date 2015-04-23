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
package pt.uminho.ceb.biosystems.mew.mewcore.model.components;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;

public interface IStoichiometricMatrix {
	
	public int rows();
	
	public int columns();
	
	public double getValue(int metaboliteIndex, int reactionIndex);
	
	public void setValue(int metaboliteIndex, int reactionIndex, double value);
	
	public double[] getRow (int rowIndex);
	
	public double[] getColumn (int columnIndex);
	
	public IStoichiometricMatrix copy();
	
	public IStoichiometricMatrix removeColumns(List<Integer> indexesToRemove);
	
	public IStoichiometricMatrix removeRows(List<Integer> indexesToRemove);
	
	// adds column and returns new matrix
	public IStoichiometricMatrix addColumn();
	
	public int rowCardinality(int rowIndex);
	
	public int columnCardinality(int columnIndex);
	
	public DoubleMatrix2D convertToColt ();

	public int getRank();
	
}
