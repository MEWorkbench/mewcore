package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.methods.algebra;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/** Class used to transform a matrix into its Row Echelon Form 
 * 
 * A matrix is in row echelon form (ref) when it satisfies the following conditions:
 *  1. The first non-zero element in each row, called the leading entry, is 1.
 *  2. Each leading entry is in a column to the right of the leading entry in the previous row.
 *  3. Rows with all zero elements, if any, are below rows having a non-zero element.
 *  
 *  
 * Any matrix can be transformed into its echelon forms, using a series of elementary row operations:
 *  
 *  1. Interchange rows i and j	: Ri <--> Rj
 *  2. Multiply row i by s, where s  0	: sRi --> Ri
 *  3. Add s times row i to row j : sRi + Rj --> Rj
 *  
 *  
 * Steps of the transformation:
 *  
 *  1. Pivot the matrix: 
 *    a. Find the pivot, the first non-zero entry in the first column of the matrix.
 *    b. Interchange rows, moving the pivot row to the first row.
 *    c. Multiply each element in the pivot row by the inverse of the pivot, so the pivot equals 1.
 *    d. Add multiples of the pivot row to each of the lower rows, so every element in the pivot column of the lower rows equals 0.
 *
 *  2. To get the matrix in row echelon form, repeat the pivot:
 *    a. Repeat the procedure from Step 1 above, ignoring previous pivot rows.
 *    b. Continue until there are no more pivots to be processed.
 *
 *  3. To get the matrix in reduced row echelon form, process non-zero entries above each pivot.
 *    a. Identify the last row having a pivot equal to 1, and let this be the pivot row.
 *    b. Add multiples of the pivot row to each of the upper rows, until every element above the pivot equals 0.
 *    c. Moving up the matrix, repeat this process for each row.
*/
public class EchelonTransformation {

	/** The original matrix */
	protected double[][] original;
	
	/** The matrix in its echelon form */
	protected double[][] transformed;
	
	/** Maps the indexes of the rows of the transformed matrix to the original matrix, since row permutations can 
	 * be used to transform the matrix. 
	 * The positions correspond to the index of the rows in the transformed matrix, and the values are the indexes in the
	 * original matrix. */
	protected int[] rowPermutations;
	
	
	public EchelonTransformation(double[][] originalMatrix){
		this.original = originalMatrix;
		this.transformed = new double[original.length][original[0].length];
		this.rowPermutations = new int[original.length];
		
		for(int r=0; r<original.length; r++)
		{
			for(int c=0; c<original[0].length; c++)
				transformed[r][c] = original[r][c];
			rowPermutations[r] = r;
		}
	}
	
	
	/** Verify if the matrix satisfy the conditions to be in row echelon form */
	public boolean validate(){
		
		int previousLeading = -1;
		boolean hasNonZeroRow = false; // if the validation already reached a row with all zero elements
		
		for(int r=0; r<transformed.length; r++)
		{
			boolean notFound = true; // flag that identifies if the current row leading entry has been not found
			for(int c=0; notFound && c<transformed[r].length; c++)
			{
				if(transformed[r][c]!=0)
				{	
System.out.println("TEST 3");
					if(hasNonZeroRow) // Third condition Failed!
						return false;
System.out.println("TEST 1");					
					if(transformed[r][c]!=1) // First condition Failed!
						return false;
System.out.println("TEST 2");					
					if(c<=previousLeading) // Second condition Failed!
						return false;
System.out.println("Valid Row " + r);					
					previousLeading = c;
					notFound = false;
				}
			}
			if(notFound)
				hasNonZeroRow = true; // If the line is reached, it means that the current row has only zero values
		}
		return true;
	}
	
	
	protected int findPivot(int column, int lastPivotRow){
		for(int r=lastPivotRow+1; r<transformed.length; r++)
			if(transformed[r][column] != 0)
				return r;
		return -1;
	}
	
	protected void permuteRows(int pivotRow, int lastPivotRow){
		int[] permutedRowIndexes = new int[rowPermutations.length];
		double[][] permutedRowsMatrix = new double[transformed.length][transformed[0].length];
		
		for(int r=0; r<=lastPivotRow; r++)
		{
			permutedRowIndexes[r] = rowPermutations[r];
			permutedRowsMatrix[r] = transformed[r];
		}
		
		permutedRowIndexes[lastPivotRow+1] = rowPermutations[pivotRow];
		permutedRowsMatrix[lastPivotRow+1] = transformed[pivotRow];
		
		for(int r=lastPivotRow+2; r<=pivotRow; r++)
		{
			permutedRowIndexes[r] = rowPermutations[r-1];
			permutedRowsMatrix[r] = transformed[r-1];
		}
		
		for(int r=pivotRow+1; r<transformed.length; r++)
		{
			permutedRowIndexes[r] = rowPermutations[r];
			permutedRowsMatrix[r] = transformed[r];
		}
		
		transformed = permutedRowsMatrix;
		rowPermutations = permutedRowIndexes;
	}
	
	/** Multiply each element in the pivot row by the inverse of the pivot, so the pivot equals 1 */
	protected void convertPivotToOne(int pivotRow, int pivotColumn){
		double pivot = transformed[pivotRow][pivotColumn];
		if(pivot!=1)
		{
			double pivotInverse = 1 / pivot;
			for(int c=pivotColumn; c<transformed[pivotRow].length; c++)
				transformed[pivotRow][c] *= pivotInverse;
		}
	}
	
