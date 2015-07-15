package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/** This Class is a Map where its values are lists V that extends LinkedList.
 *  Each LinkedList contain elements of the type E.
 *  The keys of this LinkedHash are of the type K */
public class LinkedMap<K,E,V extends LinkedList<E>> extends HashMap<K, V>{

	private static final long serialVersionUID = -887456648707706381L;

	public LinkedMap(){
		super();
	}
	
	/** This method adds an element to the LinkedList that belongs to the key */
	@SuppressWarnings("unchecked")
	public void add(K key, E element){
		if(containsKey(key))
		{
			V v = get(key);
			v.add(element);
			put(key,v);
		}
		else
		{
			List<E> v = new LinkedList<E>();
			v.add(element);
			put(key,(V) v);
		}
	}
 	
}
