package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAGKCSOM extends JecoliSARKCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliSAGKCSOM() {
        super(new JecoliGKConverter());
    }
}
