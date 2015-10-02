package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2GOUCSOM extends JecoliSPEA2ROUCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliSPEA2GOUCSOM() {
        super(new JecoliGOUConverter());
    }
}
