package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAGOUCSOM extends JecoliEAROUCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliEAGOUCSOM() {
        super(new JecoliGOUConverter());
    }
}
