package pt.uminho.ceb.biosystems.mew.core.balance;

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class Smatrixbuilder implements IndexedMatrixBuilder<IndexedSparseColtMatrix>{
	
	ISteadyStateModel model;
	
	public Smatrixbuilder (ISteadyStateModel model){
		this.model = model;
		
	}

	@Override
	public IndexedSparseColtMatrix buildMatrix() {
		
		DoubleMatrix2D s = model.getStoichiometricMatrix().convertToColt();
		
		Map<String, Integer> rowIndex = getMap(model.getMetabolites());
		Map<String, Integer> colIndex = getMap(model.getReactions());
		
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(s.toArray());
		
		return new IndexedSparseColtMatrix(rowIndex, colIndex, matrix);
	}


	private Map<String, Integer> getMap(IndexedHashMap<String, ?> map) {
		
		Map<String, Integer> ret = new HashMap<>();
		for (int j =0; j < map.size(); j++){
			ret.put(map.getKeyAt(j), j);
		}
		return ret;
	}

}
