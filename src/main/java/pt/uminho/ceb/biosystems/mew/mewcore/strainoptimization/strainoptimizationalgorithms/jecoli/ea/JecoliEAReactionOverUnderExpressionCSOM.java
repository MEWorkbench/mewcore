package pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.ea;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.crossover.HybridSetUniformCrossover;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliEASCOM;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.mewcore.strainoptimization.strainoptimizationalgorithms.jecoli.strategyconverter.JecoliROUConverter;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliEAReactionOverUnderExpressionCSOM extends JecoliEASCOM {

	private static final long	serialVersionUID	= 1L;

	public JecoliEAReactionOverUnderExpressionCSOM() {
        super(new JecoliROUConverter());
    }

    protected JecoliEAReactionOverUnderExpressionCSOM(IJecoliOptimizationStrategyConverter converter) {
        super(converter);
    }

    @Override
    protected ReproductionOperatorContainer createAlgorithmReproductionOperatorContainer() throws Exception {
        ReproductionOperatorContainer reproductionOperatorContainer = new ReproductionOperatorContainer();
        boolean isVariableSizeGenome = (boolean) algorithmConfiguration.getProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME);

        if (isVariableSizeGenome) {
            reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomSetMutation(2));
            reproductionOperatorContainer.addOperator(0.2, new HybridSetRandomIntegerMutation(2));
            reproductionOperatorContainer.addOperator(0.15, new HybridSetGaussianPertubationMutation(2));
            reproductionOperatorContainer.addOperator(0.15, new HybridSetGrowthMutation());
            reproductionOperatorContainer.addOperator(0.15, new HybridSetShrinkMutation());
        } else {
            reproductionOperatorContainer.addOperator(0.15, new HybridSetUniformCrossover());
            reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomSetMutation(2));
            reproductionOperatorContainer.addOperator(0.3, new HybridSetRandomIntegerMutation(2));
            reproductionOperatorContainer.addOperator(0.25, new HybridSetGaussianPertubationMutation(2));
        }

        return reproductionOperatorContainer;
    }

}
