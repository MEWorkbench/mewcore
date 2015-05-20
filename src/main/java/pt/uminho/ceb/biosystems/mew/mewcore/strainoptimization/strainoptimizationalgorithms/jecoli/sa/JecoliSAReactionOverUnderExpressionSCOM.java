package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.sa;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliSASCOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliROUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAReactionOverUnderExpressionSCOM extends JecoliSASCOM {

	private static final long	serialVersionUID	= -5873246407402620577L;

	public JecoliSAReactionOverUnderExpressionSCOM() {
        super(new JecoliROUConverter());
    }

    protected JecoliSAReactionOverUnderExpressionSCOM(IJecoliOptimizationStrategyConverter converter) {
        super(converter);
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = (boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME);

         if (isVariableSizeGenome) {
                reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation());
                reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation());
                reproductionOperatorContainer.addOperator(0.2, new HybridSetGaussianPertubationMutation());
                reproductionOperatorContainer.addOperator(0.2, new HybridSetGrowthMutation());
                reproductionOperatorContainer.addOperator(0.2, new HybridSetShrinkMutation());
            } else {
                reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomSetMutation());
                reproductionOperatorContainer.addOperator(0.333, new HybridSetRandomIntegerMutation());
                reproductionOperatorContainer.addOperator(0.334, new HybridSetGaussianPertubationMutation());
            }


        return reproductionOperatorContainer;
    }


}
