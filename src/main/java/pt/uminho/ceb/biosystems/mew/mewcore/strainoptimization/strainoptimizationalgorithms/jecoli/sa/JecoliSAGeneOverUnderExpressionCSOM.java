package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAGeneOverUnderExpressionCSOM extends JecoliSAReactionKnockoutCSOM {

    
	private static final long	serialVersionUID	= 1L;

	public JecoliSAGeneOverUnderExpressionCSOM() {
        super(new JecoliGOUConverter());
    }
}
