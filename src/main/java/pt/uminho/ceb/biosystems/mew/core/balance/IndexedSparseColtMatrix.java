package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class IndexedSparseColtMatrix extends AbstractIndexedMatrix<SparseDoubleMatrix2D>{

	public IndexedSparseColtMatrix(Map<String, Integer> rowIndex, Map<String, Integer> colIndex,
			SparseDoubleMatrix2D matrix) {
		super(rowIndex, colIndex, matrix); //to invoke a superclass's constructor of the AbstractIndexedMatrix class
	}

	@Override         // @Override -> element is meant to override an element declared in a superclass
	public IndexedSparseColtMatrix transpose() {
		
		Map<String, Integer> ri = new HashMap<>(colIndex);
		Map<String, Integer> ci= new HashMap<>(rowIndex);
		
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(getMatrix().viewDice().toArray()) ;
		
		return new IndexedSparseColtMatrix(ri, ci, matrix);
	}

	@Override
	public IndexedSparseColtMatrix mult(AbstractIndexedMatrix<SparseDoubleMatrix2D> b) {
		
		Map<String, Integer> ri = new HashMap<>(rowIndex);
		Map<String, Integer> ci= new HashMap<>(b.getColIndex());
		
		Algebra a = new Algebra();
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(a.mult(getMatrix(),b.getMatrix()).toArray());
		return new IndexedSparseColtMatrix(ri, ci, matrix);
	}
	
	@Override
	public AbstractIndexedMatrix<SparseDoubleMatrix2D> solve(AbstractIndexedMatrix<SparseDoubleMatrix2D> b) {
		Map<String, Integer> ri = new HashMap<>(colIndex);
		Map<String, Integer> ci= new HashMap<>(b.getColIndex());
		
		Algebra al = new Algebra();
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(al.solve(getMatrix(),b.getMatrix()).toArray());
		return new IndexedSparseColtMatrix(ri, ci, matrix);
	}
	
	@Override
	public AbstractIndexedMatrix<SparseDoubleMatrix2D> augmentedMatrix(AbstractIndexedMatrix<SparseDoubleMatrix2D> b) {
		
		Map<String, Integer> ri = new HashMap<>(rowIndex);
		double [][] newmatrix = new double [getNumberOfRows()][getNumberOfColumns()+b.getNumberOfColumns()];
		Map<String,Integer> newci =new HashMap<>(colIndex);
		
		int j = getNumberOfColumns();
		for (String idCol: b.getColIndex().keySet()){
			newci.put (idCol, j+b.getColIndex().get(idCol));
		}
		
		for (String idRow: rowIndex.keySet()){
			int indRow = rowIndex.get(idRow).intValue();
			for (String idCol: newci.keySet()){
				int indCol = newci.get(idCol).intValue();
				if (indCol < getNumberOfColumns()){
					newmatrix[indRow][indCol] = getValue(indRow, indCol);
				}
				else{
					newmatrix[indRow][indCol] = b.getValue(b.getRowIndex().get(idRow).intValue(), b.getColIndex().get(idCol).intValue());
				}
			}		
		}
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(newmatrix);
		return new IndexedSparseColtMatrix(ri, newci, matrix);
	}
	
	@Override
	public Integer rank() {
		Algebra a = new Algebra();
		DoubleMatrix2D coltMatrix = getMatrix();
		if (!(coltMatrix.rows()> coltMatrix.columns()))
			coltMatrix = coltMatrix.viewDice();
		return a.rank(coltMatrix);
		}
	
	@Override
	public void remmoveRows(Set<String> idsToRemove) {
		
		SparseDoubleMatrix2D mat = new SparseDoubleMatrix2D(getMatrix().toArray()) ;
		double [][] newmatrix = new double [rowIndex.size()-idsToRemove.size()][colIndex.size()];
		Map<String,Integer> newri =new HashMap<>();
		
		int i = 0;
		for (String id: rowIndex.keySet()){
			if ( idsToRemove.contains(id) == false){
				newri.put(id, i);
				i++;
			}
		}
		
		for (String idRow: newri.keySet()){
			int indRow = newri.get(idRow).intValue();
			for (String idCol: colIndex.keySet()){
				int indCol = colIndex.get(idCol).intValue();
				newmatrix[indRow][indCol] = mat.getQuick(rowIndex.get(idRow).intValue(), indCol); 
			}		
		}
		
		rowIndex = new HashMap<>(newri);
		matrix = new SparseDoubleMatrix2D(newmatrix);
				
	}
	
	@Override
	public void remmoveCol(Set<String> idsToRemove){
	
		SparseDoubleMatrix2D mat = new SparseDoubleMatrix2D(getMatrix().toArray()) ;
		double [][] newmatrix = new double [rowIndex.size()][colIndex.size()-idsToRemove.size()];
		Map<String,Integer> newci =new HashMap<>();
		
		int j = 0;
		for (String id: colIndex.keySet()){
			if ( idsToRemove.contains(id) == false){
				newci.put(id, j);
				j++;
			}
		}
		
		for (String idCol: newci.keySet()){
			int indCol = newci.get(idCol).intValue();
			for (String idRow: rowIndex.keySet()){
				int indRow = rowIndex.get(idRow).intValue();
				newmatrix[indRow] [indCol] = mat.getQuick(indRow, colIndex.get(idCol).intValue()); 
				}
				
		}
		
		colIndex = new HashMap<>(newci);
		matrix = new SparseDoubleMatrix2D(newmatrix);
	}
	
	@Override
	public void printMatrix() {
		System.out.println(matrix.toString());
				
	}

	@Override
	public Double getValue(int row, int col) {
		return matrix.getQuick(row, col);
	}

	@Override
	public IndexedSparseColtMatrix clone() {
		return new IndexedSparseColtMatrix(new HashMap<>(rowIndex), new HashMap<>(colIndex), (SparseDoubleMatrix2D) matrix.copy());
	}

}
