package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.randomsearch.strategy;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliRKConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.randomsearch.JecoliRANDOMCSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliRANDOMRKCSOM extends JecoliRANDOMCSOM {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliRANDOMRKCSOM() {
		super(new JecoliRKConverter());
	}
	
	protected JecoliRANDOMRKCSOM(IJecoliOptimizationStrategyConverter converter) {
		super(converter);
	}
	
	@Override
	protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
		return null;
	}
	
}
