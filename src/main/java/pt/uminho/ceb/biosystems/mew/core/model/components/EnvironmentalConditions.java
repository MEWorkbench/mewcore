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
package pt.uminho.ceb.biosystems.mew.core.model.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;

public class EnvironmentalConditions extends LinkedHashMap<String, ReactionConstraint> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected String id;
	
	public EnvironmentalConditions(){
		super();
		this.id = "";
	}
	
	public EnvironmentalConditions(String id){
		super();
		this.id = id;
	}
	
	public EnvironmentalConditions copy()
	{
		EnvironmentalConditions res = new EnvironmentalConditions();
		
		for(String p: keySet())
		{
			res.put(p, new ReactionConstraint(get(p)));
		}
		
		return res;
	}
	
	public void addReactionConstraint(String reactionId, ReactionConstraint reactionConstraint){
		put(reactionId, reactionConstraint);
	}
	
	// add all reaction constraints from another EC - merges both
	public void addAllReactionConstraints(EnvironmentalConditions ec)
	{
		
		if(ec!= null)
			for(String p: ec.keySet())
			{
				if(this.containsKey(p) )
					this.addReplaceReactionContraint(p, ec.get(p));	
				else this.addReactionConstraint(p, ec.get(p));
			}
	}
	
	
	public void addMultipleReactionConstraints(ISteadyStateModel model, Map<String, ReactionConstraint> constraints) throws NonExistentIdException{
	       
        for(String reactionId : constraints.keySet()){
                put(reactionId, constraints.get(reactionId));   
        }
}

	
	public boolean addReplaceReactionContraint(String reactionId,ReactionConstraint reactionConstraint){
		ReactionConstraint rc = get(reactionId);
		
		put(reactionId, reactionConstraint);
		
		if (rc == null) return false;
		else return true;
	}
	
	public ReactionConstraint getReactionConstraint(String reactionId) {
		return get(reactionId);
	}
		
	
	public void removeReactionConstraint(String reactionId){
		remove(reactionId);
	}
	
	public int getNumberOfEnvironmentalConditions(){
		return size();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void putAllFromContainer(Map<String, ReactionConstraintCI> contBounds){
		
		for(String id : contBounds.keySet()){
			
			put(id, new ReactionConstraint(contBounds.get(id)));
		}
	}
	
	// read and write from file added by M Rocha - May 7th 2010
	
	public void writeToFile (String outputFilename, String sep) throws Exception
	{
		FileWriter fw = new FileWriter(outputFilename);
//		BufferedWriter bw = new BufferedWriter(fw);
		write(fw, sep);
//		bw.close();
		fw.close();
	}
	
	public void write(OutputStreamWriter bw, String sep) throws IOException{
		for(String s: keySet())
		{
			ReactionConstraint rc = get(s);
			bw.write(s + sep + rc.getLowerLimit() + sep + rc.getUpperLimit() + "\n");
		}
		bw.flush();
	}
	
	public static EnvironmentalConditions readFromFile (String inputFilename, String sep) throws IOException {

		File f = new File(inputFilename);
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		EnvironmentalConditions envCond = new EnvironmentalConditions(f.getName());
		
		int errorNum = 0;
		Map<String, ReactionConstraint> wrongEnvCond = new HashMap(); 
		List<String> errors = new ArrayList<String>();
		
		int linecounter=0;
		while( br.ready() )
		{
			String str = br.readLine();
			String[] fields = str.split(sep);
			if (fields.length < 3){ 
				br.close();
				fr.close();
				throw new IOException ("Incomplete number of fields in environmental conditions file [" + inputFilename+"] line: "+linecounter);
			}
			
			double lowerBound = Double.parseDouble(fields[1]);
			double upperBound = Double.parseDouble(fields[2]);
			if (lowerBound > upperBound){
				errorNum++;
				wrongEnvCond.put(fields[0], new ReactionConstraint(lowerBound, upperBound));
				errors.add("The Entity with id: " + fields[0] + " has invalid bounds.");
				errors.add("Cause: UpperBound: " + upperBound + "\\< LowerBound: " + lowerBound);
			}
			
			if(errorNum == 0)
				envCond.addReactionConstraint(fields[0], new ReactionConstraint(lowerBound, upperBound));
			
			linecounter++;
		}
		
		if(errorNum > 0)
		{
			br.close();
			fr.close();
			
			throw new IOException (errors.toString());
		}
		
		br.close();
		fr.close();
		System.out.println("Finished reading environmental conditions from file ["+inputFilename+"].");
		
		return envCond;
	}
	
	@Override
	public boolean equals(Object o) {
		
		return o!=null && this.getClass().equals(o.getClass()) && super.equals(o) && id.equals(((EnvironmentalConditions)o).getId());
	}
	
}
