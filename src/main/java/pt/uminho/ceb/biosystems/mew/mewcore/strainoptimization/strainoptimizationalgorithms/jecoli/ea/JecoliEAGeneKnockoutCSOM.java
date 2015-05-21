package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAGeneKnockoutCSOM extends JecoliEAReactionKnockoutCSOM {

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public JecoliEAGeneKnockoutCSOM() {
        super(new JecoliGKConverter());
    }
}
