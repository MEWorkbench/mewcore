package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;

import pt.uminho.ceb.biosystems.mew.mewcore.cmd.searchtools.Walltime;
import pt.uminho.ceb.biosystems.mew.mewcore.optimization.objectivefunctions.ObjectiveFunctionType;

public class ConfigurationGenerator {
	
	public final String CONFIGURATIONS_HEADER 	=		"===CONFIGURATIONS===";
	public final String SOLUTIONS_HEADER 		= 		"===SOLUTIONS===";
	public final String FITNESSES_HEADER		=		"===FITNESSES===";
	
	public final String JAR						=		GeneratorConstants.JAR_FILE_NAME;
	public final String CONN					=		GeneratorConstants.DEFAULT_NAME_CONNECTOR;		
		
	protected String configurationFile;
	protected int numRuns;
	protected TestRunner runner;
	protected String BASE_NAME;
	protected String BASE_DIR;

	
	public ConfigurationGenerator(String configurationFile, int numRuns){
		this.configurationFile = configurationFile;
		this.numRuns = numRuns;
	}
		
	public void generateConfigurations(String[] algs,String[] ofs, String[] ks, String[] bms, String[] alphas, String[] prs, Boolean crateDir, Boolean varsize) throws IOException{
		
		String scriptName	= "runAll" + GeneratorConstants.SCRIPT_SUFFIX;
		
		FileWriter writer = new FileWriter(scriptName);
		BufferedWriter bw = new BufferedWriter(writer);
		
		runner = new TestRunner(configurationFile);
		
		BASE_NAME = generateBaseName();
		
		Properties variableConf = new Properties();
		variableConf = (Properties) runner.CONFIGURATION.clone();
		String currDir = new File("").getAbsolutePath();
		
		if(variableConf.containsKey("baseDirectory")){
			String baseDir = variableConf.getProperty("baseDirectory");
			if(!baseDir.equals("")){
				currDir += GeneratorConstants.DASH + baseDir;
				new File(currDir).mkdir();
			}
		}
		currDir += GeneratorConstants.DASH + variableConf.getProperty("organism");
		
		new File(currDir).mkdir();
		
		runAllAlgs(algs, prs, ofs, ks, bms,alphas, crateDir, varsize, currDir, variableConf, bw);
		
		bw.flush();
		writer.flush();
		bw.close();		
		writer.close();
		makeExecutable(scriptName);
		
	}
	//########### Running cascade
	
	private void runAllAlgs(String[] algs, String[] prs,String[] ofs, String[] ks, String[] bms, String[] alphas, Boolean crateDir, Boolean varsize, String currDir, Properties variableConf, BufferedWriter bw ) throws IOException{
		
		if(algs == null)
			runnAllProds(prs,ofs, ks, bms, alphas, crateDir, varsize, currDir, variableConf, bw);
		else{
			for(String alg: algs){
				
				if(crateDir){
					System.out.println("DIR="+currDir);
			
					currDir += GeneratorConstants.DASH + alg;
					System.out.println("DIR="+currDir);
					//create directory for algorithm
					new File(currDir).mkdir();
				}
				
				//System.out.println("ALG = " + alg);
				variableConf.setProperty(GeneratorConstants.ALGORITHM_KEY, alg);
				runnAllProds(prs,ofs, ks, bms, alphas, crateDir, varsize, currDir, variableConf, bw);
				if(crateDir) currDir = new File(currDir).getParent();
			}
		}
		
	}
	

