package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.randomsearch;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliGenericConfiguration;

/**
 * 
 * 
 * 
 * @author pmaia
 * @version 
 * @since
 */
public class JecoliRANDOMCSOMConfig extends JecoliGenericConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	public JecoliRANDOMCSOMConfig() {
		super();
		this.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, "Random");
	}
}
