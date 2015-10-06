package pt.uminho.ceb.biosystems.mew.core.criticality;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.MetaboliteFormula;
import pt.uminho.ceb.biosystems.mew.core.criticality.experimental.IExperimentalGeneEssentiality;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

public abstract class AbstractOptimizationTargetsStrategy implements IOptimizationTargetsStrategy {	
	
	protected Integer								_carbonOffset				= null;
	
	protected Container								_container					= null;
	
	protected ISteadyStateModel						_model						= null;
	
	protected EnvironmentalConditions				_environmentalConditions	= null;
	
	protected SolverType							_solver						= null;
	
	protected Set<String>							_ignoredCofactors			= null;
	
	protected Set<String>							_ignoredPathways			= null;
	
	protected List<IExperimentalGeneEssentiality>	_experimental				= null;
	
	protected boolean								_onlyDrains					= false;
	
	protected Map<TargetIDStrategy, Set<String>>	_flags_data					= new IndexedHashMap<TargetIDStrategy, Set<String>>();
	protected Map<TargetIDStrategy, Flag>			_flags						= new IndexedHashMap<TargetIDStrategy, Flag>();
	
	public AbstractOptimizationTargetsStrategy(Container container, ISteadyStateModel model, EnvironmentalConditions environmentalConditions, SolverType solver, Set<String> ignoredPathways, Set<String> ignoredCofactors, Integer carbonOffset) {
		
		_carbonOffset = carbonOffset;
		_container = container;
		_model = model;
		_environmentalConditions = environmentalConditions;
		_solver = solver;
		_ignoredCofactors = ignoredCofactors;
		_ignoredPathways = ignoredPathways;
		loadFlags();
	}
	
	private void loadFlags() {
		
		Flag IDENTIFY_CRITICAL = new Flag(TargetIDStrategy.IDENTIFY_CRITICAL);
		Flag IDENTIFY_ZEROS = new Flag(TargetIDStrategy.IDENTIFY_ZEROS);
		Flag IDENTIFY_EQUIVALENCES = new Flag(TargetIDStrategy.IDENTIFY_EQUIVALENCES);
		Flag IDENTIFY_NONGENE_ASSOCIATED = new Flag(TargetIDStrategy.IDENTIFY_NONGENE_ASSOCIATED);
		Flag IDENTIFY_DRAINS_TRANSPORTS = new Flag(TargetIDStrategy.IDENTIFY_DRAINS_TRANSPORTS);
		Flag IDENTIFY_PATHWAY_RELATED = new Flag(TargetIDStrategy.IDENTIFY_PATHWAY_RELATED);
		Flag IDENTIFY_HIGH_CARBON_RELATED = new Flag(TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED);
		Flag IDENTIFY_NO_FLUX_WT = new Flag(TargetIDStrategy.IDENTIFY_NO_FLUX_WT);
		Flag IDENTIFY_EXPERIMENTAL = new Flag(TargetIDStrategy.IDENTIFY_EXPERIMENTAL);
		
		_flags.put(TargetIDStrategy.IDENTIFY_DRAINS_TRANSPORTS, IDENTIFY_DRAINS_TRANSPORTS);
		_flags.put(TargetIDStrategy.IDENTIFY_NONGENE_ASSOCIATED, IDENTIFY_NONGENE_ASSOCIATED);
		_flags.put(TargetIDStrategy.IDENTIFY_PATHWAY_RELATED, IDENTIFY_PATHWAY_RELATED);
		_flags.put(TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED, IDENTIFY_HIGH_CARBON_RELATED);
		_flags.put(TargetIDStrategy.IDENTIFY_EXPERIMENTAL, IDENTIFY_EXPERIMENTAL);
		_flags.put(TargetIDStrategy.IDENTIFY_CRITICAL, IDENTIFY_CRITICAL);
		_flags.put(TargetIDStrategy.IDENTIFY_ZEROS, IDENTIFY_ZEROS);
		_flags.put(TargetIDStrategy.IDENTIFY_EQUIVALENCES, IDENTIFY_EQUIVALENCES);
		_flags.put(TargetIDStrategy.IDENTIFY_NO_FLUX_WT, IDENTIFY_NO_FLUX_WT);
	}
	
