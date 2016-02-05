package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.OptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter.StrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.java.TimeUtils;

public class ClusterRunner {
	
	private OptimizationConfiguration	_configuration	= null;
	private int							_run			= 0;
	private String						_baseName		= null;
	
	public ClusterRunner(OptimizationConfiguration configuration) throws Exception {
		_configuration = configuration;
	}
	
	public ClusterRunner(OptimizationConfiguration configuration, int run, String baseName) throws Exception {
		_configuration = configuration;
		_run = run;
		_baseName = baseName;
	}
	
	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	public void run() throws Exception, InvalidConfigurationException {
	//		
	//		//NOTE: pmaia legacy support for mergeSort when using java 7
	//		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	//		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
	//		CplexParamConfiguration.setWarningStream(null);
	//		
	//		if (_baseName == null)
	//			_baseName = generateOutputFileName(_configuration) + ClusterConstants.DEFAULT_NAME_CONNECTOR + "run";
	//		else {
	//			_baseName += ClusterConstants.DEFAULT_NAME_CONNECTOR + "run" + _run;
	//		}
	//		
	//		// strain opt
	//		StrainOptimizationControlCenter optCenter = new StrainOptimizationControlCenter(_configuration);
	//		int archiveMaxSize = _configuration.getOptimizationArchiveMaxSize();
	//		int numObjectives = _configuration.getObjectiveFunctions().size();
	//		
	//		boolean maximization = optCenter.getEvaluationFunction().isMaximization();
	//		
	//		IEvolutionTracker tracker = _configuration.getEvolutionTracker();
	//		if (tracker != null) {
	//			GeneticConditionsStringDecoder decoder = new GeneticConditionsStringDecoder(optCenter.getDecoder());
	//			tracker.setFile(_baseName + ".track.csv");
	//			tracker.setSolutionDecoder(decoder);
	//			optCenter.getOptimizationAlgorithm().setEvolutionTracker(tracker);
	//		}
	//		
	//		boolean isPopulationBased = _configuration.getAlgorithm().isPopulationBased();
	//		ProcessingStrategy notificationStrategy = (isPopulationBased) ? ProcessingStrategy.PROCESS_ARCHIVE_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT
	//				: ProcessingStrategy.PROCESS_ARCHIVE_ON_SINGLE_EVALUATION_FUNCTION_EVENT;
	//		
	//		ArchiveManagerBestSolutions archive = new ArchiveManagerBestSolutions(optCenter.getOptimizationAlgorithm(),
	//				InsertionStrategy.ADD_ON_SINGLE_EVALUATION_FUNCTION_EVENT, InsertionStrategy.ADD_SMART_KEEP_BEST,
	//				notificationStrategy, maximization);
	//		
	//		archive.setMaximumArchiveSize(archiveMaxSize);
	//		
	//		ITrimmingFunction trimmer = (numObjectives > 1) ? new ZitzlerTruncation(archiveMaxSize,
	//				optCenter.getEvaluationFunction()) : new SelectionValueTrimmer(archiveMaxSize, 0.000001, true,
	//				maximization);
	//		
	//		archive.addTrimmingFunction(trimmer);
	//		
	//		optCenter.setArchive(archive);
	//		optCenter.setTerminationCriteria(_configuration.getTerminationCriteria());
	//		
	//		// plotting
	////		Plot2DGUI<ILinearRepresentation<Double>> plotter = new Plot2DGUI<ILinearRepresentation<Double>>(optCenter.getOptimizationAlgorithm());
	////		plotter.setObserveArchive(true);
	////		plotter.run();
	////		
	//		// redirect java output
	//		if(_configuration.isRedirectOutput()){
	//			System.out.println("redirecting logging output to " + _baseName + ".log");
	//			System.out.println("redirecting error output to " + _baseName + ".err");
	//			File file = new File(_baseName + ".log");
	//			FileOutputStream fos = new FileOutputStream(file);
	//			PrintStream ps = new PrintStream(fos);
	//			System.setOut(ps);
	//			
	//			File file_err = new File(_baseName + ".err");
	//			FileOutputStream fos_err = new FileOutputStream(file_err);
	//			PrintStream ps_err = new PrintStream(fos_err);
	//			System.setErr(ps_err);
	//		}
	//
	//
	//		// run optimization
	//		long timestart = System.currentTimeMillis();
	//		SteadyStateMTOptimizationResult result = optCenter.run();
	//		long timeend = System.currentTimeMillis();
	//		
	//		long timetotal = timeend - timestart;
	//		System.out.println("Execution took: " + TimeUtils.formatMillis(timetotal));
	//		
	//		if (result.getNumberOfResults() > 0) {
	//			System.out.println("Saving solutions...");
	//			result.writeToFile(_baseName + ".ss", ClusterConstants.DELIMITER);
	//			System.out.println("Saved solutions to " + _baseName + ".ss");
	//			System.out.println("Saving bests...");
	//			archive.writeBestSolutionsToFile(
	//					_baseName + ".best",
	//					ClusterConstants.DELIMITER,
	//					true,
	//					optCenter.getDecoder());
	//			System.out.println("Saved bests to " + _baseName + ".best");
	//			if (tracker != null) {
	//				tracker.terminate();
	//				System.out.println("Saved tracking information to " + _baseName + ".track.csv");
	//			}
	//		}
	//		
	////		// set console as default output again
	////		System.setOut(console);
	////		System.setErr(console_err);
	//		
	//	}
	
	public void run() throws Exception {
		
		//legacy support for mergeSort when using java 7
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setWarningStream(null);
		
		_baseName = (_baseName == null) ? generateOutputFileName(_configuration) + ClusterConstants.DEFAULT_NAME_CONNECTOR + "run" : _baseName + ClusterConstants.DEFAULT_NAME_CONNECTOR + "run" + _run;
		
		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
		
		
		ISteadyStateModel model = _configuration.getModel();
		EnvironmentalConditions envConditions = _configuration.getEnvironmentalConditions();
		Boolean isMaximization = true; // always maximization, objective functions will deal with specific objective senses on their own
		Boolean isOverUnder2stepApproach =  _configuration.isOverUnder2stepApproach();
		SolverType solver =  _configuration.getSimulationSolver();
		Map<String,Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);
		
		Map<String,Map<String,Object>> simulationConfiguration = new HashMap<String,Map<String,Object>>();
		for(String method : _configuration.getSimulationMethod()){
			Map<String,Object> methodConf = new HashMap<>();
			methodConf.put(SimulationProperties.METHOD_ID, method);
			methodConf.put(SimulationProperties.MODEL, model);
			methodConf.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, envConditions);
			methodConf.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);
			methodConf.put(SimulationProperties.SOLVER, solver);
			methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, isOverUnder2stepApproach);
			methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
			simulationConfiguration.put(method, methodConf);
		}
		
		GenericConfiguration genericConfiguration = new GenericConfiguration();
		genericConfiguration.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		genericConfiguration.setProperty(GenericOptimizationProperties.STEADY_STATE_GENE_REACTION_MODEL, model);
		genericConfiguration.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, _configuration.getMaxSize());
		genericConfiguration.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, _configuration.isVariableSize());
		genericConfiguration.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, _configuration.getOptimizationCriticalIDs());
		genericConfiguration.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, _configuration.getOptimizationStrategy());		
		genericConfiguration.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, _configuration.getObjectiveFunctions());
		genericConfiguration.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		genericConfiguration.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, _configuration.getTerminationCriteria());
		genericConfiguration.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, _configuration.getAlgorithm().toString());
		
		// redirect java output
		if (_configuration.isRedirectOutput()) {
			System.out.println("redirecting logging output to " + _baseName + ".log");
			System.out.println("redirecting error output to " + _baseName + ".err");
			File file = new File(_baseName + ".log");
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			System.setOut(ps);
			
			File file_err = new File(_baseName + ".err");
			FileOutputStream fos_err = new FileOutputStream(file_err);
			PrintStream ps_err = new PrintStream(fos_err);
			System.setErr(ps_err);
		}
		
		// run optimization
		long timestart = System.currentTimeMillis();
		IStrainOptimizationResultSet<?, ?> resultSet = cc.execute(genericConfiguration);
		
		System.out.println("Execution took: " + TimeUtils.formatMillis(System.currentTimeMillis() - timestart));
		
		if (resultSet != null && resultSet.getResultList() != null && !resultSet.getResultList().isEmpty()) {
			resultSet.writeToFile(_baseName + ".ss");
		}		
	}
	
	public String generateOutputFileName(OptimizationConfiguration conf) throws Exception {
		
		StringBuffer buff = new StringBuffer();
		String organism = conf.getModelName();
		AlgorithmTypeEnum algorithm = conf.getAlgorithm();
		List<String> simMethods = conf.getSimulationMethod();
		String strat = conf.getOptimizationStrategy();
		IndexedHashMap<IObjectiveFunction, String> objectiveFunctions = conf.getObjectiveFunctions();
		
		buff.append(organism + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(algorithm.getShortName() + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		for (String sm : simMethods)
			buff.append(sm + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(strat + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		
		for (int i = 0; i < objectiveFunctions.size(); i++) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			String ofn = (of.isMaximization() ? "MAX" : "MIN") + "~" + of.getShortString();
			buff.append(ofn + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		}
		
		buff.append("run" + _run);
		
		return buff.toString();
	}
	
	public void setBaseName(String baseName) {
		_baseName = baseName;
	}
	
	public void setRun(int run) {
		_run = run;
	}
	
	public static void main(String... args) {
		try {
			if (args.length == 0) {
				OptimizationConfiguration configuration = new OptimizationConfiguration("files/propertiesTest/yields_test_iaf1260.conf");
				ClusterRunner runner = new ClusterRunner(configuration, 1, "yields_test_iaf1260.conf");
				runner.run();
			} else {
				OptimizationConfiguration configuration = new OptimizationConfiguration(args[0]);
				ClusterRunner tr = new ClusterRunner(configuration, Integer.parseInt(args[1]), "test_tracker_GK");
				tr.run();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