	private void runnAllProds(String[] prs, String[] ofs, String[] ks,
			String[] bms, String[] alphas, Boolean crateDir, Boolean varsize , String currDir,
			Properties variableConf,BufferedWriter bw ) throws IOException {
		
		if(prs == null){
			runnAllOfs(ofs, ks, bms, alphas, crateDir, varsize, currDir, variableConf, bw);
		}else{
			for(String product: prs){
				
				if(crateDir){
					String productFt = product.replaceAll("\\(", "");
					product = product.replaceAll("\\)", "");
					
					currDir += GeneratorConstants.DASH + productFt;
					System.out.println("DIR="+currDir);
					//create directory for objective function
					new File(currDir).mkdir();
				}
				
				//System.out.println("PROD = " + product);
				variableConf.setProperty(GeneratorConstants.PRODUCT_KEY, product);
				runnAllOfs(ofs, ks, bms, alphas, crateDir,varsize, currDir, variableConf, bw);
				if(crateDir) currDir = new File(currDir).getParent();
			}
			
		}
		
	}

	private void runnAllOfs(String[] ofs, String[] ks, String[] bms, String[] alphas,
			Boolean createDir, Boolean varsize, String currDir, Properties variableConf,BufferedWriter bw ) throws IOException {
		
		if(ofs == null){
			runAllKs(ks, bms,alphas,
					createDir,varsize, currDir, variableConf, bw);
		}else{
			
			AlgorithmTypeEnum algType = Enum.valueOf(AlgorithmTypeEnum.class, variableConf.getProperty(GeneratorConstants.ALGORITHM_KEY));
			
			
			if(!(algType==AlgorithmTypeEnum.SPEA2 || algType== AlgorithmTypeEnum.NSGAII)){
				
				for(String of : ofs){
					if(createDir){
						currDir += GeneratorConstants.DASH + of;
						System.out.println("DIR="+currDir);
						//create directory for objective function
						new File(currDir).mkdir();
					}
					
					//System.out.println("OF = " + of);
					variableConf.setProperty(GeneratorConstants.OBJ_FUNC_KEY, of);
					runAllKs(ks, bms, alphas, createDir,varsize, currDir, variableConf, bw);
					
					if(createDir) currDir = new File(currDir).getParent();
				}
			}else{
				
				variableConf.setProperty(GeneratorConstants.MAX_SET_KEY, runner.CONFIGURATION.getProperty(GeneratorConstants.MAX_SET_KEY));
				variableConf.setProperty(GeneratorConstants.VAR_SIZE_KEY, "true");

				generateConfigurationFiles(variableConf,currDir);
				createAllFile(currDir, variableConf, bw);
				
				//currDir = new File(currDir).getParent();
			}
		}
		
	}
	
	
	private void runAllKs(String[] ks, String[] bms, String[] alphas, Boolean crateDir, Boolean varsize,
			String currDir, Properties variableConf, BufferedWriter bw ) throws IOException {
		
		if(ks == null){
			runAllBMS(bms, alphas, crateDir,
					currDir, variableConf, bw);
		}else{
			for(String k: ks){
				if(crateDir){
					currDir += GeneratorConstants.DASH + "K"+k;
					System.out.println("DIR="+currDir);
					new File(currDir).mkdir();
				}
				variableConf.setProperty(GeneratorConstants.MAX_SET_KEY, k);
				variableConf.setProperty(GeneratorConstants.VAR_SIZE_KEY, Boolean.toString(varsize));

				//System.out.println("KS = " + k);
				runAllBMS(bms, alphas, crateDir, currDir, variableConf, bw);
				if(crateDir) currDir = new File(currDir).getParent();
			}
		}
		
	}
	
	private void runAllBMS(String[] bms, String[] alphas, Boolean crateDir, String currDir,
			Properties variableConf, BufferedWriter bw ) throws IOException {
		
		if(bms == null){
			runAllAlphas(alphas, crateDir,
					currDir, variableConf, bw);
			
		}else{
			
			String of = variableConf.getProperty(GeneratorConstants.OBJ_FUNC_KEY);
			if(of.equalsIgnoreCase(ObjectiveFunctionType.YIELD.name())){
				for(String bm: bms){
					//create directory for biomass perc. setting
					if(crateDir){
						currDir += GeneratorConstants.DASH +"BM"+bm;
						System.out.println("DIR="+currDir);
						new File(currDir).mkdir();
					}
					
					variableConf.setProperty(GeneratorConstants.BIOMASS_PERC_KEY, String.valueOf(new Double(bm)/new Double(100)));
					createAllFile(currDir, variableConf,bw);
					if(crateDir) currDir = new File(currDir).getParent();
					
					
				}
				//copyFiles(variableConf,currDir);
			}else
				createAllFile(currDir, variableConf,bw);
				
		}		
	}
	
