package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIndexedMatrix<T> implements Cloneable{

	Map<String,Integer> rowIndex;
	Map<String,Integer> colIndex;
	T matrix;
	
	public AbstractIndexedMatrix (Map<String,Integer> rowIndex,Map<String,Integer> colIndex, T matrix ){
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.matrix = matrix;
	}
	
	public abstract AbstractIndexedMatrix<T> transpose();
	public abstract AbstractIndexedMatrix<T> mult(AbstractIndexedMatrix<T> b);
	public abstract AbstractIndexedMatrix<T> solve(AbstractIndexedMatrix<T> b);
	public abstract AbstractIndexedMatrix<T> augmentedMatrix(AbstractIndexedMatrix<T> b);
	public abstract Integer rank();
	public abstract void remmoveRows(Set<String> idsToRemove);
	public abstract void remmoveCol(Set<String> idsToRemove);
	public abstract Double getValue(int row, int col);
	public abstract void printMatrix();
	public abstract AbstractIndexedMatrix<T> clone();
	
	
	public Double getValue (String row, String col){
		Double value = getValue(getRowIndex().get(row), getColIndex().get(col)); 
		return value;
	}
	
	public int getNumberOfRows (){
		return rowIndex.size();
	}
	
	public int getNumberOfColumns (){
		return colIndex.size();
	}
	
	public Map<String, Integer> getRowIndex() {
		return rowIndex;
	}
	
	public String getIDRow(Integer value) {
		String row = new String();
		for (String id:rowIndex.keySet()){
			if(rowIndex.get(id)==value){
				row=id;
			}
		}
		return row;
	}

	public void setRowIndex(Map<String, Integer> rowIndex) {
		this.rowIndex = rowIndex;
	}

	public Map<String, Integer> getColIndex() {
		return colIndex;
	}
	
	public String getIDColumn(Integer value) {
		String col = new String();
		for (String id:colIndex.keySet()){
			if(colIndex.get(id)==value){
				col=id;
			}
		}
		return col;
	}

	public void setColIndex(Map<String, Integer> colIndex) {
		this.colIndex = colIndex;
	}

	public T getMatrix() {
		return matrix;
	}

	public void setMatrix(T matrix) {
		this.matrix = matrix;
	}
	
	public Set<String> getZeroRows() {
		Set<String> ids = new HashSet<>();
		
		for(int i = 0; i < getNumberOfRows(); i++){
			Boolean zero = true;
			int j=0;
            while(j < getNumberOfColumns() && zero){
            	if (getValue(i,j) != 0.0){
            		 zero= false;
            	}
            	j++;
            }
            
			if (j == getNumberOfColumns()&& zero){
            	ids.add(getIDRow(i));
            }
            zero=true;
		}
		
		return ids;
	}

	public Set<String> getZeroColumns() {
		Set<String> ids = new HashSet<>(); 
		
		for(int j = 0; j < getNumberOfColumns(); j++){
			Boolean zero = true;
			int i=0;
            while(i < getNumberOfRows() && zero){
            	if (getValue(i,j) != 0.0){
            		 zero= false;
            	}
            	i++;
            }
            
			if (i == getNumberOfRows() && zero){
            	ids.add(getIDColumn(j));
            }
            zero=true;
		}
		
		return ids;
	}

}
