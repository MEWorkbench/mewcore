package pt.uminho.ceb.biosystems.mew.core.criticality.experimental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ModelType;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;

/**
 * E. coli Keio essentiality database basic utilities
 * 
 * @author pmaia
 * @version 0.1
 * @since metabolic3persistent
 */
public class ManualEssencials implements IExperimentalGeneEssentiality {
	
	protected Set<String>	_essentials		= null;
	protected boolean		_isReactions	= false;
	
	/*
	 * Score, essentiality. An “essentiality score” was calculated on the basis
	 * of all available data, in which each gene was given values. Keio: E, 1.5,
	 * N, -1.5, u, 0; MG_Tn5: N, -1, u, 0; genetic footprinting, E, 0.5, N,
	 * -0.5, X, ?, or -, 0; PEC, E, 1, N, -1, u, 0. The Essentiality score is
	 * the arithmetic sum. A value of 3 is highest, meaning all data from Keio,
	 * PEC, and genetic footprinting are E. Conversely, a value of -4 is lowest,
	 * meaning all results of Keio, MG_Tn5, PEC and genetic footprinting are N.
	 * As example, ileS and glyS have an essentiality score of 0, meaning that
	 * the results are ambiguous.
	 */
	protected double		_lowerThreshold	= 1.5;
	
	public ManualEssencials(String file, boolean isReactions) throws IOException {
		load(file);
		_isReactions = isReactions;
	}
	
	private void load(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		if (_essentials == null) _essentials = new HashSet<String>();
		
		while (br.ready()) {
			String line = br.readLine().trim();
			_essentials.add(line);
		}
		
		br.close();
		
		System.out.println("loaded " + _essentials.size() + " manual essentials");
	}
	
	public Set<String> getEssentials() {
		return _essentials;
	}
	
	public boolean isEssential(String identifier) {
		return _essentials.contains(identifier);
	}
	
	@Override
	public Set<String> getEssentialGenesFromModel(ISteadyStateModel model) throws Exception {
		if (!model.getModelType().equals(ModelType.GENE_REACTION_STEADY_STATE_MODEL))
			throw new IllegalArgumentException("Model type must be " + ModelType.GENE_REACTION_STEADY_STATE_MODEL.name());
		else {
			Set<String> essentials = new HashSet<String>();
			SteadyStateGeneReactionModel gprmodel = (SteadyStateGeneReactionModel) model;
			for (String g : gprmodel.getGenes().keySet())
				if (isEssential(g)) essentials.add(g);
			return essentials;
		}
	}
	
	@Override
	public Set<String> getEssentialReactionsFromModel(ISteadyStateModel model) throws Exception {
		if(!_isReactions){
			Set<String> essentialGenes = getEssentialGenesFromModel(model);
			GeneChangesList gcl = new GeneChangesList(essentialGenes);
			GeneticConditions gc = new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) model, false);
			gc.updateReactionsList((ISteadyStateGeneReactionModel) model);
			return gc.getReactionList().getReactionIds();
		}else{
			Set<String> essentials = new HashSet<String>();
			for(String r : model.getReactions().keySet())
				if(isEssential(r)) essentials.add(r);
			
			return essentials;
		}
	}
	
}