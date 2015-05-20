package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAGeneKnockoutCSOM extends JecoliSAReactionKnockoutCSOM {
    
	private static final long	serialVersionUID	= -4329911263127129574L;

	public JecoliSAGeneKnockoutCSOM() {
        super(new JecoliGKConverter());
    }
}