	/** Add multiples of the pivot row to each of the lower rows, so every element in the
	 *  pivot column of the lower rows equals 0. */
	protected void zeroLowerPivotRows(int pivotRow, int pivotColumn){
		for(int r=pivotRow+1; r<transformed.length; r++)
		{
			if(transformed[r][pivotColumn]!=0)
			{
				double coeff = -1 *  transformed[r][pivotColumn];
				for(int c=pivotColumn; c<transformed[r].length; c++)
					transformed[r][c] += coeff * transformed[pivotRow][c];
			}
		}
	}
	
	/** Transform the original matrix into its Row Echelon Form */ 
	public double[][] transform(){
		int lastPivotRow = -1;
		for(int c=0; c<transformed[0].length; c++)
		{
			int pivotRow = findPivot(c, lastPivotRow);
			if(pivotRow>=0)
			{
				if(pivotRow>lastPivotRow+1)
					permuteRows(pivotRow, lastPivotRow);
				pivotRow = ++lastPivotRow;
				convertPivotToOne(pivotRow, c);
				zeroLowerPivotRows(pivotRow, c);
			}
		}
		return transformed;
	}
	
	/** For a nonzero matrix in row echelon form, its non zero rows are always linearly independent 
	 * If the transformed matrix contains rows with only zero values, it means that the corresponding rows in the 
	 * original matrix are linearly dependent 
	 * @return the linearly dependent rows of the original matrix */
	public int[] getLinearlyDependentRows(){
		int r=0;
		boolean nHasZeroRow = true;
		for(; nHasZeroRow && r<transformed.length; r++)
		{
			int c=0;
			for(; c<transformed[r].length && transformed[r][c]==0; c++);
			if(c==transformed[r].length)
				nHasZeroRow = false;
		}
		r--;
		if(!nHasZeroRow)
		{
			int nZeroRows = transformed.length-r;
			int[] lRows = new int[nZeroRows];
			for(int i=0; i<nZeroRows; i++)
				lRows[i] = rowPermutations[r+i];
			return lRows;	
		}
		return null;
	}
	
	/** For a nonzero matrix in row echelon form, its non zero rows are always linearly independent 
	 * If the transformed matrix contains rows with only zero values, it means that the corresponding rows in the 
	 * original matrix are linearly independent
	 * @return the original matrix without the linearly dependent rows */
	public double[][] getLinearlyIndependentRowsMatrix(){
		int r=0;
		boolean nHasZeroRow = true;
		Set<Integer> originalIndependentRows = new TreeSet<Integer>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		
		for(; nHasZeroRow && r<transformed.length; r++)
		{
			int c=0;
			for(; c<transformed[r].length && transformed[r][c]==0; c++);
			if(c==transformed[r].length)
				nHasZeroRow = false;
			else
				originalIndependentRows.add(rowPermutations[r]);
		}
		
		double[][] matrix = new double[originalIndependentRows.size()][original[0].length];
		System.out.println(originalIndependentRows.size());
		int i=0;
		for(int oRow : originalIndependentRows)
		{	
			for(int c=0; c<original[oRow].length; c++)
				matrix[i][c] = original[oRow][c];
			i++;
		}
		
		return matrix;
	}
	
	@Override
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("Original matrix:");
		for(int r=0; r<original.length; r++)
		{
			str.append("\n" + original[r][0]);
			for(int c=1; c<original[r].length; c++)
				str.append(" , " + original[r][c]);
		}
		
		str.append("\nTransformed matrix:");
		for(int r=0; r<transformed.length; r++)
		{
			str.append("\n" + transformed[r][0]);
			for(int c=1; c<transformed[r].length; c++)
				str.append(" , " + transformed[r][c]);
		}
		
		str.append("\nPermutations:");
		for(int r=0; r<rowPermutations.length; r++)
			str.append("\n\t T(" + r + ") -> O(" + rowPermutations[r] + ")");
		
		return str.toString();
	}
	
	public static void main(String[] args) {
		
//		double[][] originalMatrix = new double[][]{{0,3,0,1}, {0,6,0,2},{0,0,1,2}, {1,2,1,2},{0,9,0,3}};
		double[][] originalMatrix = new double[][]{ {5,6,7,8}, {-1,-2,1,0},{2,4,1,3}, {13,62,2,5}};
		
		EchelonTransformation echelon = new EchelonTransformation(originalMatrix);
		
//		int pivotRow = echelon.findPivot(1, 0);
//		System.out.println("Pivot row: " + pivotRow);
//		echelon.convertPivotToOne(pivotRow, 1);
//		System.out.println(echelon.toString());
//		echelon.permuteRows(pivotRow, 0);
//		System.out.println(echelon.toString());
//		
//		pivotRow = echelon.findPivot(2, 1);
//		System.out.println("Pivot row: " + pivotRow);
//		echelon.convertPivotToOne(pivotRow, 2);
//		System.out.println(echelon.toString());
//		echelon.permuteRows(pivotRow, 1);
//		System.out.println(echelon.toString());
		
		
		System.out.println(echelon.validate());
		echelon.transform();
		System.out.println(echelon.toString());
		
		int[] independentRows = echelon.getLinearlyDependentRows();
		if(independentRows!=null)
			for(int v : independentRows)
				System.out.println(" # " + v);
		
		double[][] indMatrix = echelon.getLinearlyIndependentRowsMatrix();
		System.out.print("Linearly independent rows matrix:");
		for(int r=0; r<indMatrix.length; r++)
		{
			System.out.print("\n" + indMatrix[r][0]);
			for(int c=1; c<indMatrix[r].length; c++)
				System.out.print(" , " + indMatrix[r][c]);
		}
		
		System.out.println();
		System.out.println(echelon.validate());
		
	}	
}
