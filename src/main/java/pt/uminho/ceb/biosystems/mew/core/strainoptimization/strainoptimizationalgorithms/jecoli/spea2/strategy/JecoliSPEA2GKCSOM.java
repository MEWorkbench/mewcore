package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.spea2.strategy;

import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSPEA2GKCSOM extends  JecoliSPEA2RKCSOM {
    
	private static final long	serialVersionUID	= 1L;

	public  JecoliSPEA2GKCSOM(){
        super(new JecoliGKConverter());
    }
}
