package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.utils;

public class ThreeTuple<A,B,C> {
	
	protected A a;
	protected B b;
	protected C c;
	
	
	public ThreeTuple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}


	public A getA() {return a;}
	public void setA(A a) {this.a = a;}
	public B getB() {return b;}
	public void setB(B b) {this.b = b;}
	public C getC() {return c;}
	public void setC(C c) {this.c = c;}
}
