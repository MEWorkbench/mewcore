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
package pt.uminho.ceb.biosystems.mew.core.model.components;

import java.io.Serializable;
import java.util.List;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class ColtSparseStoichiometricMatrix implements IStoichiometricMatrix, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected DoubleMatrix2D matrix;
	
	
	public ColtSparseStoichiometricMatrix(DoubleMatrix2D stoichiometricMatrix) {
		this.matrix = stoichiometricMatrix;
	}

	public ColtSparseStoichiometricMatrix(int rows, int columns) {
		this.matrix = new SparseDoubleMatrix2D(rows, columns);
	}
	
	
	@Override
	public ColtSparseStoichiometricMatrix copy() {
		return new ColtSparseStoichiometricMatrix(matrix.copy());
	}

	@Override
	public double getValue(int metaboliteIndex, int reactionIndex) {
		return matrix.getQuick(metaboliteIndex, reactionIndex);
	}

	@Override
	public void setValue(int metaboliteIndex, int reactionIndex, double value)
	{
		matrix.setQuick(metaboliteIndex, reactionIndex, value);
	}

	@Override
	public IStoichiometricMatrix addColumn() {

		int newNumberCols = this.columns()  + 1;
		ColtSparseStoichiometricMatrix newMatrix = 
			new ColtSparseStoichiometricMatrix(this.rows(), newNumberCols);
	
		for(int j=0;j< this.columns();j++)
		{
			for(int i=0;i< this.rows();i++)
			{	
				double value = this.getValue(i, j);
				if (value!=0){
						newMatrix.setValue(i,j,value);
					}
			}
		}		
		
		return newMatrix;
	}

	
	@Override
	public ColtSparseStoichiometricMatrix removeColumns(List<Integer> indexesToRemove) {
		
		int newNumberCols = this.columns() - indexesToRemove.size();
		ColtSparseStoichiometricMatrix newMatrix = 
			new ColtSparseStoichiometricMatrix(this.rows(), newNumberCols);
	
		for(int j=0, k=0;j< this.columns();j++)
		{
			if (!indexesToRemove.contains(j))
			{
//				System.out.print("Reaction " + k + " ====> ");
				for(int i=0;i< this.rows();i++)
				{	
					double value = this.getValue(i, j);
					if (value!=0){
//						System.out.print("Met " + i + " Value = "+ value +" | ");
						newMatrix.setValue(i,k,value);
					}
				}
				k++;
//				System.out.println();
			}
		}
		return newMatrix;
	}

	@Override
	public ColtSparseStoichiometricMatrix removeRows(List<Integer> indexesToRemove) {
		int newNumberRows = this.rows() - indexesToRemove.size();
		ColtSparseStoichiometricMatrix newMatrix = 
			new ColtSparseStoichiometricMatrix(newNumberRows, this.columns());

		for(int i=0, k=0;i< this.rows();i++)
		{
			if (!indexesToRemove.contains(i))
			{
//				System.out.print("Metabolite " + k + " ====> ");
				for(int j=0;j< this.columns();j++)
				{	
					double value = getValue(i, j);
					if (value!=0){
//						System.out.print("reac " + j + " Value = "+ value +" | ");
						newMatrix.setValue(k,j,value);
					}
				}
				k++;
//				System.out.println();
			}
		}

		return newMatrix;
	}

	@Override
	public int columns() {
		return matrix.columns();
	}

	@Override
	public int rows() {
		return matrix.rows();
	}

	public DoubleMatrix2D convertToColt ()
	{
		return matrix;
	}

	@Override
	public double[] getColumn (int columnIndex) 
	{
		double[] col = new double[rows()];
		for(int i=0; i< rows(); i++)
			col[i] = getValue(i,columnIndex);
		return col;
	}

	@Override
	public double[] getRow (int rowIndex) 
	{
		double[] row = new double[columns()];
		for(int i=0; i< columns(); i++)
			row[i] = getValue(rowIndex,i);
		return row;
	}

	@Override
	public int columnCardinality(int columnIndex) {
		return matrix.viewColumn(columnIndex).cardinality();
	}

	@Override
	public int rowCardinality(int rowIndex) {
		return matrix.viewRow(rowIndex).cardinality();
	}
	
	public void printMatrix(){
		for(int i=0; i < matrix.columns(); i++){
			DoubleMatrix1D colum =  matrix.viewColumn(i);
			IntArrayList list = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			colum.getNonZeros(list, values);
			System.out.print("\n"+i+ " ====> ");
			for(int k =0; k < values.size();k++){
				System.out.print("idx = " + list.getQuick(k) +" val = "+values.getQuick(k) + " | ");
			}
		}
	}
	
	public void printRowsMatrix(){
		for(int i=0; i < matrix.rows(); i++){
			DoubleMatrix1D row =  matrix.viewRow(i);
			IntArrayList list = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			row.getNonZeros(list, values);
			System.out.print("\n"+i+ " ====> ");
			for(int k =0; k < values.size();k++){
				System.out.print("idx = " + list.getQuick(k) +" val = "+values.getQuick(k) + " | ");
			}
		}
	}

	@Override
	public int getRank(){
		Algebra algebra = new Algebra();
		if(matrix.columns() > matrix.rows()){
			DoubleMatrix2D matrixTranspose = algebra.transpose(matrix);
			return algebra.rank(matrixTranspose);
		}
		
		return algebra.rank(matrix);
	}


	
	
}
