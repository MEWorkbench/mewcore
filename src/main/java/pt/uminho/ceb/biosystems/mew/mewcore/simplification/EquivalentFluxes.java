/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.mewcore.simplification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class EquivalentFluxes implements Serializable{
	
	private static final long serialVersionUID = 1L;

	protected List<ListEquivalentReactions> equivalenceList;
//	private List<Integer> rowsToRemove;
	
	
	public EquivalentFluxes(List<ListEquivalentReactions> list) {
		this.equivalenceList = list;
	}
	
//	public ISteadyStateModel simplifyModel() throws Exception

//	}
	
    public int numberFluxesReduced ()
    {
    	int res = 0;
    	for(int i=0; i < this.equivalenceList.size(); i++)
      		res += (equivalenceList.get(i).numberReactions()-1);    	
    	return res;
    }
    
    public List<Set<String>> getEquivalenceLists()
    {
    	ArrayList<Set<String>> res = 
    		new ArrayList<Set<String>> ( this.equivalenceList.size() );
    	for(int i=0; i < this.equivalenceList.size(); i++)
    	{
    		res.add(equivalenceList.get(i).getListIds());
    	}
    	return res;
    }
    
    public List<ListEquivalentReactions> getListsOfEquivalences(){
    	return this.equivalenceList;
    }
 
    public void printList()
    {
    	for(int i=0; i < this.equivalenceList.size(); i++)
    	{
    		System.out.println(this.equivalenceList.get(i).toString());
    	}
    }
    
    public Set<String> assumeUnchanged() throws Exception{
    	Set<String> set = new HashSet<String>();
    	
    	
    	Set<String> todebug = new HashSet<String>();
    	
    	for(ListEquivalentReactions ler : equivalenceList){
    		
    		String idBase = ler.getListIds().iterator().next();
    		
    		Set<String> debug = CollectionUtils.getIntersectionValues(ler.getListIds(), todebug);
    		
    		
    		if(debug.size()>0){
    			System.out.println(todebug);
    			System.out.println(debug);
    			System.out.println(ler.getListIds());
    			throw new Exception("problem with equivalence");
    		}
    		todebug.removeAll(ler.getListIds());
    		
    		
    		Set<String> newSet = new HashSet<String>(ler.getListIds());
    		newSet.remove(idBase);
    		
    		set.addAll(newSet);
    	}
    	
    	return set;
    }
    
//    public Set<String> getListRemovedReactions()
//    {
//    	if (this.equivalenceList == null) return null;
//    	
//    	Set<String> res = new Set<String>();
//      	for(int i=0; i < this.equivalenceList.size(); i++)
//    	{
//      		ListEquivalentReactions ler = equivalenceList.get(i);
//      		for(int k=1; k < ler.numberReactions(); k++)
//    		{
//				res.add( ler.getListIds().get(k) );
//    		}
//    	}
//      	return res;
//    }
    
    
//    public List<String> getListRemovedMetabolites()
//    {
//    	if (this.rowsToRemove == null) return null;
//    	
//    	List<String> res = new ArrayList<String>();
//    	for(int i=0; i < this.rowsToRemove.size(); i++)
//    	{
//    		res.add( model.getMetaboliteId(rowsToRemove.get(i)) );
//    	}
//    	
//    	return res;
//    }
    
}
