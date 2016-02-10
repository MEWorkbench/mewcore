package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.randomsearch;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;

/**
 * 
 * 
 * 
 * @author pmaia
 * @date Jan 20, 2016
 * @version 
 * @since
 */
public class JecoliRANDOMCSOMConfig extends JecoliGenericConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliRANDOMCSOMConfig() {
		super();
		this.setProperty(JecoliOptimizationProperties.OPTIMIZATION_ALGORITHM, "Random");
	}
}
