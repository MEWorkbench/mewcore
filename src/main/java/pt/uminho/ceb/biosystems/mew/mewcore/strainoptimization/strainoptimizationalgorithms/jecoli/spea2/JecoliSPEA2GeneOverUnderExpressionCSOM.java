package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2GeneOverUnderExpressionCSOM extends JecoliSPEA2ReactionOverUnderExpressionCSOM {
    
	private static final long	serialVersionUID	= -3782699215552281314L;

	public JecoliSPEA2GeneOverUnderExpressionCSOM() {
        super(new JecoliGOUConverter());
    }
}