	public Set<String> findHighCarbonMetabolites() {
		BalanceValidator balance = new BalanceValidator(_container);
		balance.setFormulasFromContainer();
		Map<String, MetaboliteCI> metabolites = _container.getMetabolites();
		Set<String> metabolitesNoFormula = balance.getMetabolitesWithoutFormulas();
		
		Set<String> metabolitesToAnalyze = CollectionUtils.getSetDiferenceValues(metabolites.keySet(), metabolitesNoFormula);
		Set<String> highCarbonMetabolites = new HashSet<String>();
		
		if (_ignoredCofactors != null && !_ignoredCofactors.isEmpty()) metabolitesToAnalyze = CollectionUtils.getSetDiferenceValues(metabolitesToAnalyze, _ignoredCofactors);
		
		for (String met : metabolitesToAnalyze) {
			MetaboliteFormula formula = balance.getformula(met);
			int numCarbons = formula.getValue(OptimizationTargetsControlCenter.CARBON);
			if (numCarbons > _carbonOffset) highCarbonMetabolites.add(met);
		}
		return highCarbonMetabolites;
	}
	
	@Override
	public void addExperimentalValidations(IExperimentalGeneEssentiality... experimental) {
		for (int i = 0; i < experimental.length; i++) {
			getExperimentalValidations().add(i, experimental[i]);
		}
	}
	
	public void addPathways(String... pathways) {
		for (String p : pathways) {
			if (!_container.getPathwaysIDs().contains(p))
				throw new IllegalArgumentException("[" + p + "] is not a valid pathway identifier for the current model.");
			else {
				getIgnoredPathways().add(p);
				_flags.get(TargetIDStrategy.IDENTIFY_PATHWAY_RELATED).on();
				;
			}
		}
	}
	
	public void addCofactorsToIgnore(String... cofactors) {
		for (String c : cofactors) {
			if (_container.getMetabolite(c) == null)
				throw new IllegalArgumentException("[" + c + "] is not a valid metabolite (co-factor) identifier for the current model.");
			else {
				getIgnoredCofactors().add(c);
				_flags.get(TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED).on();
			}
		}
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
		_environmentalConditions = environmentalConditions;
		_flags.get(TargetIDStrategy.IDENTIFY_CRITICAL).on();
	}
	
	public void setCarbonOffset(int carbonOffset) {
		if (carbonOffset > 0) {
			_carbonOffset = carbonOffset;
			_flags.get(TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED).on();
		} else
			throw new IllegalArgumentException("Carbon offset must be a value higher than 0");
	}
	
	public void setOnlyDrains(boolean onlyDrains){
		_onlyDrains = onlyDrains;
	}
	
	public boolean isOnlyDrains(){
		return _onlyDrains;
	}
	
