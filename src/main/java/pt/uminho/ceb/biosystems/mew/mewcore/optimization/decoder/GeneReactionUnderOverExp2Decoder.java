package pt.uminho.ceb.biosystems.mew.mewcore.optimization.decoder;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.hybridset.HybridSetRepresentation;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class GeneReactionUnderOverExp2Decoder extends SteadyStateUnderOverExp2Decoder{

	public GeneReactionUnderOverExp2Decoder(ISteadyStateGeneReactionModel model){
		super(model);
	}
	

	public GeneReactionUnderOverExp2Decoder(ISteadyStateGeneReactionModel model, List<Integer> notAllowedGeneKnockouts){
		super(model, notAllowedGeneKnockouts);
	}

	
	public int getNumberVariables(){
		if (this.notAllowedRegulations==null) return ((ISteadyStateGeneReactionModel)model).getNumberOfGenes();
		else return ((ISteadyStateGeneReactionModel)model).getNumberOfGenes() - notAllowedRegulations.size();
	}
	
	public int getInitialNumberVariables(){
		return ((ISteadyStateGeneReactionModel)model).getNumberOfGenes();
	}
	
	public void createNotAllowedGenesFromIds (List<String> notAllowedGeneIds) throws Exception{
		notAllowedRegulations = new ArrayList<Integer>(notAllowedGeneIds.size());
		
		for(String notAllowedGeneID : notAllowedGeneIds)
			notAllowedRegulations.add( ((ISteadyStateGeneReactionModel)model).getGeneIndex(notAllowedGeneID));
		
		createInternalDecodeTable();
	}
	
	public List<Pair<Integer,Double>> decodeGeneRegulations(HybridSetRepresentation<Integer,Integer> genome) throws Exception
	{
		List<Pair<Integer,Double>> geneRegulationsList = new ArrayList<Pair<Integer,Double>>();

		int genomeSize = genome.getNumberOfElements();
		for(int i=0; i<genomeSize ;i++)
			geneRegulationsList.add(new Pair<Integer,Double>( convertValue(genome.getElementAt(i)) , convertExpressionValue(genome.getListValueAt(i)) ));
		
		return geneRegulationsList;
	}
	
	public GeneticConditions decode (IRepresentation solution) throws Exception
	{
		List<Pair<Integer,Double>> regulationList = decodeGeneRegulations((HybridSetRepresentation<Integer,Integer>)solution);
		
		GeneChangesList rcl = new GeneChangesList();
		rcl.setFromListPairsIndexValue(regulationList, (ISteadyStateGeneReactionModel) model);
		
		return new GeneticConditions(rcl, (ISteadyStateGeneReactionModel) model, true);
	}
}
