package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGKConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAGeneKnockoutCSOM extends JecoliSAReactionKnockoutCSOM {
    public JecoliSAGeneKnockoutCSOM() {
        super(new JecoliGKConverter());
    }
}
