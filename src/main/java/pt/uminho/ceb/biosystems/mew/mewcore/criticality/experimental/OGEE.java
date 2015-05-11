package pt.uminho.ceb.biosystems.mew.mewcore.criticality.experimental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.mewcore.model.components.enums.ModelType;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.model.steadystatemodel.gpr.SteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneChangesList;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

/**
 * 
 * Simple utilities for interaction if OGEE Gene essentiality database
 * //http://ogeedb.embl.de/
 * 
 * @author pmaia
 * @date Jul 29, 2014
 * @version 1.0
 * @since metabolic3persistent
 */
public class OGEE implements IExperimentalGeneEssentiality {
	
	private static final String					DELIMITER				= "\t";
	
	protected static final int					ORGANISM_COLUMN_INDEX	= 0;
	protected static final int					DATASET_COLUMN_INDEX	= 2;
	protected static final int					LOCUS_COLUMN_INDEX		= 3;
	protected static final int					ESSENTIAL_COLUMN_INDEX	= 4;
	
	protected Map<String, Map<String, Boolean>>	_oggedb					= null;
	protected String							_organismID				= null;
	
	/**
	 * For each organism, several datasets are available, this number represents
	 * the percentage of these datasets that must predict a given gene as
	 * essential for that gene to be effectively considered essential
	 */
	protected double							_minimumAggrement		= 3 / 3;
	
	public OGEE(String ogeeDBfile, String organismID) throws IOException {
		_oggedb = new HashMap<String, Map<String, Boolean>>();
		_organismID = organismID;
		load(ogeeDBfile);
	}
	
	private void load(String ogeeDBfile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(ogeeDBfile));
		br.readLine(); //skip header
		
		//		int skippedLines = 0;
		while (br.ready()) {
			String line = br.readLine();
			String[] tokens = line.split(DELIMITER);
			String organism = tokens[ORGANISM_COLUMN_INDEX];
			String dataset = tokens[DATASET_COLUMN_INDEX];
			String locus = tokens[LOCUS_COLUMN_INDEX];
			String essentialString = tokens[ESSENTIAL_COLUMN_INDEX];
			
			boolean essential = (essentialString.equalsIgnoreCase("y")) ? true : false;
			
			if (organism.equalsIgnoreCase(_organismID)) {
				if (_oggedb.containsKey(locus)) {
					_oggedb.get(locus).put(dataset, essential);
				} else {
					HashMap<String, Boolean> dsmap = new HashMap<String, Boolean>();
					dsmap.put(dataset, essential);
					_oggedb.put(locus, dsmap);
				}
			}
			//			skippedLines++;
		}
		
		br.close();
	}
	
	public Set<String> getEssentials() {
		Set<String> essentials = new HashSet<String>();
		for (String g : _oggedb.keySet())
			if (isEssential(g)) essentials.add(g);
		
		return essentials;
	}
	
	public boolean isEssential(String identifier) {
		if (_oggedb.containsKey(identifier)) {
			Map<String, Boolean> dsmap = _oggedb.get(identifier);
			int ne = 0;
			for (String s : dsmap.keySet()) {
				if (dsmap.get(s)) ne++;
			}
			return ((double) ne / (double) dsmap.size()) >= _minimumAggrement;
		} else return false;
//			throw new IllegalArgumentException("[" + identifier + "] does not seem to be a valid identifier.");
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
		Set<String> essentialGenes = getEssentialGenesFromModel(model);
		GeneChangesList gcl = new GeneChangesList(essentialGenes);
		GeneticConditions gc = new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) model, false);
		gc.updateReactionsList((ISteadyStateGeneReactionModel) model);
		return gc.getReactionList().getReactionIds();
	}
	
	public static void main(String... args) throws IOException {
		String home = System.getenv("HOME");
		String file = home + "/ownCloud/documents/essentiality/ogee/ogee.csv";
		String keiofile = home + "/ownCloud/documents/essentiality/keio/keio.csv";
		
		OGEE ogee = new OGEE(file, "Escherichia coli K12");
		KEIO keio = new KEIO(keiofile);
		
		Set<String> keioEssentials = keio.getEssentials();
		Set<String> ogeeEssentials = ogee.getEssentials();
		
		Set<String> intersect = CollectionUtils.getIntersectionValues(keioEssentials, ogeeEssentials);
		Set<String> diff = CollectionUtils.getSetDiferenceValues(keioEssentials, ogeeEssentials);
		
		System.out.println("intersect = " + intersect.size());
		System.out.println("diff = " + diff.size());
		for (String s : diff)
			System.out.println("\t" + s);
		
	}
	
}
