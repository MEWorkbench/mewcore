package pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.set.SetRepresentation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.ReactionChangesList;

public class SteadyStateKnockoutDecoder implements ISteadyStateDecoder {

	private static final long serialVersionUID = 1L;
	protected ISteadyStateModel model; 
	protected List<Integer> notAllowedKnockouts = null;
	protected ArrayList<Integer> internalDecodeTable = null;
	
	
	public SteadyStateKnockoutDecoder(ISteadyStateModel model) {
		this.model = model;
		this.notAllowedKnockouts = null;
		this.internalDecodeTable = null;
	}
	

	public SteadyStateKnockoutDecoder(ISteadyStateModel model, List<Integer> notAllowedReactionKnockouts) {
		this.model = model;
		this.notAllowedKnockouts = notAllowedReactionKnockouts;
		createInternalDecodeTable();
	}


	public int getNumberVariables()
	{
		if (this.notAllowedKnockouts==null) return model.getNumberOfReactions();
		else return model.getNumberOfReactions() - notAllowedKnockouts.size();
	}

	public int getInitialNumberVariables()
	{
		return model.getNumberOfReactions();
	}

	public void addNotAllowedIds (List<String> notAllowedReactionIds) throws Exception
	{
		notAllowedKnockouts = new ArrayList<Integer>(notAllowedReactionIds.size());
		
		Iterator<String> it = notAllowedReactionIds.iterator();
		while(it.hasNext())
		{
			String nextId = it.next();
			int index = model.getReactionIndex(nextId);
			if(!notAllowedKnockouts.contains(index))
				notAllowedKnockouts.add(index);
		}
		createInternalDecodeTable();
	}
	
	public Integer convertValue (Integer valueToDecode)
	{
		Integer res = null;
		if (internalDecodeTable== null)	
			res = valueToDecode;
		else
		{
			res = internalDecodeTable.get(valueToDecode);
		}
		return res;
	}
	
	
	public List<Integer> decodeReactionKnockouts (TreeSet<Integer> genome) throws Exception
	{
		List<Integer> knockoutList = new ArrayList<Integer>();

		Iterator<Integer> it = genome.iterator();

		while(it.hasNext())
				knockoutList.add(convertValue(it.next()));
		
		return knockoutList;
	}
	
	public GeneticConditions decode (IRepresentation solution) throws Exception{
		
		//FIXME: The input of this function should be a SetRepresentation, the cast does not make sense here...
		TreeSet<Integer> genome = ((SetRepresentation)solution).getGenome();
		
		List<Integer> knockoutList = decodeReactionKnockouts(genome);
		
		ReactionChangesList rcl = new ReactionChangesList(knockoutList, model);
		
		return new GeneticConditions(rcl, false);
	}
	
	public int getNumberOfNotAllowedKnockouts(){
		if (this.notAllowedKnockouts!=null)
			return this.notAllowedKnockouts.size();
		else 
			return 0;
	}
	
	
	protected void createInternalDecodeTable()  
	{
		this.internalDecodeTable = new ArrayList<Integer>();

		if (this.notAllowedKnockouts!= null){
			Collections.sort(this.notAllowedKnockouts);
			for(int nextValue = 0 ; nextValue< getInitialNumberVariables(); nextValue++ )
				if(!notAllowedKnockouts.contains(nextValue))
						internalDecodeTable.add(nextValue); 
		}
	}

	
	public ISteadyStateModel getModel() {
		return model;
	}


	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}
	
	public void addDrainReactions(boolean includeTransports) throws NonExistentIdException{
		int ndrains = 0;
		int ntransports = 0;
				
		if(notAllowedKnockouts==null)
			notAllowedKnockouts = new ArrayList<Integer>();
		
		for(Reaction r : model.getReactions().values()){
			Integer indexToAdd = model.getReactionIndex(r.getId());
			
			if(r.getType().equals(ReactionType.DRAIN)) {
				ndrains++;
				if (!notAllowedKnockouts.contains(indexToAdd))
						notAllowedKnockouts.add(indexToAdd);
			}	
			else if(includeTransports && r.getType().equals(ReactionType.TRANSPORT))
			{
					ntransports++;
					if (!notAllowedKnockouts.contains(indexToAdd))
						notAllowedKnockouts.add(indexToAdd);
				
			}
		}
		
//		System.out.println("Added critical DRAINs: "+ ndrains+"\t Added critical Tranports: "+ntransports);
		
		createInternalDecodeTable();
	}


	public TreeSet<Integer> incode(Set<Integer> modelIdxKos) {
		
		System.out.println(internalDecodeTable);
		TreeSet<Integer> ret = new TreeSet<Integer>();
		
		for(Integer modelIndex : modelIdxKos){
			
//			System.out.print(modelIndex +" "+ model.getReactionId(modelIndex));
//			System.out.println("\t " +convertModelIdxToDecodeIdx(modelIndex));
			ret.add(convertModelIdxToDecodeIdx(modelIndex));
		}
		
		return ret;
	}

	
	public Integer convertModelIdxToDecodeIdx(Integer dataIdxModel){
		
		Integer decodeIndex = null;
		
		if (internalDecodeTable== null)	
			decodeIndex = dataIdxModel;
		else
		{
			decodeIndex = internalDecodeTable.indexOf(dataIdxModel);
				
		}
		return decodeIndex;
		
	}


	@Override
	public Object deepCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
