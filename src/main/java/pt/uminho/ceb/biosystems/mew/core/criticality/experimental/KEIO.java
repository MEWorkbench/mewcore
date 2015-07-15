package pt.uminho.ceb.biosystems.mew.core.criticality.experimental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * @date Jul 29, 2014
 * @version 0.1
 * @since metabolic3persistent
 */
public class KEIO implements IExperimentalGeneEssentiality {
	
	private static final String		DELIMITER				= ",";
	private static final int		HEADER_LINES			= 4;
	
	protected static final int		GENE_COLUMN_INDEX		= 1;
	protected static final int		BNUMBER_COLUMN_INDEX	= 6;
	protected static final int		SCORE_COLUMN_INDEX		= 13;
	
	protected Map<String, Double>	_keiodb					= null;
	protected Map<String, String>	_geneid2bnumber			= null;
	
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
	protected double				_lowerThreshold			= 1.5;
	
	public KEIO(String keioDBfile) throws IOException {
		_keiodb = new HashMap<String, Double>();
		_geneid2bnumber = new HashMap<String, String>();
		load(keioDBfile);
	}
	
	public KEIO(String keioDBfile, double lowerThreshold) throws IOException {
		this(keioDBfile);
		if (lowerThreshold > 3 || lowerThreshold < -4)
			throw new IllegalArgumentException("The lower threshold value must be between the range [-4,3]");
		else {
			_lowerThreshold = lowerThreshold;
		}
	}
	
	private void load(String keioDBfile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(keioDBfile));
		
		int skippedLines = 0;
		do {
			br.readLine();
			skippedLines++;
		} while (skippedLines < HEADER_LINES);
		
		while (br.ready()) {
			String line = br.readLine();
			String[] tokens = line.split(DELIMITER);
			
			String geneID = tokens[GENE_COLUMN_INDEX];
			String bnumber = tokens[BNUMBER_COLUMN_INDEX];
			Double score = null;
			try {
				score = Double.parseDouble(tokens[SCORE_COLUMN_INDEX]);
			} catch (NumberFormatException | NullPointerException e) {
				System.out.println("[line: " + skippedLines + "] Cannot parse score for gene [" + bnumber + "]");
			}
			
			if (score != null) {
				_keiodb.put(bnumber, score);
				if (geneID != null) {
					_geneid2bnumber.put(geneID, bnumber);
				} else
					System.out.println("[line: " + skippedLines + "] No gene ID for bnumber[" + bnumber + "]");
			}
			skippedLines++;
		}
		
		br.close();
	}
	
	public Set<String> getEssentials() {
		Set<String> essentials = new HashSet<String>();
		for (String g : _keiodb.keySet())
			if (_keiodb.get(g) >= _lowerThreshold) essentials.add(g);
		
		return essentials;
	}
	
	public boolean isEssential(String identifier) {
		Double score = null;
		if (_keiodb.containsKey(identifier))
			score = _keiodb.get(identifier);
		else if (_geneid2bnumber.containsKey(identifier)) score = _keiodb.get(_geneid2bnumber.get(identifier));
		
		if (score != null)
			return score >= _lowerThreshold;
		else return false;
//			throw new IllegalArgumentException("[" + identifier + "] does not seem to be a valid identifier.");
	}
	
	@Override
	public Set<String> getEssentialGenesFromModel(ISteadyStateModel model) throws Exception {
		if(!model.getModelType().equals(ModelType.GENE_REACTION_STEADY_STATE_MODEL))
			throw new IllegalArgumentException("Model type must be "+ModelType.GENE_REACTION_STEADY_STATE_MODEL.name());
		else{
			Set<String> essentials = new HashSet<String>();
			SteadyStateGeneReactionModel gprmodel = (SteadyStateGeneReactionModel) model;
			for(String g : gprmodel.getGenes().keySet())
				if(isEssential(g))
					essentials.add(g);
			return essentials;
		}
	}
	
	@Override
	public Set<String> getEssentialReactionsFromModel(ISteadyStateModel model) throws Exception {
		Set<String> essentialGenes = getEssentialGenesFromModel(model);
		GeneChangesList gcl = new GeneChangesList(essentialGenes);
		GeneticConditions gc = new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) model, false);
		gc.updateReactionsList((ISteadyStateGeneReactionModel) model);
		return gc.getReactionList().getReactionIds();
	}
	
	public static void main(String... args) throws IOException {
		String home = System.getenv("HOME");
		String file = home + "/ownCloud/documents/essentiality/keio/keio.csv";
		
		KEIO keio = new KEIO(file);
		
		int i = 0;
		for (String s : keio.getEssentials()) {
			System.out.println(i + " = " + s);
			i++;
		}
		
	}

}