package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa.utils;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class AlgebraUtils {
	
	public static double[][] removeRow(double[][] matrix, int row){
		double[][] res = null;
		if(row<matrix.length)
		{
			int l = matrix.length-1;
			res = new double[l][];
				
			int x=0;
			for(int i=0; i<l; i++)
			{
				if(i!=row)
				{
					res[x] = new double[matrix[i].length];
					for(int j=0; j<matrix[i].length; j++)
						res[x][j] = matrix[i][j];
					x++;
				}
			}
		}
		return res;
	}
	
	public static DoubleMatrix2D removeRow(DoubleMatrix2D matrix, int row){
		DenseDoubleMatrix2D res = null;
		if(row<matrix.rows())
		{
			int l = matrix.rows()-1;
			int c = matrix.columns();
			res = new DenseDoubleMatrix2D(l, c);
				
			int x=0;
			for(int i=0; i<l; i++)
			{
				if(i!=row)
				{
					for(int j=0; j<c; j++)
						res.set(x, j, matrix.get(i,j));
					x++;
				}
			}
		}
		return res;
	}
	
	public static double[][] removeRows(double[][] matrix, List<Integer> rows){
		double[][] res = null;
		if(rows.size()<matrix.length)
		{
			int l = matrix.length-rows.size();
			res = new double[l][];
				
			int x=0;
			for(int i=0; i<l; i++)
			{
				if(!rows.contains(i))
				{
					res[x] = new double[matrix[i].length];
					for(int j=0; j<matrix[i].length; j++)
						res[x][j] = matrix[i][j];
					x++;
				}
			}
		}
		return res;
	}
	
	public static double[][] removeColumn(double[][] matrix, int column){
		double[][] res = new double[matrix.length][];
		
		for(int i=0; i<matrix.length; i++)
		{
			int y=0;
			res[i] = new double[matrix[i].length-1];
			for(int j=0; j<matrix[i].length; j++)
				if(j!=column)
					res[i][y++] = matrix[i][j];
		}
		return res;
	}
	
	public static DoubleMatrix2D removeColumn(DoubleMatrix2D matrix, int column){
		int l = matrix.rows();
		int c = matrix.columns()-1;
		
		DenseDoubleMatrix2D res = new DenseDoubleMatrix2D(l, c);
		
		for(int i=0; i<l; i++)
		{
			int y=0;
			for(int j=0; j<c; j++)
				if(j!=column)
					res.set(i, y++, matrix.get(i, j));
		}
		return res;
	}
	
	public static double[][] removeColumns(double[][] matrix, List<Integer> columns){
		double[][] res = new double[matrix.length][];
		
		for(int i=0; i<matrix.length; i++)
		{
			int y=0;
			res[i] = new double[matrix[i].length-columns.size()];
			for(int j=0; j<matrix[i].length; j++)
				if(!columns.contains(j))
					res[i][y++] = matrix[i][j];
		}
		return res;
	}
	
	public static DoubleMatrix2D removeColumns(DoubleMatrix2D matrix, List<Integer> columns){
		int l = matrix.rows();
		int c = matrix.columns()-columns.size();
		DenseDoubleMatrix2D res = new DenseDoubleMatrix2D(l, c);
		
		for(int i=0; i<l; i++)
		{
			int y=0;
			for(int j=0; j<c; j++)
				if(!columns.contains(j))
					res.set(i, y++, matrix.get(i, j));
		}
		return res;
	}
	
	
	 public static DoubleMatrix2D matrixSubtraction(DoubleMatrix2D matrix1, DoubleMatrix2D matrix2) {
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

	 /** @return Returns the inverse or pseudo-inverse of matrix.
	  * Inverse(matrix) if the matrix is square, pseudoinverse otherwise: ((M. T^T))^-1) . M ) */
	 public static DoubleMatrix2D pseudoInverse(DoubleMatrix2D gcTransposeMatrix) {
		 Algebra algebra = new Algebra();
		 DoubleMatrix2D result = algebra.inverse(gcTransposeMatrix);
		 return result;
	 }


	 /** Return an nxn identity matrix */
	 public static DoubleMatrix2D buildIdentityMatrix(int n) {
		 double[][] iM = new double[n][n];  
		 for(int i=0; i<n; i++)
			 for(int j=0; j<n; j++)
				 iM[i][j] = (i==j) ? 1 : 0;
		 return new DenseDoubleMatrix2D(iM);
	 }
	
}