	private void runAllAlphas(String[] alphas, Boolean crateDir, String currDir,
			Properties variableConf, BufferedWriter bw ) throws IOException {
		
		if(alphas == null){
			createAllFile(currDir, variableConf, bw);
			
		}else{
			
			String of = variableConf.getProperty(GeneratorConstants.OBJ_FUNC_KEY);
			if(of.equalsIgnoreCase(ObjectiveFunctionType.WBP.name())){
				for(String alpha: alphas){
					//create directory for biomass perc. setting
					if(crateDir){
						currDir += GeneratorConstants.DASH +"A"+alpha;
						System.out.println("DIR="+currDir);
						new File(currDir).mkdir();
					}
					
					//System.out.println("BMS = " + bw);
					variableConf.setProperty(GeneratorConstants.WEIGHTED_BP_ALPHA_KEY, String.valueOf(new Double(alpha)/new Double(100)));
					createAllFile(currDir, variableConf,bw);
					if(crateDir) currDir = new File(currDir).getParent();
					
					
				}
				//copyFiles(variableConf,currDir);
			}else
				createAllFile(currDir, variableConf,bw);
				
		}		
	}


	private void createAllFile(String currDir, Properties variableConf, BufferedWriter bw ) throws IOException{
		
		generateConfigurationFiles(variableConf,currDir);
//		copyFiles(variableConf,currDir);
		
		
		
		bw.write(generateRunAllScript(currDir+GeneratorConstants.DASH,variableConf)+"\n");

		//currDir = new File(currDir).getParent();
		
	}

	public void generateConfigurations() throws IOException{
		
		runner = new TestRunner(configurationFile);
		
		BASE_NAME = generateBaseName();
		
		String loggerFile = generateLogFileName();
		String runAllFile = generateRunAllScript("",runner.getCONFIGURATION());
		
		FileWriter writer = new FileWriter(new File(loggerFile));
		BufferedWriter bw = new BufferedWriter(writer); 
		
		bw.write(CONFIGURATIONS_HEADER+GeneratorConstants.NEW_LINE);
		
		for(int i=1; i<=numRuns; i++){
			String confName = generateConfigurationFile(i);
			String scriptName = generateRunnerScript(confName,i);
			bw.write(confName+GeneratorConstants.NEW_LINE);
			bw.write(scriptName+GeneratorConstants.NEW_LINE);
		}
		
		bw.write(runAllFile+GeneratorConstants.NEW_LINE);
		
		
		bw.write(SOLUTIONS_HEADER+GeneratorConstants.NEW_LINE);
		
		for(int i=1; i<=numRuns; i++){
			bw.write(BASE_NAME + CONN + "run"+i+GeneratorConstants.SOLUTIONS_SUFFIX+GeneratorConstants.NEW_LINE);
		}
		
		bw.write(FITNESSES_HEADER+GeneratorConstants.NEW_LINE);
		
		for(int i=1; i<=numRuns; i++){
			bw.write(BASE_NAME + CONN + "run"+i+GeneratorConstants.FITNESSES_SUFFIX+GeneratorConstants.NEW_LINE);
		}
		
		
		bw.flush();
		writer.flush();
		bw.close();		
		writer.close();
		
	}
	
	private String generateRunnerScript(String confName, int i) throws IOException{
		return generateRunnerScript(confName, i, ".");
	}

