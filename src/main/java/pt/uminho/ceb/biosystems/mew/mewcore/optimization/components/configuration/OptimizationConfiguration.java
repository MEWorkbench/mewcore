package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.IterationListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.tracker.EvolutionTrackerFile;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.tracker.IEvolutionTracker;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.components.OptimizationStrategy;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidFieldException;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.ObjectiveFunctionType;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.interfaces.IObjectiveFunction;
import cern.colt.Arrays;

public class OptimizationConfiguration extends SimulationConfiguration {
	
	private static final long		serialVersionUID		= 1L;
	
	private static final Pattern	TERM_FE_PATT			= Pattern.compile("[fF][eE]\\(([0-9]+?)\\)");
	
	private static final Pattern	TERM_IT_PATT			= Pattern.compile("[iI][tT]\\(([0-9]+?)\\)");
	
	private static final Pattern	OF_PATTERN				= Pattern.compile("([A-Z]+\\d?)\\((.+)\\)");
	
	private static final Pattern	LINK_PATTERN			= Pattern.compile("LINK\\((.+?)\\s*,\\s*(.+?)\\)");
	
	private static final Pattern	TRACKER_FILE			= Pattern.compile("^FILE");
	
	private static final String		OF_DELIMITER			= Delimiter.SEMICOLON.toString();
	
	private static final int		ARCHIVE_DEFAULT_SIZE	= 100;
	
	private static final int		DEFAULT_MAX_THREADS		= 1;
	
	public static final String		OPT_PREFIX				= "optimization";
	
	public static final String		OPT_STRATEGY			= "optimization.strategy";
	
	public static final String		OPT_ALGORITHM			= "optimization.algorithm";
	
	public static final String		OPT_ALG_MAXTHREADS		= "optimization.algorithm.maxthreads";
	
	public static final String		OPT_ALG_TERM			= "optimization.algorithm.termination";
	
	public static final String		OPT_ALG_TRACKER			= "optimization.algorithm.tracker";
	
	public static final String		OPT_OBJ_FUNC			= "optimization.objectivefunction";
	
	public static final String		OPT_SOL_VARSIZE			= "optimization.solution.varsize";
	
	public static final String		OPT_SOL_MAXSIZE			= "optimization.solution.maxsize";
	
	public static final String		OPT_ARCHIVE_SIZE		= "optimization.archive.size";
	
	public static final String		OPT_RUN					= "optimization.run";
	
	public OptimizationConfiguration(String properties) throws Exception {
		super(properties);
		analyzeOptimizationProperties();
	}
	
	private void analyzeOptimizationProperties() throws Exception {
		
		if (!containsKey(OPT_ALGORITHM)) throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_ALGORITHM + "] property.");
		
