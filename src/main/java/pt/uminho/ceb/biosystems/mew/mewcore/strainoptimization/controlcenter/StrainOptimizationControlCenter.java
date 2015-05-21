package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.controlcenter;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEAGeneKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEAGeneOverUnderExpressionCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEAReactionKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEAReactionOverUnderExpressionCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea.JecoliEAReactionSwapReactionKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSAGeneKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSAGeneOverUnderExpressionCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSAReactionKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSAReactionOverUnderExpressionSCOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSAReactionSwapReactionKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEA2GeneKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEA2GeneOverUnderExpressionCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEA2ReactionKnockoutCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEA2ReactionOverUnderExpressionCSOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.JecoliSPEA2ReactionSwapReactionKnockoutCSOM;

/**
 * Created by ptiago on 23-02-2015.
 */
public class StrainOptimizationControlCenter extends AbstractStrainOptimizationControlCenter<IStrainOptimizationResultSet> {
	
	private static final long	serialVersionUID	= 1L;

	public StrainOptimizationControlCenter() {
		super();
		//EA Based Methods
		factory.registerMethod("EARK", JecoliEAReactionKnockoutCSOM.class);
		factory.registerMethod("EAGK", JecoliEAGeneKnockoutCSOM.class);
		factory.registerMethod("EAROU", JecoliEAReactionOverUnderExpressionCSOM.class);
		factory.registerMethod("EAGOU", JecoliEAGeneOverUnderExpressionCSOM.class);
		factory.registerMethod("EARKRS", JecoliEAReactionSwapReactionKnockoutCSOM.class);
		
		//SA Based Methods
		factory.registerMethod("SARK", JecoliSAReactionKnockoutCSOM.class);
		factory.registerMethod("SAGK", JecoliSAGeneKnockoutCSOM.class);
		factory.registerMethod("SAROU", JecoliSAReactionOverUnderExpressionSCOM.class);
		factory.registerMethod("SAGOU", JecoliSAGeneOverUnderExpressionCSOM.class);
		factory.registerMethod("SARKRS", JecoliSAReactionSwapReactionKnockoutCSOM.class);
		
		//SPEA2 Based Methods
		factory.registerMethod("SPEA2RK", JecoliSPEA2ReactionKnockoutCSOM.class);
		factory.registerMethod("SPEA2GK", JecoliSPEA2GeneKnockoutCSOM.class);
		factory.registerMethod("SPEA2ROU", JecoliSPEA2ReactionOverUnderExpressionCSOM.class);
		factory.registerMethod("SPEA2GOU", JecoliSPEA2GeneOverUnderExpressionCSOM.class);
		factory.registerMethod("SPEA2RKRS", JecoliSPEA2ReactionSwapReactionKnockoutCSOM.class);
		
		//PBIL Based Methods
	}
	
	public IStrainOptimizationResultSet execute(IGenericConfiguration genericConfiguration) throws Exception {
		String optimizationAlgorithm = (String) genericConfiguration.getProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM);
		
		if (optimizationAlgorithm == null) throw new Exception("Optimization Method Not Defined");
		if (!validateOptimizationAlgorithm(optimizationAlgorithm)) throw new Exception("Invalid Optimization Method: " + optimizationAlgorithm);
		
		String strategy = (String) genericConfiguration.getProperty(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY);
		
		if (strategy == null) throw new Exception("Strategy Not Defined");
		if (!validateStrategy(strategy)) throw new Exception("Invalid Strategy: " + strategy);
		String methodType = optimizationAlgorithm + strategy;
		return factory.getMethod(methodType, genericConfiguration).execute();
	}
	
	protected boolean validateStrategy(String strategy) {
		switch (strategy) {
			case "RK":
				return true;
			case "GK":
				return true;
			case "ROU":
				return true;
			case "GOU":
				return true;
			case "RKRS":
				return true;
			default:
				return false;
		}
	}
	
	protected boolean validateOptimizationAlgorithm(String optimizationAlgorithm) {
		switch (optimizationAlgorithm) {
			case "EA":
				return true;
			case "SA":
				return true;
			case "SPEA2":
				return true;
			case "PBIL":
				return true;
			default:
				return false;
		}
	}
	
}
