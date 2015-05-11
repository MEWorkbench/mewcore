package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliGOUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAGeneOverUnderExpressionCSOM extends JecoliEAReactionOverUnderExpressionCSOM {
    public JecoliEAGeneOverUnderExpressionCSOM() {
        super(new JecoliGOUConverter());
    }
}
