package pt.uminho.ceb.biosystems.mew.core.sampling.abstraction;

import java.util.ArrayList;


public class Bias {
	
	private String method; // uniform or normal. 1: uniform and 0: normal
	private ArrayList<String> index; // reaction indexes which to bias (nBias total)
	private double[][] param; // nBias x 2

	public Bias(){
		this.method = "";
		this.index = new ArrayList<String>();
		this.param = new double[index.size()][2];
	}
	
	public Bias(String method, ArrayList<String> index, double[][] param){
		this.method = method;
		this.index = index;
		this.param = param;
	}
	
	public Bias(Bias b) {
		this.method = b.getMethod();
		this.index = b.getIndex();
		this.param = b.getParam();
	}
	
	public String getMethod(){
		return this.method;
	}
	
	public ArrayList<String> getIndex() {
		return this.index;
	}
	
	public double[][] getParam() {
		return this.param;
	}
	
	public String getReaction(Integer ind){
		return this.index.get(ind);
	}
	
	public double getValueParam(Integer row, Integer col){
		return this.param[row][col];
	}
	
	public void setMethod(String newMethod){
		this.method = newMethod;
	}
	
	public void setIndex(ArrayList<String> newIndex){
		this.index = newIndex;
	}
	
	public void setParam(double[][] newParam){
		this.param = newParam;
	}
	
	public void setValueParam(Integer row, Integer col, Double value){
		this.param[row][col]=value;
	}
	
	//It will ignore the definition of the previous class and apply this
	@Override
	public boolean equals (Object o) {
        if(o==this) return true;
        
        if(o==null || (o.getClass()!=this.getClass()))
            return false;
            
        Bias b = (Bias) o;
        
        return this.method.equals(b.getMethod()) && this.index.equals(b.getIndex()) && this.param.equals(b.getParam());       
    }
}
