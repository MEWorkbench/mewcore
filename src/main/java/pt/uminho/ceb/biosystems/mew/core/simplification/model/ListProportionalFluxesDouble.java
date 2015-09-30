package pt.uminho.ceb.biosystems.mew.core.simplification.model;

import java.util.ArrayList;
import java.util.List;

public class ListProportionalFluxesDouble extends ListEquivalentReactions{

	private static final long serialVersionUID = 1L;
	
	private List<Double> ratios;
	private List<String> ratioStrings;
	
	public ListProportionalFluxesDouble(){
		super();
		ratios = new ArrayList<Double>();
		ratioStrings = new ArrayList<String>();
	}

	public List<Double> getRatios() {
		return ratios;
	}

	public void setRatios(List<Double> ratios) {
		this.ratios = ratios;
	}
	
	public void addRatio(Double ratio){
		this.ratios.add(ratio);
	}
	
	public void addRatioAt(int index, Double ratio){
		this.ratios.add(index, ratio);
	}
	
	public void addRatioString(String string){
		this.ratioStrings.add(string);
	}
	
	public void addRatioStringAt(int index, String string){
		this.ratioStrings.add(index, string);
	}

	public List<String> getRatioStrings() {
		return ratioStrings;
	}

	public void setRatioStrings(List<String> ratioStrings) {
		this.ratioStrings = ratioStrings;
	}

}
