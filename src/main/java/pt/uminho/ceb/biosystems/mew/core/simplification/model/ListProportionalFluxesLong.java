package pt.uminho.ceb.biosystems.mew.core.simplification.model;

import java.util.ArrayList;
import java.util.List;

public class ListProportionalFluxesLong extends ListEquivalentReactions{

	private static final long serialVersionUID = 1L;
	
	private List<Long> ratios;
	
	public ListProportionalFluxesLong(){
		super();
		ratios = new ArrayList<Long>();
	}

	public List<Long> getRatios() {
		return ratios;
	}

	public void setRatios(List<Long> ratios) {
		this.ratios = ratios;
	}
	
	public void addRatio(Long ratio){
		this.ratios.add(ratio);
	}
	
	public void addRationAt(int index, Long ratio){
		this.ratios.add(index, ratio);
	}

}