	private String generateRunnerScript(String confName,int i, String baseDir) throws IOException {
		String scriptName	= baseDir + GeneratorConstants.DASH + BASE_NAME + CONN + "run"+i+GeneratorConstants.SCRIPT_SUFFIX;
		
		FileWriter writer = new FileWriter(scriptName);
		BufferedWriter bw = new BufferedWriter(writer);
		
		String JAVA_HOME = GeneratorConstants.JAVA_HOME;
		String JAVA_EXEC = (JAVA_HOME!=null) ? (JAVA_HOME += (JAVA_HOME.endsWith("/") ? "bin/" : "/bin/")) : "";
		
		String DIRJAR = new File("").getAbsolutePath()+GeneratorConstants.DASH+JAR; 
		String EXEC_COMMAND = JAVA_EXEC + "java" +" "+GeneratorConstants.JAR_ARGS+ " -jar " + DIRJAR + " " + GeneratorConstants.RUN_ARG + " " + confName;			
		
		bw.write(EXEC_COMMAND);
		
		bw.flush();
		writer.flush();
		bw.close();
		writer.close();
				
		makeExecutable(scriptName);
		
		return scriptName;
	}
	
	private String  generateRunAllScript(String baseDir,Properties base) throws IOException{
		
		if(base!=null)
			BASE_NAME = generateBaseName(base);

		String scriptName = baseDir+GeneratorConstants.RUN_ALL_PREFIX + CONN + BASE_NAME + GeneratorConstants.SCRIPT_SUFFIX;
		
		String wallString = base.getProperty(GeneratorConstants.WALLTIME_KEY);
		
		String qsubProps = base.getProperty(GeneratorConstants.QSUB_PROPERTIES_KEY);
		
		if(qsubProps==null)
			qsubProps = GeneratorConstants.QSUB_PROP_DEFAULT;
		
		Walltime wall = (wallString==null) ? GeneratorConstants.WALLTIME : Walltime.valueOf(wallString);
		
		FileWriter writer = new FileWriter(scriptName);
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write(
			"#!/bin/bash\n" +
			"DIR=$(dirname $0)\n" + 
			"cd ${DIR}\n" +
			"X=1\n" +"while [ $X -le "+numRuns+" ]\n" +
			"do\n" +
			"\tqsub "+qsubProps + wall.getFlags()+
//			"\tqsub -l nodes=1,mem=2gb," + GeneratorConstants.WALLTIME_SHORT_MEDIUM +
				BASE_NAME + CONN + "run$X" + GeneratorConstants.SCRIPT_SUFFIX + " " + GeneratorConstants.NEW_LINE+
			"\tX=$((X+1))\n" +
			"done "
		);
		
		bw.flush();
		writer.flush();
		bw.close();
		writer.close();
		
		makeExecutable(scriptName);
		
		return scriptName;
	}
	
//	private void copyFiles(Properties base,String dir) throws IOException{
//		
//		String reactions	= base.getProperty(GeneratorConstants.FLUXES_FILE_KEY);		
//		String matrix		= base.getProperty(GeneratorConstants.MATRIX_FILE_KEY);
//		String generules	= base.getProperty(GeneratorConstants.GENE_RULES_FILE_KEY);
//		String critReaction = base.getProperty(GeneratorConstants.CRIT_REACTIONS_FILE_KEY);
//		String critGenes	= base.getProperty(GeneratorConstants.CRIT_GENES_FILE_KEY);
//		String runner		= GeneratorConstants.JAR_FILE_NAME;
//		
//		if(reactions!=null)
//			FileCopy.copy(reactions, dir+GeneratorConstants.DASH+reactions);
//		if(matrix!=null)
//			FileCopy.copy(matrix, dir+GeneratorConstants.DASH+matrix);
//		if(generules!=null)
//			FileCopy.copy(generules, dir+GeneratorConstants.DASH+generules);
//		if(critReaction!=null)
//			FileCopy.copy(critReaction, dir+GeneratorConstants.DASH+critReaction);
//		if(critGenes!=null)
//			FileCopy.copy(critGenes, dir+GeneratorConstants.DASH+critGenes);
//		if(runner!=null)
//			FileCopy.copy(runner, dir+GeneratorConstants.DASH+runner);
//		
//		makeExecutable(dir+GeneratorConstants.DASH+runner);
//	}