		if (!containsKey(OPT_OBJ_FUNC)) throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_OBJ_FUNC + "] property.");
		
		if (!containsKey(OPT_ALG_TERM)) throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_ALG_TERM + "] property.");
		
		if (!containsKey(OPT_SOL_MAXSIZE)) throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_SOL_MAXSIZE + "] property.");
	}
	
	public AlgorithmTypeEnum getAlgorithm() {
		String tag = getProperty(OPT_ALGORITHM,currentState,true);
		if (tag != null)
			return AlgorithmTypeEnum.valueOf(tag.toUpperCase());
		else
			return null;
	}
	
	public OptimizationStrategy getOptimizationStrategy() {
		String tag = getProperty(OPT_STRATEGY,currentState,true);
		if (tag != null)
			return OptimizationStrategy.valueOf(tag.toUpperCase());
		else
			return null;
	}
	
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctions() throws Exception {
		String[] ofList = getProperty(OPT_OBJ_FUNC,currentState,true).split(OF_DELIMITER);
		
		for (String s : ofList)
			System.out.println(s);
		if (ofList.length == 0) throw new InvalidFieldException("ObjectiveFunction", "At least one Objective function must be provided!", new ArrayIndexOutOfBoundsException(-1));
		
		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
		
		for (String ofInfo : ofList) {
			Pair<IObjectiveFunction, String> of = processOFString(ofInfo.trim());
			objFunctions.put(of.getValue(), of.getPairValue());
		}
		
		return objFunctions;
	}
	
	private Pair<IObjectiveFunction, String> processOFString(String ofString) throws Exception {
		Matcher matcher = LINK_PATTERN.matcher(ofString);
		// System.out.println(">>>>>>>>>>OBJECTIVE FUNCTION="+ofString);
		if (matcher.matches()) {
			String simMethodTag = matcher.group(1);
			String simMethod = getSimulationMethod(simMethodTag);
			String objFuncTag = matcher.group(2);
			
			IObjectiveFunction ofIN = processOFParams(objFuncTag);
			return new Pair<IObjectiveFunction, String>(ofIN, simMethod);
		} else
			throw new InvalidObjectiveFunctionConfiguration("Objective functions incorrectly linked to simulation methods. "
					+ "Must follow this syntax LINK([simMethod1], OF1(param,...); LINK([simMethod2], OF2(param,...))");
	}
	
	private IObjectiveFunction processOFParams(String ofString) throws InvalidObjectiveFunctionConfiguration {
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		ObjectiveFunctionType oft = ObjectiveFunctionType.valueOf(ofTag);
		String[] argsList = ofArgs.split(Delimiter.COMMA.toString());
		String[] trimmedArgsList = new String[argsList.length];
		for (int i = 0; i < argsList.length; i++)
			trimmedArgsList[i] = argsList[i].trim();
		return oft.getObjectiveFunction(trimmedArgsList);
	}
	
	private String getSimulationMethod(String sm) throws Exception {
		SimulationMethodsEnum smconstant = Enum.valueOf(SimulationMethodsEnum.class, sm.toUpperCase());
		
		if (smconstant != null)
			return smconstant.getSimulationProperty();
		else
			throw new Exception("Simulation method [" + sm + "] could not be resolved. Available ones are " + Arrays.toString(SimulationMethodsEnum.values()));
	}
	
	public ITerminationCriteria getTerminationCriteria() throws Exception {
		String termString = getProperty(OPT_ALG_TERM,currentState,true);
		
		ITerminationCriteria termination = null;
		
		Matcher m = TERM_FE_PATT.matcher(termString);
		if (m.matches()) {
			int value = Integer.parseInt(m.group(1));
			termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(value);
			return termination;
		} else {
			m = TERM_IT_PATT.matcher(termString);
			if (m.matches()) {
				int value = Integer.parseInt(m.group(1));
				termination = new IterationListenerHybridTerminationCriteria(value);
				return termination;
			} else {
				throw new Exception("Invalid termination criteria property [" + termString + "].");
			}
		}
	}
	
	public boolean isVariableSize() {
		return Boolean.valueOf(getProperty(OPT_SOL_VARSIZE,currentState,true));
	}
	
	public int getMaxSize() {
		return Integer.valueOf(getProperty(OPT_SOL_MAXSIZE,currentState,true));
	}
	
	public List<String> getOptimizationCriticalIDs() throws Exception {
		OptimizationStrategy strategy = getOptimizationStrategy();
		ArrayList<String> critical = new ArrayList<String>();
		String criticalFile = null;
		if (strategy.isGeneBasedOptimization())
			criticalFile = getModelCriticalGenesFile();
		else
			criticalFile = getModelCriticalReactionsFile();
		
		if (criticalFile != null) {
			FileReader fr = new FileReader(criticalFile);
			BufferedReader br = new BufferedReader(fr);
			
			while (br.ready()) {
				String str = br.readLine().trim();
				critical.add(str);
			}
			
			br.close();
			fr.close();
		}
		
		return critical;
	}
	
	public int getMaxThreads() {
		if (containsKey(OPT_ALG_MAXTHREADS))
			return Integer.valueOf(getProperty(OPT_ALG_MAXTHREADS,currentState,true));
		else
			return DEFAULT_MAX_THREADS;
	}
	
	public int getOptimizationArchiveMaxSize() {
		if (containsKey(OPT_ARCHIVE_SIZE))
			return Integer.parseInt(getProperty(OPT_ARCHIVE_SIZE,currentState,true));
		else
			return ARCHIVE_DEFAULT_SIZE;
	}
	
	public IEvolutionTracker<?> getEvolutionTracker() throws Exception {
		String tag = getProperty(OPT_ALG_TRACKER,currentState,true);
		if (tag != null) {
			Matcher m = TRACKER_FILE.matcher(tag);
			if (m.matches()) {
				return new EvolutionTrackerFile();
			} else
				throw new Exception("Invalid evolution tracker property[" + tag + "]. Must be one of [FILE,BD]");
		} else
			return null;
		
	}
	
	public static void main(String... args) throws Exception {
		
		String test = "fE(1000)";
		Pattern patt = Pattern.compile("[fF][eE]\\(([0-9]+?)\\)");
		Matcher m = patt.matcher(test);
		if (m.matches())
			System.out.println("[" + m.group(1) + "]");
		else
			System.out.println("no match");
		// String file = "files/propertiesTest/hierpropertiesTest.conf";
		// OptimizationConfiguration opt = new OptimizationConfiguration(file);
		//
		// ITerminationCriteria term = opt.getTerminationCriteria();
		
	}
	
}
