package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAGeneOverUnderExpressionCSOM extends JecoliEAReactionOverUnderExpressionCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public JecoliEAGeneOverUnderExpressionCSOM() {
        super(new JecoliGOUConverter());
    }
}
