package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.spea2;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2GeneKnockoutCSOM extends  JecoliSPEA2ReactionKnockoutCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public  JecoliSPEA2GeneKnockoutCSOM(){
        super(new JecoliGKConverter());
    }
}
