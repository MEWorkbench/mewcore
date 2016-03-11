package pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter;

import java.lang.reflect.InvocationTargetException;

import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractObjTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.UnregistaredMethodException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.algorithm.AbstractStrainOptimizationAlgorithm;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions.OptimizationAlgorithmNotDefinedException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions.StrategyNotDefinedException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.exceptions.UnregisteredAlgorithmStrategyException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
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
	
	public IStrainOptimizationResultSet execute(IGenericConfiguration genericConfiguration) throws InstantiationException, InvocationTargetException, UnregistaredMethodException, Exception {
		
		AbstractObjTerm.setMaxValue(Double.MAX_VALUE);
		AbstractObjTerm.setMinValue(-Double.MAX_VALUE);
		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
		CplexParamConfiguration.setIntegerParam("MIPEmphasis", 2);
		CplexParamConfiguration.setBooleanParam("NumericalEmphasis", true);
		CplexParamConfiguration.setBooleanParam("PreInd", true);
		CplexParamConfiguration.setIntegerParam("HeurFreq", -1);
		
		String optimizationAlgorithm = (String) genericConfiguration.getProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM);
		String strategy = (String) genericConfiguration.getProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY);
		
		if (optimizationAlgorithm == null) throw new OptimizationAlgorithmNotDefinedException("Optimization algorithm not defined");
		if (strategy == null) throw new StrategyNotDefinedException("Strategy not defined");
		
//		if (!validateOptimizationAlgorithm(optimizationAlgorithm)) throw new Exception("Invalid Optimization Method: " + optimizationAlgorithm);
//		if (!validateStrategy(strategy)) throw new Exception("Invalid Strategy: " + strategy);
		
		if(!factory.validate(optimizationAlgorithm,strategy)) throw new UnregisteredAlgorithmStrategyException("There is no optimization algorithm ["+optimizationAlgorithm+"] registered for strategy ["+strategy+"]");
		
		String methodType = optimizationAlgorithm + strategy;
		return ((AbstractStrainOptimizationAlgorithm) factory.getMethod(methodType, genericConfiguration)).execute();
	}
	
}
