package pt.uminho.ceb.biosystems.mew.core.optimization.decoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.HybridSetRepresentation;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.NonExistentIdException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class SteadyStateUnderOverExp2Decoder implements ISteadyStateDecoder {
	
	private static final long		serialVersionUID		= 1L;
	protected ISteadyStateModel		model;
	protected List<Integer>			notAllowedRegulations	= null;
	protected ArrayList<Integer>	internalDecodeTable		= null;
	protected int					_specialIndex			= -6;
	
	public SteadyStateUnderOverExp2Decoder(ISteadyStateModel model) {
		this.model = model;
		this.notAllowedRegulations = null;
		this.internalDecodeTable = null;
	}
	
	public SteadyStateUnderOverExp2Decoder(ISteadyStateModel model, List<Integer> notAllowedReactionRegulations) {
		this.model = model;
		this.notAllowedRegulations = notAllowedReactionRegulations;
		createInternalDecodeTable();
	}
	
	public void setSpecialIndex(int specialIndex){
		_specialIndex = specialIndex;
	}
	
	public int getNumberVariables() {
		if (this.notAllowedRegulations == null)
			return model.getNumberOfReactions();
		else
			return model.getNumberOfReactions() - notAllowedRegulations.size();
	}
	
	public int getInitialNumberVariables() {
		return model.getNumberOfReactions();
	}
	
	public void addNotAllowedIds(List<String> notAllowedReactionIds) throws Exception {
		notAllowedRegulations = new ArrayList<Integer>(notAllowedReactionIds.size());
		
		Iterator<String> it = notAllowedReactionIds.iterator();
		while (it.hasNext()) {
			String nextId = it.next();
			int index = model.getReactionIndex(nextId);
			if (!notAllowedRegulations.contains(index)) notAllowedRegulations.add(index);
		}
		createInternalDecodeTable();
	}
	
	protected Integer convertValue(Integer valueToDecode) {
		Integer res = null;
		if (internalDecodeTable == null)
			res = valueToDecode;
		else {
			res = internalDecodeTable.get(valueToDecode);
		}
		return res;
	}
	
	protected double convertExpressionValue(Integer realValueAtIndex) {
		if (realValueAtIndex == _specialIndex)
			return 0.0;
		else
			return Math.pow(2, realValueAtIndex);
	}
	
	public List<Pair<Integer, Double>> decodeReactionRegulations(IRepresentation genome) throws Exception {
		List<Pair<Integer, Double>> regulationList = new ArrayList<Pair<Integer, Double>>();
		
		int genomeSize = ((HybridSetRepresentation) genome).getNumberOfElements();
		for (int i = 0; i < genomeSize; i++) {
			int reactionIndex = convertValue(((HybridSetRepresentation<Integer, Integer>) genome).getElementAt((i)));
			double expressionValue = convertExpressionValue(((HybridSetRepresentation<Integer, Integer>) genome).getListValueAt(i));
			
			regulationList.add(new Pair<Integer, Double>(reactionIndex, expressionValue));
		}
		
		return regulationList;
	}
	
	public GeneticConditions decode(IRepresentation solution) throws Exception {
		List<Pair<Integer, Double>> regulationList = decodeReactionRegulations(solution);
		
		ReactionChangesList rcl = new ReactionChangesList();
		rcl.setFromListPairsIndexValue(regulationList, model);
		
		return new GeneticConditions(rcl, true);
	}
	
//		protected void createInternalDecodeTable() 
//		{
//			int numberEntries = getNumberVariables();
//			
//			internalDecodeTable = new ArrayList<Integer>(numberEntries);
//	
//			if (this.notAllowedRegulations!= null)
//			{
//				Collections.sort(this.notAllowedRegulations);
//				int nextValue = 0;
//				int indexList = 0;
//				int indexTable = 0;
//				
//				for( ; nextValue< getInitialNumberVariables(); nextValue++ )
//					if (indexList < notAllowedRegulations.size() && notAllowedRegulations.get(indexList)==nextValue)
//						indexList++;
//					else
//					{
//						internalDecodeTable.add(indexTable, nextValue);
//						indexTable++;
//					}
//			}
//		}
	
	protected void createInternalDecodeTable() {
		this.internalDecodeTable = new ArrayList<Integer>();
		
		if (this.notAllowedRegulations != null) {
			Collections.sort(this.notAllowedRegulations);
			for (int nextValue = 0; nextValue < getInitialNumberVariables(); nextValue++)
				if (!notAllowedRegulations.contains(nextValue)) internalDecodeTable.add(nextValue);
		}
	}
	
	public ISteadyStateModel getModel() {
		return model;
	}
	
	public void setModel(ISteadyStateModel model) {
		this.model = model;
	}
	
	//	public void addDrainReactions(boolean includeTransports) throws NonExistentIdException{
	//				
	//		if(notAllowedRegulations==null)
	//			notAllowedRegulations = new ArrayList<Integer>();
	//		
	//		for(Reaction r : model.getReactions().values()){
	//			if(r.getType().equals(ReactionType.DRAIN)) {
	//				Integer indexToAdd = model.getReactionIndex(r.getId());
	//				if (!notAllowedRegulations.contains(indexToAdd))
	//				{
	//					notAllowedRegulations.add(indexToAdd);
	//				}
	//			}	
	//			else if(includeTransports && r.getType().equals(ReactionType.TRANSPORT))
	//			{
	//					Integer indexToAdd = model.getReactionIndex(r.getId());
	//					if (!notAllowedRegulations.contains(indexToAdd))
	//					{
	//						notAllowedRegulations.add(indexToAdd);
	//					}
	//			}
	//		}
	//		
	//		createInternalDecodeTable();
	//	}
	
	public void addDrainReactions(boolean includeTransports) throws NonExistentIdException {
		//		int ndrains = 0;
		//		int ntransports = 0;
		
		if (notAllowedRegulations == null) notAllowedRegulations = new ArrayList<Integer>();
		
		for (Reaction r : model.getReactions().values()) {
			Integer indexToAdd = model.getReactionIndex(r.getId());
			
			if (r.getType().equals(ReactionType.DRAIN)) {
				//				ndrains++;
				if (!notAllowedRegulations.contains(indexToAdd)) notAllowedRegulations.add(indexToAdd);
			} else if (includeTransports && r.getType().equals(ReactionType.TRANSPORT)) {
				//				ntransports++;
				if (!notAllowedRegulations.contains(indexToAdd)) notAllowedRegulations.add(indexToAdd);
				
			}
		}
		
		//		System.out.println("Added critical DRAINs: "+ ndrains+"\t Added critical Tranports: "+ntransports);
		
		createInternalDecodeTable();
	}
	
	//FIXME
	@Override
	public Object deepCopy() throws Exception {
		return null;
	}
	
}