	/**
	 * @return the iDENTIFY_CRITICAL
	 */
	public Flag getCriticalFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_CRITICAL);
	}
	
	/**
	 * @return the iDENTIFY_ZEROS
	 */
	public Flag getZerosFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_ZEROS);
	}
	
	/**
	 * @return the iDENTIFY_EQUIVALENCES
	 */
	public Flag getEquivalencesFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_EQUIVALENCES);
	}
	
	/**
	 * @return the iDENTIFY_NONGENE_ASSOCIATED
	 */
	public Flag getNonGeneAssociatedFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_NONGENE_ASSOCIATED);
	}
	
	/**
	 * @return the iDENTIFY_DRAINS_TRANSPORTS
	 */
	public Flag getDrainsTransportsFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_DRAINS_TRANSPORTS);
	}
	
	/**
	 * @return the iDENTIFY_PATHWAY_RELATED
	 */
	public Flag getPathwayRelatedFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_PATHWAY_RELATED);
	}
	
	/**
	 * @return the iDENTIFY_HIGH_CARBON_RELATED
	 */
	public Flag getHighCarbonRelatedFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_HIGH_CARBON_RELATED);
	}
	
	public Flag getNoFluxWTFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_NO_FLUX_WT);
	}
	
	public Flag getExperimentalFlag() {
		return _flags.get(TargetIDStrategy.IDENTIFY_EXPERIMENTAL);
	}
	
	public Set<String> getIgnoredPathways() {
		if (_ignoredPathways == null) _ignoredPathways = new HashSet<String>();
		
		return _ignoredPathways;
	}
	
	public Set<String> getIgnoredCofactors() {
		if (_ignoredCofactors == null) _ignoredCofactors = new HashSet<String>();
		
		return _ignoredCofactors;
	}
	
	public List<IExperimentalGeneEssentiality> getExperimentalValidations() {
		if (_experimental == null) _experimental = new ArrayList<IExperimentalGeneEssentiality>();
		
		return _experimental;
	}
	
	@Override
	public Map<TargetIDStrategy, Flag> getFlags() {
		return _flags;
	}
	
	public void saveTargetsToFile(String file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		Set<String> targets = getTargets();
		boolean first = true;
		for (String s : targets) {
			if (first) {
				bw.append(s);
				first = false;
			} else {
				bw.newLine();
				bw.append(s);
			}
		}
		bw.flush();
		bw.close();
	};
	
	public void saveNonTargetsToFile(String file,Set<String> nonTargets) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		boolean first = true;
		for (String s : nonTargets) {
			if (first) {
				bw.append(s);
				first = false;
			} else {
				bw.newLine();
				bw.append(s);
			}
		}
		bw.flush();
		bw.close();
	};
	
	public void saveNonTargetsToFile(String file) throws IOException {
		Set<String> nonTargets = getNonTargets();
		saveNonTargetsToFile(file, nonTargets);
	}
	
	@Override
	public void saveNonTargetsPerStrategyToFile(String file, boolean includeConfiguration) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		if (includeConfiguration) {
			String header = "=========[ CONFIGURATION ]=========";
			bw.append(header);
			if (_environmentalConditions != null) {
				bw.newLine();
				bw.append("environmental.conditions=" + _environmentalConditions.toString());
			}
			if (_solver != null) {
				bw.newLine();
				bw.append("solver=" + _solver.name());
			}
			if (_carbonOffset != null) {
				bw.newLine();
				bw.append("carbon.offset=" + _carbonOffset);
			}
			if (_ignoredPathways != null) {
				bw.newLine();
				bw.append("ignored.pathways=" + _ignoredPathways.toString());
			}
			if (_ignoredCofactors != null) {
				bw.newLine();
				bw.append("ignored.cofactors=" + _ignoredCofactors.toString());
			}
			if (_experimental != null) {
				bw.newLine();
				String[] dbs = new String[_experimental.size()];
				for (int i = 0; i < _experimental.size(); i++) {
					dbs[i] = _experimental.get(i).getClass().getSimpleName();
				}
				bw.append("experimental.databases=" + StringUtils.concat(",", dbs));
			}
			bw.newLine();
		}
		boolean first = true;
		for (TargetIDStrategy flag : _flags.keySet()) {
			System.out.println("Dealing with flag [" + flag + "]/" + (_flags.get(flag).isOn() ? "ON" : "OFF"));
			if (_flags.get(flag).isOn()) {
				String header = "=========[ " + flag + " ]=========";
				if (!first) {
					bw.newLine();
				} else
					first = false;
				
				bw.append(header);
				Set<String> set = _flags_data.get(flag);
				//				if(set!=null)
				for (String s : set) {
					bw.newLine();
					bw.append(s);
				}
			}
		}
		bw.flush();
		bw.close();
	};
}