	private void generateConfigurationFiles(Properties base,String dir) throws IOException{
		
		BASE_NAME = generateBaseName(base);
		
		for(int i=1;i<=numRuns;i++){
			Properties configuration = new Properties();
			configuration = (Properties) base.clone();
			configuration.setProperty(GeneratorConstants.RUN_NUMBER_KEY, String.valueOf(i));
			
			String confName	= dir + GeneratorConstants.DASH + BASE_NAME + CONN + "run"+i+GeneratorConstants.CONFS_SUFFIX;
			String comment 	= BASE_NAME + CONN + "run"+i+GeneratorConstants.COMMENTS_ENDING;
			
			FileOutputStream os = new FileOutputStream(new File(confName));		
			configuration.store(os, comment);
			
			os.flush();
			os.close();
			
			generateRunnerScript(confName, i, dir);
		}
		
	}
	
	
	private String generateConfigurationFile(int i) throws IOException {
		Properties configuration = new Properties();
		configuration = (Properties) runner.CONFIGURATION.clone();
		configuration.setProperty(GeneratorConstants.RUN_NUMBER_KEY, String.valueOf(i));
		
		String confName	= BASE_NAME + CONN + "run"+i+GeneratorConstants.CONFS_SUFFIX;
		String comment 	= BASE_NAME + CONN + "run"+i+GeneratorConstants.COMMENTS_ENDING;
		
		FileOutputStream os = new FileOutputStream(new File(confName));		
		configuration.store(os, comment);
		
		os.flush();
		os.close();
		
		return confName;
	}
	
	private String generateBaseName(){
		return generateBaseName(runner.getCONFIGURATION());
	}
	
	private String generateBaseName(Properties base){
		TestRunner runner = new TestRunner(base);
		StringBuffer buff = new StringBuffer();
		
		buff.append(runner.getORGANISM()+CONN);
		buff.append(runner.getALGORITHM().getShortName()+CONN);
		
		String biomass = runner.getBIOMASS().replaceAll("\\(","");
		biomass = biomass.replaceAll("\\)", "");
		buff.append(biomass+CONN);
		
		String product = runner.getPRODUCT().replaceAll("\\(", "");
		product = product.replaceAll("\\)", "");		
		buff.append(product);
		
		String of = runner.getOBJECTIVE_FUNCTION();
		
//		if(of!=null){
//			buff.append(CONN+of);
////			if(runner.VAR_SIZE)
////				buff.append(CONN+"VS_"+runner.getMAX_SET());
////			else buff.append(CONN+"K_"+runner.getMAX_SET());
////			
////			if(of.name().equalsIgnoreCase(ObjectiveFunctionType.YIELD.name())){
////				buff.append(CONN+"BM"+( (int)(runner.getBIOMASS_PERCENTAGE()*100)));
////			}
////			
////			if(of.name().equalsIgnoreCase(ObjectiveFunctionType.WEIGHTED_BP.name())){
////				buff.append(CONN+"A"+( (int)(runner.getWEIGHTED_BP_ALPHA()*100)));
////			}
//		}
		
		if(runner.isMIN_KNOCKOUTS())
			buff.append(CONN+"minKnocks");
		if(runner.isMIN_FLUX_SUM())
			buff.append(CONN+"minFluxSum");
		
		return buff.toString();
	}

	private String generateLogFileName() {
		return BASE_NAME+(GeneratorConstants.LOG_SUFFIX);
	}
	
	public static void makeExecutable(String file) throws IOException{
		Runtime.getRuntime().exec("chmod a+x "+file);
	}
	
	public static void main(String... args) throws IOException{
		
	}
		
}
