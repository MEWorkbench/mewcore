package pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.strategy;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGaussianPertubationMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetGrowthMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomIntegerMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetRandomSetMutation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.operator.reproduction.hybridset.mutation.HybridSetShrinkMutation;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.IJecoliOptimizationStrategyConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.strategyconverter.JecoliROUConverter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.sa.JecoliSACSOM;

/**
 * Created by ptiago on 25-03-2015.
 */
public class JecoliSAROUSCOM extends JecoliSACSOM {

	private static final long	serialVersionUID	= 1L;

	public JecoliSAROUSCOM() {
        super(new JecoliROUConverter());
    }

    protected JecoliSAROUSCOM(IJecoliOptimizationStrategyConverter converter) {
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
