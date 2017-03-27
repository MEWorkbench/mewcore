package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAGOUCSOM extends JecoliSAROUSCOM {

    
	private static final long	serialVersionUID	= 1L;

	public JecoliSAGOUCSOM() {
        super(new JecoliGOUConverter());
    }
}
