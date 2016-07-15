package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class Mmatrixbuilder implements IndexedMatrixBuilder<IndexedSparseColtMatrix> {
	
	ISteadyStateModel model;
	Map<String,String> map;
	
	public Mmatrixbuilder (ISteadyStateModel model, Map<String,String> map){ //map <idMetabolito,Formula>
		this.model = model;
		this.map = map;		
	}
	
	@Override
	public IndexedSparseColtMatrix buildMatrix() {
		
		Map<String, Integer> rowIndex = getMapMet(model.getMetabolites()); 
		Map<String, Integer> colIndex = getMapElem(map);
		
		double [][] matM = new double [rowIndex.size()][colIndex.size()];
		
		for (String met: rowIndex.keySet()){
			int indexMet = rowIndex.get(met);
			MetaboliteFormula metFormula = new MetaboliteFormula(map.get(met));
			Map<String, Integer> metElement = metFormula.getElements();
			
			for (String elem : colIndex.keySet()) {
				int indexElem = colIndex.get(elem);
				Integer numElem = metElement.get(elem);
				if(numElem == null) numElem = 0;
				matM[indexMet][indexElem] = new Double(numElem);
			}
		}
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(matM);
		
		return new IndexedSparseColtMatrix(rowIndex, colIndex, matrix);
	}

	private Map<String, Integer> getMapMet(IndexedHashMap<String, ?> map) {
		
		Map<String, Integer> ret = new HashMap<>();
		for (int j =0; j < map.size(); j++){
			ret.put(map.getKeyAt(j), j);
		}
		return ret;
	}
	
	private Map<String, Integer> getMapElem (Map<String, String> map) {
		
		Map<String, Integer> ret = new HashMap<>();
		
		int i=0;
		for (String met: map.keySet()){
			MetaboliteFormula metFormula = new MetaboliteFormula(map.get(met));
			if (metFormula !=null){
				Set<String> comp = new HashSet<>(metFormula.getComponets());
				for (String c: comp){
					if (ret.containsKey(c) == false){
						ret.put(c,i);
						i++;				
					}
				}
			}
		}				
		return ret;
	}

}