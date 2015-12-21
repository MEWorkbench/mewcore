package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.AbstractStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy.JecoliEAGKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy.JecoliEAGOUCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy.JecoliEARKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy.JecoliEARKRSCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy.JecoliEAROUCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy.JecoliSAGKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy.JecoliSAGOUCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy.JecoliSARKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy.JecoliSARKRSCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy.JecoliSAROUSCOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy.JecoliSPEA2GKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy.JecoliSPEA2GOUCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy.JecoliSPEA2RKCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy.JecoliSPEA2RKRSCSOM;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy.JecoliSPEA2ROUCSOM;
import pt.uminho.ceb.biosystems.mew.solvers.lp.CplexParamConfiguration;

/**
 * Created by ptiago on 23-02-2015.
 */
public class StrainOptimizationControlCenter extends AbstractStrainOptimizationControlCenter<IStrainOptimizationResultSet> {
	
	private static final long	serialVersionUID	= 1L;

	public StrainOptimizationControlCenter() {
		super();
		//EA Based Methods
		factory.registerMethod("EARK", JecoliEARKCSOM.class);
		factory.registerMethod("EAGK", JecoliEAGKCSOM.class);
		factory.registerMethod("EAROU", JecoliEAROUCSOM.class);
		factory.registerMethod("EAGOU", JecoliEAGOUCSOM.class);
		factory.registerMethod("EARKRS", JecoliEARKRSCSOM.class);
		
		//SA Based Methods
		factory.registerMethod("SARK", JecoliSARKCSOM.class);
		factory.registerMethod("SAGK", JecoliSAGKCSOM.class);
		factory.registerMethod("SAROU", JecoliSAROUSCOM.class);
		factory.registerMethod("SAGOU", JecoliSAGOUCSOM.class);
		factory.registerMethod("SARKRS", JecoliSARKRSCSOM.class);
		
		//SPEA2 Based Methods
		factory.registerMethod("SPEA2RK", JecoliSPEA2RKCSOM.class);
		factory.registerMethod("SPEA2GK", JecoliSPEA2GKCSOM.class);
		factory.registerMethod("SPEA2ROU", JecoliSPEA2ROUCSOM.class);
		factory.registerMethod("SPEA2GOU", JecoliSPEA2GOUCSOM.class);
		factory.registerMethod("SPEA2RKRS", JecoliSPEA2RKRSCSOM.class);
		
		//PBIL Based Methods
	}
	
	public IStrainOptimizationResultSet execute(IGenericConfiguration genericConfiguration) throws Exception {
		
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		String optimizationAlgorithm = (String) genericConfiguration.getProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM);
		
		if (optimizationAlgorithm == null) throw new Exception("Optimization Method Not Defined");
		if (!validateOptimizationAlgorithm(optimizationAlgorithm)) throw new Exception("Invalid Optimization Method: " + optimizationAlgorithm);
		
		String strategy = (String) genericConfiguration.getProperty(JecoliOptimizationProperties.OPTIMIZATION_STRATEGY);
		
		if (strategy == null) throw new Exception("Strategy Not Defined");
		if (!validateStrategy(strategy)) throw new Exception("Invalid Strategy: " + strategy);
		String methodType = optimizationAlgorithm + strategy;
		return ((AbstractStrainOptimizationAlgorithm) factory.getMethod(methodType, genericConfiguration)).execute();
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
