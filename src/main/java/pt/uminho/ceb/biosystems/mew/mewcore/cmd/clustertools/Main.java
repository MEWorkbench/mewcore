package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.IOException;

import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public class Main {
	
	public static void main(String... args) throws Exception{
		
		int numArgs = args.length;
		String algs = null;
		String ofs	= null;
		String ks	= null;
		String bms	= null;
		String alphas = null;
		String prds = null;
		String sms = null;
		String ots = null;
		String numberOfRuns = null;
		Boolean gendir= true;
		Boolean varsize = false;
		
		if(numArgs==0){
			print("ERROR: ARGS = 0!");
			help();
			return;
		}
		
		if(args[0].equalsIgnoreCase(GeneratorConstants.HELP_ARG)){
			help();
			return;
		}
		
		if(args[0].equalsIgnoreCase(GeneratorConstants.RUN_ARG)){
			if(numArgs!=2){
				help();
				return;
			}
			else{
				processRunner(args[1]);
			}				
		}
		
		if(args[0].equalsIgnoreCase(GeneratorConstants.GENERATE_ARG)){
			if(numArgs<3){
				help();
				return;
			}
			else{
				if(numArgs==3)
					processGeneration(args[1],args[2]);
				else {
					numberOfRuns = args[2];
					for(int i=3; i< numArgs;i++){
						System.out.println("ARG "+i+" = "+args[i]);
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.ALGS_ARG)){
							algs = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.OFS_ARG)){
							ofs = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.KS_ARG)){
							ks = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.BMS_ARG)){
							bms = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.ALPHAS_ARG)){
							alphas = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.PROD_ARG)){
							prds = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.SM_ARG)){
							sms = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.OTS_ARG)){
							ots = args[i+1];
						}
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.NO_DIR_ARGS))
							gendir = false;
						if(args[i].trim().equalsIgnoreCase(GeneratorConstants.VAR_SIZE_ARG))
							varsize = true;
						
					}
					
					processGeneration(args[1],algs,ofs,ks,bms,alphas,prds,gendir,varsize,numberOfRuns);
						
				}

			}				
		}		
		
	}
	
	private static void processRunner(String confFile) {
		TestRunner runner = new TestRunner(confFile);
		try {
			runner.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void processGeneration(String baseConfiguration, String numRuns) {
		
		Integer numberOfRuns = Integer.parseInt(numRuns);
		
		ConfigurationGenerator generator = new ConfigurationGenerator(baseConfiguration,numberOfRuns);
		
		try {
			generator.generateConfigurations();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;		
	}
	
	private static void processGeneration(String baseConfiguration, String algs, String ofs, String ks, String bms, String alphas,String prds, Boolean gendir, Boolean varsize, String numRuns) {
		
		Integer numberOfRuns = Integer.parseInt(numRuns);

		ConfigurationGenerator generator = new ConfigurationGenerator(baseConfiguration,numberOfRuns);
		
		String[] algTks	= null;
		String[] ofsTks = null;
		String[] ksTks 	= null;
		String[] bmsTks = null;
		String[] prdsTks = null;
		String[] alphasTks = null;
		
		if(algs!=null)
			algTks 	= getTokens(algs);
		if(ofs!=null)
			ofsTks 	= getTokens(ofs);
		if(ks!=null)
			ksTks	= getTokens(ks);
		if(bms!=null)
			bmsTks	= getTokens(bms);
		if(prds!=null)
			prdsTks = getTokens(prds);
		if(alphas!=null)
			alphasTks = getTokens(alphas);

		
		try {
			generator.generateConfigurations(algTks,ofsTks,ksTks,bmsTks,alphasTks,prdsTks,gendir,varsize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;		
	}
	
	private static String[] getTokens(String str){
		System.out.println(str);
		String[] tks =str.split(Delimiter.COMMA.toString());
		for(String tk:tks)
			System.out.println(tk);
		return tks;
	}

	public static void print(String str){
		System.out.println(str);
	}
	
	public static void help(){
		print(GeneratorConstants.USAGE_HELP);
		print(GeneratorConstants.USAGE_RUN);
		print(GeneratorConstants.USAGE_GENERATE);
	}

}
