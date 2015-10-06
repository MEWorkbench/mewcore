package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAGKCSOM extends JecoliEARKCSOM {

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliEAGKCSOM() {
        super(new JecoliGKConverter<>());
    }
}
